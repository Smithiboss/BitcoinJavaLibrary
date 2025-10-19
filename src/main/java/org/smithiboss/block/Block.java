package org.smithiboss.block;

import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Hash;
import org.smithiboss.utils.Helper;

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
     * Parses a {@code ByteArrayInputStream} to create a {@link Block} instance by reading
     * and interpreting the block data fields in the expected order.
     *
     * @param stream the {@code ByteArrayInputStream} containing the block data
     *               to be parsed; must not be null
     * @return a {@link Block} object constructed from the parsed data
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
     * Serializes the block into its byte array representation by encoding its fields
     * in the expected order and format.
     *
     * @return a byte array containing the serialized representation of the block
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
     * Computes the hash of the block.
     *
     * @return a {@code byte} array representing the hash of the block in little-endian format
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
     * Determines if this block signals support for BIP9 (Soft Fork Deployment).
     * Checks whether the first three bits of the block's version field are set
     * to the pattern `001` as defined by the BIP9 specification.
     *
     * @return a {@code boolean} indicating whether the block supports BIP9;
     *         {@code true} if the first three bits of the version field are `001`,
     *         otherwise {@code false}.
     */
    public boolean isBip9() {
        return version.intValue() >> 29 == 0b001;
    }

    /**
     * Determines if this block signals support for BIP91.
     * BIP91 is indicated if the 5th least significant bit of the block's version number is set to 1.
     *
     * @return a {@code boolean} indicating whether the block supports BIP91;
     *         {@code true} if the 5th least significant bit is 1, otherwise {@code false}.
     */
    public boolean isBip91() {
        return (version.intValue() >> 4 & 1) == 1;
    }

    /**
     * Determines if this block signals support for BIP141 (Segregated Witness).
     * BIP141 is indicated if the 2nd least significant bit of the block's version
     * number is set to 1.
     *
     * @return a {@code boolean} indicating whether the block supports BIP141;
     *         {@code true} if the 2nd least significant bit is set to 1, otherwise {@code false}.
     */
    public boolean isBip141() {
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
     * Calculates the difficulty of the proof-of-work required for a block.
     * The difficulty is derived by comparing the minimum target value (maximum difficulty)
     * to the current proof-of-work target value.
     *
     * @return an {@code Int} representing the difficulty of the block's proof-of-work.
     */
    public Int difficulty() {
        var lowest = Hex.parse("ffff").mul(Int.parse(256).pow(Hex.parse("1d").sub(Int.parse(3))));
        return lowest.div(target());
    }

    /**
     * Validates the proof-of-work for the block by checking whether the hash
     * of the serialized block is less than the target value. This ensures that
     * the block meets the required computational difficulty.
     *
     * @return a {@code boolean} indicating whether the block satisfies the proof-of-work requirement;
     *         {@code true} if the block's proof-of-work is valid, otherwise {@code false}.
     */
    public boolean checkProofOfWork() {
        var hash = Hash.hash256(serialize());
        var proof = Helper.littleEndianToInt(hash);
        return proof.lt(target());
    }

    /**
     * Validates the Merkle root of the block by comparing the computed Merkle root
     * from the list of transaction hashes against the stored Merkle root in the block.
     * <p>
     * The transaction hashes are processed in reverse order to match the expected
     * endian format before computing the Merkle root.
     *
     * @return a {@code boolean} indicating whether the computed Merkle root matches
     *         the stored Merkle root in the block; {@code true} if the Merkle root
     *         is valid, otherwise {@code false}.
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
