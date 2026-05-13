package com.minicompiler.controller;

import com.minicompiler.dto.request.SnippetRequest;
import com.minicompiler.dto.response.SnippetResponse;
import com.minicompiler.service.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing user-saved code snippets.
 * Supports listing, searching, creating, updating, liking, and deleting snippets.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/snippets")
@RequiredArgsConstructor
public class SnippetController {

    private final SnippetService snippetService;

    // =========================================================================
    // Endpoints
    // =========================================================================

    /**
     * Returns snippets filtered by {@code search} or {@code category} when provided,
     * or all snippets when neither parameter is present.
     */
    @GetMapping
    public ResponseEntity<List<SnippetResponse>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if (search != null)   return ResponseEntity.ok(snippetService.search(search));
        if (category != null) return ResponseEntity.ok(snippetService.findByCategory(category));
        return ResponseEntity.ok(snippetService.findAll());
    }

    /**
     * Returns a single snippet by its ID.
     *
     * @param id the snippet ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.findById(id));
    }

    /**
     * Creates a new snippet and returns its location in the {@code Location} header.
     *
     * @param request the validated snippet request body
     */
    @PostMapping
    public ResponseEntity<SnippetResponse> create(@Valid @RequestBody SnippetRequest request) {
        var created  = snippetService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        log.info("Snippet created — id={}", created.id());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Updates an existing snippet by its ID.
     *
     * @param id      the snippet ID to update
     * @param request the validated update request body
     */
    @PutMapping("/{id}")
    public ResponseEntity<SnippetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SnippetRequest request) {
        return ResponseEntity.ok(snippetService.update(id, request));
    }

    /**
     * Increments the like count of a snippet.
     *
     * @param id the snippet ID to like
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<SnippetResponse> like(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.like(id));
    }

    /**
     * Deletes a snippet by its ID.
     *
     * @param id the snippet ID to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        snippetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}