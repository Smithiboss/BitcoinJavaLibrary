package org.example.network;

import org.example.utils.Bytes;

import java.io.ByteArrayInputStream;

public class PingMessage implements Message {

    public static final String COMMAND = "ping";

    private final byte[] nonce;

    public PingMessage(byte[] nonce) {
        this.nonce = nonce;
    }

    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }

    public PingMessage parse(ByteArrayInputStream s) {
        var nonce = Bytes.read(s, 8);
        return new PingMessage(nonce);
    }

    @Override
    public byte[] serialize() {
        return nonce;
    }
}
