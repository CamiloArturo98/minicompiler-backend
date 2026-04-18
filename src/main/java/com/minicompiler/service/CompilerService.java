package com.minicompiler.service;

import com.minicompiler.compiler.codegen.CodeGenerator;
import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.lexer.Lexer;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.compiler.optimizer.Optimizer;
import com.minicompiler.compiler.parser.Parser;
import com.minicompiler.compiler.ast.Node;
import com.minicompiler.compiler.vm.ExecutionResult;
import com.minicompiler.compiler.vm.VirtualMachine;
import com.minicompiler.dto.request.CompileRequest;
import com.minicompiler.dto.response.CompileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilerService {

    private final ObjectMapper objectMapper;

    public CompileResponse compile(CompileRequest request) {
        long start = System.currentTimeMillis();
        log.debug("Starting compilation of {} chars", request.sourceCode().length());

        // ── Phase 1: Lexer ────────────────────────────────────────────────────
        Lexer lexer = new Lexer(request.sourceCode());
        List<Token> tokens = lexer.tokenize();
        log.debug("Lexer produced {} tokens", tokens.size());

        // ── Phase 2: Parser ───────────────────────────────────────────────────
        Parser parser = new Parser(tokens);
        Node.Program ast = parser.parse();
        log.debug("Parser produced AST with {} top-level nodes", ast.statements().size());

        // ── Phase 3: Code Generation ──────────────────────────────────────────
        CodeGenerator codeGen = new CodeGenerator();
        List<Instruction> bytecode = codeGen.generate(ast);
        log.debug("CodeGen produced {} instructions", bytecode.size());

        // ── Phase 4: Optimization ─────────────────────────────────────────────
        List<Instruction> finalBytecode = bytecode;
        List<Instruction> optimizedBytecode = null;
        if (request.optimize()) {
            Optimizer optimizer = new Optimizer();
            optimizedBytecode = optimizer.optimize(bytecode);
            finalBytecode = optimizedBytecode;
            log.debug("Optimizer reduced to {} instructions", finalBytecode.size());
        }

        // ── Phase 5: Virtual Machine ──────────────────────────────────────────
        VirtualMachine vm = new VirtualMachine(codeGen.getFunctionTable());
        ExecutionResult result = vm.execute(finalBytecode);

        long elapsed = System.currentTimeMillis() - start;

        // ── Build Response ────────────────────────────────────────────────────
        CompileResponse.CompileResponseBuilder builder = CompileResponse.builder()
                .success(result.isSuccess())
                .executionResult(result)
                .compilationTimeMs(elapsed);

        if (request.showTokens()) {
            builder.tokens(tokens);
        }

        if (request.showAst()) {
            try {
                builder.ast(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ast));
            } catch (Exception e) {
                log.warn("Could not serialize AST", e);
            }
        }

        if (request.showBytecode()) {
            builder.bytecode(bytecode.stream().map(Instruction::toString).toList());
        }

        if (request.optimize() && optimizedBytecode != null) {
            builder.optimizedBytecode(optimizedBytecode.stream().map(Instruction::toString).toList());
        }

        return builder.build();
    }
}