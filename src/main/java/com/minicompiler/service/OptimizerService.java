package com.minicompiler.service;

import com.minicompiler.compiler.ast.Node;
import com.minicompiler.compiler.codegen.CodeGenerator;
import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.lexer.Lexer;
import com.minicompiler.compiler.optimizer.Optimizer;
import com.minicompiler.compiler.parser.Parser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the compilation and optimization pipeline, exposing a comparison
 * between the original and optimized instruction lists.
 */
@Service
public class OptimizerService {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final double PERCENT        = 100.0;
    private static final double ROUNDING_SCALE = 100.0;

    // =========================================================================
    // Public API
    // =========================================================================>

    /**
     * Compiles {@code sourceCode} and returns both the original and optimized
     * instruction lists along with reduction metrics.
     *
     * @param  sourceCode the raw source code to compile and optimize
     * @return an {@link OptimizationResult} with counts, savings, and instruction lists
     */
    public OptimizationResult compare(String sourceCode) {
        var original  = compile(sourceCode);
        var optimized = new Optimizer().optimize(original);
        return buildResult(original, optimized);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private List<Instruction> compile(String sourceCode) {
        Node.Program ast = new Parser(new Lexer(sourceCode).tokenize()).parse();
        return new CodeGenerator().generate(ast);
    }

    private OptimizationResult buildResult(List<Instruction> original,
                                           List<Instruction> optimized) {
        int    saved     = original.size() - optimized.size();
        double reduction = original.isEmpty() ? 0.0
                : Math.round((saved * PERCENT / original.size()) * ROUNDING_SCALE) / ROUNDING_SCALE;

        return new OptimizationResult(
                original.size(),
                optimized.size(),
                saved,
                reduction,
                original.stream().map(Instruction::toString).toList(),
                optimized.stream().map(Instruction::toString).toList()
        );
    }

    // =========================================================================
    // Result record
    // =========================================================================

    /**
     * Immutable value object carrying the full optimization comparison result.
     *
     * @param originalCount      number of instructions before optimization
     * @param optimizedCount     number of instructions after optimization
     * @param instructionsSaved  difference between original and optimized counts
     * @param reductionPercent   percentage of instructions eliminated
     * @param original           string representation of original instructions
     * @param optimized          string representation of optimized instructions
     */
    public record OptimizationResult(
            int            originalCount,
            int            optimizedCount,
            int            instructionsSaved,
            double         reductionPercent,
            List<String>   original,
            List<String>   optimized
    ) {}
}