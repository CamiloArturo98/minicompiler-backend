package com.minicompiler.compiler.codegen;
import java.util.Objects;
/**
 * Immutable value object representing a single bytecode instruction.
 * Composed of an {@link OpCode}, an optional operand, and a source line number.
 */
public record Instruction(OpCode opCode, Object operand, int line) {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String FORMAT_WITH_OPERAND    = "%-16s %s";
    private static final String FORMAT_WITHOUT_OPERAND = "%s";

    // =========================================================================
    // Compact canonical constructor — validation
    // =========================================================================

    /**
     * Validates that {@code opCode} is never {@code null}.
     *
     * @throws NullPointerException si {@code opCode} es {@code null}
     */
    public Instruction {
        Objects.requireNonNull(opCode, "opCode must not be null");
    }

    // =========================================================================
    // Convenience constructor
    // =========================================================================

    /**
     * Creates an instruction with no operand.
     *
     * @param opCode the operation code
     * @param line   source line number
     */
    public Instruction(OpCode opCode, int line) {
        this(opCode, null, line);
    }

    // =========================================================================
    // Overrides
    // =========================================================================

    @Override
    public String toString() {
        return operand != null
                ? FORMAT_WITH_OPERAND.formatted(opCode.name(), operand)
                : FORMAT_WITHOUT_OPERAND.formatted(opCode.name());
    }
}