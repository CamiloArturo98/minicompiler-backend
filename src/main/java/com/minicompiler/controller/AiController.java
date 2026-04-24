package com.minicompiler.controller;

import com.minicompiler.dto.request.AiRequest;
import com.minicompiler.dto.response.AiResponse;
import com.minicompiler.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<AiResponse> chat(@Valid @RequestBody AiRequest request) {
        log.info("AI request: action={}", request.action());
        return ResponseEntity.ok(aiService.process(request));
    }
}