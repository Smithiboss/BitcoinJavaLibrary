package org.example.network;

import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.example.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class VersionMessage {

    public static final String COMMAND = "version";

    private final Int version;
    private final Int services;
    private final Int timestamp;
    private final Int receiverServices;
    private final byte[] receiverIP;
    private final Int receiverPort;
    private final Int senderServices;
    private final byte[] senderIP;
    private final Int senderPort;
    private final byte[] nonce;
    private final String userAgent;
    private final Int latestBlock;
    private final Boolean relay;

    public VersionMessage(Int version, Int services, Int timestamp, Int receiverServices, byte[] receiverIP,
                          Int receiverPort, Int senderServices, byte[] senderIP, Int senderPort, byte[] nonce,
                          String userAgent, Int latestBlock, Boolean relay) {
        this.version = Objects.requireNonNullElse(version, Int.parse(70015));
        this.services = Objects.requireNonNullElse(services, Int.parse(0));
        this.timestamp = Objects.requireNonNullElse(timestamp, Int.parse(String.valueOf(System.currentTimeMillis() / 1000)));
        this.receiverServices = Objects.requireNonNullElse(receiverServices, Int.parse(0));
        this.receiverIP = Objects.requireNonNullElse(receiverIP, new byte[]{0x00, 0x00, 0x00, 0x00});
        this.receiverPort = Objects.requireNonNullElse(receiverPort, Int.parse(8333));
        this.senderServices = Objects.requireNonNullElse(senderServices, Int.parse(0));
        this.senderIP = Objects.requireNonNullElse(senderIP, new byte[]{0x00, 0x00, 0x00, 0x00});
        this.senderPort = Objects.requireNonNullElse(senderServices, Int.parse(0));
        this.nonce = Objects.requireNonNullElse(nonce, Bytes.randomBytes(8));
        this.userAgent = Objects.requireNonNullElse(userAgent, "/bitcoinjavalibrary/");
        this.latestBlock = Objects.requireNonNullElse(latestBlock, Int.parse(0));
        this.relay = Objects.requireNonNullElse(relay, false);
    }

    /**
     * Serialize
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        // version - 4 bytes
        result.writeBytes(version.toBytesLittleEndian(4));
        // services - 8 bytes
        result.writeBytes(services.toBytesLittleEndian(8));
        // timestamp - 8 bytes
        result.writeBytes(timestamp.toBytesLittleEndian(8));
        // receiverServices - 8 bytes
        result.writeBytes(receiverServices.toBytesLittleEndian(8));
        // receiverIP - 16 bytes - 10 * 0x00 + 2 * 0xff + ip
        result.writeBytes(Bytes.concat(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
                new byte[]{(byte) 0xff, (byte) 0xff}, receiverIP));
        // receiverPort - 2 bytes
        result.writeBytes(receiverPort.toBytes(2));
        // senderServices - 8 bytes
        result.writeBytes(senderServices.toBytesLittleEndian(8));
        // senderIP - 16 bytes - 10 * 0x00 + 2 * 0xff + ip
        result.writeBytes(Bytes.concat(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
                new byte[]{(byte) 0xff, (byte) 0xff}, senderIP));
        // senderPort - 2 bytes
        result.writeBytes(senderPort.toBytes(2));
        // nonce - 8 bytes
        result.writeBytes(nonce);
        // encode the length of userAgent as varint
        result.writeBytes(Helper.encodeVarInt(Int.parse(userAgent.length())));
        // userAgent
        result.writeBytes(userAgent.getBytes());
        // latestBlock - 4 bytes
        result.writeBytes(latestBlock.toBytesLittleEndian(4));
        // add 0x01 if relay is true else 0x00
        if (relay) {
            result.writeBytes(new byte[]{(byte) 0x01});
        } else {
            result.writeBytes(new byte[]{0x00});
        }
        return result.toByteArray();
    }

}
