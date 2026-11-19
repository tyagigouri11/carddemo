package com.carddemo.mapper;

import com.carddemo.dto.TransactionDto;
import com.carddemo.entity.Transaction;

/**
 * Manual mapper for Transaction entity ↔ TransactionDto conversions.
 * Replaces MapStruct with explicit static conversion methods.
 * Note: TransactionDto.accountId is extracted from Transaction.account.id during toDto.
 */
public class TransactionMapper {
    
    public static TransactionDto toDto(Transaction entity) {
        if (entity == null) {
            return null;
        }
        Long accountId = entity.getAccount() != null ? entity.getAccount().getId() : null;
        return new TransactionDto(
            entity.getId(),
            accountId,
            entity.getCardNumber(),
            entity.getAmount(),
            entity.getDate(),
            entity.getPostDate(),
            entity.getType(),
            entity.getStatus(),
            entity.getMerchantId(),
            entity.getMerchantName(),
            entity.getProcessingCode()
        );
    }
    
    public static Transaction toEntity(TransactionDto dto) {
        if (dto == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setCardNumber(dto.getCardNumber());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setPostDate(dto.getPostDate());
        transaction.setType(dto.getType());
        transaction.setStatus(dto.getStatus());
        transaction.setMerchantId(dto.getMerchantId());
        transaction.setMerchantName(dto.getMerchantName());
        transaction.setProcessingCode(dto.getProcessingCode());
        // account is not set; callers must set it via repository or service logic
        return transaction;
    }
}
