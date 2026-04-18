package com.minicompiler.compiler.lexer;

public enum TokenType {
    // Literals
    INTEGER, FLOAT, STRING, BOOLEAN,

    // Identifiers & Keywords
    IDENTIFIER,
    VAR, CONST,
    IF, ELSE, WHILE, FOR, DO,
    FUNCTION, RETURN,
    PRINT, INPUT,
    TRUE, FALSE, NULL,
    AND, OR, NOT,
    INT_TYPE, FLOAT_TYPE, STRING_TYPE, BOOL_TYPE,

    // Arithmetic Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, POWER,

    // Comparison Operators
    EQUAL, NOT_EQUAL,
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,

    // Assignment
    ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN,

    // Increment / Decrement
    INCREMENT, DECREMENT,

    // Delimiters
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,
    SEMICOLON, COMMA, DOT, COLON,

    // Special
    EOF, NEWLINE, COMMENT
}