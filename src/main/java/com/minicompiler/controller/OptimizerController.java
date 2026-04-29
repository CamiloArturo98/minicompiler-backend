package com.minicompiler.controller;

import com.minicompiler.compiler.codegen.CodeGenerator;
import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.lexer.Lexer;
import com.minicompiler.compiler.optimizer.Optimizer;
import com.minicompiler.compiler.parser.Parser;
import com.minicompiler.compiler.ast.Node;
import com.minicompiler.dto.request.CompileRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/optimizer")
public class OptimizerController {

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compare(
            @Valid @RequestBody CompileRequest request) {
        Lexer lexer = new Lexer(request.sourceCode());
        Parser parser = new Parser(lexer.tokenize());
        Node.Program ast = parser.parse();

        CodeGenerator gen = new CodeGenerator();
        List<Instruction> original = gen.generate(ast);

        Optimizer optimizer = new Optimizer();
        List<Instruction> optimized = optimizer.optimize(original);

        int saved = original.size() - optimized.size();
        double reduction = original.size() > 0
                ? Math.round((saved * 100.0 / original.size()) * 100.0) / 100.0 : 0.0;

        return ResponseEntity.ok(Map.of(
                "originalCount",   original.size(),
                "optimizedCount",  optimized.size(),
                "instructionsSaved", saved,
                "reductionPercent", reduction,
                "original",  original.stream().map(Instruction::toString).toList(),
                "optimized", optimized.stream().map(Instruction::toString).toList()
        ));
    }
}