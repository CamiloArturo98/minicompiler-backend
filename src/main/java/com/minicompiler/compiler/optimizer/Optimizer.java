package com.minicompiler.compiler.optimizer;

import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.codegen.OpCode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Applies a multi-pass optimization pipeline over a flat instruction list.
 *
 * <p><b>Template Method</b> — {@link #optimize} defines the fixed order of the three
 * passes; each one is an independent and replaceable private method.
 *
 * <p><b>Strategy</b> — {@link #FOLDABLE_OPS} replaces the switch of
 * {@code isArithmeticOrComparison} with an O(1) lookup on an immutable {@link Set}.
 */
public class Optimizer {

    // =========================================================================
    // Strategy — opcodes eligible for constant folding
    // =========================================================================

    private static final Set<OpCode> FOLDABLE_OPS = Set.of(
            OpCode.ADD, OpCode.SUB, OpCode.MUL, OpCode.DIV, OpCode.MOD,
            OpCode.EQ,  OpCode.NEQ, OpCode.LT,  OpCode.LTE, OpCode.GT, OpCode.GTE
    );

    // =========================================================================
    // Constants
    // =========================================================================

    private static final int FOLD_WINDOW = 3;

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Runs constant folding, dead code elimination, and NOP removal over
     * the given instruction list.
     *
     * @param  instructions the instruction list to optimize
     * @return an unmodifiable optimized instruction list
     */
    public List<Instruction> optimize(List<Instruction> instructions) {
        var result = new ArrayList<>(instructions);
        result = constantFolding(result);
        result = deadCodeElimination(result);
        result = removeNops(result);
        return Collections.unmodifiableList(result);
    }

    // =========================================================================
    // Template Method steps
    // =========================================================================

    /**
     * Pass 1 — folds consecutive {@code PUSH, PUSH, <op>} triplets into a
     * single {@code PUSH <constant>} when both operands are numeric literals.
     */
    private ArrayList<Instruction> constantFolding(List<Instruction> instructions) {
        var result = new ArrayList<Instruction>();
        int i = 0;
        while (i < instructions.size()) {
            var curr = instructions.get(i);

            if (curr.opCode() == OpCode.PUSH && i + 2 < instructions.size()) {
                var second = instructions.get(i + 1);
                var op     = instructions.get(i + 2);

                if (second.opCode() == OpCode.PUSH && FOLDABLE_OPS.contains(op.opCode())) {
                    Object folded = fold(curr.operand(), second.operand(), op.opCode());
                    if (folded != null) {
                        result.add(new Instruction(OpCode.PUSH, folded, curr.line()));
                        i += FOLD_WINDOW;
                        continue;
                    }
                }
            }
            result.add(curr);
            i++;
        }
        return result;
    }

    /**
     * Pass 2 — removes instructions that follow an unconditional {@code JUMP}
     * or {@code HALT} and are not reachable via any label reference.
     *
     * <p>All {@link OpCode#LABEL} instructions are always preserved to maintain
     * the integrity of the {@code functionTable} indices used by {@code CALL}.
     */
    private ArrayList<Instruction> deadCodeElimination(List<Instruction> instructions) {
        Set<String> usedLabels = collectUsedLabels(instructions);

        var result = new ArrayList<Instruction>();
        boolean dead = false;

        for (var instr : instructions) {
            if (instr.opCode() == OpCode.LABEL) {
                dead = false;
                // Always preserve LABEL instructions — function entry-point labels
                // are referenced via functionTable (not jump targets) and must not
                // be removed, as doing so shifts instruction indices and corrupts CALL.
                result.add(instr);
                continue;
            }
            if (dead) continue;
            result.add(instr);
            if (instr.opCode() == OpCode.JUMP || instr.opCode() == OpCode.HALT) {
                dead = true;
            }
        }
        return result;
    }

    /**
     * Pass 3 — strips all {@link OpCode#NOP} instructions from the list.
     */
    private ArrayList<Instruction> removeNops(List<Instruction> instructions) {
        return instructions.stream()
                .filter(i -> i.opCode() != OpCode.NOP)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Collects every label string that is the target of a jump instruction,
     * used by {@link #deadCodeElimination} to decide which code sections are live.
     */
    private Set<String> collectUsedLabels(List<Instruction> instructions) {
        var used = new HashSet<String>();
        for (var instr : instructions) {
            if (instr.opCode() == OpCode.JUMP
                    || instr.opCode() == OpCode.JUMP_IF_FALSE
                    || instr.opCode() == OpCode.JUMP_IF_TRUE) {
                used.add(String.valueOf(instr.operand()));
            }
        }
        return used;
    }

    /**
     * Attempts to evaluate a binary operation over two constant operands.
     *
     * @return the folded value, or {@code null} if folding is not possible
     */
    private Object fold(Object a, Object b, OpCode op) {
        if (a == null || b == null) return null;
        try {
            double da = toDouble(a);
            double db = toDouble(b);
            boolean bothInt = isInt(a) && isInt(b);
            return switch (op) {
                case ADD -> bothInt ? (int)(da + db) : (da + db);
                case SUB -> bothInt ? (int)(da - db) : (da - db);
                case MUL -> bothInt ? (int)(da * db) : (da * db);
                case DIV -> db == 0 ? null : (bothInt ? (int)(da / db) : da / db);
                case MOD -> db == 0 ? null : (int)(da % db);
                case EQ  -> da == db;
                case NEQ -> da != db;
                case LT  -> da < db;
                case LTE -> da <= db;
                case GT  -> da > db;
                case GTE -> da >= db;
                default  -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private double toDouble(Object v) {
        if (v instanceof Integer i) return i.doubleValue();
        if (v instanceof Double  d) return d;
        if (v instanceof Float   f) return f.doubleValue();
        throw new IllegalArgumentException("Cannot convert to double: " + v);
    }

    private boolean isInt(Object v) {
        return v instanceof Integer;
    }
}