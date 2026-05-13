package com.minicompiler.controller;

import com.minicompiler.dto.request.CompileRequest;
import com.minicompiler.service.OptimizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the optimization comparison endpoint.
 * Delegates all compilation and analysis logic to {@link OptimizerService}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/optimizer")
@RequiredArgsConstructor
public class OptimizerController {

    private final OptimizerService optimizerService;

    /**
     * Compiles the given source code and returns a comparison between
     * the original and optimized instruction lists.
     *
     * @param  request the validated compile request body
     * @return optimization metrics and both instruction lists
     */
    @PostMapping("/compare")
    public ResponseEntity<OptimizerService.OptimizationResult> compare(
            @Valid @RequestBody CompileRequest request) {
        log.info("POST /optimizer/compare — {} chars", request.sourceCode().length());
        return ResponseEntity.ok(optimizerService.compare(request.sourceCode()));
    }
}