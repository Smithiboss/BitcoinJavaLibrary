package org.example.Chapter3;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Hasher {

    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Erster Hash
            byte[] firstHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Zweiter Hash
            byte[] secondHash = digest.digest(firstHash);

            // In Hex-String umwandeln
            StringBuilder hexString = new StringBuilder();
            for (byte b : secondHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 nicht verfügbar", e);
        }
    }
}
