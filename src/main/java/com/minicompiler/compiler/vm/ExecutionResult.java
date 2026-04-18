package com.minicompiler.compiler.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResult {
    private boolean success;
    private List<String> output;
    private Map<String, Object> finalMemory;
    private String error;
    private int instructionsExecuted;
    private long executionTimeMs;
}