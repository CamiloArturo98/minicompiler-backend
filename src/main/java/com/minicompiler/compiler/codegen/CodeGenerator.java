package com.minicompiler.compiler.codegen;

import com.minicompiler.compiler.ast.Node;
import com.minicompiler.exception.CompilerException;

import java.util.*;

public class CodeGenerator {

    private final List<Instruction> instructions;
    private int labelCounter;
    private final Map<String, Integer> functionTable;

    public CodeGenerator() {
        this.instructions    = new ArrayList<>();
        this.labelCounter    = 0;
        this.functionTable   = new HashMap<>();
    }

    public List<Instruction> generate(Node.Program program) {
        // Primera pasada: registrar funciones
        for (Node stmt : program.statements()) {
            if (stmt instanceof Node.FunctionDecl fd) {
                generateFunctionDecl(fd);
            }
        }
        // Segunda pasada: código principal
        emit(new Instruction(OpCode.NOP, 0));
        for (Node stmt : program.statements()) {
            if (!(stmt instanceof Node.FunctionDecl)) {
                generateNode(stmt);
            }
        }
        emit(new Instruction(OpCode.HALT, 0));
        return Collections.unmodifiableList(instructions);
    }

    private void generateNode(Node node) {
        switch (node) {
            case Node.Program p         -> p.statements().forEach(this::generateNode);
            case Node.VarDecl vd        -> generateVarDecl(vd);
            case Node.Assign a          -> generateAssign(a);
            case Node.BinaryOp b        -> generateBinaryOp(b);
            case Node.UnaryOp u         -> generateUnaryOp(u);
            case Node.Literal l         -> emit(new Instruction(OpCode.PUSH, l.value(), l.line()));
            case Node.Identifier id     -> emit(new Instruction(OpCode.LOAD, id.name(), id.line()));
            case Node.IfStmt is         -> generateIf(is);
            case Node.WhileStmt ws      -> generateWhile(ws);
            case Node.ForStmt fs        -> generateFor(fs);
            case Node.Block b           -> b.statements().forEach(this::generateNode);
            case Node.FunctionDecl fd   -> {} // ya procesadas en primera pasada
            case Node.FunctionCall fc   -> generateFunctionCall(fc);
            case Node.ReturnStmt rs     -> generateReturn(rs);
            case Node.PrintStmt ps      -> generatePrint(ps);
            case Node.InputStmt is      -> emit(new Instruction(OpCode.INPUT, is.variable(), is.line()));
        }
    }

    private void generateVarDecl(Node.VarDecl vd) {
        if (vd.initializer() != null) {
            generateNode(vd.initializer());
        } else {
            emit(new Instruction(OpCode.PUSH, null, vd.line()));
        }
        emit(new Instruction(OpCode.STORE, vd.name(), vd.line()));
    }

    private void generateAssign(Node.Assign a) {
        if (!a.operator().equals("=")) {
            emit(new Instruction(OpCode.LOAD, a.name(), a.line()));
            generateNode(a.value());
            OpCode op = switch (a.operator()) {
                case "+=" -> OpCode.ADD;
                case "-=" -> OpCode.SUB;
                case "*=" -> OpCode.MUL;
                case "/=" -> OpCode.DIV;
                default -> throw new CompilerException("Unknown operator: " + a.operator(), "CODEGEN", a.line(), 0);
            };
            emit(new Instruction(op, a.line()));
        } else {
            generateNode(a.value());
        }
        emit(new Instruction(OpCode.STORE, a.name(), a.line()));
    }

    private void generateBinaryOp(Node.BinaryOp b) {
        generateNode(b.left());
        generateNode(b.right());
        OpCode op = switch (b.operator()) {
            case "+"        -> OpCode.ADD;
            case "-"        -> OpCode.SUB;
            case "*"        -> OpCode.MUL;
            case "/"        -> OpCode.DIV;
            case "%"        -> OpCode.MOD;
            case "**"       -> OpCode.POW;
            case "=="       -> OpCode.EQ;
            case "!="       -> OpCode.NEQ;
            case "<"        -> OpCode.LT;
            case "<="       -> OpCode.LTE;
            case ">"        -> OpCode.GT;
            case ">="       -> OpCode.GTE;
            case "&&", "and" -> OpCode.AND;
            case "||", "or"  -> OpCode.OR;
            default -> throw new CompilerException("Unknown operator: " + b.operator(), "CODEGEN", b.line(), 0);
        };
        emit(new Instruction(op, b.line()));
    }

    private void generateUnaryOp(Node.UnaryOp u) {
        switch (u.operator()) {
            case "-" -> { generateNode(u.operand()); emit(new Instruction(OpCode.NEG, u.line())); }
            case "!" -> { generateNode(u.operand()); emit(new Instruction(OpCode.NOT, u.line())); }
            case "++pre" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(new Instruction(OpCode.LOAD,  id.name(), u.line()));
                    emit(new Instruction(OpCode.PUSH,  1, u.line()));
                    emit(new Instruction(OpCode.ADD,   u.line()));
                    emit(new Instruction(OpCode.DUP,   u.line()));
                    emit(new Instruction(OpCode.STORE, id.name(), u.line()));
                }
            }
            case "--pre" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(new Instruction(OpCode.LOAD,  id.name(), u.line()));
                    emit(new Instruction(OpCode.PUSH,  1, u.line()));
                    emit(new Instruction(OpCode.SUB,   u.line()));
                    emit(new Instruction(OpCode.DUP,   u.line()));
                    emit(new Instruction(OpCode.STORE, id.name(), u.line()));
                }
            }
            case "++post" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(new Instruction(OpCode.LOAD,  id.name(), u.line()));
                    emit(new Instruction(OpCode.DUP,   u.line()));
                    emit(new Instruction(OpCode.PUSH,  1, u.line()));
                    emit(new Instruction(OpCode.ADD,   u.line()));
                    emit(new Instruction(OpCode.STORE, id.name(), u.line()));
                }
            }
            case "--post" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(new Instruction(OpCode.LOAD,  id.name(), u.line()));
                    emit(new Instruction(OpCode.DUP,   u.line()));
                    emit(new Instruction(OpCode.PUSH,  1, u.line()));
                    emit(new Instruction(OpCode.SUB,   u.line()));
                    emit(new Instruction(OpCode.STORE, id.name(), u.line()));
                }
            }
        }
    }

    private void generateIf(Node.IfStmt is) {
        String elseLabel = newLabel("else");
        String endLabel  = newLabel("endif");
        generateNode(is.condition());
        emit(new Instruction(OpCode.JUMP_IF_FALSE, elseLabel, is.line()));
        generateNode(is.thenBranch());
        emit(new Instruction(OpCode.JUMP, endLabel, is.line()));
        emit(new Instruction(OpCode.LABEL, elseLabel, is.line()));
        if (is.elseBranch() != null) generateNode(is.elseBranch());
        emit(new Instruction(OpCode.LABEL, endLabel, is.line()));
    }

    private void generateWhile(Node.WhileStmt ws) {
        String startLabel = newLabel("while_start");
        String endLabel   = newLabel("while_end");
        emit(new Instruction(OpCode.LABEL, startLabel, ws.line()));
        generateNode(ws.condition());
        emit(new Instruction(OpCode.JUMP_IF_FALSE, endLabel, ws.line()));
        generateNode(ws.body());
        emit(new Instruction(OpCode.JUMP, startLabel, ws.line()));
        emit(new Instruction(OpCode.LABEL, endLabel, ws.line()));
    }

    private void generateFor(Node.ForStmt fs) {
        String startLabel = newLabel("for_start");
        String endLabel   = newLabel("for_end");
        if (fs.init() != null)      generateNode(fs.init());
        emit(new Instruction(OpCode.LABEL, startLabel, fs.line()));
        if (fs.condition() != null) {
            generateNode(fs.condition());
            emit(new Instruction(OpCode.JUMP_IF_FALSE, endLabel, fs.line()));
        }
        generateNode(fs.body());
        if (fs.update() != null)    generateNode(fs.update());
        emit(new Instruction(OpCode.JUMP, startLabel, fs.line()));
        emit(new Instruction(OpCode.LABEL, endLabel, fs.line()));
    }

    private void generateFunctionDecl(Node.FunctionDecl fd) {
        String funcLabel  = "func_" + fd.name();
        String endLabel   = "func_end_" + fd.name();

        // Saltar el cuerpo de la función durante ejecución normal
        emit(new Instruction(OpCode.JUMP, endLabel, fd.line()));

        // Registrar la dirección de la función (instrucción después del JUMP)
        functionTable.put(fd.name(), instructions.size());

        emit(new Instruction(OpCode.LABEL, funcLabel, fd.line()));

        // Sacar parámetros del stack en orden correcto y guardarlos
        List<String> params = new ArrayList<>(fd.params());
        Collections.reverse(params);
        for (String param : params) {
            emit(new Instruction(OpCode.STORE, param, fd.line()));
        }

        // Cuerpo de la función
        generateNode(fd.body());

        // Return por defecto si no hay return explícito
        emit(new Instruction(OpCode.PUSH, null, fd.line()));
        emit(new Instruction(OpCode.RETURN, fd.line()));

        emit(new Instruction(OpCode.LABEL, endLabel, fd.line()));
    }

    private void generateFunctionCall(Node.FunctionCall fc) {
        // Empujar argumentos al stack en orden
        for (Node arg : fc.arguments()) {
            generateNode(arg);
        }
        emit(new Instruction(OpCode.CALL, fc.name(), fc.line()));
    }

    private void generateReturn(Node.ReturnStmt rs) {
        if (rs.value() != null) {
            generateNode(rs.value());
        } else {
            emit(new Instruction(OpCode.PUSH, null, rs.line()));
        }
        emit(new Instruction(OpCode.RETURN, rs.line()));
    }

    private void generatePrint(Node.PrintStmt ps) {
        generateNode(ps.expression());
        emit(new Instruction(OpCode.PRINT, ps.line()));
    }

    private void emit(Instruction instr) {
        instructions.add(instr);
    }

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    public Map<String, Integer> getFunctionTable() {
        return Collections.unmodifiableMap(functionTable);
    }
}