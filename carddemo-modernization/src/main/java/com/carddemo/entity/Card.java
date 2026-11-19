package com.carddemo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Card entity representing a physical or virtual credit card.
 * Source: COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC (Card issuance, updates, reissue)
 *
 * PIC Clause Mappings:
 * - cardNumber: PIC X(19) → String (12-19 digit range for credit cards)
 * - expiryDate: PIC 9(8) YYYYMMDD → LocalDate
 * - status: X(1) code (A/E/B) → ENUM
 */
@Entity
@Table(name = "card",
       indexes = {
           @Index(name = "idx_account_id", columnList = "account_id"),
           @Index(name = "idx_status_expiry", columnList = "status, expiry_date")
       })
public class Card {

    /**
     * Unique card identifier (sequence).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Card number (PAN - Primary Account Number).
     * COBOL PIC X(19) → String
     * Constraint: 12-19 digits per credit card standards
     * Unique constraint enforced at database level
     */
    @Column(nullable = false, length = 19, unique = true)
    private String number;

    /**
     * Reference to parent Account.
     * Foreign key to account.id
     * Per COBIL00C rule R00001: Every card must be linked to an account
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Card status enumeration.
     * COBOL PIC X(1) codes: A=ACTIVE, E=EXPIRED, B=BLOCKED
     * Per COCRDLIC rule R00001: Status controls transaction eligibility
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    /**
     * Card expiration date.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     * Per COCRDSLC rule R00001: Expiry date determines card validity (MM/YY on physical card)
     */
    @Column(nullable = false)
    private LocalDate expiryDate;

    /**
     * Daily spending limit for the card.
     * COBOL COMP-3 9(9)V99 → BigDecimal(12,2)
     * Per Phase 4 validation #4: Total approved authorizations per day cannot exceed this
     * Default: 5000.00
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal dailyLimit;

    /**
     * Maximum single transaction amount for the card.
     * COBOL COMP-3 9(9)V99 → BigDecimal(12,2)
     * Per Phase 4 validation #5: Individual authorization amount cannot exceed this
     * Default: 2000.00
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal maxTransactionAmount;

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
     * Card status enumeration.
     * ACTIVE: Card is valid and can be used for transactions
     * EXPIRED: Card has reached expiration date
     * BLOCKED: Card has been flagged as fraud or lost
     */
    public enum CardStatus {
        ACTIVE, EXPIRED, BLOCKED
    }

    public Card() {
    }

    public Card(Long id, String number, Account account, CardStatus status, LocalDate expiryDate, 
                BigDecimal dailyLimit, BigDecimal maxTransactionAmount,
                LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.number = number;
        this.account = account;
        this.status = status;
        this.expiryDate = expiryDate;
        this.dailyLimit = dailyLimit;
        this.maxTransactionAmount = maxTransactionAmount;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getMaxTransactionAmount() {
        return maxTransactionAmount;
    }

    public void setMaxTransactionAmount(BigDecimal maxTransactionAmount) {
        this.maxTransactionAmount = maxTransactionAmount;
    }
}
