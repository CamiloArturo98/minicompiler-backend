package com.minicompiler.compiler.codegen;

import com.minicompiler.compiler.ast.Node;
import com.minicompiler.exception.CompilerException;

import java.util.*;

/**
 * Generates a flat list of {@link Instruction} objects from a parsed AST using
 * a two-pass strategy: first registers functions, then emits the main program body.
 *
 * @see Instruction
 * @see OpCode
 */
public class CodeGenerator {

    // =========================================================================
    // Constants — label prefixes
    // =========================================================================

    private static final String LABEL_ELSE        = "else";
    private static final String LABEL_ENDIF       = "endif";
    private static final String LABEL_WHILE_START = "while_start";
    private static final String LABEL_WHILE_END   = "while_end";
    private static final String LABEL_FOR_START   = "for_start";
    private static final String LABEL_FOR_END     = "for_end";
    private static final String LABEL_FUNC_PREFIX = "func_";
    private static final String LABEL_FUNC_END    = "func_end_";

    // =========================================================================
    // Strategy Pattern — operator → OpCode lookup tables
    // =========================================================================

    private static final Map<String, OpCode> BINARY_OP_MAP = Map.ofEntries(
            Map.entry("+",   OpCode.ADD),
            Map.entry("-",   OpCode.SUB),
            Map.entry("*",   OpCode.MUL),
            Map.entry("/",   OpCode.DIV),
            Map.entry("%",   OpCode.MOD),
            Map.entry("**",  OpCode.POW),
            Map.entry("==",  OpCode.EQ),
            Map.entry("!=",  OpCode.NEQ),
            Map.entry("<",   OpCode.LT),
            Map.entry("<=",  OpCode.LTE),
            Map.entry(">",   OpCode.GT),
            Map.entry(">=",  OpCode.GTE),
            Map.entry("&&",  OpCode.AND),
            Map.entry("and", OpCode.AND),
            Map.entry("||",  OpCode.OR),
            Map.entry("or",  OpCode.OR)
    );

    private static final Map<String, OpCode> ASSIGN_OP_MAP = Map.of(
            "+=", OpCode.ADD,
            "-=", OpCode.SUB,
            "*=", OpCode.MUL,
            "/=", OpCode.DIV
    );

    // =========================================================================
    // Fields
    // =========================================================================

    private final List<Instruction>    instructions;
    private final Map<String, Integer> functionTable;
    private       int                  labelCounter;

    // =========================================================================
    // Constructor
    // =========================================================================

    /** Creates a new {@code CodeGenerator} with an empty instruction list and function table. */
    public CodeGenerator() {
        this.instructions  = new ArrayList<>();
        this.functionTable = new HashMap<>();
        this.labelCounter  = 0;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Compiles the given program AST into a flat instruction list.
     *
     * @param  program the root AST node to compile
     * @return an unmodifiable list of generated instructions
     */
    public List<Instruction> generate(Node.Program program) {
        registerFunctions(program);
        emitPrologue();
        emitMainStatements(program);
        emitEpilogue();
        return Collections.unmodifiableList(instructions);
    }

    /**
     * Returns an unmodifiable view of the function-name → instruction-index table.
     *
     * @return map from function name to its entry-point instruction index
     */
    public Map<String, Integer> getFunctionTable() {
        return Collections.unmodifiableMap(functionTable);
    }

    // =========================================================================
    // Template Method steps
    // =========================================================================

    private void registerFunctions(Node.Program program) {
        for (Node stmt : program.statements()) {
            if (stmt instanceof Node.FunctionDecl fd) {
                generateFunctionDecl(fd);
            }
        }
    }

    private void emitPrologue() {
        emit(OpCode.NOP, 0);
    }

    private void emitMainStatements(Node.Program program) {
        for (Node stmt : program.statements()) {
            if (!(stmt instanceof Node.FunctionDecl)) {
                generateNode(stmt);
            }
        }
    }

    private void emitEpilogue() {
        emit(OpCode.HALT, 0);
    }

    // =========================================================================
    // Core dispatch
    // =========================================================================

    private void generateNode(Node node) {
        switch (node) {
            case Node.Program p        -> p.statements().forEach(this::generateNode);
            case Node.VarDecl vd       -> generateVarDecl(vd);
            case Node.Assign a         -> generateAssign(a);
            case Node.BinaryOp b       -> generateBinaryOp(b);
            case Node.UnaryOp u        -> generateUnaryOp(u);
            case Node.Literal l        -> emit(OpCode.PUSH,  l.value(),     l.line());
            case Node.Identifier id    -> emit(OpCode.LOAD,  id.name(),     id.line());
            case Node.IfStmt is        -> generateIf(is);
            case Node.WhileStmt ws     -> generateWhile(ws);
            case Node.ForStmt fs       -> generateFor(fs);
            case Node.Block b          -> b.statements().forEach(this::generateNode);
            case Node.FunctionDecl fd  -> {}
            case Node.FunctionCall fc  -> generateFunctionCall(fc);
            case Node.ReturnStmt rs    -> generateReturn(rs);
            case Node.PrintStmt ps     -> generatePrint(ps);
            case Node.InputStmt is     -> emit(OpCode.INPUT, is.variable(), is.line());
        }
    }

    // =========================================================================
    // Statement generators
    // =========================================================================

    private void generateVarDecl(Node.VarDecl vd) {
        if (vd.initializer() != null) {
            generateNode(vd.initializer());
        } else {
            emit(OpCode.PUSH, null, vd.line());
        }
        emit(OpCode.STORE, vd.name(), vd.line());
    }

    private void generateAssign(Node.Assign a) {
        if (!a.operator().equals("=")) {
            emit(OpCode.LOAD, a.name(), a.line());
            generateNode(a.value());
            OpCode op = resolveOperator(ASSIGN_OP_MAP, a.operator(), "CODEGEN", a.line());
            emit(op, a.line());
        } else {
            generateNode(a.value());
        }
        emit(OpCode.STORE, a.name(), a.line());
    }

    private void generateBinaryOp(Node.BinaryOp b) {
        generateNode(b.left());
        generateNode(b.right());
        OpCode op = resolveOperator(BINARY_OP_MAP, b.operator(), "CODEGEN", b.line());
        emit(op, b.line());
    }

    private void generateUnaryOp(Node.UnaryOp u) {
        switch (u.operator()) {
            case "-" -> {
                generateNode(u.operand());
                emit(OpCode.NEG, u.line());
            }
            case "!" -> {
                generateNode(u.operand());
                emit(OpCode.NOT, u.line());
            }
            case "++pre" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(OpCode.LOAD,  id.name(), u.line());
                    emit(OpCode.PUSH,  1,         u.line());
                    emit(OpCode.ADD,              u.line());
                    emit(OpCode.DUP,              u.line());
                    emit(OpCode.STORE, id.name(), u.line());
                }
            }
            case "--pre" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(OpCode.LOAD,  id.name(), u.line());
                    emit(OpCode.PUSH,  1,         u.line());
                    emit(OpCode.SUB,              u.line());
                    emit(OpCode.DUP,              u.line());
                    emit(OpCode.STORE, id.name(), u.line());
                }
            }
            case "++post" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(OpCode.LOAD,  id.name(), u.line());
                    emit(OpCode.DUP,              u.line());
                    emit(OpCode.PUSH,  1,         u.line());
                    emit(OpCode.ADD,              u.line());
                    emit(OpCode.STORE, id.name(), u.line());
                }
            }
            case "--post" -> {
                if (u.operand() instanceof Node.Identifier id) {
                    emit(OpCode.LOAD,  id.name(), u.line());
                    emit(OpCode.DUP,              u.line());
                    emit(OpCode.PUSH,  1,         u.line());
                    emit(OpCode.SUB,              u.line());
                    emit(OpCode.STORE, id.name(), u.line());
                }
            }
        }
    }

    private void generateIf(Node.IfStmt is) {
        var elseLabel = newLabel(LABEL_ELSE);
        var endLabel  = newLabel(LABEL_ENDIF);

        generateNode(is.condition());
        emit(OpCode.JUMP_IF_FALSE, elseLabel, is.line());
        generateNode(is.thenBranch());
        emit(OpCode.JUMP,          endLabel,  is.line());
        emit(OpCode.LABEL,         elseLabel, is.line());
        if (is.elseBranch() != null) {
            generateNode(is.elseBranch());
        }
        emit(OpCode.LABEL, endLabel, is.line());
    }

    private void generateWhile(Node.WhileStmt ws) {
        var startLabel = newLabel(LABEL_WHILE_START);
        var endLabel   = newLabel(LABEL_WHILE_END);

        emit(OpCode.LABEL,         startLabel, ws.line());
        generateNode(ws.condition());
        emit(OpCode.JUMP_IF_FALSE, endLabel,   ws.line());
        generateNode(ws.body());
        emit(OpCode.JUMP,          startLabel, ws.line());
        emit(OpCode.LABEL,         endLabel,   ws.line());
    }

    private void generateFor(Node.ForStmt fs) {
        var startLabel = newLabel(LABEL_FOR_START);
        var endLabel   = newLabel(LABEL_FOR_END);

        if (fs.init() != null) {
            generateNode(fs.init());
        }
        emit(OpCode.LABEL, startLabel, fs.line());
        if (fs.condition() != null) {
            generateNode(fs.condition());
            emit(OpCode.JUMP_IF_FALSE, endLabel, fs.line());
        }
        generateNode(fs.body());
        if (fs.update() != null) {
            generateNode(fs.update());
        }
        emit(OpCode.JUMP,  startLabel, fs.line());
        emit(OpCode.LABEL, endLabel,   fs.line());
    }

    private void generateFunctionDecl(Node.FunctionDecl fd) {
        var funcLabel = LABEL_FUNC_PREFIX + fd.name();
        var endLabel  = LABEL_FUNC_END    + fd.name();

        emit(OpCode.JUMP, endLabel, fd.line());

        functionTable.put(fd.name(), instructions.size());
        emit(OpCode.LABEL, funcLabel, fd.line());

        var params = new ArrayList<>(fd.params());
        Collections.reverse(params);
        for (String param : params) {
            emit(OpCode.STORE, param, fd.line());
        }

        generateNode(fd.body());

        emit(OpCode.PUSH,   null,     fd.line());
        emit(OpCode.RETURN,           fd.line());
        emit(OpCode.LABEL,  endLabel, fd.line());
    }

    private void generateFunctionCall(Node.FunctionCall fc) {
        for (Node arg : fc.arguments()) {
            generateNode(arg);
        }
        emit(OpCode.CALL, fc.name(), fc.line());
    }

    private void generateReturn(Node.ReturnStmt rs) {
        if (rs.value() != null) {
            generateNode(rs.value());
        } else {
            emit(OpCode.PUSH, null, rs.line());
        }
        emit(OpCode.RETURN, rs.line());
    }

    private void generatePrint(Node.PrintStmt ps) {
        generateNode(ps.expression());
        emit(OpCode.PRINT, ps.line());
    }

    // =========================================================================
    // Factory Method — centralized Instruction creation
    // =========================================================================

    /** Emits an instruction with an operand and a source line number. */
    private void emit(OpCode opCode, Object operand, int line) {
        instructions.add(new Instruction(opCode, operand, line));
    }

    /** Emits an instruction with only a source line number, sin operand. */
    private void emit(OpCode opCode, int line) {
        instructions.add(new Instruction(opCode, line));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * @throws CompilerException if operator does not exist in the map
     */
    private OpCode resolveOperator(Map<String, OpCode> map, String operator,
                                   String phase, int line) {
        OpCode op = map.get(operator);
        if (op == null) {
            throw new CompilerException("Unknown operator: " + operator, phase, line, 0);
        }
        return op;
    }

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }
}