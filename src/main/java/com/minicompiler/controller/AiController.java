package com.minicompiler.controller;

import com.minicompiler.dto.request.AiRequest;
import com.minicompiler.dto.response.AiHistoryResponse;
import com.minicompiler.dto.response.AiResponse;
import com.minicompiler.service.AiHistoryService;
import com.minicompiler.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing AI chat and conversation history endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final int DEFAULT_HISTORY_LIMIT = 50;

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final AiService        aiService;
    private final AiHistoryService aiHistoryService;

    // =========================================================================
    // Endpoints
    // =========================================================================

    /**
     * Processes an AI request and persists the exchange to history.
     *
     * @param  request the validated AI request body
     * @return the AI-generated response
     */
    @PostMapping("/chat")
    public ResponseEntity<AiResponse> chat(@Valid @RequestBody AiRequest request) {
        log.info("AI request: action={}", request.action());
        var response    = aiService.process(request);
        var userContent = resolveUserContent(request);

        aiHistoryService.save(
                userContent,
                response.getContent(),
                request.action().name(),
                response.getResponseTimeMs()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Returns the most recent history entries up to {@code limit}.
     *
     * @param  limit maximum number of entries to return (default: {@value DEFAULT_HISTORY_LIMIT})
     * @return list of history responses
     */
    @GetMapping("/history")
    public ResponseEntity<List<AiHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "" + DEFAULT_HISTORY_LIMIT) int limit) {
        return ResponseEntity.ok(aiHistoryService.findRecent(limit));
    }

    /**
     * Deletes a single history entry by its ID.
     *
     * @param id the ID of the history entry to delete
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteHistoryItem(@PathVariable Long id) {
        aiHistoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Clears the entire conversation history. */
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory() {
        aiHistoryService.clearAll();
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Resolves the user-facing content label from the request: uses the explicit
     * prompt when present, otherwise derives a readable label from the action name.
     */
    private String resolveUserContent(AiRequest request) {
        return request.userPrompt() != null
                ? request.userPrompt()
                : request.action().name().toLowerCase().replace('_', ' ');
    }
}