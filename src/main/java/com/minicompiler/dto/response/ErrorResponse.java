package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response body returned by the global exception handler.
 * Fields with {@code null} values are excluded from JSON serialization.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** Timestamp when the error occurred. */
    private LocalDateTime timestamp;

    /** HTTP status code (e.g. {@code 400}, {@code 500}). */
    private int status;

    /** Short error category label (e.g. {@code Bad Request}). */
    private String error;

    /** Human-readable description of what went wrong. */
    private String message;

    /** Compiler phase where the error originated (e.g. {@code LEXER}, {@code PARSER}). */
    private String phase;

    /** Source line number where the error was detected; {@code 0} if not applicable. */
    private int line;

    /** Source column number where the error was detected; {@code 0} if not applicable. */
    private int column;

    /** Per-field validation errors keyed by field name; present only on validation failures. */
    private Map<String, String> fieldErrors;
}