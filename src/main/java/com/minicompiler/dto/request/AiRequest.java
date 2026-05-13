package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for AI assistant endpoints.
 * At least one of {@code sourceCode}, {@code errorMessage}, or {@code userPrompt}
 * should be provided depending on the {@link AiAction} used.
 *
 * @param action       the AI action to perform; must not be {@code null}
 * @param sourceCode   source code to analyze or explain; optional
 * @param errorMessage compiler error message to diagnose; optional
 * @param userPrompt   free-form user prompt; optional
 */
public record AiRequest(
        @NotNull AiAction action,
        String sourceCode,
        String errorMessage,
        String userPrompt
) {

    /**
     * Defines the supported AI assistant operations.
     */
    public enum AiAction {
        /** Explains what a compiler error means in plain language. */
        EXPLAIN_ERROR,
        /** Suggests a fix for the given error or code issue. */
        SUGGEST_FIX,
        /** Generates source code based on a user prompt. */
        GENERATE_CODE,
        /** Analyzes the given source code for quality or correctness. */
        ANALYZE_CODE
    }
}