package com.carddemo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * TransactionType entity representing a transaction type category.
 * Source: COTRTUPC (Transaction type module accessing DB2 transaction type tables)
 *
 * PIC Clause Mappings:
 * - typeId: PIC X(50) → String
 * - description: PIC X(255) → String
 * - category: PIC X(50) → String
 */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    /**
     * Unique transaction type identifier.
     * COBOL PIC X(50) → String
     * Examples: PURCH (Purchase), CASH (Cash Advance), PMT (Payment), FEE (Fee), INT (Interest)
     */
    @Id
    @Column(length = 50)
    private String id;

    /**
     * Human-readable description of transaction type.
     * COBOL PIC X(255) → String
     * Example: "Purchase Transaction"
     */
    @Column(nullable = false, length = 255)
    private String description;

    /**
     * Transaction type category for classification.
     * COBOL PIC X(50) → String
     * Examples: DEBIT, CREDIT
     * Per COTRTUPC rule: Category determines posting treatment (increase/decrease balance)
     */
    @Column(length = 50)
    private String category;

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

    public TransactionType() {
    }

    public TransactionType(String id, String description, String category, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
