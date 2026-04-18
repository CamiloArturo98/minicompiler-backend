package com.minicompiler.controller;

import com.minicompiler.dto.request.CompileRequest;
import com.minicompiler.dto.response.CompileResponse;
import com.minicompiler.service.CompilerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/compiler")
@RequiredArgsConstructor
public class CompilerController {

    private final CompilerService compilerService;

    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> compile(@Valid @RequestBody CompileRequest request) {
        log.info("POST /compile — {} chars, optimize={}", request.sourceCode().length(), request.optimize());
        CompileResponse response = compilerService.compile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "minicompiler-backend",
                "version", "1.0.0"
        ));
    }

    @GetMapping("/examples")
    public ResponseEntity<Map<String, String>> examples() {
        Map<String, String> examples = Map.of(
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
        return ResponseEntity.ok(examples);
    }
}