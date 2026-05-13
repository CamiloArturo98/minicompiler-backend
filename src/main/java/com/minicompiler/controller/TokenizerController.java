package com.minicompiler.controller;

import com.minicompiler.dto.request.CompileRequest;
import com.minicompiler.service.TokenizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the tokenization step of the compilation pipeline.
 * Delegates all lexer logic to {@link TokenizerService}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tokenizer")
@RequiredArgsConstructor
public class TokenizerController {

    private final TokenizerService tokenizerService;

    /**
     * Tokenizes the submitted source code and returns the token list with its count.
     *
     * @param  request the validated compile request body
     * @return tokenization result with tokens and count
     */
    @PostMapping
    public ResponseEntity<TokenizerService.TokenizationResult> tokenize(
            @Valid @RequestBody CompileRequest request) {
        log.debug("Tokenizing {} chars", request.sourceCode().length());
        return ResponseEntity.ok(tokenizerService.tokenize(request.sourceCode()));
    }
}