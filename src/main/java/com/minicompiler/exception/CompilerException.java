package com.minicompiler.exception;

/**
 * Runtime exception thrown when the compiler encounters a lexical, syntactic,
 * or semantic error, carrying source location and phase context.
 */
public class CompilerException extends RuntimeException {

    private final int    line;
    private final int    column;
    private final String phase;

    /**
     * Creates a {@code CompilerException} with full source location context.
     *
     * @param message human-readable description of the error
     * @param phase   compiler phase where the error occurred (e.g. {@code LEXER}, {@code PARSER})
     * @param line    source line number where the error was detected
     * @param column  source column number where the error was detected
     */
    public CompilerException(String message, String phase, int line, int column) {
        super(message);
        this.phase  = phase;
        this.line   = line;
        this.column = column;
    }

    /**
     * Creates a {@code CompilerException} without source location context.
     * Line and column default to {@code 0}.
     *
     * @param message human-readable description of the error
     * @param phase   compiler phase where the error occurred
     */
    public CompilerException(String message, String phase) {
        this(message, phase, 0, 0);
    }

    /** @return source line number where the error was detected */
    public int getLine() { return line; }

    /** @return source column number where the error was detected */
    public int getColumn() { return column; }

    /** @return compiler phase where the error originated */
    public String getPhase() { return phase; }
}