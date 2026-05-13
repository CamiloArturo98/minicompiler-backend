package com.minicompiler.controller;

import com.minicompiler.dto.response.StatsResponse;
import com.minicompiler.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing compiler usage statistics.
 */
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** Returns the current aggregated compiler statistics. */
    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}