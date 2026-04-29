package com.minicompiler.controller;

import com.minicompiler.compiler.lexer.Lexer;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.dto.request.CompileRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tokenizer")
public class TokenizerController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> tokenize(
            @Valid @RequestBody CompileRequest request) {
        log.debug("Tokenizing {} chars", request.sourceCode().length());
        Lexer lexer = new Lexer(request.sourceCode());
        List<Token> tokens = lexer.tokenize();
        return ResponseEntity.ok(Map.of(
                "tokens", tokens,
                "count", tokens.size()
        ));
    }
}