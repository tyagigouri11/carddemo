package com.carddemo.repository;

import com.carddemo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entity.
 * Provides finder methods for common customer queries.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customers by name (case-insensitive contains).
     * Per COUSR00C rule: Customer lookup by name
     */
    List<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * Find customer by exact name.
     * Per COUSR00C rule: Customer name uniqueness validation
     */
    Optional<Customer> findByName(String name);

    /**
     * Find customers by contact information (email/phone).
     * Per COUSR00C rule: Customer lookup by contact
     */
    List<Customer> findByContactContaining(String contact);
}
