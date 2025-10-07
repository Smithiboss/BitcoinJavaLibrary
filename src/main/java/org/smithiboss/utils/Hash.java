package org.smithiboss.utils;

import org.smithiboss.ecc.Int;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    public static final Int SIGHASH_ALL = Int.parse(1);

    /**
     * Hashes the given {@code byte} array with SHA-1
     *
     * @param bytes an array of {@link byte} objects
     * @return an array of {@link byte} objects
     */
    public static byte[] sha1(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

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
