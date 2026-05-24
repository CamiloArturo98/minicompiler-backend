package com.minicompiler.dto.response;

/**
 * Response body returned after a successful login or registration.
 *
 * @param token    the JWT bearer token to include in subsequent requests
 * @param username the authenticated user's login name
 * @param role     the user's assigned role (e.g. {@code USER}, {@code ADMIN})
 */
public record AuthResponse(
        String token,
        String username,
        String role
) {}