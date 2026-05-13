package com.minicompiler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Cross-Origin Resource Sharing (CORS) rules for all endpoints.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String   ALL_PATHS       = "/**";
    private static final String   ALL_PATTERNS    = "*";
    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"};
    private static final long     MAX_AGE_SECONDS = 3_600;

    // =========================================================================
    // Fields
    // =========================================================================

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    // =========================================================================
    // WebMvcConfigurer
    // =========================================================================

    /**
     * Applies a permissive CORS policy across all endpoints.
     * {@code allowedOrigins} is injected from {@code app.cors.allowed-origins}.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(ALL_PATHS)
                .allowedOriginPatterns(ALL_PATTERNS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALL_PATTERNS)
                .allowCredentials(false)
                .maxAge(MAX_AGE_SECONDS);
    }
}