package org.smithiboss.script;

import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Hash;
import org.smithiboss.utils.Helper;
import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.ecc.S256Point;
import org.smithiboss.ecc.Signature;

import java.util.*;
import java.util.logging.Logger;

public class Op {

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private Op() {};

    /**
     * Executes the operation associated with the provided opcode by interacting
     * with the given stack, alternate stack, and commands list. It also handles
     * operations that require signature hashes.
     *
     * @param opCode the operation code specifying the operation to be executed
     * @param stack a stack structure used during operations
     * @param altStack an alternate stack structure used for specific operations
     * @param cmds a list of commands that may be modified or evaluated during specific operations
     * @param z an integer value representing the signature hash, required for certain cryptographic operations
     * @return a boolean indicating whether the operation was successfully executed
     */
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

        // switch statement to execute the correct operation based on the opcode
        var opResult = false;
        switch (opCode) {
            case OP_0_0:
                opResult = Op.op0(stack);
                break;
            case OP_81_1:
                opResult = Op.op1(stack);
                break;
            case OP_82_2:
                opResult = Op.op2(stack);
                break;
            case OP_86_6:
                opResult = Op.op6(stack);
                break;
            case OP_105_VERIFY:
                opResult = Op.opVerify(stack);
                break;
            case OP_110_2DUP:
                opResult = Op.op2Dup(stack);
                break;
            case OP_118_DUP:
                opResult = Op.opDup(stack);
                break;
            case OP_124_SWAP:
                opResult = Op.opSwap(stack);
                break;
            case OP_135_EQUAL:
                opResult = Op.opEqual(stack);
                break;
            case OP_136_EQUALVERIFY:
                opResult = Op.opEqualVerify(stack);
                break;
            case OP_145_NOT:
                opResult = Op.opNot(stack);
                break;
            case OP_147_ADD:
                opResult = Op.opAdd(stack);
                break;
            case OP_167_SHA1:
                opResult = Op.opSha1(stack);
                break;
            case OP_169_HASH160:
                opResult = Op.opHash160(stack);
                break;
            case OP_170_HASH256:
                opResult = Op.opHash256(stack);
                break;
            case OP_172_CHECKSIG:
                opResult = Op.opCheckSig(stack, z);
                break;
            case OP_174_CHECKMULTISIG:
                opResult = Op.opCheckMultiSig(stack, z);
                break;
            default:
                log.severe(String.format("opcode %s not implemented.", opCode));
        }
        return opResult;
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
            // check if the most significant bit of the most significant byte is set
            if ((result[result.length - 1] & 0x80) != 0) {
                // if num is negative, append byte 0x80
                if (negative) {
                    result = Bytes.concat(result, new byte[]{(byte) (0x80)});
                // if the num is positive, append byte 0x00
                } else {
                    result = Bytes.concat(result, new byte[]{(byte) (0x00)});
                }
            // if num is negative but the most significant bit of the most significant byte is not set, set it
            } else if (negative) {
                result[result.length - 1] |= (byte) 0x80;
            }
        }
        return result;
    }

    /**
     * Decodes the given {@code byte} array into an int
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
        // if the most significant bit of the most significant byte is set, the number is negative
        if ((bigEndian[0] & 0x80) != 0) {
            negative = true;
            // set the most significant bit to zero
            result = bigEndian[0] & 0x7f;
        } else {
            negative = false;
            result = bigEndian[0];
        }
        // start with the second byte
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
     * OP_VERIFY marks a transaction as invalid if the top stack value is not true
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
     * OP_EQUALVERIFY is the same as OP_EQUAL but runs OP_VERIFY afterward
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
     * OP_CHECKSIG validates the signature
     *
     * @param stack a {@link Deque} object
     * @param z a {@link Int} object
     * @return a {@code boolean}
     */
    static boolean opCheckSig(Deque<byte[]> stack, Int z) {
        // a stack needs two elements at least
        if (stack.size() < 2) {
            return false;
        }
        // the top element is the SEC pubkey
        var secPubKey = stack.pop();
        var derSigWithHashType = stack.pop();
        var derSig = Arrays.copyOf(derSigWithHashType, derSigWithHashType.length - 1);
        // parseLegacy the serialized pubkey and signature into objects
        S256Point point;
        Signature sig;
        try {
            point = S256Point.parse(secPubKey);
            sig = Signature.parse(derSig);
        } catch (Exception e) {
            log.severe(String.format("Exception %s", e.getMessage()));
            return false;
        }
        // verify the signature
        if (point.verify(z, sig)) {
            stack.push(encodeNum(1));
        } else {
            stack.push(encodeNum(0));
        }
        log.fine(String.format("%s", printStack(stack)));
        return true;
    }

    /**
     * OP_CHECKMULTISIG searches for an ECDSA match for every signature
     *
     * @param stack a {@link Deque} object
     * @param z a {@link Int} object
     * @return a {@code boolean}
     */
    static boolean opCheckMultiSig(Deque<byte[]> stack, Int z) {
        if (stack.isEmpty()) return false;
        // check that stack size matches n + 1 at least
        var n = decodeNum(stack.pop());
        if (stack.size() < n + 1) return false;
        // get all pubkeys from the stack
        var secPubKeys = new ArrayList<byte[]>();
        for (int i = 0; i < n; i++) {
            secPubKeys.add(stack.pop());
        }
        // check if the stack size matches m + 1 at least
        var m = decodeNum(stack.pop());
        if (stack.size() < m + 1) return false;
        // get all signatures from the stack
        var derSignatures = new ArrayList<byte[]>();
        for (int i = 0; i < m; i++) {
            var derSignature = stack.pop();
            derSignatures.add(Arrays.copyOf(derSignature, derSignature.length - 1));
        }
        // OP_CHECKMULTISIG Bug
        stack.pop();
        // parse all points
        var points = new ArrayList<S256Point>();
        for (byte[] secPubKey : secPubKeys) {
            points.add(S256Point.parse(secPubKey));
        }
        // parse all signatures
        var sigs = new ArrayList<Signature>();
        for (byte[] derSignature : derSignatures) {
            sigs.add(Signature.parse(derSignature));
        }
        // loop over every signature
        for (Signature sig : sigs) {
            if (points.isEmpty()) return false;
            // check if the current point works with the signature
            for (S256Point point : points) {
                points.remove(point);
                if (point.verify(z, sig)) {
                    break;
                }
            }
        }
        // the signatures are valid, push a 1 to the stack
        stack.push(encodeNum(1));
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
