package org.smithiboss.network;

public class VerAckMessage implements Message{

    public static final String COMMAND = "verack";

    /**
     * Parse
     * @param s a {@code byte} array
     * @return a {@link VerAckMessage} object
     */
    public static VerAckMessage parse(byte[] s) {
        return new VerAckMessage();
    }

    /**
     * Serialize
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }

}
