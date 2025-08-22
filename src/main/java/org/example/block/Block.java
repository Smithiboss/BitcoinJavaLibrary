package org.example.block;

import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.example.utils.Hash;
import org.example.utils.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Block {

    private final Int version;
    private final byte[] prevBlock;
    private final byte[] merkleRoot;
    private final Int timestamp;
    private final byte[] bits;
    private final byte[] nonce;

    public Block(Int version, byte[] prevBlock, byte[] merkleRoot, Int timestamp, byte[] bits, byte[] nonce) {
        this.version = version;
        this.prevBlock = prevBlock;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
    }

    /**
     * Parse
     * @param stream a {@link java.io.ByteArrayOutputStream} object
     * @return a {@link Block} object
     */
    public static Block parse(ByteArrayInputStream stream) {
        // version is 4 bytes little endian
        var version = Helper.littleEndianToInt(Bytes.read(stream, 4));
        // prevBlock is 32 bytes little endian
        var prevBlock = Bytes.reverseOrder(Bytes.read(stream, 32));
        // merkleRoot is 32 bytes little endian
        var merkleRoot = Bytes.reverseOrder(Bytes.read(stream, 32));
        // timestamp is 4 bytes little endian
        var timestamp = Helper.littleEndianToInt(Bytes.read(stream, 4));
        // bits are 4 bytes
        var bits = Bytes.read(stream, 4);
        // nonce is 4 bytes
        var nonce = Bytes.read(stream, 4);
        return new Block(version, prevBlock, merkleRoot, timestamp, bits, nonce);
    }

    /**
     * Serialize
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        // version is 4 bytes little endian
        result.writeBytes(version.toBytesLittleEndian(4));
        // prevBlock is 32 bytes little endian
        result.writeBytes(Bytes.reverseOrder(prevBlock));
        // merkleRoot is 32 bytes little endian
        result.writeBytes(Bytes.reverseOrder(merkleRoot));
        // timestamp is 4 bytes little endian
        result.writeBytes(timestamp.toBytesLittleEndian(4));
        // bits are 4 bytes
        result.writeBytes(bits);
        // nonce is 4 bytes
        result.writeBytes(nonce);
        return result.toByteArray();
    }

    /**
     * Hash
     * @return a {@code byte} array
     */
    public byte[] hash() {
        // serialize
        var s = serialize();
        // hash256
        var hash256 = Hash.hash256(s);
        // reverse bytes
        return Bytes.reverseOrder(hash256);
    }

}
