package com.minicompiler.controller;

import com.minicompiler.dto.response.PageResponse;
import com.minicompiler.dto.response.RecordResponse;
import com.minicompiler.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing read-only access to compilation logs (records).
 *
 * <p>All endpoints are public — see {@code SecurityConfig#PUBLIC_ENDPOINTS}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RecordService recordService;

    // =========================================================================
    // Endpoints
    // =========================================================================

    /**
     * Returns a paginated list of all compilation logs, most recent first.
     *
     * @param page zero-based page index (default {@code 0})
     * @param size number of records per page (default {@code 20}, capped at {@value MAX_PAGE_SIZE})
     */
    @GetMapping
    public ResponseEntity<PageResponse<RecordResponse>> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /logs?page={}&size={}", page, size);
        return ResponseEntity.ok(recordService.findAll(page, Math.min(size, MAX_PAGE_SIZE)));
    }

    /**
     * Returns a single compilation log by its ID.
     *
     * @param id the log record ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecordResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.findById(id));
    }
}