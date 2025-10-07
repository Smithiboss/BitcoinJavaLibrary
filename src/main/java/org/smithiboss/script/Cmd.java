package org.smithiboss.script;

import org.smithiboss.utils.Bytes;

public class Cmd {

    private byte[] element;

    private OpCodes opCodes;

    public Cmd(byte[] element) {
        this.element = element;
    }

    public Cmd(OpCodes opCodes) {
        this.opCodes = opCodes;
    }

    public Cmd(int element) {
        this.element = new byte[] { (byte) element };
    }

    public boolean isOpCode() {
        return opCodes != null;
    }

    public boolean isElement() {
        return element != null;
    }

    public byte[] getElement() {
        return element;
    }

    public String getElementAsString() {
        return Bytes.byteArrayToHexString(element);
    }

    public OpCodes getOpCode() {
        return opCodes;
    }
}
