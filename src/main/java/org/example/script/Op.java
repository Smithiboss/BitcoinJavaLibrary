package org.example.script;

import org.example.Utils.Bytes;
import org.example.Utils.Hash;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Op {

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private Op() {};

    public static boolean operation(OpCodes opCode, Deque<byte[]> stack, Deque<byte[]> altStack, List<Cmd> cmds, Int z) {
        if (Set.of(OpCodes.OP_99_IF.getCode(), OpCodes.OP_100_NOTIF.getCode()).contains(opCode.getCode())) {
            if (cmds.isEmpty()) {
                log.warning(String.format("Bad op: %s - missing cmds", opCode));
                return false;
            }
        } else if (Set.of(OpCodes.OP_107_TOALTSTACK.getCode(), OpCodes.OP_108_FROMALTSTACK.getCode()).contains(opCode.getCode())) {
            if (altStack.isEmpty()) {
                log.warning(String.format("Bad op: %s - missing altstack", opCode));
                return false;
            }
        } else if (Set.of(OpCodes.OP_172_CHECKSIG.getCode(), OpCodes.OP_173_CHECKSIGVERIFY.getCode(),
                OpCodes.OP_174_CHECKMULTISIG.getCode(), OpCodes.OP_175_CHECKMULTISIGVERIFY.getCode())
                .contains(opCode.getCode())) {
            if (z == null) {
                log.warning(String.format("Bad op: %s - missing z", opCode));
                return false;
            }
        }

        // This implementation for dispatching opCodes is not the most efficient.
        // Using a switch statement would be easier and faster, as it runs in constant time.
        // I used reflection here purely for learning purposes.

        String functionName = opCode.getCodeFunc();

        try {
            // Find method by function name
            for (Method method : Op.class.getDeclaredMethods()) {
                if (method.getName().equals(functionName)) {

                    // Check parameters
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] args = new Object[parameterTypes.length];

                    // Loop through all parameter types to find matching ones
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (parameterTypes[i].isAssignableFrom(Deque.class)) {
                            // Check parameters for stack or altstack
                            if (method.getParameters()[i].getName().toLowerCase().contains("alt")) {
                                args[i] = altStack;
                            } else {
                                args[i] = stack;
                            }
                        } else if (parameterTypes[i].isAssignableFrom(List.class)) {
                            args[i] = cmds;
                        } else if (parameterTypes[i].isAssignableFrom(Int.class)) {
                            args[i] = z;
                        } else {
                            log.warning("Unknown parameter: " + parameterTypes[i]);
                            return false;
                        }
                    }

                    // Call the method
                    Object result = method.invoke(null, args);
                    if (result instanceof Boolean) {
                        return (boolean) result;
                    } else {
                        log.warning("Method " + functionName + " does not return boolean!");
                        return false;
                    }
                }
            }

            log.warning("Method not found: " + functionName);
            return false;

        } catch (Exception e) {
            log.severe("Error while executing " + functionName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * <p>op0.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean op0(Deque<byte[]> stack) {
        stack.push(encodeNum(0));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op1.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean op1(Deque<byte[]> stack) {
        stack.push(encodeNum(1));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op2.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean op2(Deque<byte[]> stack) {
        stack.push(encodeNum(2));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op6.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean op6(Deque<byte[]> stack) {
        stack.push(encodeNum(6));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op105Verify.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opVerify(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        if (decodeNum(element) == 0) {
            return false;
        }
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op1102Dup.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean op2Dup(Deque<byte[]> stack) {
        if (stack.size() < 2) {
            return false;
        }
        var element1 = stack.pop();
        var element2 = stack.pop();
        stack.push(element2);
        stack.push(element1);
        stack.push(element2);
        stack.push(element1);
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    static boolean opDup(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        stack.push(element);
        stack.push(element);
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op124Swap.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opSwap(Deque<byte[]> stack) {
        if (stack.size() < 2) {
            return false;
        }
        var element1 = stack.pop();
        var element2 = stack.pop();
        stack.push(element1);
        stack.push(element2);
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op135Equal.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opEqual(Deque<byte[]> stack) {
        if (stack.size() < 2) {
            return false;
        }
        Int element1 = Hex.parse(stack.pop());
        Int element2 = Hex.parse(stack.pop());
        if (element1.eq(element2)) {
            stack.push(encodeNum(1));
        } else {
            stack.push(encodeNum(0));
        }
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op136EqualVerify.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opEqualVerify(Deque<byte[]> stack) {
        return opEqual(stack) && opVerify(stack);
    }

    /**
     * <p>op145Not.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opNot(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        var element = stack.pop();
        if (decodeNum(element) == 0) {
            stack.push(encodeNum(1));
        } else {
            stack.push(encodeNum(0));
        }
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>op147Add.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opAdd(Deque<byte[]> stack) {
        if (stack.size() < 2) {
            return false;
        }
        Int element1 = Hex.parse(stack.pop());
        Int element2 = Hex.parse(stack.pop());
        stack.push(encodeNum(element1.add(element2).intValue()));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    static boolean opSha1(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        stack.push(Hash.sha1(element));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    static boolean opHash160(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        stack.push(Hash.hash160(element));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    static boolean opHash256(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        stack.push(Hash.hash256(element));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * <p>printStack.</p>
     *
     * @param stack a {@link java.util.Deque} object
     * @return a {@link java.lang.String} object
     */
    public static String printStack(Deque<byte[]> stack) {
        StringBuilder stackBuilder = new StringBuilder();
        stackBuilder.append("[");
        String sep = "";
        for (byte[] bytes : stack) {
            stackBuilder.append(sep);
            String hexString = Bytes.byteArrayToHexString(bytes);
            if (hexString.length() >= 10) {
                stackBuilder.append(Helper.maskString(hexString, 4));
            } else {
                stackBuilder.append(hexString);
            }
            sep = " : ";
        }
        stackBuilder.append("]");
        return stackBuilder.toString();
    }

}
