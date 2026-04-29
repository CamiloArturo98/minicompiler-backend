package com.minicompiler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SnippetRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 300) String description,
        @NotBlank String code,
        @Size(max = 50) String category
) {}