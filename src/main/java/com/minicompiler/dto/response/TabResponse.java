package com.minicompiler.dto.response;

import java.time.LocalDateTime;

/**
 * Response body representing a single editor tab.
 *
 * @param id        unique tab identifier
 * @param name      display name shown in the tab bar
 * @param code      full source code content
 * @param position  zero-based order in the tab bar
 * @param createdAt timestamp when the tab was created
 * @param updatedAt timestamp of the last save
 */
public record TabResponse(
        Long          id,
        String        name,
        String        code,
        int           position,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}