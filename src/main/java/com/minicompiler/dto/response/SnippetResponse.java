package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SnippetResponse(
        Long id,
        String title,
        String description,
        String code,
        String category,
        int likes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}