package org.example.block;

import org.example.ecc.Int;
import org.example.spv.MerkleTree;
import org.example.utils.Bytes;
import org.example.utils.Helper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerkleBlock {

    private final Int version;
    private final byte[] prevBlock;
    private final byte[] merkleRoot;
    private final Int timestamp;
    private final byte[] bits;
    private final byte[] nonce;
    private final int total;
    private final List<byte[]> hashes;
    private final byte[] flags;


    /**
     * Constructs a MerkleBlock object
     * @param version a {@link Int} object
     * @param prevBlock a {@code byte} array
     * @param merkleRoot a {@code byte} array
     * @param timestamp a {@link Int} object
     * @param bits a {@code byte} array
     * @param nonce a {@code byte} array
     * @param total a {@code int}
     * @param hashes a {@link List} of {@code byte} arrays
     * @param flags a {@code byte} array
     */
    public MerkleBlock(Int version, byte[] prevBlock, byte[] merkleRoot, Int timestamp, byte[] bits, byte[] nonce, int total, List<byte[]> hashes, byte[] flags) {
        this.version = version;
        this.prevBlock = prevBlock;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
        this.total = total;
        this.hashes = hashes;
        this.flags = flags;
    }

    /**
     * Parse
     * @param s a {@link ByteArrayInputStream}
     * @return a {@link MerkleBlock} object
     */
    public static MerkleBlock parse(ByteArrayInputStream s) {
        // version is 4 bytes little endian
        var version = Helper.littleEndianToInt(Bytes.read(s, 4));
        // prevBlock is 32 bytes little endian
        var prevBlock = Bytes.reverseOrder(Bytes.read(s, 32));
        // merkleRoot is 32 bytes little endian
        var merkleRoot = Bytes.reverseOrder(Bytes.read(s, 32));
        // timestamp is 4 bytes little endian
        var timestamp = Helper.littleEndianToInt(Bytes.read(s, 4));
        // bits are 4 bytes
        var bits = Bytes.read(s, 4);
        // nonce is 4 bytes
        var nonce = Bytes.read(s, 4);
        // total is 4 bytes little endian
        var total = Helper.littleEndianToInt(Bytes.read(s, 4)).intValue();
        // numHashes is a varint
        var numHashes = Helper.readVarint(s);
        var hashes = new ArrayList<byte[]>();
        // each transaction hash is 32 bytes little endian
        for (int i = 0; i < numHashes.intValue(); i++) {
            hashes.add(Bytes.reverseOrder(Bytes.read(s, 32)));
        }
        // flags is a varint
        var flagsLength = Helper.readVarint(s);
        // flags is a bit field of length flagsLength
        var flags = Bytes.read(s, flagsLength.intValue());
        return new MerkleBlock(version, prevBlock, merkleRoot, timestamp, bits, nonce, total, hashes, flags);
    }

    /**
     * Verifies whether the merkle tree information validates the merkle root
     * @return a {@code boolean}
     */
    public boolean isValid() {
        // convert flag bytes to bit field
        var flagBits = Bytes.bytesToBitField(flags);
        // reverse hashes for the merkle root calculation
        var h = hashes.stream().map(Bytes::reverseOrder).toList();
        // create a merkle tree
        var merkleTree = new MerkleTree(total);
        // populate the merkle tree with the hashes and flagbits
        merkleTree.populateTree(flagBits, h);
        // compare the merkle root calculated from the merkle tree with the merkle root in the merkle block
        return Arrays.equals(merkleRoot, Bytes.reverseOrder(merkleTree.root()));
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

    public int getTotal() {
        return total;
    }

    public List<byte[]> getHashes() {
        return hashes;
    }

    public byte[] getFlags() {
        return flags;
    }
}
