package org.example.Utils;

import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

public class Helper {


    /**
     * Takes a {@code byte} array in little endian and computes the {@link Int} object
     * @param bytes a {@code byte} array
     * @return a {@link Int} object
     */
    public static Int littleEndianToInt(byte[] bytes) {
        byte[] reversed = Bytes.changeOrder(bytes);
        return Hex.parse(reversed);
    }

    public static byte[] bigIntToLittleEndian(BigInteger value, int byteLength) {
        byte[] temp = value.toByteArray();

        // Entferne führendes 0-Byte falls nötig
        if (temp.length > byteLength) {
            temp = Arrays.copyOfRange(temp, temp.length - byteLength, temp.length);
        }

        // Fülle auf gewünschte Länge mit 0
        byte[] result = new byte[byteLength];
        int copyStart = byteLength - temp.length;
        System.arraycopy(temp, 0, result, copyStart, temp.length);

        // Jetzt drehen für Little Endian
        for (int i = 0; i < byteLength / 2; i++) {
            byte tmp = result[i];
            result[i] = result[byteLength - 1 - i];
            result[byteLength - 1 - i] = tmp;
        }

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
     * @throws IOException Stream exception
     */
    public static Int readVarint(InputStream s) throws IOException {
        // read the byte
        int prefix = s.read();
        if (prefix == -1) throw new IOException("Stream is corrupted");

        // if the prefix is smaller than 253 (0xfd), it is just the integer
        if (prefix < 0xfd) return Int.parse(prefix);
        // 0xfd means the next two bytes are the number. The number is between 253 and 2^16-1
        else if (prefix == 0xfd) return littleEndianToInt(s.readNBytes(2));
        // 0xfe means the next four bytes are the number. The number is between 2^16 and 2^32-1
        else if (prefix == 0xfe) return littleEndianToInt(s.readNBytes(4));
        // 0xff means the next eight bytes are the number. The number is between 2^32 and 2^64-1
        else return littleEndianToInt(s.readNBytes(8));
    }

    /**
     * Encodes an integer as a varint. This is used to save space.
     * @param i a {@link Int} object
     * @return a {@code byte} array
     */
    public static byte[] encodeVarInt(Int i) {
        // if the number is smaller than 253, encode it as a single byte
        if (i.compareTo(Hex.parse("fd")) < 0) return i.toBytes();
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

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);

            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character at position " + i);
            }

            data[i / 2] = (byte) ((high << 4) + low);
        }

        return data;
    }

    /**
     * <p>maskString.</p>
     *
     * @param str a {@link java.lang.String} object
     * @param len an int
     * @return a {@link java.lang.String} object
     */
    public static String maskString(String str, int len) {
        return str.substring(0, len) + ":" + str.substring(str.length() - len);
    }

}
