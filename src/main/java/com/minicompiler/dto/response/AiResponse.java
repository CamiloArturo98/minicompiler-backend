package com.minicompiler.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiResponse {
    private String content;
    private String action;
    private long responseTimeMs;
}