package com.carddemo.repository;

import com.carddemo.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Authorization entity.
 * Provides finder methods for common authorization queries.
 */
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {

    /**
     * Find all authorizations for a given account.
     * Per COPAUA0C rule R00001: List account authorizations
     */
    List<Authorization> findByAccountId(Long accountId);

    /**
     * Find all authorizations for a card number.
     * Per COPAUA0C rule: Card-based authorization lookup
     */
    List<Authorization> findByCardNumber(String cardNumber);

    /**
     * Find all pending authorizations.
     * Per COPAUA0C rule R00010: Pending authorization summary lookup
     */
    @Query("SELECT a FROM Authorization a WHERE a.status = 'PENDING' ORDER BY a.requestDate DESC")
    List<Authorization> findByStatusPending();

    /**
     * Find all approved authorizations for an account.
     * Per COPAUA0C rule: Account credit calculation (pending approved auths)
     */
    List<Authorization> findByAccountIdAndStatus(Long accountId, Authorization.AuthorizationStatus status);

    /**
     * Find all authorizations with a specific status.
     * Per COPAUA0C rule: Status-based filtering
     */
    List<Authorization> findByStatus(Authorization.AuthorizationStatus status);

    /**
     * Find all authorizations that have expired (expiryDate <= today).
     * Per CBPAUP0C rule: Expired authorization cleanup
     */
    List<Authorization> findByExpiryDateBefore(LocalDate date);

    /**
     * Find all pending authorizations for an account.
     * Per COPAUA0C rule R00010: Account-specific pending auth summary
     */
    List<Authorization> findByAccountIdAndStatus(Long accountId, String status);

    /**
     * Find authorizations by account and status within expiry date range.
     * Per CBPAUP0C rule: Cleanup aged pending authorizations
     */
    @Query("SELECT a FROM Authorization a WHERE a.account.id = :accountId AND a.status = :status AND a.expiryDate BETWEEN :fromDate AND :toDate")
    List<Authorization> findByAccountIdStatusAndExpiryDateBetween(
            @Param("accountId") Long accountId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Count pending authorizations for an account.
     * Per COPAUA0C rule R00010: Used in available credit calculation
     */
    @Query("SELECT COUNT(a) FROM Authorization a WHERE a.account.id = :accountId AND a.status = 'APPROVED'")
    long countPendingApprovedByAccountId(@Param("accountId") Long accountId);
}
