package com.carddemo.exception;

/**
 * Exception thrown when an account is not found.
 * HTTP Status: 404 Not Found
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long accountId) {
        super("Account not found with ID: " + accountId);
    }
}
