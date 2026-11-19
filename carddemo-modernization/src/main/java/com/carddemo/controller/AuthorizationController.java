package com.carddemo.controller;

import com.carddemo.dto.AuthorizationDto;
import com.carddemo.entity.Authorization;
import com.carddemo.entity.Card;
import com.carddemo.mapper.AuthorizationMapper;
import com.carddemo.repository.CardRepository;
import com.carddemo.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Phase 4 - Authorization Management REST Controller
 * 6 Endpoints for authorization request, retrieval, manual approval/decline, listing, and reporting.
 * 
 * Endpoints:
 * 1. POST /api/authorizations - Create authorization with automatic approval
 * 2. GET /api/authorizations/{id} - Get authorization details
 * 3. PUT /api/authorizations/{id}/approve - Manual approve
 * 4. PUT /api/authorizations/{id}/decline - Manual decline
 * 5. GET /api/authorizations - List with filters (status, cardId, dateRange)
 * 6. GET /api/authorizations/reports/summary - Statistics summary
 */
@RestController
@RequestMapping("/api/authorizations")
@Validated
@Tag(name = "Authorizations", description = "Transaction authorization management endpoints")
public class AuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    private final AuthorizationService authorizationService;
    private final CardRepository cardRepository;

    public AuthorizationController(AuthorizationService authorizationService, CardRepository cardRepository) {
        this.authorizationService = authorizationService;
        this.cardRepository = cardRepository;
    }

    /**
     * Endpoint 1: POST /api/authorizations
     * Create authorization with automatic approval based on 5 validations.
     * Request body: accountId, cardNumber, amount, merchantId, merchantName
     * Response: 201 CREATED with AuthorizationDto (includes status, declineReasonCode if declined)
     */
    @PostMapping
    @Operation(summary = "Create authorization", 
               description = "Request authorization for a transaction with automatic approval. Request includes accountId, cardNumber, amount, merchantId, merchantName. Response includes approval decision and decline reason if declined.")
    public ResponseEntity<AuthorizationDto> createAuthorization(@Valid @RequestBody AuthorizationDto authorizationDto) {
        // Phase 4: Use the new createAuthorizationWithAutoApproval method
        // Lookup card by cardNumber to get cardId
        Card card = cardRepository.findByNumber(authorizationDto.getCardNumber())
            .orElse(null);
        
        Long cardId = null;
        if (card != null) {
            cardId = card.getId();
        }
        // If card not found, cardId remains null and service will handle it as INVALID_CARD
        
        Authorization auth = authorizationService.createAuthorizationWithAutoApproval(
            authorizationDto.getAccountId(),
            cardId,
            authorizationDto.getCardNumber(),  // Pass cardNumber for tracking
            authorizationDto.getMerchantId(),
            authorizationDto.getAmount(),
            authorizationDto.getMerchantName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthorizationMapper.toDto(auth));
    }

    /**
     * Endpoint 2: GET /api/authorizations/{id}
     * Get authorization details by ID.
     * Response: 200 OK with AuthorizationDto; 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get authorization details", description = "Retrieve authorization details by ID")
    public ResponseEntity<AuthorizationDto> getAuthorizationById(@PathVariable String id) {
        try {
            Authorization auth = authorizationService.getAuthorizationById(id);
            return ResponseEntity.ok(AuthorizationMapper.toDto(auth));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint 3: PUT /api/authorizations/{id}/approve
     * Manually approve a pending or declined authorization (admin operation).
     * Response: 200 OK with updated AuthorizationDto; 404 if not found; 422 if already finalized
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "Manual approve authorization", description = "Admin operation to manually approve a pending or declined authorization")
    public ResponseEntity<?> approveAuthorization(@PathVariable String id) {
        try {
            Authorization auth = authorizationService.approveAuthorizationManually(id);
            return ResponseEntity.ok(AuthorizationMapper.toDto(auth));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint 4: PUT /api/authorizations/{id}/decline
     * Manually decline a pending or approved authorization (admin operation).
     * Request body: Map with "reason" field
     * Response: 200 OK with updated AuthorizationDto; 404 if not found; 422 if already finalized
     */
    @PutMapping("/{id}/decline")
    @Operation(summary = "Manual decline authorization", description = "Admin operation to manually decline an authorization with reason")
    public ResponseEntity<?> declineAuthorization(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Manual decline");
            Authorization auth = authorizationService.declineAuthorizationManually(id, reason);
            return ResponseEntity.ok(AuthorizationMapper.toDto(auth));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint 5: GET /api/authorizations
     * List authorizations with optional filters (status, cardId, dateRange).
     * Query params (all optional): status=PENDING|APPROVED|DECLINED, cardId=<long>, fromDate=<date>, toDate=<date>
     * Response: 200 OK with List<AuthorizationDto>
     */
    @GetMapping
    @Operation(summary = "List authorizations", 
               description = "Retrieve list of authorizations with optional filters by status, cardId, and date range")
    public ResponseEntity<List<AuthorizationDto>> listAuthorizations(
            @Parameter(description = "Authorization status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Card ID filter") @RequestParam(required = false) Long cardId,
            @Parameter(description = "From date filter (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @Parameter(description = "To date filter (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {
        
        Authorization.AuthorizationStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Authorization.AuthorizationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        List<Authorization> auths = authorizationService.listAuthorizations(statusEnum, cardId, fromDate, toDate);
        List<AuthorizationDto> dtos = auths.stream()
            .map(AuthorizationMapper::toDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint 6: GET /api/authorizations/reports/summary
     * Get authorization statistics summary (totals, averages, counts).
     * Response: 200 OK with Map containing totalCount, approvedCount, declinedCount, pendingCount, totalAmount, averageAmount
     */
    @GetMapping("/reports/summary")
    @Operation(summary = "Get authorization statistics", description = "Retrieve summary statistics for all authorizations")
    public ResponseEntity<Map<String, Object>> getAuthorizationStatistics() {
        Map<String, Object> stats = authorizationService.getAuthorizationStatistics();
        return ResponseEntity.ok(stats);
    }

    // ========== LEGACY ENDPOINTS (Phase 3 backward compatibility) ==========

    @GetMapping("/pending")
    @Operation(summary = "List pending authorizations (legacy)", description = "Get all pending authorization requests")
    public ResponseEntity<List<AuthorizationDto>> getPendingAuthorizations() {
        List<Authorization> auths = authorizationService.getPendingAuthorizations();
        List<AuthorizationDto> dtos = auths.stream().map(AuthorizationMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/expire")
    @Operation(summary = "Expire pending authorizations (legacy)", description = "Mark expired pending authorizations as declined")
    public ResponseEntity<Map<String, Object>> expireAuthorizations() {
        int count = authorizationService.expireAuthorizations();
        return ResponseEntity.ok(Map.of("expiredCount", count));
    }
}
