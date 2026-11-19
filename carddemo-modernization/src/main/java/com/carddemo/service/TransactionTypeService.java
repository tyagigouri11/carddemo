package com.carddemo.service;

import com.carddemo.entity.TransactionType;
import com.carddemo.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for TransactionType business logic.
 * Source: COTRTUPC (Transaction type module accessing DB2)
 */
@Service
@Transactional
public class TransactionTypeService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionTypeService.class);

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    /**
     * Get all transaction types.
     * Per COTRTUPC rule: List all transaction type definitions
     */
    @Transactional(readOnly = true)
    public List<TransactionType> getAll() {
        return transactionTypeRepository.findAll();
    }

    /**
     * Get transaction type by ID.
     */
    @Transactional(readOnly = true)
    public TransactionType getById(String typeId) {
        return transactionTypeRepository.findById(typeId)
            .orElseThrow(() -> new RuntimeException("Transaction type not found: " + typeId));
    }

    /**
     * Update transaction type.
     * Per COTRTUPC rule: Update and save
     */
    public TransactionType update(String typeId, String description, String category) {
        TransactionType type = getById(typeId);
        type.setDescription(description);
        type.setCategory(category);
        type.setUpdatedDate(LocalDateTime.now());
        logger.info("Transaction type updated: id={}, description={}, category={}", typeId, description, category);
        return transactionTypeRepository.save(type);
    }

    /**
     * Get transaction types by category.
     */
    @Transactional(readOnly = true)
    public List<TransactionType> getByCategory(String category) {
        return transactionTypeRepository.findByCategory(category);
    }
}
