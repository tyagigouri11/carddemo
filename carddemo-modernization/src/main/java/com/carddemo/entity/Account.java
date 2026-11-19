package com.carddemo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Account entity representing a credit card account.
 * Source: CBACT01C-04C (Account file processing, balance tracking, interest calculation)
 *
 * PIC Clause Mappings:
 * - creditLimit: COMP-3 9(9)V99 → BigDecimal(12,2)
 * - currentBalance: COMP-3 money field → BigDecimal(12,2)
 * - cashCreditLimit: COMP-3 9(5)V99 → BigDecimal(7,2)
 * - currentCycleDebit/Credit: COMP-3 9(7)V99 → BigDecimal(9,2)
 * - dates: PIC 9(8) YYYYMMDD → LocalDate
 */
@Entity
@Table(name = "account",
       indexes = {
           @Index(name = "idx_active_status", columnList = "active_status"),
           @Index(name = "idx_reissue_date", columnList = "reissue_date")
       })
public class Account {

    /**
     * Unique account identifier.
     * PIC Mapping: 9(12) → Long
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Credit limit for the account.
     * COBOL COMP-3 9(9)V99 → BigDecimal(12,2)
     * Per CBACT01C rule R00001: Credit limit determines available credit
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal creditLimit;

    /**
     * Current account balance.
     * COBOL COMP-3 money field → BigDecimal(12,2)
     * Per CBACT01C rule R00008: Cycle debit default 2525.00
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentBalance;

    /**
     * Account active status flag.
     * COBOL PIC X(1) Y/N → Boolean
     * Per CBACT01C rule R00003: Active status controls transaction eligibility
     */
    @Column(nullable = false)
    private Boolean activeStatus;

    /**
     * Card reissue date for account.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     */
    @Column
    private LocalDate reissueDate;

    /**
     * Account open date.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     */
    @Column
    private LocalDate openDate;

    /**
     * Account expiration date.
     * COBOL PIC 9(8) YYYYMMDD → LocalDate
     */
    @Column
    private LocalDate expirationDate;

    /**
     * Cash advance credit limit (subset of main credit limit).
     * COBOL COMP-3 9(5)V99 → BigDecimal(7,2)
     * Per CBACT01C rule R00005: Cash limit ≤ credit limit
     */
    @Column(precision = 7, scale = 2)
    private BigDecimal cashCreditLimit;

    /**
     * Current cycle debit amount.
     * COBOL COMP-3 9(7)V99 → BigDecimal(9,2)
     * Per CBACT01C rule R00008: Tracks purchase debits for current billing cycle
     */
    @Column(precision = 9, scale = 2)
    private BigDecimal currentCycleDebit;

    /**
     * Current cycle credit amount (payments received).
     * COBOL COMP-3 9(7)V99 → BigDecimal(9,2)
     * Per CBACT01C rule R00009: Tracks payment credits for current billing cycle
     */
    @Column(precision = 9, scale = 2)
    private BigDecimal currentCycleCredit;

    /**
     * Account group identifier for batch operations.
     * COBOL PIC X(50) → String
     */
    @Column(length = 50)
    private String groupId;

    /**
     * One-to-many relationship: Account has multiple Cards.
     * CascadeType.ALL ensures cards are deleted when account is deleted
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Card> cards;

    /**
     * One-to-many relationship: Account has multiple Transactions.
     * CascadeType.ALL ensures transactions are deleted when account is deleted
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    /**
     * One-to-many relationship: Account has multiple Authorizations.
     * CascadeType.ALL ensures authorizations are deleted when account is deleted
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Authorization> authorizations;

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

    public Account() {
    }

    public Account(Long id, BigDecimal creditLimit, BigDecimal currentBalance, Boolean activeStatus, 
                   LocalDate reissueDate, LocalDate openDate, LocalDate expirationDate, 
                   BigDecimal cashCreditLimit, BigDecimal currentCycleDebit, BigDecimal currentCycleCredit, 
                   String groupId, List<Card> cards, List<Transaction> transactions, 
                   List<Authorization> authorizations, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
        this.activeStatus = activeStatus;
        this.reissueDate = reissueDate;
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.cashCreditLimit = cashCreditLimit;
        this.currentCycleDebit = currentCycleDebit;
        this.currentCycleCredit = currentCycleCredit;
        this.groupId = groupId;
        this.cards = cards;
        this.transactions = transactions;
        this.authorizations = authorizations;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Boolean getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public LocalDate getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(LocalDate reissueDate) {
        this.reissueDate = reissueDate;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public BigDecimal getCurrentCycleDebit() {
        return currentCycleDebit;
    }

    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) {
        this.currentCycleDebit = currentCycleDebit;
    }

    public BigDecimal getCurrentCycleCredit() {
        return currentCycleCredit;
    }

    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) {
        this.currentCycleCredit = currentCycleCredit;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Authorization> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(List<Authorization> authorizations) {
        this.authorizations = authorizations;
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
