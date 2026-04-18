package com.minicompiler.exception;

public class CompilerException extends RuntimeException {

    private final int line;
    private final int column;
    private final String phase;

    public CompilerException(String message, String phase, int line, int column) {
        super(message);
        this.phase = phase;
        this.line = line;
        this.column = column;
    }

    public CompilerException(String message, String phase) {
        this(message, phase, 0, 0);
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getPhase() { return phase; }
}