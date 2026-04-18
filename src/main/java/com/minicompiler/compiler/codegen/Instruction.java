package com.minicompiler.compiler.codegen;

public record Instruction(OpCode opCode, Object operand, int line) {

    public Instruction(OpCode opCode, int line) {
        this(opCode, null, line);
    }

    @Override
    public String toString() {
        return operand != null
                ? "%-16s %s".formatted(opCode.name(), operand)
                : opCode.name();
    }
}