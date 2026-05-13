package com.minicompiler.compiler.parser;

import com.minicompiler.compiler.ast.Node;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.compiler.lexer.TokenType;
import com.minicompiler.exception.CompilerException;

import java.util.*;

/**
 * Converts a flat token list produced by the {@link com.minicompiler.compiler.lexer.Lexer}
 * into an AST rooted at {@link Node.Program}.
 *
 * <p><b>Strategy</b> — {@link #ASSIGNMENT_OPS}, {@link #COMPARISON_OPS}, and
 * {@link #TYPE_TOKENS} replace repeated {@code check()} chains with O(1) immutable
 * {@link Set} lookups, making it trivial to add new operators without touching
 * the parsing methods.
 */
public class Parser {

    // =========================================================================
    // Strategy — token-type sets for operator/type groups
    // =========================================================================

    private static final Set<TokenType> ASSIGNMENT_OPS = Set.of(
            TokenType.ASSIGN,
            TokenType.PLUS_ASSIGN,
            TokenType.MINUS_ASSIGN,
            TokenType.MULTIPLY_ASSIGN,
            TokenType.DIVIDE_ASSIGN
    );

    private static final Set<TokenType> COMPARISON_OPS = Set.of(
            TokenType.LESS,
            TokenType.LESS_EQUAL,
            TokenType.GREATER,
            TokenType.GREATER_EQUAL
    );

    private static final Set<TokenType> TYPE_TOKENS = Set.of(
            TokenType.INT_TYPE,
            TokenType.FLOAT_TYPE,
            TokenType.STRING_TYPE,
            TokenType.BOOL_TYPE
    );

    // =========================================================================
    // Constants — error messages
    // =========================================================================

    private static final String ERR_EXPECTED_VAR_NAME    = "Expected variable name";
    private static final String ERR_EXPECTED_LPAREN      = "Expected '('";
    private static final String ERR_EXPECTED_RPAREN      = "Expected ')'";
    private static final String ERR_EXPECTED_LBRACE      = "Expected '{'";
    private static final String ERR_EXPECTED_RBRACE      = "Expected '}'";
    private static final String ERR_EXPECTED_SEMICOLON   = "Expected ';' in for loop";
    private static final String ERR_EXPECTED_FUNC_NAME   = "Expected function name";
    private static final String ERR_EXPECTED_PARAM_NAME  = "Expected parameter name";
    private static final String ERR_INVALID_ASSIGN       = "Invalid assignment target";
    private static final String ERR_EXPECTED_TYPE        = "Expected type keyword";
    private static final String PARSER_PHASE             = "PARSER";

    // =========================================================================
    // Fields
    // =========================================================================

    private final List<Token> tokens;
    private       int         pos;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * @param tokens unmodifiable token list produced by the Lexer,
     *               must be terminated by a {@link TokenType#EOF} token
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos    = 0;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Parses the full token stream into a {@link Node.Program}.
     *
     * @return the root AST node
     * @throws com.minicompiler.exception.CompilerException on any syntax error
     */
    public Node.Program parse() {
        var statements = new ArrayList<Node>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new Node.Program(statements, 1);
    }

    // =========================================================================
    // Statement dispatch
    // =========================================================================

    private Node parseStatement() {
        var t = current();
        return switch (t.type()) {
            case VAR, CONST -> parseVarDecl();
            case IF         -> parseIf();
            case WHILE      -> parseWhile();
            case FOR        -> parseFor();
            case FUNCTION   -> parseFunctionDecl();
            case RETURN     -> parseReturn();
            case PRINT      -> parsePrint();
            case INPUT      -> parseInput();
            case LBRACE     -> parseBlock();
            default         -> parseExpressionStatement();
        };
    }

    private Node parseVarDecl() {
        boolean isConst = current().type() == TokenType.CONST;
        int line = current().line();
        advance(); // var | const

        var name = expect(TokenType.IDENTIFIER, ERR_EXPECTED_VAR_NAME).value();

        String varType = null;
        if (match(TokenType.COLON)) {
            varType = parseType();
        }

        Node initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = parseExpression();
        }

        consumeSemicolon();
        return new Node.VarDecl(name, varType, initializer, isConst, line);
    }

    private String parseType() {
        var t = current();
        if (TYPE_TOKENS.contains(t.type())) {
            advance();
            return t.value();
        }
        throw new CompilerException(ERR_EXPECTED_TYPE, PARSER_PHASE, t.line(), t.column());
    }

    private Node parseIf() {
        int line = current().line();
        advance(); // if
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN + " after 'if'");
        var condition  = parseExpression();
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after condition");
        var thenBranch = parseStatement();
        Node elseBranch = null;
        if (check(TokenType.ELSE)) {
            advance();
            elseBranch = parseStatement();
        }
        return new Node.IfStmt(condition, thenBranch, elseBranch, line);
    }

    private Node parseWhile() {
        int line = current().line();
        advance(); // while
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN + " after 'while'");
        var condition = parseExpression();
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after condition");
        var body = parseStatement();
        return new Node.WhileStmt(condition, body, line);
    }

    private Node parseFor() {
        int line = current().line();
        advance(); // for
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN + " after 'for'");

        Node init = null;
        if (!check(TokenType.SEMICOLON)) {
            init = check(TokenType.VAR) ? parseVarDecl() : parseExpressionStatement();
        } else {
            advance();
        }

        Node condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }
        expect(TokenType.SEMICOLON, ERR_EXPECTED_SEMICOLON);

        Node update = null;
        if (!check(TokenType.RPAREN)) {
            update = parseExpression();
        }
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after for clauses");

        var body = parseStatement();
        return new Node.ForStmt(init, condition, update, body, line);
    }

    private Node parseFunctionDecl() {
        int line = current().line();
        advance(); // function
        var name = expect(TokenType.IDENTIFIER, ERR_EXPECTED_FUNC_NAME).value();
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN + " after function name");

        var params = new ArrayList<String>();
        if (!check(TokenType.RPAREN)) {
            params.add(expect(TokenType.IDENTIFIER, ERR_EXPECTED_PARAM_NAME).value());
            while (match(TokenType.COMMA)) {
                params.add(expect(TokenType.IDENTIFIER, ERR_EXPECTED_PARAM_NAME).value());
            }
        }
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after parameters");
        var body = parseBlock();
        return new Node.FunctionDecl(name, params, body, line);
    }

    private Node parseReturn() {
        int line = current().line();
        advance(); // return
        Node value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }
        consumeSemicolon();
        return new Node.ReturnStmt(value, line);
    }

    private Node parsePrint() {
        int line = current().line();
        advance(); // print
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN + " after 'print'");
        var expr = parseExpression();
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after print expression");
        consumeSemicolon();
        return new Node.PrintStmt(expr, line);
    }

    private Node parseInput() {
        int line = current().line();
        advance(); // input
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN + " after 'input'");
        var varName = expect(TokenType.IDENTIFIER, ERR_EXPECTED_VAR_NAME + " in input").value();
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after input variable");
        consumeSemicolon();
        return new Node.InputStmt(varName, line);
    }

    private Node.Block parseBlock() {
        int line = current().line();
        expect(TokenType.LBRACE, ERR_EXPECTED_LBRACE);
        var stmts = new ArrayList<Node>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            stmts.add(parseStatement());
        }
        expect(TokenType.RBRACE, ERR_EXPECTED_RBRACE);
        return new Node.Block(stmts, line);
    }

    private Node parseExpressionStatement() {
        var expr = parseExpression();
        consumeSemicolon();
        return expr;
    }

    // =========================================================================
    // Expression parsing — Pratt-style precedence climb
    // =========================================================================

    private Node parseExpression() {
        return parseAssignment();
    }

    /**
     * Resolves assignment expressions. Uses {@link #ASSIGNMENT_OPS} Strategy set
     * instead of a chain of {@code check()} calls.
     */
    private Node parseAssignment() {
        var left = parseOr();
        int line = current().line();

        if (ASSIGNMENT_OPS.contains(current().type())) {
            var op    = current().value();
            advance();
            var value = parseAssignment();
            if (left instanceof Node.Identifier id) {
                return new Node.Assign(id.name(), op, value, line);
            }
            throw new CompilerException(ERR_INVALID_ASSIGN, PARSER_PHASE, line, 0);
        }
        return left;
    }

    private Node parseOr() {
        var left = parseAnd();
        while (check(TokenType.OR)) {
            int line = current().line();
            var op   = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseAnd(), line);
        }
        return left;
    }

    private Node parseAnd() {
        var left = parseEquality();
        while (check(TokenType.AND)) {
            int line = current().line();
            var op   = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseEquality(), line);
        }
        return left;
    }

    private Node parseEquality() {
        var left = parseComparison();
        while (check(TokenType.EQUAL) || check(TokenType.NOT_EQUAL)) {
            int line = current().line();
            var op   = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseComparison(), line);
        }
        return left;
    }

    /**
     * Uses {@link #COMPARISON_OPS} Strategy set to replace four chained {@code check()} calls.
     */
    private Node parseComparison() {
        var left = parseAddSub();
        while (COMPARISON_OPS.contains(current().type())) {
            int line = current().line();
            var op   = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseAddSub(), line);
        }
        return left;
    }

    private Node parseAddSub() {
        var left = parseMulDiv();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            int line = current().line();
            var op   = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseMulDiv(), line);
        }
        return left;
    }

    private Node parseMulDiv() {
        var left = parsePower();
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE) || check(TokenType.MODULO)) {
            int line = current().line();
            var op   = current().value(); advance();
            left = new Node.BinaryOp(op, left, parsePower(), line);
        }
        return left;
    }

    private Node parsePower() {
        var left = parseUnary();
        if (check(TokenType.POWER)) {
            int line = current().line();
            advance();
            return new Node.BinaryOp("**", left, parsePower(), line); // right-associative
        }
        return left;
    }

    private Node parseUnary() {
        int line = current().line();
        if (check(TokenType.MINUS))     { advance(); return new Node.UnaryOp("-",     parseUnary(), line); }
        if (check(TokenType.NOT))       { advance(); return new Node.UnaryOp("!",     parseUnary(), line); }
        if (check(TokenType.INCREMENT)) { advance(); return new Node.UnaryOp("++pre", parseUnary(), line); }
        if (check(TokenType.DECREMENT)) { advance(); return new Node.UnaryOp("--pre", parseUnary(), line); }
        return parsePostfix();
    }

    private Node parsePostfix() {
        var left = parsePrimary();
        int line = current().line();
        if (check(TokenType.INCREMENT)) { advance(); return new Node.UnaryOp("++post", left, line); }
        if (check(TokenType.DECREMENT)) { advance(); return new Node.UnaryOp("--post", left, line); }
        return left;
    }

    private Node parsePrimary() {
        var t    = current();
        int line = t.line();

        switch (t.type()) {
            case INTEGER    -> { advance(); return new Node.Literal(Integer.parseInt(t.value()),      "int",    line); }
            case FLOAT      -> { advance(); return new Node.Literal(Double.parseDouble(t.value()),   "float",  line); }
            case STRING     -> { advance(); return new Node.Literal(t.value(),                        "string", line); }
            case BOOLEAN    -> { advance(); return new Node.Literal(Boolean.parseBoolean(t.value()), "bool",   line); }
            case NULL       -> { advance(); return new Node.Literal(null,                             "null",   line); }
            case TRUE       -> { advance(); return new Node.Literal(true,                             "bool",   line); }
            case FALSE      -> { advance(); return new Node.Literal(false,                            "bool",   line); }
            case IDENTIFIER -> {
                advance();
                if (check(TokenType.LPAREN)) {
                    return parseFunctionCall(t.value(), line);
                }
                return new Node.Identifier(t.value(), line);
            }
            case LPAREN -> {
                advance();
                var expr = parseExpression();
                expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN + " after expression");
                return expr;
            }
            default -> throw new CompilerException(
                    "Unexpected token: '" + t.value() + "'", PARSER_PHASE, t.line(), t.column());
        }
    }

    private Node parseFunctionCall(String name, int line) {
        expect(TokenType.LPAREN, ERR_EXPECTED_LPAREN);
        var args = new ArrayList<Node>();
        if (!check(TokenType.RPAREN)) {
            args.add(parseExpression());
            while (match(TokenType.COMMA)) {
                args.add(parseExpression());
            }
        }
        expect(TokenType.RPAREN, ERR_EXPECTED_RPAREN);
        return new Node.FunctionCall(name, args, line);
    }

    // =========================================================================
    // Token utilities — Factory Method for token consumption
    // =========================================================================

    private Token current() {
        return tokens.get(pos);
    }

    private boolean isAtEnd() {
        return current().type() == TokenType.EOF;
    }

    private boolean check(TokenType type) {
        return current().type() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) { advance(); return true; }
        return false;
    }

    private void advance() {
        if (!isAtEnd()) pos++;
    }

    /**
     * Factory Method — consumes and returns the current token if it matches
     * {@code type}, otherwise throws a {@link CompilerException}.
     */
    private Token expect(TokenType type, String message) {
        if (!check(type)) {
            var t = current();
            throw new CompilerException(
                    message + " but got '" + t.value() + "'", PARSER_PHASE, t.line(), t.column());
        }
        var t = current();
        advance();
        return t;
    }

    private void consumeSemicolon() {
        match(TokenType.SEMICOLON);
    }
}