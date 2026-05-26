package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a tab.
 *
 * @param name     display name shown in the tab bar; must not be blank
 * @param code     source code content; may be empty
 * @param position zero-based order in the tab bar
 */
public record TabRequest(
        @NotBlank @Size(max = 100) String name,
        String code,
        int    position
) {}