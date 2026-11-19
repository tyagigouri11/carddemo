package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Transaction entity.
 * Provides finder methods for common transaction queries.
 */
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find all transactions for a given account.
     * Per CBTRN01C rule R00001: List account transactions
     */
    List<Transaction> findByAccountId(Long accountId);

    /**
     * Find all transactions for a card number.
     * Per CBTRN01C rule: Card-based transaction lookup
     */
    List<Transaction> findByCardNumber(String cardNumber);

    /**
     * Find all transactions for an account within a date range.
     * Per CBTRN03C rule: Transaction reporting by date
     */
    List<Transaction> findByAccountIdAndDateBetween(Long accountId, LocalDate fromDate, LocalDate toDate);

    /**
     * Find all transactions within a date range.
     * Per CBTRN03C rule: Global transaction reporting
     */
    List<Transaction> findByDateBetween(LocalDate fromDate, LocalDate toDate);

    /**
     * Find all transactions for an account with a specific status.
     * Per CBTRN02C rule: Filter transactions by status
     */
    List<Transaction> findByAccountIdAndStatus(Long accountId, Transaction.TransactionStatus status);

    /**
     * Find all transactions with a specific status.
     * Per CBTRN02C rule: Status-based filtering for batch processing
     */
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    /**
     * Find all transactions with a specific status before a given date.
     * Per CBTRN02C rule: Age-based batch processing (e.g., settle old pending)
     */
    List<Transaction> findByStatusAndDateBefore(Transaction.TransactionStatus status, LocalDate date);

    /**
     * Find all transactions for an account with a specific status between dates.
     * Per CBTRN03C rule: Detailed account reporting
     */
    List<Transaction> findByAccountIdAndStatusAndDateBetween(
            Long accountId,
            Transaction.TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate
    );

    /**
     * Find all transactions with a specific type.
     * Per CBTRN03C rule: Filter by transaction type
     */
    List<Transaction> findByType(Transaction.TransactionType type);

    /**
     * Count transactions for an account.
     * Per CBTRN03C rule: Transaction count for reporting
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);

    /**
     * Count transactions with a specific status.
     * Per CBTRN03C rule: Status summary for reporting
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") Transaction.TransactionStatus status);
}
