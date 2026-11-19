package com.carddemo.exception;

/**
 * Exception thrown when account has insufficient available credit.
 * HTTP Status: 409 Conflict
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(Long accountId, String availableCredit, String requestedAmount) {
        super("Insufficient funds for account " + accountId + ". Available: " + availableCredit + ", Requested: " + requestedAmount);
    }
}
