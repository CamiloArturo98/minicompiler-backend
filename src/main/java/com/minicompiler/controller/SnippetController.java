package com.minicompiler.controller;

import com.minicompiler.dto.request.SnippetRequest;
import com.minicompiler.dto.response.SnippetResponse;
import com.minicompiler.service.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/snippets")
@RequiredArgsConstructor
public class SnippetController {

    private final SnippetService snippetService;

    @GetMapping
    public ResponseEntity<List<SnippetResponse>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if (search != null)    return ResponseEntity.ok(snippetService.search(search));
        if (category != null)  return ResponseEntity.ok(snippetService.findByCategory(category));
        return ResponseEntity.ok(snippetService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SnippetResponse> create(@Valid @RequestBody SnippetRequest request) {
        SnippetResponse created = snippetService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SnippetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SnippetRequest request) {
        return ResponseEntity.ok(snippetService.update(id, request));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<SnippetResponse> like(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.like(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        snippetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}