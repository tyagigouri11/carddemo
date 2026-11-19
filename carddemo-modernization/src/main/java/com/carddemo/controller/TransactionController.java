package com.carddemo.controller;

import com.carddemo.dto.TransactionDto;
import com.carddemo.entity.Transaction;
import com.carddemo.mapper.TransactionMapper;
import com.carddemo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Validated
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve all transactions")
    public ResponseEntity<java.util.List<TransactionDto>> getAllTransactions() {
        java.util.List<Transaction> txns = transactionService.getAllTransactions();
        return ResponseEntity.ok(txns.stream().map(TransactionMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve transaction details")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String id) {
        try {
            Transaction txn = transactionService.getTransactionById(id);
            return ResponseEntity.ok(TransactionMapper.toDto(txn));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Post new transaction", description = "Create a new transaction record")
    public ResponseEntity<TransactionDto> postTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        Transaction txn = transactionService.postTransaction(
            transactionDto.getAccountId(),
            transactionDto.getCardNumber(),
            transactionDto.getAmount(),
            transactionDto.getType(),
            transactionDto.getMerchantId(),
            transactionDto.getMerchantName(),
            transactionDto.getDate(),
            transactionDto.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionMapper.toDto(txn));
    }

    @PostMapping("/settle")
    @Operation(summary = "Settle transaction", description = "Mark transaction as settled")
    public ResponseEntity<TransactionDto> settleTransaction(@RequestBody Map<String, String> request) {
        String transactionId = request.get("transactionId");
        try {
            Transaction txn = transactionService.settleTransaction(transactionId);
            return ResponseEntity.ok(TransactionMapper.toDto(txn));
        } catch (IllegalStateException e) {
            // Already settled → 422 Unprocessable Entity
            logger.warn("Settlement rejected: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (RuntimeException e) {
            // Transaction not found → 404 Not Found
            if (e.getMessage().contains("not found")) {
                logger.warn("Settlement failed: {}", e.getMessage());
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @GetMapping("/reports/transactions")
    @Operation(summary = "Generate transaction report", description = "Get transaction summary by status")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam(required = false) LocalDate asOf) {
        LocalDate reportDate = asOf != null ? asOf : LocalDate.now();
        Map<String, Object> report = transactionService.generateTransactionReport(reportDate);
        return ResponseEntity.ok(report);
    }
}
