package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecordResponse(
        Long id,
        Long sessionId,
        String sourceCode,
        String output,
        String bytecode,
        boolean success,
        String errorMessage,
        long compilationTimeMs,
        int instructionsExecuted,
        boolean optimized,
        LocalDateTime createdAt
) {}