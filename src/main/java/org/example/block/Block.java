package org.example.block;

import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.example.utils.Helper;

import java.io.ByteArrayInputStream;

public class Block {

    private Int version;
    private byte[] prevBlock;
    private byte[] merkleRoot;
    private Int timestamp;
    private byte[] bits;
    private byte[] nonce;

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

}
