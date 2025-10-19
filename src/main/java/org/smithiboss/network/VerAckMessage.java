package org.smithiboss.network;

public class VerAckMessage implements Message {

    public static final String COMMAND = "verack";

    /**
     * Parses the given byte array and constructs a {@link VerAckMessage} instance.
     *
     * @param s the byte array to parse, representing the serialized form of a VerAckMessage
     * @return a {@link VerAckMessage} instance parsed from the provided byte array
     */
    public static VerAckMessage parse(byte[] s) {
        return new VerAckMessage();
    }

    /**
     * {@inheritDoc}
     */
    public byte[] serialize() {
        return new byte[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }

}
