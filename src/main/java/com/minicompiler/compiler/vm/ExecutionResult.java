package com.minicompiler.compiler.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Immutable value object returned by the {@link VirtualMachine} after executing a program.
 *
 * <p>Fields are excluded from JSON serialization when {@code null},
 * keeping API responses clean for partial results or error states.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResult {

    /** {@code true} if the program completed without runtime errors. */
    private boolean success;

    /** Lines produced by {@code print} statements during execution, in order. */
    private List<String> output;

    /** Snapshot of all variable bindings at the moment execution ended. */
    private Map<String, Object> finalMemory;

    /** Human-readable error message; {@code null} when {@link #success} is {@code true}. */
    private String error;

    /** Total number of instructions dispatched during this execution. */
    private int instructionsExecuted;

    /** Wall-clock time in milliseconds from start to finish of execution. */
    private long executionTimeMs;
}