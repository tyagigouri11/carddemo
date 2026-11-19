package com.carddemo.repository;

import com.carddemo.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for TransactionType entity.
 * Provides finder methods for common transaction type queries.
 */
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {

    /**
     * Find all transaction types.
     * Per COTRTUPC rule: List all transaction type definitions
     */
    List<TransactionType> findAll();

    /**
     * Find transaction types by category.
     * Per COTRTUPC rule: Filter by type category (DEBIT/CREDIT)
     */
    List<TransactionType> findByCategory(String category);

    /**
     * Find transaction type by description.
     * Per COTRTUPC rule: Lookup by description
     */
    List<TransactionType> findByDescriptionContainingIgnoreCase(String description);
}
