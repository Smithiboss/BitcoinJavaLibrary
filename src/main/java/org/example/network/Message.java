package org.example.network;

public interface Message {

    byte[] getCommand();

    byte[] serialize();

}
