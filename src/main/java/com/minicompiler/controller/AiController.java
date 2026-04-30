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

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiHistoryService aiHistoryService;

    @PostMapping("/chat")
    public ResponseEntity<AiResponse> chat(@Valid @RequestBody AiRequest request) {
        log.info("AI request: action={}", request.action());
        AiResponse response = aiService.process(request);

        String userContent = request.userPrompt() != null
                ? request.userPrompt()
                : request.action().name().toLowerCase().replace('_', ' ');

        aiHistoryService.save(
                userContent,
                response.getContent(),
                request.action().name(),
                response.getResponseTimeMs()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AiHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(aiHistoryService.findRecent(limit));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteHistoryItem(@PathVariable Long id) {
        aiHistoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory() {
        aiHistoryService.clearAll();
        return ResponseEntity.noContent().build();
    }
}