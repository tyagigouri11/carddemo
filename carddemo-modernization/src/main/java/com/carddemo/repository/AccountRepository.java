package com.carddemo.repository;

import com.carddemo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity.
 * Provides finder methods for common account queries.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts with active status = true.
     * Per CBACT01C rule R00003: Filters for transaction-eligible accounts
     */
    List<Account> findByActiveStatusTrue();

    /**
     * Find all accounts with active status = false.
     */
    List<Account> findByActiveStatusFalse();

    /**
     * Find accounts with reissue date within a date range.
     * Per COCRDUPC rule: Used for card reissue batch processing
     */
    List<Account> findByReissueDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find accounts with expiration date before or equal to a given date.
     * Used to identify accounts approaching or past expiration
     */
    List<Account> findByExpirationDateLessThanEqual(LocalDate date);

    /**
     * Find accounts by group ID for batch processing.
     * Per CBACT01C rule: Group ID enables batch operations
     */
    List<Account> findByGroupId(String groupId);

    /**
     * Find account by ID with all related cards loaded (eager fetch).
     * Reduces N+1 query problems for account + cards scenario
     */
    @Query("SELECT DISTINCT a FROM Account a " +
           "LEFT JOIN FETCH a.cards " +
           "WHERE a.id = :accountId")
    Optional<Account> findByIdWithCards(@Param("accountId") Long accountId);
}
