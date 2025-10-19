package org.smithiboss.network;

import org.smithiboss.utils.Bytes;

import java.io.ByteArrayInputStream;

public class PingMessage implements Message {

    public static final String COMMAND = "ping";

    private final byte[] nonce;

    public PingMessage(byte[] nonce) {
        this.nonce = nonce;
    }

    /**
     * Parses a ByteArrayInputStream to construct a PingMessage object.
     *
     * @param s the ByteArrayInputStream containing the serialized data to parse
     * @return a new PingMessage instance created from the parsed data
     */
    public PingMessage parse(ByteArrayInputStream s) {
        var nonce = Bytes.read(s, 8);
        return new PingMessage(nonce);
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
