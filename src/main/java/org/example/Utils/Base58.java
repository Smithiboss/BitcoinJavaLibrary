package org.example.Utils;

import org.example.ecc.Hex;
import org.example.ecc.Int;

import java.math.BigInteger;
import java.util.Arrays;

public class Base58 {

    private final static String BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /**
     * Takes a {@code byte} array and returns the Base58 string
     * @param input a {@code byte} array
     * @return a {@link String} object
     */
    public static String encode(byte[] input) {
        // Counts all leading 0 bytes
        int zeroCount = 0;
        for (byte b : input) {
            if (b == 0) {
                zeroCount++;
            } else {
                break;
            }
        }

        Int num = Hex.parse(input);
        String prefix = "1".repeat(zeroCount);

        // Builds the string
        StringBuilder result = new StringBuilder();
        // Loops over num and builds the Base58 string
        while (num.gt(Int.parse(0))) {
            BigInteger[] divMod = num.getBigInteger().divideAndRemainder(BigInteger.valueOf(58));
            num = Int.parse(divMod[0]);
            result.insert(0, BASE58_ALPHABET.charAt(divMod[1].intValue()));
        }

        return prefix + result;
    }

    public static byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Encodes the address checksum
     * @param input a {@code byte} array
     * @return a {@link String} object
     */
    public static String encodeChecksum(byte[] input) {
        // Does hash256 on the input array and takes the first 4 bytes
        byte[] checksum = Arrays.copyOfRange(Hash.hash256(input), 0, 4);
        // Encodes the combination input + checksum in Base58
        return Base58.encode(Bytes.concat(input, checksum));
    }
}
