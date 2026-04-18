package com.minicompiler.compiler.vm;

import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.codegen.OpCode;
import com.minicompiler.exception.CompilerException;

import java.util.*;

public class VirtualMachine {

    private static final int MAX_INSTRUCTIONS = 100_000;
    private static final int MAX_STACK_DEPTH  = 1_000;

    private final Deque<Object>            stack;
    private final Deque<Map<String, Object>> callStack;
    private       Map<String, Object>      memory;
    private final List<String>             output;
    private final Map<String, Integer>     labelMap;
    private final Map<String, Integer>     functionTable;

    public VirtualMachine(Map<String, Integer> functionTable) {
        this.stack         = new ArrayDeque<>();
        this.callStack     = new ArrayDeque<>();
        this.memory        = new HashMap<>();
        this.output        = new ArrayList<>();
        this.labelMap      = new HashMap<>();
        this.functionTable = functionTable;
    }

    public ExecutionResult execute(List<Instruction> instructions) {
        long start = System.currentTimeMillis();

        // First pass: build label map
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i).opCode() == OpCode.LABEL) {
                labelMap.put(String.valueOf(instructions.get(i).operand()), i);
            }
        }

        int ip = 0;
        int executed = 0;

        try {
            while (ip < instructions.size()) {
                if (executed++ > MAX_INSTRUCTIONS) {
                    return ExecutionResult.builder()
                            .success(false)
                            .output(output)
                            .error("Execution limit exceeded (possible infinite loop)")
                            .instructionsExecuted(executed)
                            .executionTimeMs(System.currentTimeMillis() - start)
                            .build();
                }

                Instruction instr = instructions.get(ip);
                ip++;

                switch (instr.opCode()) {
                    case NOP, LABEL -> {}

                    case HALT -> {
                        return ExecutionResult.builder()
                                .success(true)
                                .output(output)
                                .finalMemory(new HashMap<>(memory))
                                .instructionsExecuted(executed)
                                .executionTimeMs(System.currentTimeMillis() - start)
                                .build();
                    }

                    case PUSH -> push(instr.operand());

                    case POP -> pop();

                    case DUP -> {
                        Object top = peek();
                        push(top);
                    }

                    case SWAP -> {
                        Object a = pop();
                        Object b = pop();
                        push(a);
                        push(b);
                    }

                    case ADD -> { Object b = pop(); Object a = pop(); push(addValues(a, b)); }
                    case SUB -> { Object b = pop(); Object a = pop(); push(numericOp(a, b, "-")); }
                    case MUL -> { Object b = pop(); Object a = pop(); push(numericOp(a, b, "*")); }
                    case DIV -> {
                        Object b = pop(); Object a = pop();
                        double db = toDouble(b);
                        if (db == 0) throw new CompilerException("Division by zero", "VM", instr.line(), 0);
                        push(numericOp(a, b, "/"));
                    }
                    case MOD -> {
                        Object b = pop(); Object a = pop();
                        if (toDouble(b) == 0) throw new CompilerException("Modulo by zero", "VM", instr.line(), 0);
                        push((int)(toLong(a) % toLong(b)));
                    }
                    case POW -> { Object b = pop(); Object a = pop(); push(Math.pow(toDouble(a), toDouble(b))); }
                    case NEG -> { Object a = pop(); push(negateValue(a)); }

                    case EQ  -> { Object b = pop(); Object a = pop(); push(equalsValues(a, b)); }
                    case NEQ -> { Object b = pop(); Object a = pop(); push(!equalsValues(a, b)); }
                    case LT  -> { Object b = pop(); Object a = pop(); push(toDouble(a) < toDouble(b)); }
                    case LTE -> { Object b = pop(); Object a = pop(); push(toDouble(a) <= toDouble(b)); }
                    case GT  -> { Object b = pop(); Object a = pop(); push(toDouble(a) > toDouble(b)); }
                    case GTE -> { Object b = pop(); Object a = pop(); push(toDouble(a) >= toDouble(b)); }

                    case AND -> { Object b = pop(); Object a = pop(); push(toBool(a) && toBool(b)); }
                    case OR  -> { Object b = pop(); Object a = pop(); push(toBool(a) || toBool(b)); }
                    case NOT -> { Object a = pop(); push(!toBool(a)); }

                    case LOAD -> {
                        String name = (String) instr.operand();
                        if (!memory.containsKey(name)) {
                            throw new CompilerException("Undefined variable: " + name, "VM", instr.line(), 0);
                        }
                        push(memory.get(name));
                    }

                    case STORE -> {
                        String name = (String) instr.operand();
                        Object val = pop();
                        memory.put(name, val);
                    }

                    case LOAD_CONST -> push(instr.operand());

                    case JUMP -> {
                        String label = (String) instr.operand();
                        ip = resolveLabel(label, instr.line());
                    }

                    case JUMP_IF_FALSE -> {
                        Object cond = pop();
                        if (!toBool(cond)) {
                            ip = resolveLabel((String) instr.operand(), instr.line());
                        }
                    }

                    case JUMP_IF_TRUE -> {
                        Object cond = pop();
                        if (toBool(cond)) {
                            ip = resolveLabel((String) instr.operand(), instr.line());
                        }
                    }

                    case DEFINE_FUNC -> {} // marker only

                    case CALL -> {
                        String funcName = (String) instr.operand();
                        Integer funcAddr = functionTable.get(funcName);
                        if (funcAddr == null) {
                            throw new CompilerException("Undefined function: " + funcName, "VM", instr.line(), 0);
                        }
                        // Save return address and memory snapshot
                        callStack.push(new HashMap<>(memory));
                        push(ip); // return address
                        ip = funcAddr;
                    }

                    case RETURN -> {
                        Object returnValue = pop();
                        ip = (int) pop(); // return address
                        memory = callStack.pop();
                        push(returnValue);
                    }

                    case PRINT -> {
                        Object val = pop();
                        output.add(formatValue(val));
                    }

                    case INPUT -> {
                        // In server mode, push a placeholder
                        String varName = (String) instr.operand();
                        memory.put(varName, "<<input>>");
                    }

                    default -> throw new CompilerException("Unknown opcode: " + instr.opCode(), "VM", instr.line(), 0);
                }

                if (stack.size() > MAX_STACK_DEPTH) {
                    throw new CompilerException("Stack overflow", "VM", instr.line(), 0);
                }
            }

        } catch (CompilerException e) {
            return ExecutionResult.builder()
                    .success(false)
                    .output(output)
                    .error("[" + e.getPhase() + "] L" + e.getLine() + ": " + e.getMessage())
                    .instructionsExecuted(executed)
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build();
        }

        return ExecutionResult.builder()
                .success(true)
                .output(output)
                .finalMemory(new HashMap<>(memory))
                .instructionsExecuted(executed)
                .executionTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void push(Object value) { stack.push(value); }

    private Object pop() {
        if (stack.isEmpty()) throw new CompilerException("Stack underflow", "VM", 0, 0);
        return stack.pop();
    }

    private Object peek() {
        if (stack.isEmpty()) throw new CompilerException("Stack underflow (peek)", "VM", 0, 0);
        return stack.peek();
    }

    private int resolveLabel(String label, int line) {
        Integer addr = labelMap.get(label);
        if (addr == null) throw new CompilerException("Undefined label: " + label, "VM", line, 0);
        return addr + 1; // jump past the LABEL instruction itself
    }

    private Object addValues(Object a, Object b) {
        if (a instanceof String || b instanceof String) {
            return formatValue(a) + formatValue(b);
        }
        return numericOp(a, b, "+");
    }

    private Object numericOp(Object a, Object b, String op) {
        boolean isInt = (a instanceof Integer) && (b instanceof Integer);
        double da = toDouble(a), db = toDouble(b);
        double result = switch (op) {
            case "+" -> da + db;
            case "-" -> da - db;
            case "*" -> da * db;
            case "/" -> da / db;
            default  -> throw new IllegalArgumentException("Unknown op: " + op);
        };
        return isInt ? (int) result : result;
    }

    private Object negateValue(Object a) {
        if (a instanceof Integer i) return -i;
        if (a instanceof Double d)  return -d;
        throw new CompilerException("Cannot negate non-numeric value: " + a, "VM", 0, 0);
    }

    private boolean equalsValues(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a instanceof Number na && b instanceof Number nb) {
            return Double.compare(na.doubleValue(), nb.doubleValue()) == 0;
        }
        return a.equals(b);
    }

    private double toDouble(Object v) {
        if (v instanceof Integer i) return i.doubleValue();
        if (v instanceof Double d)  return d;
        if (v instanceof Float f)   return f.doubleValue();
        if (v instanceof Long l)    return l.doubleValue();
        if (v instanceof Boolean b) return b ? 1.0 : 0.0;
        throw new CompilerException("Cannot convert to number: " + v, "VM", 0, 0);
    }

    private long toLong(Object v) {
        return (long) toDouble(v);
    }

    private boolean toBool(Object v) {
        if (v == null)          return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Integer i) return i != 0;
        if (v instanceof Double d)  return d != 0.0;
        if (v instanceof String s)  return !s.isEmpty();
        return true;
    }

    private String formatValue(Object v) {
        if (v == null)         return "null";
        if (v instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf(d.intValue());
            return String.valueOf(d);
        }
        return String.valueOf(v);
    }
}