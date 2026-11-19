package com.carddemo.exception;

/**
 * Exception thrown when a transaction is invalid.
 * HTTP Status: 400 Bad Request
 * Reasons: Expired card, card blocked, insufficient balance, etc.
 */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
