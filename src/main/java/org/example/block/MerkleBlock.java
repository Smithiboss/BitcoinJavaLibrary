package org.example.block;

import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.example.utils.Helper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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

        var version = Helper.littleEndianToInt(Bytes.read(s, 4));

        var prevBlock = Bytes.reverseOrder(Bytes.read(s, 32));

        var merkleRoot = Bytes.reverseOrder(Bytes.read(s, 32));

        var timestamp = Helper.littleEndianToInt(Bytes.read(s, 4));

        var bits = Bytes.read(s, 4);

        var nonce = Bytes.read(s, 4);

        var total = Helper.littleEndianToInt(Bytes.read(s, 4)).intValue();

        var numHashes = Helper.readVarint(s);

        var hashes = new ArrayList<byte[]>();
        for (int i = 0; i < numHashes.intValue(); i++) {
            hashes.add(Bytes.reverseOrder(Bytes.read(s, 32)));
        }

        var flagsLength = Helper.readVarint(s);

        var flags = Bytes.read(s, flagsLength.intValue());

        return new MerkleBlock(version, prevBlock, merkleRoot, timestamp, bits, nonce, total, hashes, flags);
    }



}
