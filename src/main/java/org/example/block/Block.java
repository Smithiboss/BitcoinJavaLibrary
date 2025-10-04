package org.example.block;

import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.example.utils.Hash;
import org.example.utils.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Block {

    private final Int version;
    private final byte[] prevBlock;
    private final byte[] merkleRoot;
    private final Int timestamp;
    private final byte[] bits;
    private final byte[] nonce;
    private List<byte[]> txHashes;

    public Block(Int version, byte[] prevBlock, byte[] merkleRoot, Int timestamp, byte[] bits, byte[] nonce, List<byte[]> txHashes) {
        this.version = version;
        this.prevBlock = prevBlock;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
        this.txHashes = Objects.requireNonNullElse(txHashes, new ArrayList<>());
    }

    public static Block parse(String raw) {
        return parse(Bytes.hexStringToByteArray(raw));
    }

    public static Block parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes));
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
        return new Block(version, prevBlock, merkleRoot, timestamp, bits, nonce, null);
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

    /**
     * Returns whether this block supports Bip9
     * @return a {@code boolean}
     */
    public boolean isBip9() {
        // bip9 is signaled if the first 3 bits are 001
        // shift 29 to the right, leaving the first 3 bits
        return version.intValue() >> 29 == 0b001;
    }

    /**
     * Returns whether this block supports Bip91
     * @return a {@code boolean}
     */
    public boolean isBip91() {
        // bip91 is signaled if the 5th last bit is 1
        // shift 4 to the right, leaving the first 28 bits. Bitwise AND checking for 1
        return (version.intValue() >> 4 & 1) == 1;
    }

    /**
     * Returns whether this block supports Bip141
     * @return a {@code boolean}
     */
    public boolean isBip141() {
        // bip9 is signaled if the 2nd last bit is 1
        // shift 1 to the right, leaving the first 31 bits. Bitwise AND checking for 1
        return (version.intValue() >> 1 & 1) == 1;
    }

    /**
     * Returns the proof of work target
     * @return a {@link Int} object
     */
    public Int target() {
        return Bytes.bitsToTarget(bits);
    }

    /**
     * Returns the difficulty
     * @return a {@link Int} object
     */
    public Int difficulty() {
        var lowest = Hex.parse("ffff").mul(Int.parse(256).pow(Hex.parse("1d").sub(Int.parse(3))));
        return lowest.div(target());
    }

    /**
     * Check for valid proof of work
     * @return a {@code boolean}
     */
    public boolean checkProofOfWork() {
        var hash = Hash.hash256(serialize());
        var proof = Helper.littleEndianToInt(hash);
        return proof.lt(target());
    }

    /**
     * Validates the merkle root
     * @return a {@code boolean}
     */
    public boolean validateMerkleRoot() {
        var hashes = txHashes.stream().map(Bytes::reverseOrder).toList();

        var root = Helper.merkleRoot(hashes);

        return Arrays.equals(Bytes.reverseOrder(root), merkleRoot);
    }

    public Int getVersion() {
        return version;
    }

    public byte[] getPrevBlock() {
        return prevBlock;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public Int getTimestamp() {
        return timestamp;
    }

    public byte[] getBits() {
        return bits;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setTxHashes(List<byte[]> txHashes) {
        this.txHashes = txHashes;
    }
}
