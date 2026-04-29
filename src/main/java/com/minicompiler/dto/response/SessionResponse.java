package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        int totalRecords
) {}