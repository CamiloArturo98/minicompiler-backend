package com.minicompiler.compiler.optimizer;

import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.codegen.OpCode;

import java.util.*;

public class Optimizer {

    /**
     * Applies constant folding and dead code elimination.
     */
    public List<Instruction> optimize(List<Instruction> instructions) {
        List<Instruction> result = new ArrayList<>(instructions);
        result = constantFolding(result);
        result = deadCodeElimination(result);
        result = removeNops(result);
        return Collections.unmodifiableList(result);
    }

    // ─── Pass 1: Constant Folding ─────────────────────────────────────────────

    private List<Instruction> constantFolding(List<Instruction> instructions) {
        List<Instruction> result = new ArrayList<>();
        int i = 0;
        while (i < instructions.size()) {
            Instruction curr = instructions.get(i);

            if (curr.opCode() == OpCode.PUSH && i + 2 < instructions.size()) {
                Instruction second = instructions.get(i + 1);
                Instruction op = instructions.get(i + 2);

                if (second.opCode() == OpCode.PUSH && isArithmeticOrComparison(op.opCode())) {
                    Object folded = fold(curr.operand(), second.operand(), op.opCode());
                    if (folded != null) {
                        result.add(new Instruction(OpCode.PUSH, folded, curr.line()));
                        i += 3;
                        continue;
                    }
                }
            }
            result.add(curr);
            i++;
        }
        return result;
    }

    private boolean isArithmeticOrComparison(OpCode op) {
        return switch (op) {
            case ADD, SUB, MUL, DIV, MOD, EQ, NEQ, LT, LTE, GT, GTE -> true;
            default -> false;
        };
    }

    private Object fold(Object a, Object b, OpCode op) {
        if (a == null || b == null) return null;
        try {
            double da = toDouble(a);
            double db = toDouble(b);
            return switch (op) {
                case ADD -> isInt(a) && isInt(b) ? (int)(da + db) : (da + db);
                case SUB -> isInt(a) && isInt(b) ? (int)(da - db) : (da - db);
                case MUL -> isInt(a) && isInt(b) ? (int)(da * db) : (da * db);
                case DIV -> db == 0 ? null : (isInt(a) && isInt(b) ? (int)(da / db) : da / db);
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
        if (v instanceof Double d)  return d;
        if (v instanceof Float f)   return f.doubleValue();
        throw new IllegalArgumentException("Cannot convert to double: " + v);
    }

    private boolean isInt(Object v) { return v instanceof Integer; }

    // ─── Pass 2: Dead Code Elimination ───────────────────────────────────────

    private List<Instruction> deadCodeElimination(List<Instruction> instructions) {
        // Collect all labels that are jumped to
        Set<String> usedLabels = new HashSet<>();
        for (Instruction instr : instructions) {
            if (instr.opCode() == OpCode.JUMP
                    || instr.opCode() == OpCode.JUMP_IF_FALSE
                    || instr.opCode() == OpCode.JUMP_IF_TRUE) {
                usedLabels.add(String.valueOf(instr.operand()));
            }
        }

        List<Instruction> result = new ArrayList<>();
        boolean dead = false;
        for (Instruction instr : instructions) {
            if (instr.opCode() == OpCode.LABEL) {
                dead = false; // revive on label
                if (usedLabels.contains(String.valueOf(instr.operand()))) {
                    result.add(instr);
                }
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

    // ─── Pass 3: Remove NOPs ─────────────────────────────────────────────────

    private List<Instruction> removeNops(List<Instruction> instructions) {
        return instructions.stream()
                .filter(i -> i.opCode() != OpCode.NOP)
                .toList();
    }
}