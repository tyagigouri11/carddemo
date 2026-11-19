package com.carddemo.mapper;

import com.carddemo.dto.CardDto;
import com.carddemo.entity.Card;

/**
 * Manual mapper for Card entity ↔ CardDto conversions.
 * Replaces MapStruct with explicit static conversion methods.
 * Note: CardDto.accountId is extracted from Card.account.id during toDto.
 */
public class CardMapper {
    
    public static CardDto toDto(Card entity) {
        if (entity == null) {
            return null;
        }
        Long accountId = entity.getAccount() != null ? entity.getAccount().getId() : null;
        return new CardDto(
            entity.getId(),
            entity.getNumber(),
            accountId,
            entity.getStatus(),
            entity.getExpiryDate()
        );
    }
    
    public static Card toEntity(CardDto dto) {
        if (dto == null) {
            return null;
        }
        Card card = new Card();
        card.setId(dto.getId());
        card.setNumber(dto.getNumber());
        card.setStatus(dto.getStatus());
        card.setExpiryDate(dto.getExpiryDate());
        // account is not set; callers must set it via repository or service logic
        return card;
    }
}
