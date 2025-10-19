package org.smithiboss.network;

public interface Message {

    /**
     * Retrieves the command of the implementing message.
     *
     * @return a byte array representing the command associated with the message.
     */
    byte[] getCommand();

    /**
     * Serializes the message into a byte array representation.
     *
     * @return a byte array containing the serialized form of the message.
     */
    byte[] serialize();

}
