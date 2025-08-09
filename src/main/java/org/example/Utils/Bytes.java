package org.example.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Bytes {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private Bytes() {}

    /**
     * Fills a byte array with given filler byte
     * @param length int
     * @param filler byte
     * @return byte array
     */
    public static byte[] initFill(int length, byte filler) {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, filler);
        return bytes;
    }

    /**
     * Reverses the byte order
     * @param bytes byte array
     * @return byte array
     */
    public static byte[] reverseOrder(byte[] bytes) {
        byte[] result = Arrays.copyOf(bytes, bytes.length);
        for (int i = 0; i < result.length / 2; i++) {
            byte temp = result[i];
            result[i] = result[result.length - 1 - i];
            result[result.length - 1 - i] = temp;
        }
        return result;
    }

    /**
     * Concatenates any amount of {@code byte} arrays
     * @param arrays {@code byte} array
     * @return byte array
     */
    public static byte[] concat(byte[]... arrays) {
        int len = Arrays.stream(arrays).mapToInt(a -> a.length).sum();
        byte[] result = new byte[len];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    /**
     * Removes leading {@code bytes} "0x00" from given {@code byte} array
     * @param bytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] lStrip(byte[] bytes) {
        int start = 0;
        // Count number of 0x00 bytes
        while (start < bytes.length && bytes[start] == (byte) 0x00) {
            start++;
        }
        // Copy to new array excluding all leading 0x00 bytes
        byte[] result = new byte[bytes.length - start];
        System.arraycopy(bytes, start, result, 0, result.length);
        return result;
    }

    /**
     * Converts a {@code byte} array to a hex string
     * @param bytes a {@code byte} array
     * @return a {@link String} object
     */
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            // Get unsigned byte value
            int v = bytes[j] & 0xFF;
            // Get upper nibble and replace with hex char
            hexChars[j*2] = HEX_ARRAY[v >>> 4];
            // Get lower nibble and replace with hex char
            hexChars[j*2+1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Wrapper for readNBytes method from {@link ByteArrayInputStream}
     *
     * @param s a {@link ByteArrayInputStream}
     * @param len a {@code int}
     * @return a {@code byte} array
     */
    public static byte[] read(ByteArrayInputStream s, int len) {
        try {
            return s.readNBytes(len);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
