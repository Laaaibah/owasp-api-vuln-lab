package edu.nu.owaspapivulnlab.exception;

// SECURITY FIX (Task 3): Custom exception for resource not found
// Returns 404 instead of 500
// Helps distinguish between "not found" and "not authorized"
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}