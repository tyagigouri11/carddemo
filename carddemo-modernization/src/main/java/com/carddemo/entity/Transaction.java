package com.carddemo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a credit card transaction.
 * Source: CBTRN01C-03C (Daily transaction posting, settlement, reporting)
 *
 * PIC Clause Mappings:
 * - transactionId: PIC X(12) → String
 * - amount: COMP-3 money field 9V99 → BigDecimal(12,2)
 * - dates: PIC 9(8) YYYYMMDD → LocalDate
 * - type: X(1) code (P/C/F/I) → ENUM
 * - status: X(1) code (P/S/E) → ENUM
 */
@Entity
@Table(name = "transaction",
       indexes = {
           @Index(name = "idx_account_id", columnList = "account_id"),
           @Index(name = "idx_card_number", columnList = "card_number"),
           @Index(name = "idx_date", columnList = "date"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_status_date", columnList = "status, date")
       })
public class Transaction {

    /**
     * Unique transaction identifier.
     * COBOL PIC X(12) → String
     * Format: TXN + sequence or timestamp-based
     */
    @Id
    @Column(length = 50)
    private String id;

    /**
     * Reference to Account for this transaction.
     * Foreign key to account.id
     * Per CBTRN01C rule R00001: Every transaction must be linked to an account
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Card number used for this transaction.
     * Weak foreign key reference (String) to card.number
     * Per CBTRN01C rule R00002: Transaction references card that performed it
     */
    @Column(length = 19)
    private String cardNumber;

    /**
     * Transaction amount.
     * COBOL COMP-3 9(9)V99 → BigDecimal(12,2)
     * Per CBTRN01C rule R00003: Amount must be positive for debits, negative for credits
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Transaction date (when transaction occurred).
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     * Per CBTRN01C rule R00004: Transaction date may differ from post date
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Post date (when transaction was posted to account).
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     * Per CBTRN02C rule R00005: Post date typically 1-2 days after transaction date
     */
    @Column
    private LocalDate postDate;

    /**
     * Transaction type enumeration.
     * COBOL PIC X(1) codes: P=PURCHASE, C=CASH_ADVANCE, F=FEE, I=INTEREST, M=PAYMENT
     * Per CBTRN01C rule R00005: Type determines debit/credit treatment
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /**
     * Transaction status enumeration.
     * COBOL PIC X(1) codes: P=PENDING, S=POSTED, E=SETTLED
     * Per CBTRN01C rule R00006: Status tracks posting lifecycle
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    /**
     * Merchant identifier for purchase transactions.
     * COBOL PIC X(50) → String
     * Per CBTRN01C rule R00007: Merchant ID identifies transaction source
     */
    @Column(length = 50)
    private String merchantId;

    /**
     * Merchant name for purchase transactions.
     * COBOL PIC X(100) → String
     */
    @Column(length = 100)
    private String merchantName;

    /**
     * Processing code for transaction classification.
     * COBOL PIC X(10) → String
     * Per CBTRN01C rule R00008: Processing code denotes transaction category
     */
    @Column(length = 10)
    private String processingCode;

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
     * Transaction type enumeration.
     * PURCHASE: Regular purchase transaction (debit)
     * CASH_ADVANCE: Cash withdrawal (debit)
     * PAYMENT: Payment received (credit)
     * FEE: Service or late fee (debit)
     * INTEREST: Interest charge (debit)
     */
    public enum TransactionType {
        PURCHASE, CASH_ADVANCE, PAYMENT, FEE, INTEREST
    }

    /**
     * Transaction status enumeration.
     * PENDING: Transaction submitted but not yet posted
     * POSTED: Transaction posted to account balance
     * SETTLED: Transaction cleared through payment processor
     */
    public enum TransactionStatus {
        PENDING, POSTED, SETTLED
    }

    public Transaction() {
    }

    public Transaction(String id, Account account, String cardNumber, BigDecimal amount, LocalDate date, 
                       LocalDate postDate, TransactionType type, TransactionStatus status, String merchantId, 
                       String merchantName, String processingCode, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.account = account;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.date = date;
        this.postDate = postDate;
        this.type = type;
        this.status = status;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.processingCode = processingCode;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDate postDate) {
        this.postDate = postDate;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
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
