package org.smithiboss.network;

import org.smithiboss.utils.Bytes;

import java.io.ByteArrayInputStream;

public class PongMessage implements Message {

    public static final String COMMAND = "pong";

    private final byte[] nonce;

    public PongMessage(byte[] nonce) {
        this.nonce = nonce;
    }

    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }

    public PongMessage parse(ByteArrayInputStream s) {
        var nonce = Bytes.read(s, 8);
        return new PongMessage(nonce);
    }

    @Override
    public byte[] serialize() {
        return nonce;
    }
}
