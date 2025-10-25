package edu.nu.owaspapivulnlab.exception;

// SECURITY FIX (Task 3): Custom exception for unauthorized access
// Before: Generic RuntimeException or ResponseEntity with 403
// After: Specific exception that can be caught and handled globally
// This allows consistent 403 responses across all endpoints
public class AccessDeniedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}