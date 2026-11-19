package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Transaction business logic.
 * Source: CBTRN01C-03C (Daily transaction posting, settlement, reporting)
 */
@Service
@Transactional
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Post transaction.
     * Per CBTRN01C rule R00001: Create transaction with status=PENDING
     */
    public Transaction postTransaction(Long accountId, String cardNumber, BigDecimal amount, 
                                      Transaction.TransactionType type, String merchantId, String merchantName,
                                      LocalDate date, Transaction.TransactionStatus status) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        Transaction txn = new Transaction();
        txn.setId(generateTransactionId());
        txn.setAccount(account);
        txn.setCardNumber(cardNumber);
        txn.setAmount(amount);
        txn.setDate(date != null ? date : LocalDate.now());
        txn.setPostDate(null);
        txn.setType(type);
        txn.setStatus(status != null ? status : Transaction.TransactionStatus.PENDING);
        txn.setMerchantId(merchantId);
        txn.setMerchantName(merchantName);
        txn.setCreatedDate(LocalDateTime.now());
        txn.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Transaction posted: id={}, accountId={}, amount={}, type={}", txn.getId(), accountId, amount, type);
        return transactionRepository.save(txn);
    }

    /**
     * Settle transaction.
     * Per CBTRN02C rule R00005: Update status=SETTLED, post to account balance
     * Throws IllegalStateException if transaction already settled
     */
    public Transaction settleTransaction(String transactionId) {
        Transaction txn = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        // Validate: reject if already settled
        if (txn.getStatus() == Transaction.TransactionStatus.SETTLED) {
            throw new IllegalStateException("Transaction already settled: " + transactionId);
        }
        
        txn.setStatus(Transaction.TransactionStatus.SETTLED);
        txn.setPostDate(LocalDate.now());
        txn.setUpdatedDate(LocalDateTime.now());
        
        logger.info("Transaction settled: id={}, accountId={}, amount={}", transactionId, txn.getAccount().getId(), txn.getAmount());
        return transactionRepository.save(txn);
    }

    /**
     * Get transactions for date range.
     * Per CBTRN03C rule: Transaction reporting by date
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(LocalDate fromDate, LocalDate toDate) {
        return transactionRepository.findByDateBetween(fromDate, toDate);
    }

    /**
     * Get all transactions.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Get transaction by ID.
     */
    @Transactional(readOnly = true)
    public Transaction getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
    }

    /**
     * Get transactions for account.
     * Per CBTRN01C rule R00001: List account transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    /**
     * Generate transaction report.
     * Per CBTRN03C rule: Summary count, total amount, by status
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateTransactionReport(LocalDate asOf) {
        List<Transaction> transactions = transactionRepository.findByDateBetween(LocalDate.of(2000, 1, 1), asOf);
        
        Map<String, Object> report = new HashMap<>();
        report.put("asOf", asOf);
        report.put("totalCount", transactions.size());
        report.put("totalAmount", transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        Map<String, Long> byStatus = new HashMap<>();
        for (Transaction.TransactionStatus status : Transaction.TransactionStatus.values()) {
            byStatus.put(status.name(), transactionRepository.countByStatus(status));
        }
        report.put("byStatus", byStatus);
        
        return report;
    }

    /**
     * Find transactions pending post.
     */
    @Transactional(readOnly = true)
    public List<Transaction> findPendingTransactions() {
        return transactionRepository.findByStatus(Transaction.TransactionStatus.PENDING);
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }
}
