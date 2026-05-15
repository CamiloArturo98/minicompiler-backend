package com.minicompiler.compiler.vm;

import com.minicompiler.compiler.codegen.Instruction;
import com.minicompiler.compiler.codegen.OpCode;
import com.minicompiler.exception.CompilerException;

import java.util.*;

/**
 * Stack-based virtual machine that executes a flat {@link Instruction} list.
 *
 * <p><b>Template Method</b> — {@link #execute} follows a fixed pipeline:
 * label indexing → dispatch loop → result construction.
 *
 * <p><b>Factory Method</b> — {@link #buildSuccess} and {@link #buildError}
 * are the single construction points for {@link ExecutionResult}.
 */
public class VirtualMachine {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final int    MAX_INSTRUCTIONS       = 100_000;
    private static final int    MAX_STACK_DEPTH        = 1_000;
    private static final String VM_PHASE               = "VM";
    private static final String FUNC_LABEL_PREFIX      = "func_";
    private static final String FUNC_END_LABEL_PREFIX  = "func_end_";
    private static final String ERR_DIVISION_ZERO      = "Division by zero";
    private static final String ERR_MODULO_ZERO        = "Modulo by zero";
    private static final String ERR_STACK_OVERFLOW     = "Stack overflow";
    private static final String ERR_STACK_UNDERFLOW    = "Stack underflow";
    private static final String ERR_EXEC_LIMIT         = "Execution limit exceeded (possible infinite loop)";
    private static final String ERR_UNDEFINED_VAR      = "Undefined variable: ";
    private static final String ERR_UNDEFINED_FUNC     = "Undefined function: ";
    private static final String ERR_UNDEFINED_LABEL    = "Undefined label: ";
    private static final String ERR_UNKNOWN_OPCODE     = "Unknown opcode: ";
    private static final String ERR_CANNOT_NEGATE      = "Cannot negate: ";
    private static final String ERR_CANNOT_CONVERT     = "Cannot convert to number: ";
    private static final String ERROR_FORMAT           = "[%s] L%d: %s";

    // =========================================================================
    // Fields
    // =========================================================================

    private final Deque<Object>        stack;
    private final Deque<Frame>         callStack;
    private final List<String>         output;
    private final Map<String, Integer> labelMap;
    private final Map<String, Integer> functionTable;
    private       Map<String, Object>  memory;

    /** Runtime-resolved function addresses built from the optimized instruction list. */
    private final Map<String, Integer> runtimeFunctionAddresses;

    private record Frame(Map<String, Object> memory, int returnAddress) {}

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * @param functionTable function-name → entry-point index map built by the code generator
     */
    public VirtualMachine(Map<String, Integer> functionTable) {
        this.stack                   = new ArrayDeque<>();
        this.callStack               = new ArrayDeque<>();
        this.memory                  = new HashMap<>();
        this.output                  = new ArrayList<>();
        this.labelMap                = new HashMap<>();
        this.functionTable           = functionTable;
        this.runtimeFunctionAddresses = new HashMap<>();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Executes the given instruction list and returns a result regardless of
     * whether execution succeeds or fails.
     *
     * @param  instructions the optimized instruction list to run
     * @return an {@link ExecutionResult} capturing output, memory, timing, and any error
     */
    public ExecutionResult execute(List<Instruction> instructions) {
        long start = System.currentTimeMillis();
        buildLabelMap(instructions);

        int ip       = 0;
        int executed = 0;

        try {
            while (ip < instructions.size()) {
                if (executed++ > MAX_INSTRUCTIONS) {
                    return buildError(ERR_EXEC_LIMIT, executed, start);
                }

                var instr = instructions.get(ip);
                ip++;

                switch (instr.opCode()) {
                    case NOP, LABEL -> {}

                    case HALT -> { return buildSuccess(executed, start); }

                    case PUSH -> push(instr.operand());
                    case POP  -> pop();
                    case DUP  -> push(peek());
                    case SWAP -> { var a = pop(); var b = pop(); push(a); push(b); }

                    case ADD -> { var b = pop(); var a = pop(); push(addValues(a, b)); }
                    case SUB -> { var b = pop(); var a = pop(); push(numericOp(a, b, "-")); }
                    case MUL -> { var b = pop(); var a = pop(); push(numericOp(a, b, "*")); }
                    case DIV -> {
                        var b = pop(); var a = pop();
                        if (toDouble(b) == 0) throw new CompilerException(ERR_DIVISION_ZERO, VM_PHASE, instr.line(), 0);
                        push(numericOp(a, b, "/"));
                    }
                    case MOD -> {
                        var b = pop(); var a = pop();
                        if (toDouble(b) == 0) throw new CompilerException(ERR_MODULO_ZERO, VM_PHASE, instr.line(), 0);
                        push((int)(toLong(a) % toLong(b)));
                    }
                    case POW -> { var b = pop(); var a = pop(); push(Math.pow(toDouble(a), toDouble(b))); }
                    case NEG -> push(negateValue(pop()));

                    case EQ  -> { var b = pop(); var a = pop(); push(equalsValues(a, b)); }
                    case NEQ -> { var b = pop(); var a = pop(); push(!equalsValues(a, b)); }
                    case LT  -> { var b = pop(); var a = pop(); push(toDouble(a) <  toDouble(b)); }
                    case LTE -> { var b = pop(); var a = pop(); push(toDouble(a) <= toDouble(b)); }
                    case GT  -> { var b = pop(); var a = pop(); push(toDouble(a) >  toDouble(b)); }
                    case GTE -> { var b = pop(); var a = pop(); push(toDouble(a) >= toDouble(b)); }

                    case AND -> { var b = pop(); var a = pop(); push(toBool(a) && toBool(b)); }
                    case OR  -> { var b = pop(); var a = pop(); push(toBool(a) || toBool(b)); }
                    case NOT -> push(!toBool(pop()));

                    case LOAD -> {
                        var name = (String) instr.operand();
                        if (!memory.containsKey(name)) {
                            throw new CompilerException(ERR_UNDEFINED_VAR + name, VM_PHASE, instr.line(), 0);
                        }
                        push(memory.get(name));
                    }

                    case STORE      -> memory.put((String) instr.operand(), pop());
                    case LOAD_CONST -> push(instr.operand());

                    case JUMP          -> ip = resolveLabel((String) instr.operand(), instr.line());
                    case JUMP_IF_FALSE -> { if (!toBool(pop())) ip = resolveLabel((String) instr.operand(), instr.line()); }
                    case JUMP_IF_TRUE  -> { if  (toBool(pop())) ip = resolveLabel((String) instr.operand(), instr.line()); }

                    case DEFINE_FUNC -> {}

                    case CALL -> {
                        var funcName = (String) instr.operand();
                        // Use runtime-resolved address (post-optimization) from labelMap.
                        // Falls back to pre-optimization functionTable if label not found.
                        Integer funcAddr = runtimeFunctionAddresses.get(funcName);
                        if (funcAddr == null) funcAddr = functionTable.get(funcName);
                        if (funcAddr == null) {
                            throw new CompilerException(ERR_UNDEFINED_FUNC + funcName, VM_PHASE, instr.line(), 0);
                        }
                        callStack.push(new Frame(new HashMap<>(memory), ip));
                        memory = new HashMap<>(memory);
                        ip = funcAddr;
                    }

                    case RETURN -> {
                        var returnValue = pop();
                        var frame       = callStack.pop();
                        memory = frame.memory();
                        ip     = frame.returnAddress();
                        push(returnValue);
                    }

                    case PRINT -> output.add(formatValue(pop()));
                    case INPUT -> memory.put((String) instr.operand(), "<<input>>");

                    default -> throw new CompilerException(ERR_UNKNOWN_OPCODE + instr.opCode(), VM_PHASE, instr.line(), 0);
                }

                if (stack.size() > MAX_STACK_DEPTH) {
                    throw new CompilerException(ERR_STACK_OVERFLOW, VM_PHASE, instr.line(), 0);
                }
            }

        } catch (CompilerException e) {
            return buildError(ERROR_FORMAT.formatted(e.getPhase(), e.getLine(), e.getMessage()), executed, start);
        }

        return buildSuccess(executed, start);
    }

    // =========================================================================
    // Template Method steps
    // =========================================================================

    /**
     * Indexes every {@link OpCode#LABEL} instruction and rebuilds function entry
     * addresses from the actual (post-optimization) instruction positions.
     * This ensures {@code CALL} always resolves to the correct address regardless
     * of how the optimizer has modified the instruction list.
     */
    private void buildLabelMap(List<Instruction> instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            var instr = instructions.get(i);
            if (instr.opCode() == OpCode.LABEL) {
                var label = String.valueOf(instr.operand());
                labelMap.put(label, i);
                // Rebuild function entry addresses from runtime positions.
                // Function entry labels match "func_<name>" (not "func_end_<name>").
                if (label.startsWith(FUNC_LABEL_PREFIX) && !label.startsWith(FUNC_END_LABEL_PREFIX)) {
                    var funcName = label.substring(FUNC_LABEL_PREFIX.length());
                    runtimeFunctionAddresses.put(funcName, i);
                }
            }
        }
        // Ensure every function in the table has a resolved address
        functionTable.forEach(runtimeFunctionAddresses::putIfAbsent);
    }

    // =========================================================================
    // Factory Method — ExecutionResult construction
    // =========================================================================

    private ExecutionResult buildSuccess(int executed, long start) {
        return ExecutionResult.builder()
                .success(true)
                .output(output)
                .finalMemory(new HashMap<>(memory))
                .instructionsExecuted(executed)
                .executionTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    private ExecutionResult buildError(String error, int executed, long start) {
        return ExecutionResult.builder()
                .success(false)
                .output(output)
                .error(error)
                .instructionsExecuted(executed)
                .executionTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    // =========================================================================
    // Stack helpers
    // =========================================================================

    private void push(Object value) { stack.push(value); }

    private Object pop() {
        if (stack.isEmpty()) throw new CompilerException(ERR_STACK_UNDERFLOW, VM_PHASE, 0, 0);
        return stack.pop();
    }

    private Object peek() {
        if (stack.isEmpty()) throw new CompilerException(ERR_STACK_UNDERFLOW + " (peek)", VM_PHASE, 0, 0);
        return stack.peek();
    }

    private int resolveLabel(String label, int line) {
        var addr = labelMap.get(label);
        if (addr == null) throw new CompilerException(ERR_UNDEFINED_LABEL + label, VM_PHASE, line, 0);
        return addr + 1;
    }

    // =========================================================================
    // Value helpers
    // =========================================================================

    private Object addValues(Object a, Object b) {
        if (a instanceof String || b instanceof String) return formatValue(a) + formatValue(b);
        return numericOp(a, b, "+");
    }

    private Object numericOp(Object a, Object b, String op) {
        boolean isInt  = (a instanceof Integer) && (b instanceof Integer);
        double  da     = toDouble(a);
        double  db     = toDouble(b);
        double  result = switch (op) {
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
        if (a instanceof Double  d) return -d;
        throw new CompilerException(ERR_CANNOT_NEGATE + a, VM_PHASE, 0, 0);
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
        if (v instanceof Double  d) return d;
        if (v instanceof Float   f) return f.doubleValue();
        if (v instanceof Long    l) return l.doubleValue();
        if (v instanceof Boolean b) return b ? 1.0 : 0.0;
        throw new CompilerException(ERR_CANNOT_CONVERT + v, VM_PHASE, 0, 0);
    }

    private long toLong(Object v) { return (long) toDouble(v); }

    private boolean toBool(Object v) {
        if (v == null)              return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Integer i) return i != 0;
        if (v instanceof Double  d) return d != 0.0;
        if (v instanceof String  s) return !s.isEmpty();
        return true;
    }

    private String formatValue(Object v) {
        if (v == null) return "null";
        if (v instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf(d.intValue());
            return String.valueOf(d);
        }
        return String.valueOf(v);
    }
}