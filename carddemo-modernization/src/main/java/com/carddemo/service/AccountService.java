package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Account business logic.
 * Source: CBACT01C-04C (Account management, balance tracking, interest calculation)
 */
@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get account by ID.
     * Per CBACT01C rule R00001: Account lookup
     * Throws AccountNotFoundException if not found
     */
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> {
                logger.warn("Account not found: {}", accountId);
                return new AccountNotFoundException(accountId);
            });
    }

    /**
     * Create new account.
     * Per CBACT01C rule R00001: Create new account with credit limit and balance
     */
    public Account createAccount(Account account) {
        account.setCreatedDate(LocalDateTime.now());
        account.setUpdatedDate(LocalDateTime.now());
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        
        Account created = accountRepository.save(account);
        logger.info("Account created: id={}, creditLimit={}, currentBalance={}", 
            created.getId(), created.getCreditLimit(), created.getCurrentBalance());
        return created;
    }

    /**
     * Get account with all related cards eagerly loaded.
     * Per COCRDUPC rule: Reduce N+1 query problems
     */
    @Transactional(readOnly = true)
    public Account getAccountWithCards(Long accountId) {
        return accountRepository.findByIdWithCards(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * Update account balance.
     * Per CBACT01C rule R00002: Post debit or credit transaction to balance
     * Creates associated Transaction record
     */
    public Account updateBalance(Long accountId, BigDecimal amount, Transaction.TransactionType type, String merchantId, String merchantName) {
        Account account = getAccount(accountId);
        
        // Per CBACT01C rule R00003: Check active status
        if (!account.getActiveStatus()) {
            logger.warn("Cannot post to inactive account: {}", accountId);
            throw new IllegalStateException("Account is not active");
        }
        
        // Per CBACT01C rule R00008: Adjust current cycle debits/credits
        if (type == Transaction.TransactionType.PURCHASE || type == Transaction.TransactionType.CASH_ADVANCE) {
            account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(amount));
            account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        } else if (type == Transaction.TransactionType.PAYMENT) {
            account.setCurrentCycleCredit(account.getCurrentCycleCredit().add(amount));
            account.setCurrentBalance(account.getCurrentBalance().add(amount));
        } else if (type == Transaction.TransactionType.FEE || type == Transaction.TransactionType.INTEREST) {
            account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(amount));
            account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        }
        
        account.setUpdatedDate(LocalDateTime.now());
        Account updated = accountRepository.save(account);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setId(generateTransactionId());
        transaction.setAccount(updated);
        transaction.setAmount(amount);
        transaction.setDate(LocalDate.now());
        transaction.setPostDate(LocalDate.now());
        transaction.setType(type);
        transaction.setStatus(Transaction.TransactionStatus.POSTED);
        transaction.setMerchantId(merchantId);
        transaction.setMerchantName(merchantName);
        transaction.setCreatedDate(LocalDateTime.now());
        transaction.setUpdatedDate(LocalDateTime.now());
        transactionRepository.save(transaction);
        
        logger.info("Account balance updated: accountId={}, amount={}, type={}", accountId, amount, type);
        return updated;
    }

    /**
     * Get account transactions within date range.
     * Per CBTRN03C rule: List account transactions for reporting
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAccountTransactions(Long accountId, LocalDate fromDate, LocalDate toDate) {
        // Verify account exists
        getAccount(accountId);
        
        if (fromDate != null && toDate != null) {
            return transactionRepository.findByAccountIdAndDateBetween(accountId, fromDate, toDate);
        } else if (fromDate != null) {
            return transactionRepository.findByAccountIdAndDateBetween(accountId, fromDate, LocalDate.now());
        } else if (toDate != null) {
            return transactionRepository.findByAccountIdAndDateBetween(accountId, LocalDate.of(1900, 1, 1), toDate);
        } else {
            return transactionRepository.findByAccountId(accountId);
        }
    }

    /**
     * Calculate interest on account balance.
     * Per CBACT04C rule: Compute interest on balance, post as FEE transaction
     */
    public Account calculateInterest(Long accountId, BigDecimal annualRate, LocalDate asOf) {
        Account account = getAccount(accountId);
        
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            logger.debug("No interest on zero or negative balance: accountId={}", accountId);
            return account;
        }
        
        // Calculate daily interest (simplified: annual rate / 365 / 100)
        BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(365), 4, java.math.RoundingMode.HALF_UP).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal interestAmount = account.getCurrentBalance().multiply(dailyRate).setScale(2, java.math.RoundingMode.HALF_UP);
        
        // Post interest as FEE transaction
        if (interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            return updateBalance(accountId, interestAmount, Transaction.TransactionType.INTEREST, "INT", "Interest Charge");
        }
        
        return account;
    }

    /**
     * Reissue card for account.
     * Per COCRDUPC rule: Create new card, set expiry 5 years out, mark old card BLOCKED
     */
    public Card reissueCard(Long accountId, String newCardNumber, LocalDate expiryDate) {
        Account account = getAccount(accountId);
        
        // Per COCRDUPC rule: Invalidate all existing cards
        if (account.getCards() != null) {
            for (Card card : account.getCards()) {
                if (card.getStatus() == Card.CardStatus.ACTIVE) {
                    card.setStatus(Card.CardStatus.BLOCKED);
                }
            }
        }
        
        // Create new card
        Card newCard = new Card();
        newCard.setNumber(newCardNumber);
        newCard.setAccount(account);
        newCard.setStatus(Card.CardStatus.ACTIVE);
        newCard.setExpiryDate(expiryDate != null ? expiryDate : LocalDate.now().plusYears(5));
        newCard.setCreatedDate(LocalDateTime.now());
        newCard.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Card reissued: accountId={}, oldCards={}, newCardNumber={}", accountId, account.getCards().size(), newCardNumber);
        return newCard;
    }

    /**
     * Find all active accounts.
     * Per CBACT01C rule R00003: Filter active accounts
     */
    @Transactional(readOnly = true)
    public List<Account> findActiveAccounts() {
        return accountRepository.findByActiveStatusTrue();
    }

    /**
     * Find accounts with expiration approaching.
     * Per CBACT01C rule: Identify accounts needing renewal
     */
    @Transactional(readOnly = true)
    public List<Account> findAccountsExpiringBefore(LocalDate date) {
        return accountRepository.findByExpirationDateLessThanEqual(date);
    }

    /**
     * Generate unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }
}
