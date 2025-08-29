package org.example.network;

public class VerAckMessage {

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

}
