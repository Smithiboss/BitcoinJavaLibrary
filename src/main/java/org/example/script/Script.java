package org.example.script;

import org.example.Utils.Bytes;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.io.ByteArrayInputStream;import

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Script {

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private List<Cmd> cmds = new ArrayList<>();

    public Script(List<Cmd> cmds) {
        if (cmds != null) this.cmds = cmds;
    }

    /**
     * Combines the command sets
     * @param other a {@link Script} object
     * @return a combined {@link Script} object
     */
    public Script add(Script other) {
        List<Cmd> newCmds = new ArrayList<>();
        newCmds.addAll(this.cmds);
        newCmds.addAll(other.cmds);
        return new Script(newCmds);
    }

    /**
     * Parse
     * @param s a {@link ByteArrayInputStream}
     * @return a {@link Script} object
     * @throws Exception Stream Exception
     */
    public static Script parse(ByteArrayInputStream s) throws Exception {
        // get length of entire script
        Int length = Helper.readVarint(s);

        var cmds = new ArrayList<Cmd>();
        // keep track of the current position inside the stream
        var count = 0;
        // loop until we've read the entire script
        while (count < length.intValue()) {
            // the byte determines if we have an opcode or an element
            var currentByte = Hex.parse(s.read());
            count++;
            // if the byte is between 1 and 75 inclusive
            if (currentByte.ge(Int.parse(1)) && currentByte.le(Int.parse(75))) {
                cmds.add(new Cmd(s.readNBytes(currentByte.intValue())));
                count += currentByte.intValue();
            } else if (currentByte.eq(Int.parse(76))) {
                // OP_PUSHDATA1
                int dataLength = Helper.littleEndianToInt(s.readNBytes(1)).intValue();
                cmds.add(new Cmd(s.readNBytes(dataLength)));
                count += dataLength + 1;
            } else if (currentByte.eq(Int.parse(77))) {
                // OP_PUSHDATA2
                int dataLength = Helper.littleEndianToInt(s.readNBytes(2)).intValue();
                cmds.add(new Cmd(s.readNBytes(dataLength)));
                count += dataLength + 2;
            } else {
                // it is an opcode
                cmds.add(OpCodes.findByCode(currentByte).toCmd());
            }
        }
        if (count != length.intValue()) {
            throw new Exception("Parsing script failed!");
        }
    return new Script(cmds);
    }

    /**
     * Serialize
     * @return a {@code byte} array
     */
    public byte[] rawSerialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        for (Cmd cmd : this.cmds) {
            if (cmd.isOpCode()) {
                result.writeBytes(cmd);
            } else if (cmd.isElement()) {

                var length = cmd.getElement().length;

                if (length < 75) {

                    result.writeBytes(Int.parse(length).toBytesLittleEndian(1));
                } else if (length > 75 && length < 0x100) {
                    // OP_PUSHDATA1
                    result.writeBytes(Int.parse(76).toBytesLittleEndian(1));
                    result.writeBytes(Int.parse(length).toBytesLittleEndian(1));
                } else if (length > 0x100 && length <= 520) {
                    // OP_PUSHDATA2
                    result.writeBytes(Int.parse(77).toBytesLittleEndian(1));
                    result.writeBytes(Int.parse(length).toBytesLittleEndian(2));
                } else {
                    throw new IllegalStateException("Too long cmd: " + length);
                }
            }
        }
        return result.toByteArray();
    }

    /**
     * Serialize with varint
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        // serialize the script
        var result = this.rawSerialize();
        var total = Int.parse(result.length);
        // encode the varint based on the script length
        var varInt = Helper.encodeVarInt(total);
        // add it to the serialized byte array
        return Bytes.concat(varInt, result);
    }

    /**
     * Evaluates the combined command set. Returns a{@code true} if the script succeeds, else false.
     * @param z The Signature hash. A {@link Int} object
     * @return a {@code boolean}
     */
    public boolean evaluate(Int z) {
        var cmdsCopy = new ArrayList<>(this.cmds);
        var stack = new ArrayDeque<byte[]>();
        var altStack = new ArrayDeque<byte[]>();

        // loop over all cmds
        while (!cmdsCopy.isEmpty()) {
            // get first
            var cmd = cmdsCopy.removeFirst();
            // check if cmd is an opcode
            if (cmd.isOpCode()) {
                // call operation method, will return a boolean
                var operationResult = Op.operation(cmd.getOpCode(), stack, altStack, cmdsCopy, z);
                // terminate if false
                if (!operationResult) {
                    log.warning(String.format("bad op: %s", cmd.getOpCode()));
                    return false;
                }
            // check if cmd is an element
            } else if (cmd.isElement()) {
                // add the element to the stack
                stack.push(cmd.getElement());
            }
        }
        // return false if stack is empty
        if (stack.isEmpty()) {
            log.warning("Script evaluation failed! Stack is empty!");
            return false;
        }
        // return false if the top element is zero - verification failed
        if (Arrays.equals(stack.pop(), new byte[0])) {
            log.warning("Script evaluation failed! Top element is zero!");
            return false;
        }
        // else the script succeeded
        return true;
    }
}
