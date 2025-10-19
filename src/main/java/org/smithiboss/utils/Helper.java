package org.smithiboss.utils;

import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Helper {


    /**
     * Takes a {@code byte} array in little endian and computes the {@link Int} object
     *
     * @param bytes a {@code byte} array
     * @return a {@link Int} object
     */
    public static Int littleEndianToInt(byte[] bytes) {
        byte[] reversed = Bytes.reverseOrder(bytes);
        return Hex.parse(reversed);
    }

    /**
     * Reads a variable integer from a stream
     *
     * @param s a {@link InputStream}
     * @return a {@link Int} object
     */
    public static Int readVarint(ByteArrayInputStream s) {
        // read the byte
        byte prefix = Bytes.read(s, 1)[0];

        // 0xfd means the next two bytes are the number. The number is between 253 and 2^16-1
        if (prefix == (byte) 0xfd) return littleEndianToInt(Bytes.read(s, 2));
        // 0xfe means the next four bytes are the number. The number is between 2^16 and 2^32-1
        else if (prefix == (byte) 0xfe) return littleEndianToInt(Bytes.read(s, 4));
        // 0xff means the next eight bytes are the number. The number is between 2^32 and 2^64-1
        else if (prefix == (byte) 0xff) return littleEndianToInt(Bytes.read(s, 8));
        // if the prefix is smaller than 253 (0xfd), it is just the integer
        else return Hex.parse(new byte[]{prefix});
    }

    /**
     * Encodes an integer as a varint. This is used to save space.
     *
     * @param i a {@link Int} object
     * @return a {@code byte} array
     */
    public static byte[] encodeVarInt(Int i) {
        // if the number is smaller than 253, encode it as a single byte
        if (i.lt(Hex.parse("fd"))) return i.toBytes(1);
        // if the number is between 253 and 2^16-1, start with 0xfd and then encode it in 2 bytes little endian
        else if (i.lt(Hex.parse("10000"))) {
            return Bytes.concat(new byte[]{(byte) 0xfd}, i.toBytesLittleEndian(2));
        // if the number is between 2^16 and 2^32-1, start with 0xfe and then encode it in 4 bytes little endian
        } else if (i.lt(Hex.parse("100000000"))) {
            return Bytes.concat(new byte[]{(byte) 0xfe}, i.toBytesLittleEndian(4));
        // if the number is between 2^32 and 2^64-1, start with 0xff and then encode it in 8 bytes little endian
        } else if (i.lt(Hex.parse("10000000000000000"))) {
            return Bytes.concat(new byte[]{(byte) 0xff}, i.toBytesLittleEndian(8));
        // if the number is above 2^64-1, it is too large
        } else {
            throw new IllegalArgumentException("Integer too large: " + i);
        }
    }

    /**
     * Calculates the merkle parent hash with the given child hashes
     *
     * @param hash1 a {@code byte} array
     * @param hash2 a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] merkleParent(byte[] hash1, byte[] hash2) {
        return Hash.hash256(Bytes.concat(hash1, hash2));
    }

    /**
     * Computes the merkle parent hash
     *
     * @param hashes a {@code byte} array
     * @return a {@link List} of {@code byte} arrays
     */
    public static List<byte[]> merkleParentLevel(List<byte[]> hashes) {
        if (hashes.size() == 1) throw new IllegalArgumentException("Cannot take a parent level with only 1 item");

        if (hashes.size() % 2 == 1) hashes.add(hashes.getLast());

        var parentLevel = new ArrayList<byte[]>();

        for (int i = 0; i < hashes.size(); i += 2) {
            var parent = merkleParent(hashes.get(i), hashes.get(i + 1));
            parentLevel.add(parent);
        }
        return parentLevel;
    }

    /**
     * Computes the merkle root of given hashes
     *
     * @param hashes  {@link List} of {@code byte} arrays
     * @return a {@code byte} array
     */
    public static byte[] merkleRoot(List<byte[]> hashes) {
        var currentLevel = hashes;

        while (currentLevel.size() > 1) currentLevel = merkleParentLevel(currentLevel);

        return currentLevel.getFirst();
    }

    /**
     * Masks a string by keeping a specified number of characters at the beginning and at the end
     * and replaces the rest with a colon.
     *
     * @param str the original string to be masked
     * @param len the number of characters to keep at both the beginning and end of the string
     * @return a new string with the middle portion replaced by a colon
     */
    public static String maskString(String str, int len) {
        return str.substring(0, len) + ":" + str.substring(str.length() - len);
    }

    public static String zfill(int length, String bytes) {
        return String.format("%" + length + "s", bytes).replace(' ', '0');
    }

    /**
     * Calculates the logarithm of a given number with a specified base.
     *
     * @param x the number for which the logarithm is to be calculated
     * @param base the base of the logarithm
     * @return the calculated logarithm of the number with the specified base
     */
    public static double log(double x, double base) {
        return Math.log(x) / Math.log(base);
    }

}
