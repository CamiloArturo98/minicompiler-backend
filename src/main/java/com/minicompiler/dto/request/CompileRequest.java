package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompileRequest(
        @NotBlank(message = "Source code cannot be blank")
        @Size(max = 10000, message = "Source code too large (max 10000 chars)")
        String sourceCode,

        boolean optimize,
        boolean showTokens,
        boolean showAst,
        boolean showBytecode
) {}