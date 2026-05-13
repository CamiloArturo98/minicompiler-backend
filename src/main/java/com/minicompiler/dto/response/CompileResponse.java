package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.compiler.vm.ExecutionResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Response body returned after a compilation request.
 * Fields with {@code null} values are excluded from JSON serialization,
 * keeping the response lean when optional pipeline stages are not requested.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompileResponse {

    /** {@code true} if compilation and execution completed without errors. */
    private boolean success;

    /** Token list produced by the lexer; present only when {@code showTokens} is requested. */
    private List<Token> tokens;

    /** String representation of the AST; present only when {@code showAst} is requested. */
    private String ast;

    /** Original bytecode instruction list; present only when {@code showBytecode} is requested. */
    private List<String> bytecode;

    /** Optimized bytecode instruction list; present only when optimization is enabled. */
    private List<String> optimizedBytecode;

    /** Full execution result including output, memory snapshot, and timing. */
    private ExecutionResult executionResult;

    /** Non-fatal warnings generated during compilation or execution. */
    private List<String> warnings;

    /** Total time in milliseconds from compilation start to execution end. */
    private long compilationTimeMs;
}