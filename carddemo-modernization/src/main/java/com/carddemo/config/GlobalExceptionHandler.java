package com.carddemo.config;

import com.carddemo.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controller advice.
 * Provides standardized error responses across all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle AccountNotFoundException (404).
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        logger.error("Account not found: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "ACCOUNT_NOT_FOUND",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle CardNotFoundException (404).
     */
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<?> handleCardNotFound(CardNotFoundException ex, WebRequest request) {
        logger.error("Card not found: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "CARD_NOT_FOUND",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle InvalidTransactionException (400).
     */
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<?> handleInvalidTransaction(InvalidTransactionException ex, WebRequest request) {
        logger.error("Invalid transaction: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_TRANSACTION",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle InsufficientFundsException (409).
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<?> handleInsufficientFunds(InsufficientFundsException ex, WebRequest request) {
        logger.error("Insufficient funds: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_FUNDS",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle AuthorizationExpiredException (409).
     */
    @ExceptionHandler(AuthorizationExpiredException.class)
    public ResponseEntity<?> handleAuthorizationExpired(AuthorizationExpiredException ex, WebRequest request) {
        logger.error("Authorization expired: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "AUTHORIZATION_EXPIRED",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle validation errors from @Valid annotations (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("Validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "Input validation failed");
        response.put("errors", errors);
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle general exceptions (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error", ex);
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            request
        );
    }

    /**
     * Build standardized error response.
     */
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String code, String message, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("code", code);
        response.put("message", message);
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("status", status.value());
        
        return ResponseEntity.status(status).body(response);
    }
}
