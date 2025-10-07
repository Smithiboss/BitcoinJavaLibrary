package org.example.block;

import org.example.ecc.Int;
import org.example.network.GenericMessage;
import org.example.utils.Bytes;
import org.example.utils.Helper;
import org.example.utils.Murmur3;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class BloomFilter {

    public static final int BIP37_CONSTANT =0xfba4c795;

    private final int size;
    private final byte[] bitField;
    private final int functionCount;
    private final int tweak;

    /**
     * Constructs a new BloomFilter object. The filter is initialized with the given size, function count and tweak.
     *
     * @param size a {@code int} object
     * @param functionCount a {@code int} object
     * @param tweak a {@code int} object
     */
    public BloomFilter(int size, int functionCount, int tweak) {
        this.size = size;
        this.bitField = Bytes.initFill(this.size * 8, (byte) 0);
        this.functionCount = functionCount;
        this.tweak = tweak;
    }

    /**
     * Add an item to the filter
     *
     * @param item an array of {@link byte} objects
     */
    public void add(byte[] item) {
        for (int i = 0; i < functionCount; i++) {
            // BIP0037 seed is i*BIP37_CONSTANT + tweak
            var seed = i * BIP37_CONSTANT + tweak;
            // get the murmur3 hash given that seed
            var hInt = Murmur3.murmur3(item, seed);
            var h = hInt & 0xffffffffL;
            // set the bit at the hash mod the bitfield size
            var bit = h % (size * 8L);
            // set the bit field at bit to be 1
            this.bitField[(int) bit] = 1;
        }
    }

    /**
     * Returns the filter as a byte array. The filter is returned as a bitfield
     *
     * @return an array of {@link byte} objects
     */
    public byte[] filterBytes() {
        return Bytes.bitFieldToBytes(this.bitField);
    }

    /**
     * Returns a filterload message with the current filter and the given flag
     * @param flag a {@link Int} object
     * @return a {@link GenericMessage} object
     */
    public GenericMessage filterload(Int flag) {
        flag = Objects.requireNonNullElse(flag, Int.parse(1));
        var payload = new ByteArrayOutputStream();
        // encode the length of the filter as varint
        payload.writeBytes(Helper.encodeVarInt(Int.parse(size)));
        // write the filter bytes to the payload
        payload.writeBytes(filterBytes());
        // write the number of functions to the payload
        payload.writeBytes(Int.parse(functionCount).toBytesLittleEndian(4));
        // write the tweak to the payload
        payload.writeBytes(Int.parse(tweak).toBytesLittleEndian(4));
        // write the flag to the payload
        payload.writeBytes(flag.toBytesLittleEndian(1));
        return new GenericMessage("filterload", payload.toByteArray());
    }

}
