package org.example.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    /**
     * Applies SHA-256 on given {@code byte} array
     * @param bytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies two rounds of SHA-256 on given {@code byte} array
     * @param bytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] hash256(byte[] bytes) {
        return sha256(sha256(bytes));
    }

    /**
     * Applies SHA-256 followed by Ripemd160 on given {@code byte} array
     * @param bytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] hash160(byte[] bytes) {
        return ripemd160(sha256(bytes));
    }

    /**
     * Applies Ripemd160 on given {@code byte} array
     * @param bytes a {@code byte} array
     * @return a {@code byte} array
     */
    public static byte[] ripemd160(byte[] bytes) {
        return Ripemd160.getHash(bytes);
    }


}
