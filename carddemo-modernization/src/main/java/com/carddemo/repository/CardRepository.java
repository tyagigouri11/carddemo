package com.carddemo.repository;

import com.carddemo.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Card entity.
 * Provides finder methods for common card queries.
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Find card by card number (PAN).
     * Per COCRDLIC rule R00001: Card lookup by number
     */
    Optional<Card> findByNumber(String number);

    /**
     * Find all cards for a given account.
     * Per COCRDUPC rule: List all cards linked to account
     */
    List<Card> findByAccountId(Long accountId);

    /**
     * Find all cards for an account with a specific status.
     * Per COCRDSLC rule: Filter cards by status
     */
    List<Card> findByAccountIdAndStatus(Long accountId, Card.CardStatus status);

    /**
     * Find all cards with given status that have expired.
     * Per CBACT01C rule R00001: Identify expired cards for cleanup
     */
    List<Card> findByStatusAndExpiryDateBefore(Card.CardStatus status, LocalDate date);

    /**
     * Find all expired cards (status = EXPIRED and expiryDate <= today).
     * Per CBACT01C rule: Card expiration detection
     */
    List<Card> findByExpiryDateBefore(LocalDate date);

    /**
     * Find all blocked cards.
     * Per COCRDUPC rule: Identify blocked cards for transaction rejection
     */
    List<Card> findByStatus(Card.CardStatus status);
}
