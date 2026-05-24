package com.minicompiler.exception;

import com.minicompiler.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 * Maps known exception types to structured {@link ErrorResponse} bodies
 * with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String ERR_COMPILER   = "Compiler Error";
    private static final String ERR_VALIDATION = "Validation Error";
    private static final String ERR_NOT_FOUND  = "Not Found";
    private static final String ERR_CONFLICT   = "Conflict";
    private static final String ERR_INTERNAL   = "Internal Server Error";
    private static final String MSG_VALIDATION = "Request validation failed";
    private static final String PARSER_PHASE   = "PARSER";

    // =========================================================================
    // Handlers
    // =========================================================================

    /**
     * Handles {@link CompilerException} thrown during any compilation phase.
     * Returns {@code 422 Unprocessable Entity} with phase and source location context.
     */
    @ExceptionHandler(CompilerException.class)
    public ResponseEntity<ErrorResponse> handleCompilerException(CompilerException ex) {
        var error = buildError(HttpStatus.UNPROCESSABLE_ENTITY, ERR_COMPILER, ex.getMessage())
                .phase(ex.getPhase())
                .line(ex.getLine())
                .column(ex.getColumn())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    /**
     * Handles Bean Validation failures on request bodies annotated with {@code @Valid}.
     * Returns {@code 400 Bad Request} with a per-field error map.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        var error = buildError(HttpStatus.BAD_REQUEST, ERR_VALIDATION, MSG_VALIDATION)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles {@link ResourceNotFoundException} when a requested entity does not exist.
     * Returns {@code 404 Not Found}.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ERR_NOT_FOUND, ex.getMessage()).build());
    }

    /**
     * Handles {@link IllegalArgumentException} for business rule violations such as
     * duplicate usernames or emails during registration.
     * Returns {@code 409 Conflict}.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ERR_CONFLICT, ex.getMessage()).build());
    }

    /**
     * Catch-all handler for any unhandled exception.
     * Returns {@code 500 Internal Server Error}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, ERR_INTERNAL, ex.getMessage()).build());
    }

    // =========================================================================
    // Factory Method — centralised ErrorResponse construction
    // =========================================================================

    /**
     * Builds a pre-populated {@link ErrorResponse} builder with timestamp, status,
     * error label, and message.
     *
     * @param status  the HTTP status to reflect in the response body
     * @param error   short error category label
     * @param message human-readable error description
     * @return a partially built {@link ErrorResponse.ErrorResponseBuilder}
     */
    private ErrorResponse.ErrorResponseBuilder buildError(HttpStatus status,
                                                          String error,
                                                          String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message);
    }
}