package org.example.script;

public class Cmd {

    private byte[] element;
    private OpCodeNames opCode;

    public Cmd(byte[] element) {
        this.element = element;
    }

    public Cmd(OpCodeNames opCode) {
        this.opCode = opCode;
    }

    public Cmd(int element) {
        this.element = new byte[] { (byte) element };
    }

}
