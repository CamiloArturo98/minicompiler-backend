package com.minicompiler.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Provides access to the built-in mini-compiler code examples.
 * Acts as the single source of truth for example retrieval logic,
 * keeping the controller focused solely on HTTP concerns.
 */
@Service
public class ExampleService {

    // =========================================================================
    // Code snippets
    // =========================================================================

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

    // =========================================================================
    // Strategy — single registry consumed by all public methods
    // =========================================================================

    private static final Map<String, String> CODE_BY_NAME = Map.of(
            "fibonacci", FIBONACCI,
            "factorial", FACTORIAL,
            "loops",     LOOPS,
            "strings",   STRINGS,
            "power",     POWER
    );

    private static final Map<String, Object> ALL_EXAMPLES = Map.of(
            "fibonacci",  Map.of("label", "Fibonacci",    "code", FIBONACCI),
            "factorial",  Map.of("label", "Factorial",    "code", FACTORIAL),
            "bubbleSort", Map.of("label", "Loops & Math", "code", LOOPS),
            "strings",    Map.of("label", "Strings",      "code", STRINGS),
            "power",      Map.of("label", "Power & Ops",  "code", POWER)
    );

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns all examples with their label and source code.
     *
     * @return unmodifiable map of all examples
     */
    public Map<String, Object> findAll() {
        return ALL_EXAMPLES;
    }

    /**
     * Finds a single example by name.
     *
     * @param  name the example key (e.g. {@code fibonacci})
     * @return an {@link Optional} containing the source code, or empty if not found
     */
    public Optional<String> findByName(String name) {
        return Optional.ofNullable(CODE_BY_NAME.get(name));
    }
}