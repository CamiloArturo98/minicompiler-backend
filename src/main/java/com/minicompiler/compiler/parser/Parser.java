package com.minicompiler.compiler.parser;

import com.minicompiler.compiler.ast.Node;
import com.minicompiler.compiler.lexer.Token;
import com.minicompiler.compiler.lexer.TokenType;
import com.minicompiler.exception.CompilerException;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Node.Program parse() {
        List<Node> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new Node.Program(statements, 1);
    }

    // ─── Statement Dispatch ──────────────────────────────────────────────────

    private Node parseStatement() {
        Token t = current();
        return switch (t.type()) {
            case VAR, CONST   -> parseVarDecl();
            case IF           -> parseIf();
            case WHILE        -> parseWhile();
            case FOR          -> parseFor();
            case FUNCTION     -> parseFunctionDecl();
            case RETURN       -> parseReturn();
            case PRINT        -> parsePrint();
            case INPUT        -> parseInput();
            case LBRACE       -> parseBlock();
            default           -> parseExpressionStatement();
        };
    }

    private Node parseVarDecl() {
        boolean isConst = current().type() == TokenType.CONST;
        int line = current().line();
        advance(); // var | const

        String name = expect(TokenType.IDENTIFIER, "Expected variable name").value();

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
        Token t = current();
        if (t.type() == TokenType.INT_TYPE || t.type() == TokenType.FLOAT_TYPE
                || t.type() == TokenType.STRING_TYPE || t.type() == TokenType.BOOL_TYPE) {
            advance();
            return t.value();
        }
        throw new CompilerException("Expected type keyword", "PARSER", t.line(), t.column());
    }

    private Node parseIf() {
        int line = current().line();
        advance(); // if
        expect(TokenType.LPAREN, "Expected '(' after 'if'");
        Node condition = parseExpression();
        expect(TokenType.RPAREN, "Expected ')' after condition");
        Node thenBranch = parseStatement();
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
        expect(TokenType.LPAREN, "Expected '(' after 'while'");
        Node condition = parseExpression();
        expect(TokenType.RPAREN, "Expected ')' after condition");
        Node body = parseStatement();
        return new Node.WhileStmt(condition, body, line);
    }

    private Node parseFor() {
        int line = current().line();
        advance(); // for
        expect(TokenType.LPAREN, "Expected '(' after 'for'");

        Node init = null;
        if (!check(TokenType.SEMICOLON)) {
            if (check(TokenType.VAR)) {
                init = parseVarDecl();
            } else {
                init = parseExpressionStatement();
            }
        } else {
            advance();
        }

        Node condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }
        expect(TokenType.SEMICOLON, "Expected ';' in for loop");

        Node update = null;
        if (!check(TokenType.RPAREN)) {
            update = parseExpression();
        }
        expect(TokenType.RPAREN, "Expected ')' after for clauses");

        Node body = parseStatement();
        return new Node.ForStmt(init, condition, update, body, line);
    }

    private Node parseFunctionDecl() {
        int line = current().line();
        advance(); // function
        String name = expect(TokenType.IDENTIFIER, "Expected function name").value();
        expect(TokenType.LPAREN, "Expected '(' after function name");

        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params.add(expect(TokenType.IDENTIFIER, "Expected parameter name").value());
            while (match(TokenType.COMMA)) {
                params.add(expect(TokenType.IDENTIFIER, "Expected parameter name").value());
            }
        }
        expect(TokenType.RPAREN, "Expected ')' after parameters");
        Node body = parseBlock();
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
        expect(TokenType.LPAREN, "Expected '(' after 'print'");
        Node expr = parseExpression();
        expect(TokenType.RPAREN, "Expected ')' after print expression");
        consumeSemicolon();
        return new Node.PrintStmt(expr, line);
    }

    private Node parseInput() {
        int line = current().line();
        advance(); // input
        expect(TokenType.LPAREN, "Expected '(' after 'input'");
        String varName = expect(TokenType.IDENTIFIER, "Expected variable name in input").value();
        expect(TokenType.RPAREN, "Expected ')' after input variable");
        consumeSemicolon();
        return new Node.InputStmt(varName, line);
    }

    private Node.Block parseBlock() {
        int line = current().line();
        expect(TokenType.LBRACE, "Expected '{'");
        List<Node> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            stmts.add(parseStatement());
        }
        expect(TokenType.RBRACE, "Expected '}'");
        return new Node.Block(stmts, line);
    }

    private Node parseExpressionStatement() {
        Node expr = parseExpression();
        consumeSemicolon();
        return expr;
    }

    // ─── Expression Parsing (Pratt-style precedence) ─────────────────────────

    private Node parseExpression() {
        return parseAssignment();
    }

    private Node parseAssignment() {
        Node left = parseOr();
        int line = current().line();

        if (check(TokenType.ASSIGN) || check(TokenType.PLUS_ASSIGN) ||
                check(TokenType.MINUS_ASSIGN) || check(TokenType.MULTIPLY_ASSIGN) ||
                check(TokenType.DIVIDE_ASSIGN)) {
            String op = current().value();
            advance();
            Node value = parseAssignment();
            if (left instanceof Node.Identifier id) {
                return new Node.Assign(id.name(), op, value, line);
            }
            throw new CompilerException("Invalid assignment target", "PARSER", line, 0);
        }
        return left;
    }

    private Node parseOr() {
        Node left = parseAnd();
        while (check(TokenType.OR)) {
            int line = current().line();
            String op = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseAnd(), line);
        }
        return left;
    }

    private Node parseAnd() {
        Node left = parseEquality();
        while (check(TokenType.AND)) {
            int line = current().line();
            String op = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseEquality(), line);
        }
        return left;
    }

    private Node parseEquality() {
        Node left = parseComparison();
        while (check(TokenType.EQUAL) || check(TokenType.NOT_EQUAL)) {
            int line = current().line();
            String op = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseComparison(), line);
        }
        return left;
    }

    private Node parseComparison() {
        Node left = parseAddSub();
        while (check(TokenType.LESS) || check(TokenType.LESS_EQUAL) ||
               check(TokenType.GREATER) || check(TokenType.GREATER_EQUAL)) {
            int line = current().line();
            String op = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseAddSub(), line);
        }
        return left;
    }

    private Node parseAddSub() {
        Node left = parseMulDiv();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            int line = current().line();
            String op = current().value(); advance();
            left = new Node.BinaryOp(op, left, parseMulDiv(), line);
        }
        return left;
    }

    private Node parseMulDiv() {
        Node left = parsePower();
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE) || check(TokenType.MODULO)) {
            int line = current().line();
            String op = current().value(); advance();
            left = new Node.BinaryOp(op, left, parsePower(), line);
        }
        return left;
    }

    private Node parsePower() {
        Node left = parseUnary();
        if (check(TokenType.POWER)) {
            int line = current().line();
            advance();
            return new Node.BinaryOp("**", left, parsePower(), line); // right-assoc
        }
        return left;
    }

    private Node parseUnary() {
        int line = current().line();
        if (check(TokenType.MINUS)) {
            advance();
            return new Node.UnaryOp("-", parseUnary(), line);
        }
        if (check(TokenType.NOT)) {
            advance();
            return new Node.UnaryOp("!", parseUnary(), line);
        }
        if (check(TokenType.INCREMENT)) {
            advance();
            return new Node.UnaryOp("++pre", parseUnary(), line);
        }
        if (check(TokenType.DECREMENT)) {
            advance();
            return new Node.UnaryOp("--pre", parseUnary(), line);
        }
        return parsePostfix();
    }

    private Node parsePostfix() {
        Node left = parsePrimary();
        int line = current().line();
        if (check(TokenType.INCREMENT)) {
            advance();
            return new Node.UnaryOp("++post", left, line);
        }
        if (check(TokenType.DECREMENT)) {
            advance();
            return new Node.UnaryOp("--post", left, line);
        }
        return left;
    }

    private Node parsePrimary() {
        Token t = current();
        int line = t.line();

        switch (t.type()) {
            case INTEGER -> { advance(); return new Node.Literal(Integer.parseInt(t.value()), "int", line); }
            case FLOAT   -> { advance(); return new Node.Literal(Double.parseDouble(t.value()), "float", line); }
            case STRING  -> { advance(); return new Node.Literal(t.value(), "string", line); }
            case BOOLEAN -> { advance(); return new Node.Literal(Boolean.parseBoolean(t.value()), "bool", line); }
            case NULL    -> { advance(); return new Node.Literal(null, "null", line); }
            case TRUE    -> { advance(); return new Node.Literal(true, "bool", line); }
            case FALSE   -> { advance(); return new Node.Literal(false, "bool", line); }
            case IDENTIFIER -> {
                advance();
                if (check(TokenType.LPAREN)) {
                    return parseFunctionCall(t.value(), line);
                }
                return new Node.Identifier(t.value(), line);
            }
            case LPAREN -> {
                advance();
                Node expr = parseExpression();
                expect(TokenType.RPAREN, "Expected ')' after expression");
                return expr;
            }
            default -> throw new CompilerException(
                    "Unexpected token: '" + t.value() + "'", "PARSER", t.line(), t.column());
        }
    }

    private Node parseFunctionCall(String name, int line) {
        expect(TokenType.LPAREN, "Expected '('");
        List<Node> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args.add(parseExpression());
            while (match(TokenType.COMMA)) {
                args.add(parseExpression());
            }
        }
        expect(TokenType.RPAREN, "Expected ')'");
        return new Node.FunctionCall(name, args, line);
    }

    // ─── Token Utilities ─────────────────────────────────────────────────────

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

    private Token expect(TokenType type, String message) {
        if (!check(type)) {
            Token t = current();
            throw new CompilerException(message + " but got '" + t.value() + "'",
                    "PARSER", t.line(), t.column());
        }
        Token t = current();
        advance();
        return t;
    }

    private void consumeSemicolon() {
        match(TokenType.SEMICOLON);
    }
}