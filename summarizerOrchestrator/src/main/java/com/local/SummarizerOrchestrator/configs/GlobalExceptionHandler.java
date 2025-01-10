package com.local.SummarizerOrchestrator.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for managing and standardizing error responses across the application.
 *
 * <p>Handles both general and runtime exceptions, ensuring detailed logging for debugging
 * and user-friendly error messages for clients.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles uncaught exceptions in the application.
     *
     * <p>Logs the exception details and returns a generic error response with a 500 status code.</p>
     *
     * @param ex The exception to handle.
     * @return A {@link ResponseEntity} containing a generic error message and HTTP 500 status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        logError("An unexpected error occurred", ex);
        return buildErrorResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles runtime exceptions in the application.
     *
     * <p>Logs the exception details and returns an error response with a detailed message and 500 status code.</p>
     *
     * @param ex The runtime exception to handle.
     * @return A {@link ResponseEntity} containing a detailed error message and HTTP 500 status.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logError("Runtime error occurred", ex);
        return buildErrorResponse("Runtime error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Logs error details, including the exception message and stack trace.
     *
     * @param message A custom message describing the context of the error.
     * @param ex      The exception to log.
     */
    private void logError(String message, Exception ex) {
        logger.error("{}: {}", message, ex.getMessage(), ex);
    }

    /**
     * Constructs a standardized error response.
     *
     * @param errorMessage The error message to include in the response.
     * @param status       The HTTP status code for the response.
     * @return A {@link ResponseEntity} containing the error message and status code.
     */
    private ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus status) {
        return ResponseEntity.status(status).body(errorMessage);
    }
}
