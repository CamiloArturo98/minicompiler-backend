package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiHistoryResponse(
        Long id,
        String userContent,
        String assistantContent,
        String action,
        long responseTimeMs,
        LocalDateTime createdAt
) {}