package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Authorization;
import com.carddemo.entity.Card;
import com.carddemo.repository.AuthorizationRepository;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for Authorization business logic.
 * Source: COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C (Real-time authorization, pending auth management)
 * Phase 4: Implements 5 core validations with automatic approval decision
 */
@Service
@Transactional
public class AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final AuthorizationRepository authorizationRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    public AuthorizationService(AuthorizationRepository authorizationRepository, 
                              AccountRepository accountRepository,
                              CardRepository cardRepository) {
        this.authorizationRepository = authorizationRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
    }

    /**
     * Create authorization with automatic approval based on 5 core validations.
     * Per Phase 4 spec: Automatic approval if all validations pass; DECLINED with reason code otherwise.
     * 
     * 5 Validations (in order):
     * 1. Card Exists and Is ACTIVE (not EXPIRED, not BLOCKED)
     * 2. Card Not Expired (expiryDate > TODAY)
     * 3. Sufficient Balance (availableBalance >= amount)
     * 4. Daily Spending Limit (dailySpent + amount <= dailyLimit)
     * 5. Single Transaction Limit (amount <= maxTransactionAmount)
     */
    public Authorization createAuthorizationWithAutoApproval(
            Long accountId, Long cardId, String cardNumber, String merchantId, BigDecimal amount, String merchantName) {
        
        // Lookup account
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Lookup card by ID (may be null if card not found by cardNumber in controller)
        Card card = null;
        if (cardId != null) {
            card = cardRepository.findById(cardId).orElse(null);
        }
        
        String declineReasonCode = null;
        Authorization.AuthorizationStatus status = Authorization.AuthorizationStatus.APPROVED;
        BigDecimal approvedAmount = amount;
        
        // Validation 1: Card Exists
        if (card == null) {
            declineReasonCode = "INVALID_CARD";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card not found): accountId={}, cardId={}", accountId, cardId);
        }
        
        // Validation 2: Card Not Expired (check expiry date first, before status)
        if (status == Authorization.AuthorizationStatus.APPROVED && card != null &&
            card.getExpiryDate().isBefore(LocalDate.now())) {
            declineReasonCode = "CARD_EXPIRED";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card expired): accountId={}, expiryDate={}", 
                accountId, card.getExpiryDate());
        }
        
        // Validation 1b: Card Must Be ACTIVE (after expiry date check, so CARD_EXPIRED takes precedence)
        if (status == Authorization.AuthorizationStatus.APPROVED && card != null &&
            card.getStatus() != Card.CardStatus.ACTIVE) {
            declineReasonCode = "INVALID_CARD";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card not active): accountId={}, status={}", accountId, card.getStatus());
        }
        
        // Validation 3: Sufficient Balance
        if (status == Authorization.AuthorizationStatus.APPROVED) {
            BigDecimal availableBalance = account.getCreditLimit().subtract(account.getCurrentBalance());
            if (amount.compareTo(availableBalance) > 0) {
                declineReasonCode = "INSUFFICIENT_BALANCE";
                status = Authorization.AuthorizationStatus.DECLINED;
                approvedAmount = null;
                logger.warn("Authorization declined (insufficient balance): accountId={}, requested={}, available={}", 
                    accountId, amount, availableBalance);
            }
        }
        
        // Validation 4: Daily Spending Limit
        if (status == Authorization.AuthorizationStatus.APPROVED) {
            BigDecimal dailyLimit = card.getDailyLimit() != null ? card.getDailyLimit() : new BigDecimal("5000.00");
            BigDecimal dailySpent = calculateDailySpent(card.getNumber(), LocalDate.now());
            
            if (dailySpent.add(amount).compareTo(dailyLimit) > 0) {
                declineReasonCode = "DAILY_LIMIT_EXCEEDED";
                status = Authorization.AuthorizationStatus.DECLINED;
                approvedAmount = null;
                logger.warn("Authorization declined (daily limit exceeded): cardNumber={}, dailySpent={}, requested={}, limit={}", 
                    card.getNumber(), dailySpent, amount, dailyLimit);
            }
        }
        
        // Validation 5: Single Transaction Limit
        if (status == Authorization.AuthorizationStatus.APPROVED) {
            BigDecimal maxTransactionAmount = card.getMaxTransactionAmount() != null ? 
                card.getMaxTransactionAmount() : new BigDecimal("2000.00");
            
            if (amount.compareTo(maxTransactionAmount) > 0) {
                declineReasonCode = "TRANSACTION_LIMIT_EXCEEDED";
                status = Authorization.AuthorizationStatus.DECLINED;
                approvedAmount = null;
                logger.warn("Authorization declined (transaction limit exceeded): cardNumber={}, requested={}, limit={}", 
                    card.getNumber(), amount, maxTransactionAmount);
            }
        }
        
        // Create Authorization entity
        Authorization auth = new Authorization();
        auth.setId(generateAuthorizationId());
        auth.setAccount(account);
        // Use cardNumber from parameter (controller passes it), or from card if available
        auth.setCardNumber(cardNumber != null ? cardNumber : (card != null ? card.getNumber() : null));
        auth.setAmount(amount);
        auth.setMerchantId(merchantId);
        auth.setMerchantName(merchantName);
        auth.setRequestDate(LocalDate.now());
        auth.setResponseDate(LocalDate.now());
        auth.setExpiryDate(LocalDate.now().plusDays(3)); // 3-day expiry for pending/declined
        auth.setStatus(status);
        auth.setDeclineReasonCode(declineReasonCode);
        auth.setApprovedAmount(approvedAmount);
        auth.setCreatedDate(LocalDateTime.now());
        auth.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Authorization created: id={}, accountId={}, amount={}, status={}, reasonCode={}", 
            auth.getId(), accountId, amount, status, declineReasonCode);
        return authorizationRepository.save(auth);
    }

    /**
     * Calculate total approved authorization amount for a card on a given date.
     * Per Phase 4 validation #4: Used for daily limit checking
     */
    private BigDecimal calculateDailySpent(String cardNumber, LocalDate date) {
        List<Authorization> approvedAuths = authorizationRepository.findByCardNumber(cardNumber);
        return approvedAuths.stream()
            .filter(a -> a.getStatus() == Authorization.AuthorizationStatus.APPROVED)
            .filter(a -> a.getRequestDate().equals(date))
            .map(Authorization::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Authorize transaction (legacy method for backward compatibility with Phase 3).
     * Per COPAUA0C rule R00013: Authorization Approval Decision
     * Implements all 7 decline reason codes per COPAUA0C rule R00016:
     * - 3100: Card not found
     * - 4100: Insufficient funds
     * - 4200: Card expired
     * - 4300: Card blocked
     * - 5100: Account closed
     * - 5200: Account suspended (not yet implemented, Account entity doesn't have suspension status)
     * - 9000+: System/review decline
     */
    public Authorization authorizeTransaction(Long accountId, String cardNumber, BigDecimal amount, 
                                             String merchantId, String merchantName) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Per COPAUA0C rule R00016: Decline reason codes
        String declineReasonCode = null;
        Authorization.AuthorizationStatus status = Authorization.AuthorizationStatus.APPROVED;
        BigDecimal approvedAmount = amount;
        
        // Rule 3100: Card Not Found
        // Per COCRDLIC rule R00001: Verify card exists and belongs to account
        Card card = cardRepository.findByNumber(cardNumber).orElse(null);
        if (card == null) {
            declineReasonCode = "3100";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card not found): accountId={}, cardNumber={}", accountId, cardNumber);
        } else if (card.getStatus() == Card.CardStatus.BLOCKED) {
            // Rule 4300: Card Blocked
            // Per COCRDUPC rule: Blocked cards cannot be used
            declineReasonCode = "4300";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card blocked): accountId={}, cardNumber={}", accountId, cardNumber);
        } else if (card.getExpiryDate().isBefore(LocalDate.now())) {
            // Rule 4200: Card Expired
            // Per COCRDSLC rule R00001: Card must not be expired
            declineReasonCode = "4200";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card expired): accountId={}, cardNumber={}, expiryDate={}", 
                accountId, cardNumber, card.getExpiryDate());
        } else if (card.getStatus() == Card.CardStatus.EXPIRED) {
            // Rule 4200: Card Expired (status-based check)
            declineReasonCode = "4200";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (card status EXPIRED): accountId={}, cardNumber={}", accountId, cardNumber);
        }
        
        // Rule 5100: Account Closed
        // Per CBACT01C rule: Account must be active
        if (status == Authorization.AuthorizationStatus.APPROVED && !account.getActiveStatus()) {
            declineReasonCode = "5100";
            status = Authorization.AuthorizationStatus.DECLINED;
            approvedAmount = null;
            logger.warn("Authorization declined (account inactive): accountId={}", accountId);
        }
        
        // Rule 4100: Insufficient Funds
        // Per CBACT01C rule R00002: Check available credit
        if (status == Authorization.AuthorizationStatus.APPROVED) {
            BigDecimal availableCredit = account.getCreditLimit().subtract(account.getCurrentBalance());
            if (amount.compareTo(availableCredit) > 0) {
                declineReasonCode = "4100";
                status = Authorization.AuthorizationStatus.DECLINED;
                approvedAmount = null;
                logger.warn("Authorization declined (insufficient funds): accountId={}, requested={}, available={}", 
                    accountId, amount, availableCredit);
            }
        }
        
        Authorization auth = new Authorization();
        auth.setId(generateAuthorizationId());
        auth.setAccount(account);
        auth.setCardNumber(cardNumber);
        auth.setAmount(amount);
        auth.setRequestDate(LocalDate.now());
        auth.setResponseDate(LocalDate.now());
        auth.setExpiryDate(LocalDate.now().plusDays(3)); // Per CBPAUP0C: 3-day expiry default
        auth.setStatus(status);
        auth.setDeclineReasonCode(declineReasonCode);
        auth.setApprovedAmount(approvedAmount);
        auth.setCreatedDate(LocalDateTime.now());
        auth.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Authorization: id={}, accountId={}, amount={}, status={}, reasonCode={}", 
            auth.getId(), accountId, amount, status, declineReasonCode);
        return authorizationRepository.save(auth);
    }

    /**
     * Get authorization by ID.
     */
    @Transactional(readOnly = true)
    public Authorization getAuthorizationById(String authorizationId) {
        return authorizationRepository.findById(authorizationId)
            .orElseThrow(() -> new RuntimeException("Authorization not found: " + authorizationId));
    }

    /**
     * Manually approve an authorization (admin operation).
     * Can only approve PENDING or DECLINED authorizations; transitions to APPROVED.
     */
    public Authorization approveAuthorizationManually(String authorizationId) {
        Authorization auth = getAuthorizationById(authorizationId);
        
        if (auth.getStatus() == Authorization.AuthorizationStatus.APPROVED) {
            throw new IllegalStateException("Authorization is already approved");
        }
        
        auth.setStatus(Authorization.AuthorizationStatus.APPROVED);
        auth.setApprovedAmount(auth.getAmount());
        auth.setDeclineReasonCode(null);
        auth.setResponseDate(LocalDate.now());
        auth.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Authorization manually approved: id={}", authorizationId);
        return authorizationRepository.save(auth);
    }

    /**
     * Manually decline an authorization (admin operation).
     * Can only decline PENDING or APPROVED authorizations; transitions to DECLINED.
     */
    public Authorization declineAuthorizationManually(String authorizationId, String reason) {
        Authorization auth = getAuthorizationById(authorizationId);
        
        if (auth.getStatus() == Authorization.AuthorizationStatus.DECLINED) {
            throw new IllegalStateException("Authorization is already declined");
        }
        
        auth.setStatus(Authorization.AuthorizationStatus.DECLINED);
        auth.setApprovedAmount(null);
        auth.setDeclineReasonCode(reason);
        auth.setResponseDate(LocalDate.now());
        auth.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Authorization manually declined: id={}, reason={}", authorizationId, reason);
        return authorizationRepository.save(auth);
    }

    /**
     * List authorizations with optional filters.
     * Per Phase 4 spec endpoint 5: Filter by status, cardId, and date range.
     */
    @Transactional(readOnly = true)
    public List<Authorization> listAuthorizations(
            Authorization.AuthorizationStatus status, Long cardId, LocalDate fromDate, LocalDate toDate) {
        
        List<Authorization> authorizations = null;
        
        // If filters are provided, apply them
        if (status != null) {
            authorizations = authorizationRepository.findByStatus(status);
        } else {
            authorizations = authorizationRepository.findAll();
        }
        
        // Apply additional filters
        if (cardId != null && authorizations != null) {
            Card card = cardRepository.findById(cardId).orElse(null);
            if (card != null) {
                final String cardNumber = card.getNumber();
                authorizations = authorizations.stream()
                    .filter(a -> cardNumber.equals(a.getCardNumber()))
                    .toList();
            }
        }
        
        if ((fromDate != null || toDate != null) && authorizations != null) {
            final LocalDate from = fromDate != null ? fromDate : LocalDate.of(1900, 1, 1);
            final LocalDate to = toDate != null ? toDate : LocalDate.of(2099, 12, 31);
            authorizations = authorizations.stream()
                .filter(a -> !a.getRequestDate().isBefore(from) && !a.getRequestDate().isAfter(to))
                .toList();
        }
        
        return authorizations != null ? authorizations : new ArrayList<>();
    }

    /**
     * Get authorization statistics/summary.
     * Per Phase 4 spec endpoint 6: Returns totals, averages, and counts.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAuthorizationStatistics() {
        List<Authorization> allAuths = authorizationRepository.findAll();
        
        long totalCount = allAuths.size();
        long approvedCount = allAuths.stream()
            .filter(a -> a.getStatus() == Authorization.AuthorizationStatus.APPROVED)
            .count();
        long declinedCount = allAuths.stream()
            .filter(a -> a.getStatus() == Authorization.AuthorizationStatus.DECLINED)
            .count();
        long pendingCount = allAuths.stream()
            .filter(a -> a.getStatus() == Authorization.AuthorizationStatus.PENDING)
            .count();
        
        BigDecimal totalAmount = allAuths.stream()
            .map(Authorization::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageAmount = totalCount > 0 ? totalAmount.divide(new BigDecimal(totalCount), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("approvedCount", approvedCount);
        stats.put("declinedCount", declinedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("totalAmount", totalAmount);
        stats.put("averageAmount", averageAmount);
        
        return stats;
    }

    /**
     * Get authorization by ID (legacy method alias).
     */
    @Transactional(readOnly = true)
    public Authorization getAuthorizationStatus(String authorizationId) {
        return getAuthorizationById(authorizationId);
    }

    /**
     * Expire old authorizations.
     * Per CBPAUP0C: Find and mark authorizations with expiryDate <= today as DECLINED
     */
    public int expireAuthorizations() {
        List<Authorization> expiredAuths = authorizationRepository.findByExpiryDateBefore(LocalDate.now().plusDays(1));
        int count = 0;
        
        for (Authorization auth : expiredAuths) {
            if (auth.getStatus() == Authorization.AuthorizationStatus.PENDING) {
                auth.setStatus(Authorization.AuthorizationStatus.DECLINED);
                auth.setDeclineReasonCode("9001"); // Expired
                auth.setUpdatedDate(LocalDateTime.now());
                authorizationRepository.save(auth);
                count++;
            }
        }
        
        logger.info("Expired {} pending authorizations", count);
        return count;
    }

    /**
     * Get pending authorizations.
     * Per COPAUA0C rule R00010: Pending authorization summary
     */
    @Transactional(readOnly = true)
    public List<Authorization> getPendingAuthorizations() {
        return authorizationRepository.findByStatusPending();
    }

    /**
     * Get pending authorizations for account.
     * Per COPAUA0C rule R00010: Used in available credit calculation
     */
    @Transactional(readOnly = true)
    public List<Authorization> getPendingAuthorizationsByAccount(Long accountId) {
        return authorizationRepository.findByAccountIdAndStatus(accountId, Authorization.AuthorizationStatus.PENDING);
    }

    private String generateAuthorizationId() {
        return "AUTH" + System.currentTimeMillis();
    }
}
