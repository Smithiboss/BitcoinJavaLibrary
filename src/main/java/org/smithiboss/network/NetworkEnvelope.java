package org.smithiboss.network;

import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Hash;
import org.smithiboss.utils.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
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

    public static NetworkEnvelope parse(String raw, boolean testnet) {
        return parse(Bytes.hexStringToByteArray(raw), testnet);
    }

    public static NetworkEnvelope parse(byte[] bytes, boolean testnet) {
        return parse(new ByteArrayInputStream(bytes), testnet);
    }

    /**
     * Parse
     * @param s a {@link ByteArrayInputStream}
     * @param testnet a {@code boolean1}
     * @return a {@link NetworkEnvelope} object
     */
    public static NetworkEnvelope parse(ByteArrayInputStream s, boolean testnet) {
        // get magic
        var magic = Bytes.read(s, 4);
        byte[] expectedMagic;
        // compare magic with magic based on the given testnet value
        if (testnet) {
            expectedMagic = TESTNET_NETWORK_MAGIC;
        } else {
            expectedMagic = NETWORK_MAGIC;
        }
        if (Arrays.compareUnsigned(expectedMagic, magic) != 0) {
            throw new IllegalArgumentException("Invalid magic number");
        }
        // get command
        var command = Bytes.read(s, 12);
        // remove trailing zero bytes
        command = Bytes.strip(command);
        // get payload length
        var payloadLength = Helper.littleEndianToInt(Bytes.read(s, 4));
        // get checksum
        var checksum = Bytes.read(s, 4);
        // get payload based on its length
        var payload = Bytes.read(s, payloadLength.intValue());
        // calculate the checksum by taking the first 4 bytes of the hash256 of the payload
        var calculatedChecksum = Arrays.copyOfRange(Hash.hash256(payload), 0, 4);
        // compare checksums
        if (Arrays.compare(checksum, calculatedChecksum) != 0) {
            throw new IllegalArgumentException("Invalid checksum");
        }
        return new NetworkEnvelope(command, payload, testnet);
    }

    /**
     * Parse the length
     * @param s a {@link ByteArrayInputStream} object
     * @return a {@code int}
     */
    public static int parseLength(ByteArrayInputStream s) {
        Bytes.read(s, 4);
        Bytes.read(s, 12);
        var length = Helper.littleEndianToInt(Bytes.read(s, 4));
        return length.intValue();
    }

    /**
     * Serialize
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        // add network magic - 4 bytes
        result.writeBytes(magic);
        // add command with zero byte trailing - 12 bytes
        result.writeBytes(Bytes.concat(command, Bytes.initFill(12 - command.length, (byte) 0x00)));
        // add payload length - 4 bytes
        result.writeBytes(Int.parse(payload.length).toBytesLittleEndian(4));
        // add checksum - 4 bytes
        result.writeBytes(Arrays.copyOfRange(Hash.hash256(payload), 0, 4));
        // add payload
        result.writeBytes(payload);
        return result.toByteArray();
    }

    public byte[] getCommand() {
        return command;
    }

    public byte[] getPayload() {
        return payload;
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
