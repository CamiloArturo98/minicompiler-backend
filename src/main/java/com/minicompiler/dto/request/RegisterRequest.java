package com.minicompiler.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for the registration endpoint.
 *
 * @param username unique login name; 3–50 chars
 * @param email    valid email address
 * @param password plain-text password; min 6 chars
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email                   String email,
        @NotBlank @Size(min = 6)           String password
) {}