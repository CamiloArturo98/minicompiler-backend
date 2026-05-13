package com.minicompiler.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response body returned by the AI assistant after processing a request.
 */
@Getter
@Builder
public class AiResponse {

    /** The AI-generated text content. */
    private String content;

    /** The action type that produced this response (e.g. {@code EXPLAIN_ERROR}). */
    private String action;

    /** Time in milliseconds the AI took to produce this response. */
    private long responseTimeMs;
}