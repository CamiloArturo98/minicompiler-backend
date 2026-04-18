package com.minicompiler.compiler.ast;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "nodeType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Node.Program.class,          name = "Program"),
    @JsonSubTypes.Type(value = Node.VarDecl.class,          name = "VarDecl"),
    @JsonSubTypes.Type(value = Node.Assign.class,           name = "Assign"),
    @JsonSubTypes.Type(value = Node.BinaryOp.class,         name = "BinaryOp"),
    @JsonSubTypes.Type(value = Node.UnaryOp.class,          name = "UnaryOp"),
    @JsonSubTypes.Type(value = Node.Literal.class,          name = "Literal"),
    @JsonSubTypes.Type(value = Node.Identifier.class,       name = "Identifier"),
    @JsonSubTypes.Type(value = Node.IfStmt.class,           name = "IfStmt"),
    @JsonSubTypes.Type(value = Node.WhileStmt.class,        name = "WhileStmt"),
    @JsonSubTypes.Type(value = Node.ForStmt.class,          name = "ForStmt"),
    @JsonSubTypes.Type(value = Node.Block.class,            name = "Block"),
    @JsonSubTypes.Type(value = Node.FunctionDecl.class,     name = "FunctionDecl"),
    @JsonSubTypes.Type(value = Node.FunctionCall.class,     name = "FunctionCall"),
    @JsonSubTypes.Type(value = Node.ReturnStmt.class,       name = "ReturnStmt"),
    @JsonSubTypes.Type(value = Node.PrintStmt.class,        name = "PrintStmt"),
    @JsonSubTypes.Type(value = Node.InputStmt.class,        name = "InputStmt"),
})
public sealed interface Node permits
        Node.Program, Node.VarDecl, Node.Assign, Node.BinaryOp, Node.UnaryOp,
        Node.Literal, Node.Identifier, Node.IfStmt, Node.WhileStmt, Node.ForStmt,
        Node.Block, Node.FunctionDecl, Node.FunctionCall, Node.ReturnStmt,
        Node.PrintStmt, Node.InputStmt {

    int line();

    record Program(java.util.List<Node> statements, int line) implements Node {}

    record VarDecl(String name, String varType, Node initializer, boolean constant, int line) implements Node {}

    record Assign(String name, String operator, Node value, int line) implements Node {}

    record BinaryOp(String operator, Node left, Node right, int line) implements Node {}

    record UnaryOp(String operator, Node operand, int line) implements Node {}

    record Literal(Object value, String type, int line) implements Node {}

    record Identifier(String name, int line) implements Node {}

    record IfStmt(Node condition, Node thenBranch, Node elseBranch, int line) implements Node {}

    record WhileStmt(Node condition, Node body, int line) implements Node {}

    record ForStmt(Node init, Node condition, Node update, Node body, int line) implements Node {}

    record Block(java.util.List<Node> statements, int line) implements Node {}

    record FunctionDecl(String name, java.util.List<String> params, Node body, int line) implements Node {}

    record FunctionCall(String name, java.util.List<Node> arguments, int line) implements Node {}

    record ReturnStmt(Node value, int line) implements Node {}

    record PrintStmt(Node expression, int line) implements Node {}

    record InputStmt(String variable, int line) implements Node {}
}