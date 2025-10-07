package org.example.network;

public class GenericMessage implements Message{

    private final String COMMAND;
    private final byte[] payload;

    public GenericMessage(String command, byte[] payload) {
        this.COMMAND = command;
        this.payload = payload;
    }


    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }

    @Override
    public byte[] serialize() {
        return this.payload;
    }
}
