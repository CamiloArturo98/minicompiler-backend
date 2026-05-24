package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the login endpoint.
 *
 * @param username the user's login name; must not be blank
 * @param password the user's plain-text password; must not be blank
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}