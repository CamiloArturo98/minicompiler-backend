package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Response body representing a saved code snippet.
 * Fields with {@code null} values are excluded from JSON serialization.
 *
 * @param id          unique identifier of the snippet
 * @param title       display title of the snippet
 * @param description optional description of what the snippet does
 * @param code        full source code of the snippet
 * @param category    optional category tag (e.g. {@code loops}, {@code functions})
 * @param likes       number of likes the snippet has received
 * @param createdAt   timestamp when the snippet was created
 * @param updatedAt   timestamp of the last modification; {@code null} if never updated
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SnippetResponse(
        Long          id,
        String        title,
        String        description,
        String        code,
        String        category,
        int           likes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}