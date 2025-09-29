package org.example.utils;

import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bytes {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static final Int TWO_WEEKS = Int.parse(60 * 60 * 24 * 7);

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
     * Returns an array of random bytes of length
     * @param length a {@code int}
     * @return a {@code byte} array
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * Remove trailing zero bytes
     * @param bytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] strip(byte[] bytes) {
        var bytesReversed = Bytes.reverseOrder(bytes);
        int i = 0;
        while(bytesReversed[i] == (byte) 0x00) {
            i++;
        }
        return Arrays.copyOfRange(bytes, 0, bytes.length - i);
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
     * Returns target from bits
     * @param bits a {@code byte} array
     * @return a {@link Int} object
     */
    public static Int bitsToTarget(byte[] bits) {
        // exponent is the last byte
        var exponent = Hex.parse(Arrays.copyOfRange(bits, bits.length - 1, bits.length));
        // coefficient are the first 3 bytes
        var coefficient = Helper.littleEndianToInt(Arrays.copyOfRange(bits, 0, bits.length - 1));
        // return coefficient * 256 ** (exponent - 3)
        return coefficient.mul(Int.parse(256).pow(exponent.sub(Int.parse(3))));
    }

    /**
     * Turns a target into bits
     * @param target a {@link Int} object
     * @return a {@code byte} array
     */
    public static byte[] targetToBits(Int target) {
        var rawBytes = target.toBytes(32);

        rawBytes = Bytes.lStrip(rawBytes);
        int exponent;
        byte[] coefficient;
        // if the first bit in the coefficient is 1, the bits field is negative
        if (((rawBytes[0] >> 8) & 1) == 1) {
            // since target is positive, shift everything over by 1 byte
            exponent = rawBytes.length + 1;
            coefficient = Bytes.concat(new byte[]{0x00}, Arrays.copyOfRange(rawBytes, 0, 2));
        } else {
            // otherwise, we can show the first 3 bytes
            // exponent is the number of digits in base-256
            exponent = rawBytes.length;
            // coefficient is the first 3 digits of the base-256 number
            coefficient = Arrays.copyOfRange(rawBytes, 0, 3);
        }
        return Bytes.concat(Bytes.reverseOrder(coefficient), new byte[]{(byte) exponent});
    }

    /**
     * Calculates the new target bits with a given 2016-block time differential and the previous target bits
     * @param previousBits a {@code byte} array
     * @param timeDifferential a {@link Int} object
     * @return a {@code byte} array
     */
    public static byte[] calculateNewBits(byte[] previousBits, Int timeDifferential) {
        // if time dif is greater than 8 weeks, set to 8 weeks
        if (timeDifferential.gt(TWO_WEEKS.mul(Int.parse(4)))) {
            timeDifferential =  TWO_WEEKS.mul(Int.parse(4));
        }
        // if time dif is less than half a week, set to half a week
        if (timeDifferential.lt(TWO_WEEKS.div(Int.parse(4)))) {
            timeDifferential =  TWO_WEEKS.div(Int.parse(4));
        }
        // calculate the new target
        var newTarget = Bytes.bitsToTarget(previousBits).mul(timeDifferential).div(TWO_WEEKS);
        // convert the new target to bits
        return targetToBits(newTarget);
    }

    /**
     * Converts a {@code byte} array to an array of bits
     * @param someBytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] bytesToBitField(byte[] someBytes) {
        List<Byte> flagBits = new ArrayList<>();

        for (byte b : someBytes) {
            for (int i = 0; i < 8; i++) {
                flagBits.add((byte) (b & 1));
                b >>= 1;
            }
        }

        var result = new byte[flagBits.size()];
        for (int i = 0; i < flagBits.size(); i++) {
            result[i] = (byte) flagBits.get(i);
        }

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
     *
     * @param hex
     * @return
     */
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
