package org.smithiboss.ecc;

import org.smithiboss.utils.Base58;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Hash;

import java.util.Arrays;

public class S256Point extends Point {

    public final static Int A = Int.parse(0);
    public final static Int B = Int.parse(7);
    public final static Int N = Hex.parse("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141");
    public final static Int GX = Hex.parse("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
    public final static Int GY = Hex.parse("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
    public final static S256Point G = new S256Point(new S256Field(GX), new S256Field(GY));

    public S256Point(S256Field x, S256Field y) {
        super(x, y, new S256Field(A), new S256Field(B));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S256Point add(Point other) {
        Point point = super.add(other);
        if (point.getX() == null) {
            return null;
        }
        var x = new S256Field(getNum(point.getX()));
        var y = new S256Field(getNum(point.getY()));
        return new S256Point(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S256Point mul(Int coefficient) {
        Int coef = coefficient.mod(N);
        Point rawPoint = super.mul(coef);
        if (rawPoint.getX() == null && rawPoint.getY() == null) {
            return new S256Point(null, null);
        } else {
            var x = new S256Field(getNum(rawPoint.getX()));
            var y = new S256Field(getNum(rawPoint.getY()));
            return new S256Point(x, y);
        }
    }

    /**
     * Verifies a digital signature for a given message hash using elliptic curve cryptography.
     * The method checks whether the given signature is valid for the input message hash.
     *
     * @param z the message hash to be verified
     * @param sig the digital signature that needs to be verified
     * @return true if the signature is valid for the given message hash, otherwise false
     */
    public boolean verify(Int z, Signature sig) {
        // Calculate 1/s with Fermat's little theorem, since N is prime
        Int sInv = sig.s().modPow(N.sub(Int.parse(2)), N);
        // Calculate u = z * 1/s  (z/s)
        Int u = z.mul(sInv).mod(N);
        // Calculate v = r * 1/s  (r/s)
        Int v = sig.r().mul(sInv).mod(N);
        // Calculate uG + vP = (x, y) and check if x = sig.x
        var total = G.mul(u).add(this.mul(v));
        return ((S256Field) total.getX()).num.eq(sig.r());
    }

    /**
     * Returns the SEC format of a {@link S256Point}
     *
     * @return byte array
     */
    public byte[] sec(boolean compressed) {
        // Get bytes for x and y coordinates and format to 32 bytes
        byte[] xBytes = ((S256Field) getX()).getNum().toBytes(32);
        byte[] yBytes = ((S256Field) getY()).getNum().toBytes(32);
        if (compressed) {
            // 0x02 if y-coordinate is even, else 0x03
            byte prefix = ((S256Field) getY()).getNum().mod(Int.parse(2)).eq(Int.parse(0)) ? (byte) 0x02 : (byte) 0x03;
            return Bytes.concat(new byte[]{prefix}, xBytes);
        } else {
            // 0x04 if uncompressed
            return Bytes.concat(Bytes.concat(new byte[]{0x04}, xBytes), yBytes);
        }
    }

    /**
     * Returns a {@link S256Point} object from SEC binary
     *
     * @param secBin byte array
     * @return {@link S256Point}
     */
    public static S256Point parse(byte[] secBin) {
        // Uncompressed
        if (secBin[0] == 4) {
            var x = Hex.parse(Arrays.copyOfRange(secBin, 1, 33));
            var y = Hex.parse(Arrays.copyOfRange(secBin, 33, 65));
            return new S256Point(new S256Field(x), new S256Field(y));
        // Compressed
        } else {
            S256Field evenBeta;
            S256Field oddBeta;
            // Read all bytes except marker bytes
            var x = new S256Field(Hex.parse((Arrays.copyOfRange(secBin, 1, 33))));
            // Right side of equation: y^2 = x^3 +7
            var alpha = x.pow(Int.parse(3)).add(new S256Field(B));
            // Solve for left side
            var beta = alpha.sqrt();
            if (beta.getNum().mod(Int.parse(2)).eq(Int.parse(0))) {
                evenBeta = beta;
                oddBeta = new S256Field(S256Field.P.sub(beta.getNum()));
            } else {
                evenBeta = new S256Field(S256Field.P.sub(beta.getNum()));
                oddBeta = beta;
            }
            boolean isEven = secBin[0] == 2;
            if (isEven) {
                return new S256Point(x, evenBeta);
            } else {
                return new S256Point(x, oddBeta);
            }
        }
    }

    /**
     * Performs a hash160 on the SEC format
     * @return byte array
     */
    public byte[] hash160(boolean compressed) {
        return Hash.hash160(this.sec(compressed));
    }

    /**
     * Generates the Base58-encoded address for the {@link S256Point} object by hashing its SEC format
     * and concatenating a network-specific prefix with the resulting hash160 digest, followed by a checksum.
     *
     * @param compressed a {@code boolean} indicating whether to use the compressed SEC format
     * @param testnet a {@code boolean} indicating whether the address is for the testnet (true) or mainnet (false)
     * @return a {@link String} representing the Base58-encoded address
     */
    public String address(boolean compressed, boolean testnet) {
        // Compute hash160 of SEC format
        byte[] h160 = this.hash160(compressed);
        // Load prefix depending on testnet
        byte prefix = (byte) (testnet ? 0x6f : 0x00);
        return Base58.encodeChecksum(Bytes.concat(new byte[]{prefix}, h160));
    }


}
