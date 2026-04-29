package com.minicompiler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/examples")
public class ExamplesController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        return ResponseEntity.ok(Map.of(
                "fibonacci",  Map.of("label","Fibonacci","code", FIBONACCI),
                "factorial",  Map.of("label","Factorial","code", FACTORIAL),
                "bubbleSort", Map.of("label","Loops & Math","code", LOOPS),
                "strings",    Map.of("label","Strings","code", STRINGS),
                "power",      Map.of("label","Power & Ops","code", POWER)
        ));
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, String>> findByName(@PathVariable String name) {
        Map<String, String> all = Map.of(
                "fibonacci", FIBONACCI, "factorial", FACTORIAL,
                "loops", LOOPS, "strings", STRINGS, "power", POWER
        );
        String code = all.get(name);
        if (code == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("name", name, "code", code));
    }

    private static final String FIBONACCI = """
            function fibonacci(n) {
                if (n <= 1) { return n; }
                return fibonacci(n - 1) + fibonacci(n - 2);
            }
            var result = fibonacci(10);
            print("fibonacci(10) = " + result);
            """;

    private static final String FACTORIAL = """
            function factorial(n) {
                var result = 1;
                var i = 1;
                while (i <= n) { result = result * i; i++; }
                return result;
            }
            print("10! = " + factorial(10));
            """;

    private static final String LOOPS = """
            var sum = 0;
            var i = 1;
            while (i <= 10) { sum += i * i; i++; }
            print("Sum of squares = " + sum);
            """;

    private static final String STRINGS = """
            var name = "MiniCompiler";
            var version = "1.0.0";
            print("Welcome to " + name + " v" + version);
            """;

    private static final String POWER = """
            var result = 2 ** 10;
            print("2^10 = " + result);
            function square(n) { return n * n; }
            print("5^2 = " + square(5));
            """;
}