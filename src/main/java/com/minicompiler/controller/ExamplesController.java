package com.minicompiler.controller;

import com.minicompiler.service.ExampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing built-in code examples for the mini-compiler playground.
 * Delegates all data access to {@link ExampleService}.
 */
@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
public class ExamplesController {

    private final ExampleService exampleService;

    /** Returns all available examples with their labels and source code. */
    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        return ResponseEntity.ok(exampleService.findAll());
    }

    /**
     * Returns a single example by name.
     *
     * @param  name the example key (e.g. {@code fibonacci}, {@code factorial})
     * @return the example code, or {@code 404} if the name is not found
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, String>> findByName(@PathVariable String name) {
        return exampleService.findByName(name)
                .map(code -> ResponseEntity.ok(Map.of("name", name, "code", code)))
                .orElse(ResponseEntity.notFound().build());
    }
}