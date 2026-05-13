package com.minicompiler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller exposing service health and liveness endpoints.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String STATUS_UP       = "UP";
    private static final String SERVICE_NAME    = "minicompiler-backend";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final String PING_RESPONSE   = "pong";

    // =========================================================================
    // Fields
    // =========================================================================

    private final LocalDateTime startTime = LocalDateTime.now();

    // =========================================================================
    // Endpoints
    // =========================================================================

    /**
     * Returns the current health status, service metadata, and uptime info.
     *
     * @return health payload with status, version, start time, and current timestamp
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status",    STATUS_UP,
                "service",   SERVICE_NAME,
                "version",   SERVICE_VERSION,
                "startedAt", startTime,
                "timestamp", LocalDateTime.now()
        ));
    }

    /** Liveness check — returns {@code pong}. */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok(PING_RESPONSE);
    }
}