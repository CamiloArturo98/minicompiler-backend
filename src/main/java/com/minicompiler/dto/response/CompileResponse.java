package com.minicompiler.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.compiler.vm.ExecutionResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompileResponse {
    private boolean success;
    private List<Token> tokens;
    private String ast;
    private List<String> bytecode;
    private List<String> optimizedBytecode;
    private ExecutionResult executionResult;
    private List<String> warnings;
    private long compilationTimeMs;
}