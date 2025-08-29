package org.example.network;

import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.example.utils.Helper;

import java.security.SecureRandom;
import java.util.Objects;

public class VersionMessage {

    public static final String COMMAND = "version";

    private final Int version;
    private final Int services;
    private final Int timestamp;
    private final Int recieverServices;
    private final byte[] recieverIP;
    private final Int recieverPort;
    private final Int senderServices;
    private final byte[] senderIP;
    private final Int senderPort;
    private final byte[] nonce;
    private final String userAgent;
    private final Int latestBlock;
    private final Boolean relay;

    public VersionMessage(Int version, Int services, Int timestamp, Int recieverServices, byte[] recieverIP,
                          Int recieverPort, Int senderServices, byte[] senderIP, Int senderPort, byte[] nonce,
                          String userAgent, Int latestBlock, Boolean relay) {
        this.version = Objects.requireNonNullElse(version, Int.parse(70015));
        this.services = Objects.requireNonNullElse(services, Int.parse(0));
        this.timestamp = Objects.requireNonNullElse(timestamp, Int.parse(String.valueOf(System.currentTimeMillis() / 1000)));
        this.recieverServices = Objects.requireNonNullElse(recieverServices, Int.parse(0));
        this.recieverIP = Objects.requireNonNullElse(recieverIP, new byte[]{0x00, 0x00, 0x00, 0x00});
        this.recieverPort = Objects.requireNonNullElse(recieverPort, Int.parse(8333));
        this.senderServices = Objects.requireNonNullElse(senderServices, Int.parse(0));
        this.senderIP = Objects.requireNonNullElse(senderIP, new byte[]{0x00, 0x00, 0x00, 0x00});
        this.senderPort = Objects.requireNonNullElse(senderServices, Int.parse(0));
        this.nonce = Objects.requireNonNullElse(nonce, Bytes.randomBytes(8));
        this.userAgent = Objects.requireNonNullElse(userAgent, "/bitcoinjavalibrary/");
        this.latestBlock = Objects.requireNonNullElse(latestBlock, Int.parse(0));
        this.relay = Objects.requireNonNullElse(relay, false);
    }

}
