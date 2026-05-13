package com.minicompiler.compiler.lexer;

import java.util.Objects;

/**
 * Immutable value object representing a single lexical token produced by the {@link Lexer}.
 *
 * @param type   the category of this token
 * @param value  the raw source text matched
 * @param line   source line number (1-based)
 * @param column source column number (1-based)
 */
public record Token(
        TokenType type,
        String    value,
        int       line,
        int       column
) {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String TO_STRING_FORMAT = "Token[%s, '%s', L%d:C%d]";

    // =========================================================================
    // Compact canonical constructor — validation
    // =========================================================================

    public Token {
        Objects.requireNonNull(type,  "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }

    // =========================================================================
    // Overrides
    // =========================================================================

    @Override
    public String toString() {
        return TO_STRING_FORMAT.formatted(type, value, line, column);
    }
}