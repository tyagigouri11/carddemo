package com.carddemo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Customer entity representing a customer account holder.
 * Source: COUSR00C-03C (User/Admin programs - User security and administrative workflows)
 */
@Entity
@Table(name = "customer")
public class Customer {

    /**
     * Unique customer identifier.
     * PIC Mapping: Inferred as 9(8) → Long
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer full name.
     * PIC Mapping: X(100) → String
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Customer contact information (email/phone combined for Phase 1).
     * PIC Mapping: X(100) → String
     */
    @Column(length = 100)
    private String contact;

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

    public Customer() {
    }

    public Customer(Long id, String name, String contact, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
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
