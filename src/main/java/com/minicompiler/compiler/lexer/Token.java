package com.minicompiler.compiler.lexer;

public record Token(
        TokenType type,
        String value,
        int line,
        int column
) {
    @Override
    public String toString() {
        return "Token[%s, '%s', L%d:C%d]".formatted(type, value, line, column);
    }
}