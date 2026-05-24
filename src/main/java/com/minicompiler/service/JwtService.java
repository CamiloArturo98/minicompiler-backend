package com.minicompiler.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.security.Keys;

/**
 * Handles JWT generation, parsing, and validation.
 * Tokens are signed with HMAC-SHA256 using the configured secret key.
 */
@Service
public class JwtService {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String CLAIM_ROLE = "role";

    // =========================================================================
    // Configuration
    // =========================================================================

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expirationMs;

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Generates a signed JWT for the given user, embedding their role as a claim.
     *
     * @param  userDetails the authenticated user
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(UserDetails userDetails) {
        var now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_ROLE, userDetails.getAuthorities().iterator().next().getAuthority())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT.
     *
     * @param  token the JWT string to parse
     * @return the username embedded in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Returns {@code true} if the token is valid for the given user and not expired.
     *
     * @param token       the JWT string to validate
     * @param userDetails the user to validate against
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(
                Jwts.parser()
                        .verifyWith(signingKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
        );
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}