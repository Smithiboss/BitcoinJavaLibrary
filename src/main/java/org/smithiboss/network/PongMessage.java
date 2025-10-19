package org.smithiboss.network;

import org.smithiboss.utils.Bytes;

import java.io.ByteArrayInputStream;

public class PongMessage implements Message {

    public static final String COMMAND = "pong";

    private final byte[] nonce;

    public PongMessage(byte[] nonce) {
        this.nonce = nonce;
    }

    /**
     * Parses the given input stream to create a new {@code PongMessage} instance.
     *
     * @param s the {@code ByteArrayInputStream} containing the data to parse. Must not be null.
     * @return a new {@code PongMessage} instance created from the parsed data.
     */
    public PongMessage parse(ByteArrayInputStream s) {
        var nonce = Bytes.read(s, 8);
        return new PongMessage(nonce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serialize() {
        return nonce;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }
}
