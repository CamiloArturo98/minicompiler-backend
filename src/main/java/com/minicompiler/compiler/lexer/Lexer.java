package com.minicompiler.compiler.lexer;

import com.minicompiler.exception.CompilerException;

import java.util.*;

/**
 * Converts a raw source string into a flat list of {@link Token} objects.
 *
 * <p>Uses a <b>Strategy</b> pattern via two immutable lookup maps:
 * {@link #KEYWORDS} for reserved words and {@link #SINGLE_CHAR_TOKENS} for
 * unambiguous single-character symbols, keeping {@link #readSymbol()} focused
 * only on operators that require lookahead.
 */
public class Lexer {

    // =========================================================================
    // Strategy — keyword map (immutable, replaces mutable static initializer)
    // =========================================================================

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("var",      TokenType.VAR),
            Map.entry("const",    TokenType.CONST),
            Map.entry("if",       TokenType.IF),
            Map.entry("else",     TokenType.ELSE),
            Map.entry("while",    TokenType.WHILE),
            Map.entry("for",      TokenType.FOR),
            Map.entry("do",       TokenType.DO),
            Map.entry("function", TokenType.FUNCTION),
            Map.entry("return",   TokenType.RETURN),
            Map.entry("print",    TokenType.PRINT),
            Map.entry("input",    TokenType.INPUT),
            Map.entry("true",     TokenType.TRUE),
            Map.entry("false",    TokenType.FALSE),
            Map.entry("null",     TokenType.NULL),
            Map.entry("and",      TokenType.AND),
            Map.entry("or",       TokenType.OR),
            Map.entry("not",      TokenType.NOT),
            Map.entry("int",      TokenType.INT_TYPE),
            Map.entry("float",    TokenType.FLOAT_TYPE),
            Map.entry("string",   TokenType.STRING_TYPE),
            Map.entry("bool",     TokenType.BOOL_TYPE)
    );

    // =========================================================================
    // Strategy — single-character tokens that need no lookahead
    // =========================================================================

    private static final Map<Character, TokenType> SINGLE_CHAR_TOKENS = Map.of(
            '(', TokenType.LPAREN,
            ')', TokenType.RPAREN,
            '{', TokenType.LBRACE,
            '}', TokenType.RBRACE,
            '[', TokenType.LBRACKET,
            ']', TokenType.RBRACKET,
            ';', TokenType.SEMICOLON,
            ',', TokenType.COMMA,
            '.', TokenType.DOT,
            ':', TokenType.COLON
    );

    // =========================================================================
    // Constants
    // =========================================================================

    private static final char NULL_CHAR     = '\0';
    private static final char NEWLINE       = '\n';
    private static final char EOF_SENTINEL  = '\0';

    // =========================================================================
    // Fields
    // =========================================================================

    private final String      source;
    private final List<Token> tokens;
    private       int         pos;
    private       int         line;
    private       int         column;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * @param source the raw source code string to tokenize
     */
    public Lexer(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.pos    = 0;
        this.line   = 1;
        this.column = 1;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Scans the entire source string and returns an unmodifiable token list
     * terminated by an {@link TokenType#EOF} token.
     *
     * @return unmodifiable list of tokens
     * @throws CompilerException on any lexical error
     */
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

    // =========================================================================
    // Helpers — Factory Method for token creation
    // =========================================================================

    private char current() {
        return source.charAt(pos);
    }

    private char peek(int offset) {
        int idx = pos + offset;
        return idx < source.length() ? source.charAt(idx) : EOF_SENTINEL;
    }

    private void advance() {
        if (pos < source.length() && source.charAt(pos) == NEWLINE) {
            line++;
            column = 1;
        } else {
            column++;
        }
        pos++;
    }

    /** Factory Method — single point of {@link Token} construction. */
    private void addToken(TokenType type, String value, int startCol) {
        tokens.add(new Token(type, value, line, startCol));
    }

    // =========================================================================
    // Skip
    // =========================================================================

    private void skipWhitespaceAndComments() {
        while (pos < source.length()) {
            char c = current();
            if (c == ' ' || c == '\t' || c == '\r' || c == NEWLINE) {
                advance();
            } else if (c == '/' && peek(1) == '/') {
                while (pos < source.length() && current() != NEWLINE) advance();
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

    // =========================================================================
    // Readers
    // =========================================================================

    private void readNumber() {
        int startCol = column;
        var sb = new StringBuilder();
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
        var sb = new StringBuilder();
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
        var sb = new StringBuilder();
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
                    default   -> throw new CompilerException(
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

        // Strategy — resolve unambiguous single-char tokens first
        TokenType singleChar = SINGLE_CHAR_TOKENS.get(c);
        if (singleChar != null) {
            advance();
            addToken(singleChar, String.valueOf(c), startCol);
            return;
        }

        // Operators requiring lookahead
        switch (c) {
            case '+' -> {
                advance();
                if (pos < source.length() && current() == '+') { advance(); addToken(TokenType.INCREMENT,      "++", startCol); }
                else if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.PLUS_ASSIGN,    "+=", startCol); }
                else addToken(TokenType.PLUS, "+", startCol);
            }
            case '-' -> {
                advance();
                if (pos < source.length() && current() == '-') { advance(); addToken(TokenType.DECREMENT,      "--", startCol); }
                else if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.MINUS_ASSIGN,   "-=", startCol); }
                else addToken(TokenType.MINUS, "-", startCol);
            }
            case '*' -> {
                advance();
                if (pos < source.length() && current() == '*') { advance(); addToken(TokenType.POWER,          "**", startCol); }
                else if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.MULTIPLY_ASSIGN, "*=", startCol); }
                else addToken(TokenType.MULTIPLY, "*", startCol);
            }
            case '/' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.DIVIDE_ASSIGN,  "/=", startCol); }
                else addToken(TokenType.DIVIDE, "/", startCol);
            }
            case '%' -> { advance(); addToken(TokenType.MODULO, "%", startCol); }
            case '=' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.EQUAL,          "==", startCol); }
                else addToken(TokenType.ASSIGN, "=", startCol);
            }
            case '!' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.NOT_EQUAL,      "!=", startCol); }
                else addToken(TokenType.NOT, "!", startCol);
            }
            case '<' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.LESS_EQUAL,     "<=", startCol); }
                else addToken(TokenType.LESS, "<", startCol);
            }
            case '>' -> {
                advance();
                if (pos < source.length() && current() == '=') { advance(); addToken(TokenType.GREATER_EQUAL,  ">=", startCol); }
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
            default -> throw new CompilerException(
                    "Unexpected character: '" + c + "'", "LEXER", line, startCol);
        }
    }
}