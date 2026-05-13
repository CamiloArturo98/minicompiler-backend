package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a code snippet.
 *
 * @param title       display title for the snippet; must not be blank, max 100 chars
 * @param description optional description of what the snippet does; max 300 chars
 * @param code        full source code of the snippet; must not be blank
 * @param category    optional category tag for filtering (e.g. {@code loops}); max 50 chars
 */
public record SnippetRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 300)           String description,
        @NotBlank                  String code,
        @Size(max = 50)            String category
) {}