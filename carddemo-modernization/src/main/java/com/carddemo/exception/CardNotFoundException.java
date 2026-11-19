package com.carddemo.exception;

/**
 * Exception thrown when a card is not found.
 * HTTP Status: 404 Not Found
 */
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message) {
        super(message);
    }
}
