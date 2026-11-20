package com.carddemo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtTokenProvider - Handles all JWT token operations
 * 
 * Modern implementation using JJWT 0.12.x with best practices
 * - Uses SecretKey instead of raw strings (type-safe)
 * - Uses parserBuilder() instead of deprecated parser()
 * - Handles all token exceptions properly
 */
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    
    @Value("${app.jwtExpirationInMs}")
    private long jwtExpirationInMs;
    
    /**
     * Generate JWT Token
     * 
     * @param username - User for whom token is created
     * @return - Signed JWT token string (format: Header.Payload.Signature)
     * 
     * Process:
     * 1. Create SecretKey from jwtSecret string
     * 2. Build JWT with subject (username), timestamps, and expiration
     * 3. Sign with HS512 algorithm (HMAC-SHA512)
     * 4. Compact into a single string
     */
    public String generateToken(String username) {
        // Step 1: Convert secret string to cryptographic key
        // Keys.hmacShaKeyFor() creates a proper SecretKey object for HMAC-SHA
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Step 2-4: Build and sign the token
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Extract Username from Token
     * 
     * @param token - JWT token string
     * @return - Username (subject) extracted from token
     * @throws JwtException - If token is invalid, expired, or malformed
     * 
     * Process:
     * 1. Create same SecretKey (must match signing key)
     * 2. Parse and verify token signature
     * 3. Extract body (claims/payload)
     * 4. Return subject (username)
     */
    public String getUsernameFromToken(String token) {
        // Step 1: Create the same key used to sign the token
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Step 2-4: Parse token and extract username
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    /**
     * Validate Token
     * 
     * @param token - JWT token string to validate
     * @return - true if token is valid and not expired, false otherwise
     * 
     * Checks:
     * - Token format is correct (3 parts separated by dots)
     * - Signature matches (not tampered with)
     * - Token hasn't expired
     * - Algorithm is supported
     * - Token body is not empty
     * 
     * Process:
     * 1. Attempt to parse and verify token
     * 2. Catch any exception (indicates invalid token)
     * 3. Return true only if no exception occurred
     */
    public boolean validateToken(String token) {
        // Create the same key used to sign tokens
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        try {
            // Attempt to parse and verify token
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            // If no exception, token is valid
            return true;
            
        } catch (SecurityException e) {
            // Token signature is invalid (tampered or wrong key)
            System.err.println("Invalid JWT signature: " + e.getMessage());
            
        } catch (MalformedJwtException e) {
            // Token format is incorrect (not a valid JWT structure)
            System.err.println("Invalid JWT format: " + e.getMessage());
            
        } catch (ExpiredJwtException e) {
            // Token's expiration time has passed (older than 24 hours)
            System.err.println("Expired JWT token: " + e.getMessage());
            
        } catch (UnsupportedJwtException e) {
            // Token uses unsupported algorithm or claims format
            System.err.println("Unsupported JWT token: " + e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // Token body/claims string is empty or null
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        
        // If ANY exception occurred, token is invalid
        return false;
    }
    
    /**
     * Helper method to get remaining time until token expiration
     * 
     * @param token - JWT token string
     * @return - Milliseconds until expiration (0 if expired or invalid)
     */
    public long getExpirationTimeFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remainingTime, 0);  // Return 0 if already expired
            
        } catch (JwtException e) {
            return 0;
        }
    }
    
    /**
     * Helper method to check if token is about to expire (within 1 minute)
     * 
     * @param token - JWT token string
     * @return - true if token expires within 1 minute
     */
    public boolean isTokenAboutToExpire(String token) {
        long remainingTime = getExpirationTimeFromToken(token);
        return remainingTime > 0 && remainingTime < 60000;  // 60000 ms = 1 minute
    }
}
