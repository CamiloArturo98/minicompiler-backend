package com.minicompiler.controller;

import com.minicompiler.dto.request.CompileRequest;
import com.minicompiler.dto.response.CompileResponse;
import com.minicompiler.service.CompilerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing compilation, health-check, and code example endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/compiler")
@RequiredArgsConstructor
public class CompilerController {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String STATUS_UP = "UP";

    private static final Map<String, String> EXAMPLES = Map.of(
            "fibonacci", """
                    function fibonacci(n) {
                        if (n <= 1) {
                            return n;
                        }
                        return fibonacci(n - 1) + fibonacci(n - 2);
                    }
                    var result = fibonacci(10);
                    print(result);
                    """,
            "factorial", """
                    function factorial(n) {
                        if (n <= 1) { return 1; }
                        return n * factorial(n - 1);
                    }
                    print(factorial(6));
                    """,
            "bubbleSort", """
                    // Bubble sort simulation with constants
                    var sum = 0;
                    var i = 1;
                    while (i <= 10) {
                        sum += i;
                        i++;
                    }
                    print(sum);
                    """,
            "strings", """
                    var name = "World";
                    var greeting = "Hello, " + name + "!";
                    print(greeting);
                    var x = 42;
                    var msg = "Answer is: " + x;
                    print(msg);
                    """
    );

    // =========================================================================
    // Fields
    // =========================================================================

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${app.version}")
    private String serviceVersion;

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final CompilerService compilerService;

    // =========================================================================
    // Endpoints
    // =========================================================================

    /**
     * Compiles the submitted source code and returns the full compilation result.
     *
     * @param  request the validated compile request body
     * @return the compilation response
     */
    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> compile(@Valid @RequestBody CompileRequest request) {
        log.info("POST /compile — {} chars, optimize={}", request.sourceCode().length(), request.optimize());
        var response = compilerService.compile(request);
        return ResponseEntity.ok(response);
    }

    /** Returns the current health status of the service. */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  STATUS_UP,
                "service", serviceName,
                "version", serviceVersion
        ));
    }

    /** Returns a set of pre-built code examples ready for compilation. */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, String>> examples() {
        return ResponseEntity.ok(EXAMPLES);
    }
}