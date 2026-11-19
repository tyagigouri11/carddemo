package com.carddemo.mapper;

import com.carddemo.dto.TransactionTypeDto;
import com.carddemo.entity.TransactionType;

/**
 * Manual mapper for TransactionType entity ↔ TransactionTypeDto conversions.
 * Replaces MapStruct with explicit static conversion methods.
 */
public class TransactionTypeMapper {
    
    public static TransactionTypeDto toDto(TransactionType entity) {
        if (entity == null) {
            return null;
        }
        return new TransactionTypeDto(
            entity.getId(),
            entity.getDescription(),
            entity.getCategory()
        );
    }
    
    public static TransactionType toEntity(TransactionTypeDto dto) {
        if (dto == null) {
            return null;
        }
        TransactionType transactionType = new TransactionType();
        transactionType.setId(dto.getId());
        transactionType.setDescription(dto.getDescription());
        transactionType.setCategory(dto.getCategory());
        return transactionType;
    }
}
