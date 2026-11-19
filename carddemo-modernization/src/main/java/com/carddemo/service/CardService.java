package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.exception.InvalidTransactionException;
import com.carddemo.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Card business logic.
 * Source: COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC (Card issuance, updates, reissue)
 */
@Service
@Transactional
public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final AccountService accountService;

    public CardService(CardRepository cardRepository, AccountService accountService) {
        this.cardRepository = cardRepository;
        this.accountService = accountService;
    }

    /**
     * Get card by number.
     * Per COCRDLIC rule R00001: Card lookup
     */
    @Transactional(readOnly = true)
    public Card getCard(String cardNumber) {
        return cardRepository.findByNumber(cardNumber)
            .orElseThrow(() -> new CardNotFoundException(cardNumber));
    }

    /**
     * Get all cards for an account.
     * Per COCRDUPC rule: List account cards
     */
    @Transactional(readOnly = true)
    public List<Card> getCardsByAccount(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    /**
     * Issue new card.
     * Per COCRDUPC rule: Create card, set ACTIVE status, link to account
     */
    public Card issueCard(Long accountId, String cardNumber, LocalDate expiryDate) {
        Account account = accountService.getAccount(accountId);
        
        Card card = new Card();
        card.setNumber(cardNumber);
        card.setAccount(account);
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setExpiryDate(expiryDate);
        card.setCreatedDate(LocalDateTime.now());
        card.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Card issued: accountId={}, cardNumber={}", accountId, cardNumber);
        return cardRepository.save(card);
    }

    /**
     * Expire card.
     * Per CBACT01C rule: Set status=EXPIRED
     */
    public Card expireCard(String cardNumber) {
        Card card = getCard(cardNumber);
        card.setStatus(Card.CardStatus.EXPIRED);
        card.setUpdatedDate(LocalDateTime.now());
        cardRepository.save(card);
        logger.info("Card expired: {}", cardNumber);
        return card;
    }

    /**
     * Block card.
     * Per COCRDUPC rule: Set status=BLOCKED for fraud/lost cards
     */
    public Card blockCard(String cardNumber) {
        Card card = getCard(cardNumber);
        card.setStatus(Card.CardStatus.BLOCKED);
        card.setUpdatedDate(LocalDateTime.now());
        cardRepository.save(card);
        logger.info("Card blocked: {}", cardNumber);
        return card;
    }

    /**
     * Verify card is valid for transaction.
     * Per COCRDSLC rule R00001: Check status and expiry
     * Throws InvalidTransactionException if card is invalid
     */
    @Transactional(readOnly = true)
    public boolean verifyCard(String cardNumber) {
        Card card = getCard(cardNumber);
        
        if (card.getStatus() == Card.CardStatus.EXPIRED) {
            throw new InvalidTransactionException("Card has expired: " + cardNumber);
        }
        
        if (card.getStatus() == Card.CardStatus.BLOCKED) {
            throw new InvalidTransactionException("Card is blocked: " + cardNumber);
        }
        
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidTransactionException("Card has passed expiry date: " + cardNumber);
        }
        
        return true;
    }

    /**
     * Find all expired cards.
     * Per CBACT01C rule: Card expiration detection for cleanup
     */
    @Transactional(readOnly = true)
    public List<Card> findExpiredCards() {
        return cardRepository.findByExpiryDateBefore(LocalDate.now());
    }

    /**
     * Find all blocked cards.
     * Per COCRDUPC rule: Identify blocked cards for transaction rejection
     */
    @Transactional(readOnly = true)
    public List<Card> findBlockedCards() {
        return cardRepository.findByStatus(Card.CardStatus.BLOCKED);
    }
}
