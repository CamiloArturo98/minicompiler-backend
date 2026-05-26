package com.minicompiler.controller;

import com.minicompiler.dto.request.TabRequest;
import com.minicompiler.dto.response.TabResponse;
import com.minicompiler.service.TabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing per-user editor tabs.
 * All endpoints require authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tabs")
@RequiredArgsConstructor
public class TabController {

    private final TabService tabService;

    @GetMapping
    public ResponseEntity<List<TabResponse>> getAll(Authentication auth) {
        return ResponseEntity.ok(tabService.findAllByUser(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<TabResponse> create(@Valid @RequestBody TabRequest request,
                                              Authentication auth) {
        log.debug("Creating tab '{}' for user={}", request.name(), auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tabService.create(auth.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody TabRequest request,
                                              Authentication auth) {
        return ResponseEntity.ok(tabService.update(id, auth.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        tabService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}