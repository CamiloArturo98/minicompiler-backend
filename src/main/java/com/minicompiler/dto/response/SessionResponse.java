package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Response body representing a compilation session.
 * Fields with {@code null} values are excluded from JSON serialization.
 *
 * @param id           unique identifier of the session
 * @param name         display name of the session
 * @param description  optional context description; {@code null} if not provided
 * @param createdAt    timestamp when the session was created
 * @param totalRecords number of compilation records associated with this session
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionResponse(
        Long          id,
        String        name,
        String        description,
        LocalDateTime createdAt,
        int           totalRecords
) {}