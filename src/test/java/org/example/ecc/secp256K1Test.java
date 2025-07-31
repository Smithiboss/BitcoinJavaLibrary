package org.example.ecc;

import org.example.Chapter3.SHA256Hasher;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HexFormat;

import static org.junit.Assert.*;

public class secp256K1Test {

    @Test
    public void verifyGeneratorPoint() {
        String gxHex = "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
        String gyHex = "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8";
        String pHex = "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f";

        BigInteger gX = new BigInteger(gxHex, 16);
        BigInteger gY = new BigInteger(gyHex, 16);
        BigInteger p = new BigInteger(pHex, 16);

        assertEquals(gY.pow(2).mod(p), (gX.pow(3).add(BigInteger.valueOf(7))).mod(p));
    }

    @Test
    public void verifyPointAtInfinity() {
        BigInteger gX = new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
        BigInteger gY = new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16);
        BigInteger p = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);
        BigInteger n = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

        FieldElement x = new FieldElement(gX, p);
        FieldElement y = new FieldElement(gY, p);
        FieldElement seven = new FieldElement(BigInteger.valueOf(7), p);
        FieldElement zero = new FieldElement(BigInteger.ZERO, p);
        

        Point G = new Point(x, y, zero, seven);
        Point res = new Point(null, null, zero, seven);

        assertEquals(res, G.rMul(n));
    }

    @Test
    public void testGeneratorPointIsOnCurve() {
        BigInteger gx = new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
        BigInteger gy = new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16);
        BigInteger p = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);

        FieldElement x = new FieldElement(gx, p);
        FieldElement y = new FieldElement(gy, p);
        FieldElement a = new FieldElement(BigInteger.ZERO, p);
        FieldElement b = new FieldElement(BigInteger.valueOf(7), p);

        assertEquals(y.pow(BigInteger.TWO), x.pow(BigInteger.valueOf(3)).add(a.mul(x)).add(b));
    }

    @Test
    public void testGeneratorPointIsOnCurve2() {
        S256Point G = new S256Point(
                "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798",
                "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");

        BigInteger n = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

        S256Point infinity = new S256Point(null, null);

        assertEquals(infinity, G.rMul(n));

    }

    @Test
    public void testVerification() {
        S256Point G = new S256Point(
                "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798",
                "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
        BigInteger n = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
        BigInteger z = new BigInteger("bc62d4b80d9e36da29c16c5d4d9f11731f36052c72401a76c23c0fb5a9b74423", 16);
        BigInteger r = new BigInteger("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6", 16);
        BigInteger s = new BigInteger("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec", 16);

        S256Point point = new S256Point("04519fac3d910ca7e7138f7013706f619fa8f033e6ec6e09370ea38cee6a7574",
                "82b51eab8c27c66e26c858a079bcdf4f1ada34cec420cafc7eac1a42216fb6c4");

        BigInteger s_inv = s.modPow(n.subtract(BigInteger.valueOf(2)), n);
        BigInteger u = z.multiply(s_inv).mod(n);
        BigInteger v = r.multiply(s_inv).mod(n);

        assertEquals(G.rMul(u).add(point.rMul(v)).getX().getNum(), r);
    }

    @Test
    public void testVerificationMethod() {
        Signature sig = new Signature(new BigInteger("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6", 16),
                new BigInteger("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec", 16));
        S256Point point = new S256Point("04519fac3d910ca7e7138f7013706f619fa8f033e6ec6e09370ea38cee6a7574",
                "82b51eab8c27c66e26c858a079bcdf4f1ada34cec420cafc7eac1a42216fb6c4");

        assertTrue(point.verify(new BigInteger("bc62d4b80d9e36da29c16c5d4d9f11731f36052c72401a76c23c0fb5a9b74423", 16), sig
        ));
    }

    @Test
    public void testSignAndVerify() {
        BigInteger e = new BigInteger(SHA256Hasher.hash("my secret"), 16);
        BigInteger z = new BigInteger(SHA256Hasher.hash("my message"), 16);
        PrivateKey privateKey = new PrivateKey(e);
        Signature sig = privateKey.sign(z);
        S256Point point = S256Point.G.rMul(e);

        assertTrue(point.verify(z, sig));
    }

    @Test
    public void testSec() {
        PrivateKey priv = new PrivateKey(BigInteger.valueOf(5001
        ));
        PrivateKey priv2 = new PrivateKey(new BigInteger("deadbeef54321", 16));


        assertEquals("0357a4f368868a8a6d572991e484e664810ff14c05c0fa023275251151fe0e53d1", HexFormat.of().formatHex(priv.getPublicKey().sec()));
        assertEquals("0296be5b1292f6c856b3c5654e886fc13511462059089cdf9c479623bfcbe77690", HexFormat.of().formatHex(priv2.getPublicKey().sec()));
    }

    @Test
    public void testDerFormat() {
        BigInteger r = new BigInteger("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6", 16);
        BigInteger s = new BigInteger("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec", 16);
        Signature sig = new Signature(r, s);

        assertEquals("3045022037206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c60221008ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec",
                HexFormat.of().formatHex(sig.der()));
    }

}
