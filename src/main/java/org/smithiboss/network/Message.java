package org.smithiboss.network;

public interface Message {

    byte[] getCommand();

    byte[] serialize();

}
