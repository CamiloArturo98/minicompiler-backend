package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiRequest(
        @NotNull AiAction action,
        String sourceCode,
        String errorMessage,
        String userPrompt
) {
    public enum AiAction {
        EXPLAIN_ERROR,
        SUGGEST_FIX,
        GENERATE_CODE,
        ANALYZE_CODE
    }
}