package org.example.script;

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

    public OpCodes getOpCode() {
        return opCodes;
    }
}
