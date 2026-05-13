package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a compilation session.
 *
 * @param name        display name for the session; must not be blank, max 100 chars
 * @param description optional context description for the session; max 300 chars
 */
public record SessionRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300)           String description
) {}