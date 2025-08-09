package org.example.script;

import org.example.Utils.Bytes;
import org.example.Utils.Hash;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Op {

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private Op() {};

    public static boolean operation(OpCodes opCode, Deque<byte[]> stack, Deque<byte[]> altStack, List<Cmd> cmds, Int z) {
        // OP_IF and OP_NOTIF require manipulation of the cmds array based on the top element of the stack
        if (Set.of(OpCodes.OP_99_IF.getCode(), OpCodes.OP_100_NOTIF.getCode()).contains(opCode.getCode())) {
            // terminate if cmds are missing
            if (cmds.isEmpty()) {
                log.warning(String.format("Bad op: %s - missing cmds", opCode));
                return false;
            }
        // OP_TOALTSTACK and OP_FROMALTSTACK move stack elements to/from an "alternate" stack, they need the altstack
        } else if (Set.of(OpCodes.OP_107_TOALTSTACK.getCode(), OpCodes.OP_108_FROMALTSTACK.getCode()).contains(opCode.getCode())) {
            // terminate if altstack is empty
            if (altStack.isEmpty()) {
                log.warning(String.format("Bad op: %s - missing altstack", opCode));
                return false;
            }
        // OP_CHECKSIG, OP_CHECKSIGVERIFY, OP_CHECKMULTISIG and OP_CHECKMULTISIGVERIFY all require the signature hash, z
        } else if (Set.of(OpCodes.OP_172_CHECKSIG.getCode(), OpCodes.OP_173_CHECKSIGVERIFY.getCode(),
                OpCodes.OP_174_CHECKMULTISIG.getCode(), OpCodes.OP_175_CHECKMULTISIGVERIFY.getCode())
                .contains(opCode.getCode())) {
            // terminate if z is null
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
     * Encodes the given {@code int} into bytes
     *
     * @param num a {@code int}
     * @return a {@code byte} array
     */
    static byte[] encodeNum(int num) {
        var result = new byte[0];
        // if num == 0, return the empty byte array
        if (num == 0) {
            return result;
        }
        var absNum = Math.abs(num);
        var negative = num < 0;

        // loop until absNum == 0
        while (absNum != 0) {
            // add the least significant byte to the result
            result = Bytes.concat(result, new byte[]{(byte) (absNum & 0xff)});
            // shift absNum 8 bits to the right (division by 256, integer)
            absNum >>= 8;
            // check if most significant bit of most significant byte is set
            if ((result[result.length - 1] & 0x80) != 0) {
                // if num is negative, append byte 0x80
                if (negative) {
                    result = Bytes.concat(result, new byte[]{(byte) (0x80)});
                // if num is positive, append byte 0x00
                } else {
                    result = Bytes.concat(result, new byte[]{(byte) (0x00)});
                }
            // if num is negative but most significant bit of most significant byte is not set, set it
            } else if (negative) {
                result[result.length - 1] |= (byte) 0x80;
            }
        }
        return result;
    }

    /**
     * Decodes the given {@code byte} array into a number
     *
     * @param bytes a {@code byte} array
     * @return an {@code int}
     */
    static int decodeNum(byte[] bytes) {
        // if the byte array is empty, return zero
        if (Arrays.equals(bytes, new byte[0])) {
            return 0;
        }
        // reverse order to big endian
        var bigEndian = Bytes.reverseOrder(bytes);
        boolean negative;
        int result;
        // if most significant bit of most significant byte is set, number is negative
        if ((bigEndian[0] & 0x80) != 0) {
            negative = true;
            // set most significant bit to zero
            result = bigEndian[0] & 0x7f;
        } else {
            negative = false;
            result = bigEndian[0];
        }
        // start with second byte
        for (int c = 1; c < bytes.length; c++) {
            // multiply with 256
            result <<= 8;
            // add the byte
            result += c;
        }
        if (negative) {
            return -result;
        } else {
            return result;
        }
    }

    /**
     * OP_0 pushes a 0 to the stack
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
     * OP_1 pushes a 1 to the stack
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
     * OP_2 pushes a 2 to the stack
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
     * OP_6 pushes a 6 to the stack
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
     * OP_VERIFY marks a transaction as invalid if top stack value is not true
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
     * OP_2DUP duplicates the top two stack items
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

    /**
     * OP_DUP duplicates the top stack item
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
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
     * OP_SWAP swaps the top two items on the stack
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
     * OP_EQUAL returns true if the inputs are exactly equal, false otherwise
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
     * OP_EQUALVERIFY is the same as OP_EQUAL, but runs OP_VERIFY afterward
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    public static boolean opEqualVerify(Deque<byte[]> stack) {
        return opEqual(stack) && opVerify(stack);
    }

    /**
     * OP_NOT flips the input if it is 0 or 1. Otherwise, the output will be 0
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
     * OP_ADD adds a to b
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

    /**
     * OP_SHA1 hashes the input using SHA-1
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    static boolean opSha1(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        stack.push(Hash.sha1(element));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * OP_HASH160 hashed the input twice: first with SHA-256 and then with RIPEMD-160
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
    static boolean opHash160(Deque<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        byte[] element = stack.pop();
        stack.push(Hash.hash160(element));
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * OP_HASH256 hashes the input two times with SHA-256
     *
     * @param stack a {@link java.util.Deque} object
     * @return a boolean
     */
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
     * Prints the stack
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
