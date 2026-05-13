package com.minicompiler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicompiler.compiler.ast.Node;
import com.minicompiler.compiler.codegen.CodeGenerator;
import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.lexer.Lexer;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.compiler.optimizer.Optimizer;
import com.minicompiler.compiler.parser.Parser;
import com.minicompiler.compiler.vm.ExecutionResult;
import com.minicompiler.compiler.vm.VirtualMachine;
import com.minicompiler.dto.request.CompileRequest;
import com.minicompiler.dto.response.CompileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the full compilation pipeline: lexing → parsing → code generation
 * → optimization → execution, then assembles the response.
 *
 * <p><b>Template Method</b> — {@link #compile} delegates each phase to a focused
 * private method, keeping the pipeline order explicit and each step independently readable.
 *
 * <p><b>Factory Method</b> — {@link #buildResponse} is the single construction
 * point for {@link CompileResponse}, separating result assembly from pipeline logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompilerService {

    private final RecordService recordService;
    private final ObjectMapper  objectMapper;

    // =========================================================================
    // Public API — Template Method pipeline
    // =========================================================================

    /**
     * Compiles and executes the source code in {@code request}, returning
     * a response that optionally includes tokens, AST, and bytecode.
     *
     * @param  request the validated compile request
     * @return the full compilation and execution result
     */
    public CompileResponse compile(CompileRequest request) {
        long start = System.currentTimeMillis();
        log.debug("Starting compilation of {} chars", request.sourceCode().length());

        var tokens   = lex(request.sourceCode());
        var ast      = parse(tokens);
        var codeGen  = generateCode(ast);
        var bytecode = codeGen.generate(ast);
        var final_bc = request.optimize() ? optimize(bytecode) : bytecode;
        var result   = execute(codeGen, final_bc);

        return buildResponse(request, tokens, ast, codeGen, bytecode,
                request.optimize() ? final_bc : null, result,
                System.currentTimeMillis() - start);
    }

    // =========================================================================
    // Template Method steps
    // =========================================================================

    /** Phase 1 — tokenizes the source code via the {@link Lexer}. */
    private List<Token> lex(String sourceCode) {
        var tokens = new Lexer(sourceCode).tokenize();
        log.debug("Lexer produced {} tokens", tokens.size());
        return tokens;
    }

    /** Phase 2 — parses the token list into a {@link Node.Program} AST. */
    private Node.Program parse(List<Token> tokens) {
        var ast = new Parser(tokens).parse();
        log.debug("Parser produced AST with {} top-level nodes", ast.statements().size());
        return ast;
    }

    /** Phase 3 — creates and returns a {@link CodeGenerator} ready for use. */
    private CodeGenerator generateCode(Node.Program ast) {
        var gen = new CodeGenerator();
        log.debug("CodeGen produced {} instructions", gen.generate(ast).size());
        return gen;
    }

    /** Phase 4 — applies the {@link Optimizer} pass to the instruction list. */
    private List<Instruction> optimize(List<Instruction> bytecode) {
        var optimized = new Optimizer().optimize(bytecode);
        log.debug("Optimizer reduced to {} instructions", optimized.size());
        return optimized;
    }

    /** Phase 5 — executes the instruction list on the {@link VirtualMachine}. */
    private ExecutionResult execute(CodeGenerator codeGen, List<Instruction> bytecode) {
        return new VirtualMachine(codeGen.getFunctionTable()).execute(bytecode);
    }

    // =========================================================================
    // Factory Method — response assembly
    // =========================================================================

    /**
     * Assembles the {@link CompileResponse} from all pipeline outputs,
     * conditionally including optional fields based on the request flags.
     */
    private CompileResponse buildResponse(CompileRequest request,
                                          List<Token>      tokens,
                                          Node.Program     ast,
                                          CodeGenerator    codeGen,
                                          List<Instruction> bytecode,
                                          List<Instruction> optimizedBytecode,
                                          ExecutionResult   result,
                                          long              elapsedMs) {
        var builder = CompileResponse.builder()
                .success(result.isSuccess())
                .executionResult(result)
                .compilationTimeMs(elapsedMs);

        if (request.showTokens()) {
            builder.tokens(tokens);
        }
        if (request.showAst()) {
            builder.ast(serializeAst(ast));
        }
        if (request.showBytecode()) {
            builder.bytecode(bytecode.stream().map(Instruction::toString).toList());
        }
        if (request.optimize() && optimizedBytecode != null) {
            builder.optimizedBytecode(optimizedBytecode.stream().map(Instruction::toString).toList());
        }

        return builder.build();
    }

    private String serializeAst(Node.Program ast) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ast);
        } catch (Exception e) {
            log.warn("Could not serialize AST", e);
            return null;
        }
    }
}