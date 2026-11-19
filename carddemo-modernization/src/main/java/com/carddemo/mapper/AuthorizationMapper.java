package com.carddemo.mapper;

import com.carddemo.dto.AuthorizationDto;
import com.carddemo.entity.Authorization;

/**
 * Manual mapper for Authorization entity ↔ AuthorizationDto conversions.
 * Replaces MapStruct with explicit static conversion methods.
 * Note: AuthorizationDto.accountId is extracted from Authorization.account.id during toDto.
 */
public class AuthorizationMapper {
    
    public static AuthorizationDto toDto(Authorization entity) {
        if (entity == null) {
            return null;
        }
        Long accountId = entity.getAccount() != null ? entity.getAccount().getId() : null;
        return new AuthorizationDto(
            entity.getId(),
            accountId,
            entity.getCardNumber(),
            entity.getAmount(),
            entity.getMerchantId(),
            entity.getMerchantName(),
            entity.getRequestDate(),
            entity.getResponseDate(),
            entity.getExpiryDate(),
            entity.getStatus(),
            entity.getDeclineReasonCode(),
            entity.getApprovedAmount()
        );
    }
    
    public static Authorization toEntity(AuthorizationDto dto) {
        if (dto == null) {
            return null;
        }
        Authorization authorization = new Authorization();
        authorization.setId(dto.getId());
        authorization.setCardNumber(dto.getCardNumber());
        authorization.setAmount(dto.getAmount());
        authorization.setMerchantId(dto.getMerchantId());
        authorization.setMerchantName(dto.getMerchantName());
        authorization.setRequestDate(dto.getRequestDate());
        authorization.setResponseDate(dto.getResponseDate());
        authorization.setExpiryDate(dto.getExpiryDate());
        authorization.setStatus(dto.getStatus());
        authorization.setDeclineReasonCode(dto.getDeclineReasonCode());
        authorization.setApprovedAmount(dto.getApprovedAmount());
        // account is not set; callers must set it via repository or service logic
        return authorization;
    }
}
