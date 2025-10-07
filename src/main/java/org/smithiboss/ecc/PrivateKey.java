package org.smithiboss.ecc;

import org.smithiboss.utils.Base58;
import org.smithiboss.utils.Bytes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PrivateKey {

    private final Int secret;
    private final S256Point pubKey;

    public PrivateKey(Int secret) {
        this.secret = secret;
        this.pubKey = S256Point.G.mul(secret);
    }

    /**
     * Signs a given z
     * @param z a {@link Int} object
     * @return a {@link Signature} object
     */
    public Signature sign(Int z) {
        Int k;
        try {
            k = deterministicK(z);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        Int r = ((S256Field) S256Point.G.mul(k).getX()).getNum();
        Int kInv = k.modPow(S256Point.N.sub(Int.parse(2)), S256Point.N);
        Int s = (z.add(r.mul(this.secret))).mul(kInv).mod(S256Point.N);
        if (s.compareTo(S256Point.N.div(Int.parse(2))) > 0) {
            s = S256Point.N.sub(s);
        }
        return new Signature(r, s);
    }

    /**
     * Calculates a unique, deterministic k
     * <p>
     * Deterministic k generation standard specified in RFC6979     * <a href="https://tools.ietf.org/html/rfc6979"></a>
     * @param z The message
     * @return a {@link Int} object
     * @throws NoSuchAlgorithmException Algorithm not found
     * @throws InvalidKeyException Invalid hmac key
     */
    public Int deterministicK(Int z) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] k = Bytes.initFill(32, (byte) 0x00);
        byte[] v = Bytes.initFill(32, (byte) 0x01);

        if (z.gt(S256Point.N)) {
            z.sub(S256Point.N);
        }

        var zBytes = z.toBytes(32);
        var secretBytes = secret.toBytes(32);

        // Get sha256 hmac
        Mac hmac = Mac.getInstance("HmacSHA256");

        // Key: k
        // Message: v || 0x00 || secret || z
        hmac.init(new SecretKeySpec(k, "HmacSHA256"));
        k = hmac.doFinal(Bytes.concat(v, new byte[]{0x00}, secretBytes, zBytes));

        // Key: k
        // Message: v
        hmac.init(new SecretKeySpec(k, "HmacSHA256"));
        v = hmac.doFinal(v);

        // Key: k
        // Message: v || 0x01 || secret || z
        hmac.init(new SecretKeySpec(k, "HmacSHA256"));
        k = hmac.doFinal(Bytes.concat(v, new byte[]{0x01}, secretBytes, zBytes));

        // Key: k
        // Message: v
        hmac.init(new SecretKeySpec(k, "HmacSHA256"));
        v = hmac.doFinal(v);

        while (true) {
            hmac.init(new SecretKeySpec(k, "HmacSHA256"));
            v = hmac.doFinal(v);

            var candidate = Hex.parse(v);

            if (candidate.ge(Int.parse(1)) && candidate.le(S256Point.N)) {
                return candidate;
            }

            // Key: k
            // Message: v || 0x00
            hmac.init(new SecretKeySpec(k, "HmacSHA256"));
            k = hmac.doFinal(Bytes.concat(v, new byte[]{0x00}));

            // Key: k
            // Message: v
            hmac.init(new SecretKeySpec(k, "HmacSHA256"));
            v = hmac.doFinal(v);
        }

    }

    /**
     * Returns the WIF format of the private key
     * @param compressed {@code boolean} SEC
     * @param testnet {@code boolean} Testnet
     * @return a {@link String} object
     */
    public String wif(boolean compressed, boolean testnet) {
        byte[] secretBytes = secret.toBytes(32);
        // Set prefix to 0xef (testnet true) else 0x80
        byte prefix = (byte) (testnet ? 0xef : 0x80);

        // Return encoded Base58 checksum based on the compressed parameter
        if (compressed) {
            return Base58.encodeChecksum(Bytes.concat(new byte[]{prefix}, secretBytes, new byte[]{0x01}));
        } else {
            return Base58.encodeChecksum(Bytes.concat(new byte[]{prefix}, secretBytes));
        }
    }

    /**
     * Return the public key
     * @return a {@link S256Point} object
     */
    public S256Point getPublicKey() {return this.pubKey;}

}