package com.minicompiler.compiler.codegen;

public enum OpCode {
    // Stack Operations
    PUSH, POP, DUP, SWAP,

    // Arithmetic
    ADD, SUB, MUL, DIV, MOD, POW, NEG,

    // Comparison
    EQ, NEQ, LT, LTE, GT, GTE,

    // Logical
    AND, OR, NOT,

    // Variables
    LOAD, STORE, LOAD_CONST,

    // Control Flow
    JUMP, JUMP_IF_FALSE, JUMP_IF_TRUE,

    // Functions
    CALL, RETURN, DEFINE_FUNC,

    // Built-ins
    PRINT, INPUT,

    // Misc
    NOP, HALT, LABEL
}