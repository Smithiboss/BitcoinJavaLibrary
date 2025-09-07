package org.example.utils;

import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Helper {


    /**
     * Takes a {@code byte} array in little endian and computes the {@link Int} object
     * @param bytes a {@code byte} array
     * @return a {@link Int} object
     */
    public static Int littleEndianToInt(byte[] bytes) {
        byte[] reversed = Bytes.reverseOrder(bytes);
        return Hex.parse(reversed);
    }

    /**
     * Takes a {@link BigInteger} object and an {@code int} and returns the value as a {@code byte} array
     * in little endian with the length of the {@code byte} array matching the given {@code int}
     * @param value a {@link BigInteger} object
     * @param byteLength an {@code int}
     * @return a {@code byte} array
     */
    public static byte[] bigIntToLittleEndian(BigInteger value, int byteLength) {
        byte[] temp = value.toByteArray();

        // remove leading zero bytes if necessary
        if (temp.length > byteLength) {
            temp = Arrays.copyOfRange(temp, temp.length - byteLength, temp.length);
        }

        // fills the array with leading zeros in order to match byteLength
        byte[] result = new byte[byteLength];
        int copyStart = byteLength - temp.length;
        System.arraycopy(temp, 0, result, copyStart, temp.length);

        // reverse order for little endian
        Bytes.reverseOrder(result);

        return result;
    }

    public static byte[] readBytes(InputStream s, int n) throws IOException {
        byte[] buffer = new byte[n];
        int read = 0;
        while (read < n) {
            int r = s.read(buffer, read, n - read);
            if (r == -1) {
                throw new IOException("Stream ended too early");
            }
            read += r;
        }
        return buffer;
    }

    /**
     * Reads a variable integer from a stream
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
        else return Hex.parse(prefix);
    }

    /**
     * Encodes an integer as a varint. This is used to save space.
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
     * @param hash1 a {@code byte} array
     * @param hash2 a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] merkleParent(byte[] hash1, byte[] hash2) {
        return Hash.hash256(Bytes.concat(hash1, hash2));
    }

    /**
     * Computes the merkle parent hash
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
     * @param hashes  {@link List} of {@code byte} arrays
     * @return a {@code byte} array
     */
    public static byte[] merkleRoot(List<byte[]> hashes) {
        var currentLevel = hashes;

        while (currentLevel.size() > 1) currentLevel = merkleParentLevel(currentLevel);

        return currentLevel.getFirst();
    }

    /**
     * Masks given {@link String} object by given length
     *
     * @param str a {@link String} object
     * @param len an {@code int}
     * @return a {@link String} object
     */
    public static String maskString(String str, int len) {
        return str.substring(0, len) + ":" + str.substring(str.length() - len);
    }

    public static String zfill(int length, String bytes) {
        return String.format("%" + length + "s", bytes).replace(' ', '0');
    }

}
