package com.carddemo.entity;

import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Authorization entity representing a transaction authorization request.
 * Source: COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C (Real-time authorization, pending auth management, expiration)
 *
 * PIC Clause Mappings:
 * - authorizationId: PIC X(12) → String
 * - amount: COMP-3 money field 9V99 → BigDecimal(12,2)
 * - dates: PIC 9(8) YYYYMMDD → LocalDate
 * - status: X(1) code (P/A/D) → ENUM
 * - declineReasonCode: PIC X(4) → String (e.g., 3100, 4100, 4200, 4300, 5100, 5200, 9000)
 *
 * Assumption (CBPAUP0C rule): 3-day expiry window for pending authorizations
 */
@Entity
@Table(name = "\"authorization\"",
       indexes = {
           @Index(name = "idx_authorization_account_id", columnList = "account_id"),
           @Index(name = "idx_authorization_status", columnList = "status"),
           @Index(name = "idx_authorization_status_expiry", columnList = "status, expiry_date"),
           @Index(name = "idx_authorization_expiry_date", columnList = "expiry_date")
       })
public class Authorization {

    /**
     * Unique authorization identifier.
     * COBOL PIC X(12) → String
     * Format: AUTH + sequence or timestamp-based
     */
    @Id
    @Column(length = 50)
    private String id;

    /**
     * Reference to Account for authorization.
     * Foreign key to account.id
     * Per COPAUA0C rule R00001: Every authorization must be linked to an account
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Card number for authorization.
     * Weak foreign key reference (String) to card.number
     * Per COPAUA0C rule R00002: Authorization references card used
     */
    @Column(length = 19)
    private String cardNumber;

    /**
     * Merchant ID for the authorization transaction.
     */
    @Column(length = 20)
    private String merchantId;

    /**
     * Merchant name for the authorization transaction.
     */
    @Column(length = 100)
    private String merchantName;

    /**
     * Authorization amount requested.
     * COBOL COMP-3 9(9)V99 → BigDecimal(12,2)
     * Per COPAUA0C rule R00013: Amount compared to available credit
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Request date/time for authorization.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     * Per COPAUA0C rule R00014: Request timestamp for audit trail
     */
    @Column(nullable = false)
    private LocalDate requestDate;

    /**
     * Response date/time for authorization decision.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     * Per COPAUA0C rule R00015: Response timestamp for decision log
     */
    @Column
    private LocalDate responseDate;

    /**
     * Expiration date for this authorization.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     * Per CBPAUP0C rule: Default 3 days from request date for pending; permanent for APPROVED/DECLINED
     */
    @Column(nullable = false)
    private LocalDate expiryDate;

    /**
     * Authorization status enumeration.
     * COBOL PIC X(1) codes: P=PENDING, A=APPROVED, D=DECLINED
     * Per COPAUA0C rule R00010: Status determines transaction eligibility
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthorizationStatus status;

    /**
     * Decline reason code (if status = DECLINED).
     * COBOL PIC X(4) → String
     * Possible values per COPAUA0C decline rules:
     * - 3100: Card Not Found
     * - 4100: Insufficient Funds
     * - 4200: Expired Card
     * - 4300: Card Blocked
     * - 5100: Account Closed
     * - 5200: Account Suspended
     * - 9000: System Error
     * Phase 4 decline codes:
     * - INVALID_CARD: Card does not exist or is not ACTIVE
     * - CARD_EXPIRED: Card expiry date is before today
     * - INSUFFICIENT_BALANCE: Available balance < requested amount
     * - DAILY_LIMIT_EXCEEDED: Daily spending total would exceed limit (up to 19 chars)
     * - TRANSACTION_LIMIT_EXCEEDED: Single transaction exceeds max allowed (up to 24 chars)
     * Per COPAUA0C rule R00016: Decline reason codes for decision tracking
     */
    @Column(length = 50)
    private String declineReasonCode;

    /**
     * Approved amount (may be less than requested).
     * COBOL COMP-3 9(9)V99 → BigDecimal(12,2)
     * Per COPAUA0C rule R00017: Approved amount ≤ requested amount
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    /**
     * Record creation timestamp.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Record last update timestamp.
     */
    @Column(nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Authorization status enumeration.
     * PENDING: Authorization request submitted, awaiting response
     * APPROVED: Authorization approved, transaction may proceed
     * DECLINED: Authorization declined, transaction blocked
     */
    public enum AuthorizationStatus {
        PENDING, APPROVED, DECLINED
    }

    public Authorization() {
    }

    public Authorization(String id, Account account, String cardNumber, BigDecimal amount, LocalDate requestDate,
                        LocalDate responseDate, LocalDate expiryDate, AuthorizationStatus status,
                        String declineReasonCode, BigDecimal approvedAmount, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.account = account;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.requestDate = requestDate;
        this.responseDate = responseDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.declineReasonCode = declineReasonCode;
        this.approvedAmount = approvedAmount;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDate responseDate) {
        this.responseDate = responseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public AuthorizationStatus getStatus() {
        return status;
    }

    public void setStatus(AuthorizationStatus status) {
        this.status = status;
    }

    public String getDeclineReasonCode() {
        return declineReasonCode;
    }

    public void setDeclineReasonCode(String declineReasonCode) {
        this.declineReasonCode = declineReasonCode;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
