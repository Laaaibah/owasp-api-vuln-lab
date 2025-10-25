package edu.nu.owaspapivulnlab.web;

import edu.nu.owaspapivulnlab.exception.AccessDeniedException;
import edu.nu.owaspapivulnlab.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Access Denied");
        errorMap.put("message", e.getMessage());
        logger.warn("Application Access Denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleSpringSecurityAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Forbidden");
        errorMap.put("message", "You do not have the required permissions to access this resource.");
        logger.warn("Security Access Denied (from @PreAuthorize): {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Not Found");
        errorMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);
    }

    // =================================================================
    // SECURITY FIX (Task 9): Add handler for input validation errors.
    // =================================================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Bad Request");
        errorMap.put("message", e.getMessage());
        logger.warn("Invalid argument provided: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabaseException(DataAccessException e) {
        String errorId = UUID.randomUUID().toString();
        logger.error("Database access error. ErrorID: {}", errorId, e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Database Error");
        errorResponse.put("message", "A database error occurred. Reference ID: " + errorId);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        String errorId = UUID.randomUUID().toString();
        logger.error("Unexpected server error. ErrorID: {}", errorId, e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred. Reference ID: " + errorId);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}