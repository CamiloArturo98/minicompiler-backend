package com.minicompiler.compiler.lexer;

import com.minicompiler.exception.CompilerException;

import java.util.*;

public class Lexer {

    private final String source;
    private int pos;
    private int line;
    private int column;
    private final List<Token> tokens;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("var",      TokenType.VAR);
        KEYWORDS.put("const",    TokenType.CONST);
        KEYWORDS.put("if",       TokenType.IF);
        KEYWORDS.put("else",     TokenType.ELSE);
        KEYWORDS.put("while",    TokenType.WHILE);
        KEYWORDS.put("for",      TokenType.FOR);
        KEYWORDS.put("do",       TokenType.DO);
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("return",   TokenType.RETURN);
        KEYWORDS.put("print",    TokenType.PRINT);
        KEYWORDS.put("input",    TokenType.INPUT);
        KEYWORDS.put("true",     TokenType.TRUE);
        KEYWORDS.put("false",    TokenType.FALSE);
        KEYWORDS.put("null",     TokenType.NULL);
        KEYWORDS.put("and",      TokenType.AND);
        KEYWORDS.put("or",       TokenType.OR);
        KEYWORDS.put("not",      TokenType.NOT);
        KEYWORDS.put("int",      TokenType.INT_TYPE);
        KEYWORDS.put("float",    TokenType.FLOAT_TYPE);
        KEYWORDS.put("string",   TokenType.STRING_TYPE);
        KEYWORDS.put("bool",     TokenType.BOOL_TYPE);
    }

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            skipWhitespaceAndComments();
            if (pos >= source.length()) break;

            char c = current();

            if (Character.isDigit(c)) {
                readNumber();
            } else if (Character.isLetter(c) || c == '_') {
                readIdentifierOrKeyword();
            } else if (c == '"' || c == '\'') {
                readString(c);
            } else {
                readSymbol();
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return Collections.unmodifiableList(tokens);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private char current() {
        return source.charAt(pos);
    }

    private char peek(int offset) {
        int idx = pos + offset;
        return idx < source.length() ? source.charAt(idx) : '\0';
    }

    private void advance() {
        if (pos < source.length() && source.charAt(pos) == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        pos++;
    }

    private void addToken(TokenType type, String value, int startCol) {
        tokens.add(new Token(type, value, line, startCol));
    }

    // ─── Skip ────────────────────────────────────────────────────────────────

    private void skipWhitespaceAndComments() {
        while (pos < source.length()) {
            char c = current();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else if (c == '/' && peek(1) == '/') {
                while (pos < source.length() && current() != '\n') advance();
            } else if (c == '/' && peek(1) == '*') {
                skipBlockComment();
            } else {
                break;
            }
        }
    }

    private void skipBlockComment() {
        int startLine = line;
        advance(); advance(); // consume /*
        while (pos < source.length()) {
            if (current() == '*' && peek(1) == '/') {
                advance(); advance();
                return;
            }
            advance();
        }
        throw new CompilerException("Unterminated block comment", "LEXER", startLine, 0);
    }

    // ─── Readers ─────────────────────────────────────────────────────────────

    private void readNumber() {
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        while (pos < source.length() && Character.isDigit(current())) {
            sb.append(current());
            advance();
        }
        if (pos < source.length() && current() == '.' && Character.isDigit(peek(1))) {
            isFloat = true;
            sb.append(current()); advance();
            while (pos < source.length() && Character.isDigit(current())) {
                sb.append(current()); advance();
            }
        }
        // Scientific notation
        if (pos < source.length() && (current() == 'e' || current() == 'E')) {
            isFloat = true;
            sb.append(current()); advance();
            if (pos < source.length() && (current() == '+' || current() == '-')) {
                sb.append(current()); advance();
            }
            while (pos < source.length() && Character.isDigit(current())) {
                sb.append(current()); advance();
            }
        }
        addToken(isFloat ? TokenType.FLOAT : TokenType.INTEGER, sb.toString(), startCol);
    }

    private void readIdentifierOrKeyword() {
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && (Character.isLetterOrDigit(current()) || current() == '_')) {
            sb.append(current());
            advance();
        }
        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
        if (type == TokenType.TRUE || type == TokenType.FALSE) {
            addToken(TokenType.BOOLEAN, word, startCol);
        } else {
            addToken(type, word, startCol);
        }
    }

    private void readString(char quote) {
        int startCol = column;
        advance(); // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && current() != quote) {
            if (current() == '\\') {
                advance();
                char escaped = switch (current()) {
                    case 'n'  -> '\n';
                    case 't'  -> '\t';
                    case 'r'  -> '\r';
                    case '\\' -> '\\';
                    case '"'  -> '"';
                    case '\'' -> '\'';
                    default -> throw new CompilerException(
                            "Unknown escape sequence: \\" + current(), "LEXER", line, column);
                };
                sb.append(escaped);
                advance();
            } else {
                sb.append(current());
                advance();
            }
        }
        if (pos >= source.length()) {
            throw new CompilerException("Unterminated string literal", "LEXER", line, startCol);
        }
        advance(); // skip closing quote
        addToken(TokenType.STRING, sb.toString(), startCol);
    }

    private void readSymbol() {
        int startCol = column;
        char c = current();

        switch (c) {
            case '+' -> {
                advance();
                if (pos < source.length() && current() == '+') { advance(); addToken(TokenType.INCREMENT, "++", startCol); }
                else if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.PLUS_ASSIGN, "+=", startCol); }
                else addToken(TokenType.PLUS, "+", startCol);
            }
            case '-' -> {
                advance();
                if (pos < source.length() && current() == '-') { advance(); addToken(TokenType.DECREMENT, "--", startCol); }
                else if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.MINUS_ASSIGN, "-=", startCol); }
                else addToken(TokenType.MINUS, "-", startCol);
            }
            case '*' -> {
                advance();
                if (pos < source.length() && current() == '*') { advance(); addToken(TokenType.POWER, "**", startCol); }
                else if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.MULTIPLY_ASSIGN, "*=", startCol); }
                else addToken(TokenType.MULTIPLY, "*", startCol);
            }
            case '/' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.DIVIDE_ASSIGN, "/=", startCol); }
                else addToken(TokenType.DIVIDE, "/", startCol);
            }
            case '%' -> { advance(); addToken(TokenType.MODULO, "%", startCol); }
            case '=' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.EQUAL, "==", startCol); }
                else addToken(TokenType.ASSIGN, "=", startCol);
            }
            case '!' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.NOT_EQUAL, "!=", startCol); }
                else addToken(TokenType.NOT, "!", startCol);
            }
            case '<' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.LESS_EQUAL, "<=", startCol); }
                else addToken(TokenType.LESS, "<", startCol);
            }
            case '>' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.GREATER_EQUAL, ">=", startCol); }
                else addToken(TokenType.GREATER, ">", startCol);
            }
            case '&' -> {
                advance();
                if (pos < source.length() && current() == '&') { advance(); addToken(TokenType.AND, "&&", startCol); }
                else throw new CompilerException("Unexpected character '&'", "LEXER", line, startCol);
            }
            case '|' -> {
                advance();
                if (pos < source.length() && current() == '|') { advance(); addToken(TokenType.OR, "||", startCol); }
                else throw new CompilerException("Unexpected character '|'", "LEXER", line, startCol);
            }
            case '(' -> { advance(); addToken(TokenType.LPAREN, "(", startCol); }
            case ')' -> { advance(); addToken(TokenType.RPAREN, ")", startCol); }
            case '{' -> { advance(); addToken(TokenType.LBRACE, "{", startCol); }
            case '}' -> { advance(); addToken(TokenType.RBRACE, "}", startCol); }
            case '[' -> { advance(); addToken(TokenType.LBRACKET, "[", startCol); }
            case ']' -> { advance(); addToken(TokenType.RBRACKET, "]", startCol); }
            case ';' -> { advance(); addToken(TokenType.SEMICOLON, ";", startCol); }
            case ',' -> { advance(); addToken(TokenType.COMMA, ",", startCol); }
            case '.' -> { advance(); addToken(TokenType.DOT, ".", startCol); }
            case ':' -> { advance(); addToken(TokenType.COLON, ":", startCol); }
            default  -> throw new CompilerException(
                    "Unexpected character: '" + c + "'", "LEXER", line, startCol);
        }
    }
}