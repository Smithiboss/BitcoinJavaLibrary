package org.example.utils;

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

        StringBuilder result = new StringBuilder();
        // loops over num and builds the Base58 string
        while (num.gt(Int.parse(0))) {
            // calculate the quotient and the remainder
            BigInteger[] divMod = num.getBigInteger().divideAndRemainder(BigInteger.valueOf(58));
            // num equals the quotient
            num = Int.parse(divMod[0]);
            // insert the correct BASE58 character by looking it up with the remainder.
            // will always be between 0 and 57 inclusive because of mod 58 earlier
            result.insert(0, BASE58_ALPHABET.charAt(divMod[1].intValue()));
        }

        return prefix + result;
    }

    /**
     * Get the number represented by Base58
     * @param s a {@link String} object
     * @return a {@link Int} object
     */
    public static Int decode(String s) {
        var num = Int.parse(0);

        for (int i = 0; i < s.length(); i++) {
            num = num.mul(Int.parse(58));
            num = num.add(Int.parse(BASE58_ALPHABET.indexOf(s.charAt(i))));
        }

        return num;
    }

    /**
     * Get 20-byte hash from address
     * @param address a {@link String} object
     * @return a {@code byte} array
     */
    public static byte[] decodeAddress(String address) {
        var num = decode(address);
        // get hex bytes
        var combined = num.toBytes(25);
        // get the checksum
        var checksum = Arrays.copyOfRange(combined, combined.length - 4, combined.length);
        // get everything without the checksum
        var hashInput = Arrays.copyOfRange(combined, 0, combined.length - 4);
        // perform hash256 on hashInput
        var hash = Hash.hash256(hashInput);
        var hashchecksum = Arrays.copyOfRange(hash, 0, 4);
        // check if checksum is correct
        if (Arrays.compare(hashchecksum, checksum) != 0) {
            throw new IllegalStateException("Invalid checksum");
        }
        // return only the 20-byte hash
        return Arrays.copyOfRange(combined, 1, combined.length - 4);
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
        // hashes input array with hash256 and takes the first 4 bytes
        byte[] checksum = Arrays.copyOfRange(Hash.hash256(input), 0, 4);
        // returns the encoded combination of input + checksum in Base58
        return Base58.encode(Bytes.concat(input, checksum));
    }

    /**
     * Returns the p2pkh address from a h160 hash
     * @param h160 a {@code byte} array
     * @param testnet a {@code boolean}
     * @return a {@link String} object
     */
    public static String h160ToP2pkhAddress(byte[] h160, boolean testnet) {
        byte[] prefix;
        if (testnet) {
            prefix = new byte[]{0x6f};
        } else {
            prefix = new byte[]{0x00};
        }
        return encodeChecksum(Bytes.concat(prefix, h160));
    }

    /**
     * Returns the p2sh address from a h160 hash
     * @param h160 a {@code byte} array
     * @param testnet a {@code boolean}
     * @return a {@link String} object
     */
    public static String h160ToP2shAddress(byte[] h160, boolean testnet) {
        byte[] prefix;
        if (testnet) {
            prefix = new byte[]{(byte) 0xc4};
        } else {
            prefix = new byte[]{0x05};
        }
        return encodeChecksum(Bytes.concat(prefix, h160));
    }
}
