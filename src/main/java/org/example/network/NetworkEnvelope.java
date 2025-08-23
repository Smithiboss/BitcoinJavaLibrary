package org.example.network;

import org.example.ecc.Hex;
import org.example.utils.Bytes;

import java.util.Objects;

public class NetworkEnvelope {

    private final static byte[] NETWORK_MAGIC = new byte[] {(byte) 0xf9, (byte) 0xbe, (byte) 0xb4, (byte) 0xd9};
    private final static byte[] TESTNET_NETWORK_MAGIC = new byte[] {(byte) 0x0b, (byte) 0x11, (byte) 0x09, (byte) 0x07};

    private final byte[] command;
    private final byte[] payload;
    private final boolean testnet;
    private final byte[] magic;

    public NetworkEnvelope(byte[] command, byte[] payload, Boolean testnet) {
        this.command = command;
        this.payload = payload;
        this.testnet = Objects.requireNonNullElse(testnet, false);
        if (testnet) {
            magic = TESTNET_NETWORK_MAGIC;
        } else {
            magic = NETWORK_MAGIC;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NetworkEnvelope{" +
                "command=" + Bytes.byteArrayToHexString(command) +
                ", payload=" + Hex.parse(payload) +
                ", testnet=" + testnet +
                ", magic=" + Hex.parse(magic) +
                '}';
    }

}
