package com.carddemo.exception;

/**
 * Exception thrown when an authorization has expired.
 * HTTP Status: 409 Conflict
 */
public class AuthorizationExpiredException extends RuntimeException {
    public AuthorizationExpiredException(String message) {
        super(message);
    }
}
