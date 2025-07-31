package org.example.script;

import org.example.Utils.Helper;
import org.example.ecc.Int;

import java.io.ByteArrayInputStream;import
import java.util.ArrayList;
import java.util.List;

public class Script {

    private List<Cmd> cmds = new ArrayList<>();

    public Script(List<Cmd> cmds) {
        if (cmds != null) this.cmds = cmds;
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
            int currentByte = s.read();
            count++;
            // if the byte is between 1 and 75 inclusive
            if (currentByte >= 1 && currentByte <= 75) {
                cmds.add(new Cmd(s.readNBytes(currentByte)));
                count += currentByte;
            } else if (currentByte == 76) {
                // OP_PUSHDATA1
                int dataLength = Helper.littleEndianToInt(s.readNBytes(1)).intValue();
                cmds.add(new Cmd(s.readNBytes(dataLength)));
                count += dataLength + 1;
            } else if (currentByte == 77) {
                // OP_PUSHDATA2
                int dataLength = Helper.littleEndianToInt(s.readNBytes(2)).intValue();
                cmds.add(new Cmd(s.readNBytes(dataLength)));
                count += dataLength + 2;
            } else {
                // it is an opcode
                cmds.add(new Cmd(currentByte));
            }
        }
        if (count != length.intValue()) {
            throw new Exception("Parsing script failed!");
        }
    return new Script(cmds);
    }

    public static byte[] rawSerialize() {
        var result = new byte[0];
    }

}
