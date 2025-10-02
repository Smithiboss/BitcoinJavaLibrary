package org.example.script;

import org.example.utils.Bytes;
import org.example.utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.io.ByteArrayInputStream;

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
     * Takes a hash160 and returns the p2pkh ScriptPubKey
     * @param h160 a {@code byte} array
     * @return a {@link Script} object
     */
    public static Script p2pkhScript(byte[] h160) {
        List<Cmd> cmds = new ArrayList<>();
        cmds.add(OpCodes.OP_118_DUP.toCmd());
        cmds.add(OpCodes.OP_169_HASH160.toCmd());
        cmds.add(new Cmd(h160));
        cmds.add(OpCodes.OP_136_EQUALVERIFY.toCmd());
        cmds.add(OpCodes.OP_172_CHECKSIG.toCmd());
        return new Script(cmds);
    }

    /**
     * Parse
     * @param s a {@link ByteArrayInputStream}
     * @return a {@link Script} object
     */
    public static Script parse(ByteArrayInputStream s) {
        // get length of entire script
        Int length = Helper.readVarint(s);
        var cmds = new ArrayList<Cmd>();
        // keep track of the current position inside the stream
        var count = 0;
        // loop until we've read the entire script
        while (count < length.intValue()) {
            // the byte determines if we have an opcode or an element
            var currentByte = Hex.parse(Bytes.read(s, 1));
            count++;
            // if the byte is between 1 and 75 inclusive
            if (currentByte.ge(Int.parse(1)) && currentByte.le(Int.parse(75))) {
                var n = currentByte.intValue();
                cmds.add(new Cmd(Bytes.read(s, n)));
                count += n;
            } else if (currentByte.eq(Int.parse(76))) {
                // OP_PUSHDATA1
                var dataLength = Helper.littleEndianToInt(Bytes.read(s, 1));
                cmds.add(new Cmd(Bytes.read(s, dataLength.intValue())));
                count += dataLength.intValue() + 1;
            } else if (currentByte.eq(Int.parse(77))) {
                // OP_PUSHDATA2
                var dataLength = Helper.littleEndianToInt(Bytes.read(s, 2));
                cmds.add(new Cmd(Bytes.read(s, dataLength.intValue())));
                count += dataLength.intValue() + 2;
            } else {
                // it is an opcode
                cmds.add(OpCodes.findByCode(currentByte).toCmd());
            }
        }
        if (count != length.intValue()) {
            throw new IllegalArgumentException("Parsing script failed");
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
                result.writeBytes(cmd.getOpCode().getCode().toBytesLittleEndian(1));
            } else {

                var length = cmd.getElement().length;

                if (length < 75) {

                    result.writeBytes(Int.parse(length).toBytesLittleEndian(1));
                } else if (length > 75 && length < 0x100) {
                    // OP_PUSHDATA1
                    result.writeBytes(Int.parse(76).toBytesLittleEndian(1));
                    result.writeBytes(Int.parse(length).toBytesLittleEndian(1));
                } else if (length >= 0x100 && length <= 520) {
                    // OP_PUSHDATA2
                    result.writeBytes(Int.parse(77).toBytesLittleEndian(1));
                    result.writeBytes(Int.parse(length).toBytesLittleEndian(2));
                } else {
                    throw new IllegalStateException("Too long cmd: " + length);
                }
                result.writeBytes(cmd.getElement());
            }
        }
        return result.toByteArray();
    }

    /**
     * Serialize with varint
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        // serializeLegacy the script
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
                // call operation method will return a boolean
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
                if (new Script(cmdsCopy).isP2shScriptPubkey()) {
                    // remove OP_HASH160
                    cmdsCopy.removeFirst();
                    // get 20 byte hash value
                    var h160 = cmdsCopy.removeFirst();
                    // remove OP_EQUAL
                    cmdsCopy.removeFirst();
                    // run OP_HASH160
                    if (!Op.opHash160(stack)) return false;
                    // push 20 bytes to the stack
                    stack.push(h160.getElement());
                    // run OP_EQUAL
                    if (!Op.opEqual(stack)) return false;
                    // should be a 1 remaining check with OP_VERIFY
                    if (!Op.opVerify(stack)) return false;
                    // for parsing the redeem script, append the length
                    var redeemScript = Bytes.concat(Helper.encodeVarInt(Int.parse(cmd.getElement().length)), cmd.getElement());
                    var stream = new ByteArrayInputStream(redeemScript);
                    // extend the command set with the parsed commands from the redeem script
                    cmdsCopy.addAll(Script.parse(stream).cmds);
                }
            }
        }
        // return false if the stack is empty
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

    /**
     * Returns whether this is a p2sh script pubkey
     * @return a {@code boolean}
     */
    public boolean isP2shScriptPubkey() {
        return cmds.size() == 3 && OpCodes.OP_169_HASH160.equals(cmds.get(0).getOpCode())
                && cmds.get(1).isElement() && cmds.get(1).getElement().length == 20
                && OpCodes.OP_135_EQUAL.equals(cmds.get(2).getOpCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        List<String> result = new ArrayList<>();
        for (Cmd cmd : cmds) {
            if (cmd.isOpCode()) {
                // if the cmd is an integer, it's an opcode
                result.add(cmd.getOpCode().getCodeName());
            } else if (cmd.isElement()) {
                // otherwise, this is an element
                result.add(cmd.getElementAsString());
            } else {
                throw new IllegalStateException();
            }
        }
        return String.join(" ", result);
    }

    public List<Cmd> getCmds() {
        return cmds;
    }


}
