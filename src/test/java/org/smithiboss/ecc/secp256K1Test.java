package org.smithiboss.ecc;

import org.smithiboss.Chapter3.SHA256Hasher;
import org.junit.Test;

import java.util.HexFormat;

import static org.junit.Assert.*;

public class secp256K1Test {

    @Test
    public void verifyGeneratorPoint() {
        String gxHex = "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
        String gyHex = "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8";
        String pHex = "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f";

        var gX = Hex.parse(gxHex);
        var gY = Hex.parse(gyHex);
        var p = Hex.parse(pHex);

        assertEquals(gY.pow(Int.parse(2)).mod(p), (gX.pow(Int.parse(3)).add(Int.parse(7))).mod(p));
    }

    @Test
    public void verifyPointAtInfinity() {
        var gX = Hex.parse("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        var gY = Hex.parse("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
        var p = Hex.parse("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f");
        var n = Hex.parse("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141");

        FieldElement x = new FieldElement(gX, p);
        FieldElement y = new FieldElement(gY, p);
        FieldElement seven = new FieldElement(Int.parse(7), p);
        FieldElement zero = new FieldElement(Int.parse(0), p);

        Point G = new Point(x, y, zero, seven);
        Point res = new Point(null, null, zero, seven);

        assertTrue(res.eq(G.mul(n)));
    }

    @Test
    public void testGeneratorPointIsOnCurve() {
        var gx = Hex.parse("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        var gy = Hex.parse("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
        var p = Hex.parse("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f");

        FieldElement x = new FieldElement(gx, p);
        FieldElement y = new FieldElement(gy, p);
        FieldElement a = new FieldElement(Int.parse(0), p);
        FieldElement b = new FieldElement(Int.parse(7), p);

        assertTrue(y.pow(Int.parse(2)).eq(x.pow(Int.parse(3)).add(a.mul(x)).add(b)));
    }

    @Test
    public void testGeneratorPointIsOnCurve2() {
        S256Point G = new S256Point(
                new S256Field(Hex.parse("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798")),
                new S256Field(Hex.parse("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8")));

        var n = Hex.parse("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141");

        S256Point infinity = new S256Point(null, null);

        assertTrue(infinity.eq(G.mul(n)));

    }

    @Test
    public void testVerification() {
        S256Point G = new S256Point(
                new S256Field(Hex.parse("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798")),
                new S256Field(Hex.parse("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8")));
        var n = Hex.parse("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141");
        var z = Hex.parse("bc62d4b80d9e36da29c16c5d4d9f11731f36052c72401a76c23c0fb5a9b74423");
        var r = Hex.parse("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6");
        var s = Hex.parse("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec");

        S256Point point = new S256Point(new S256Field(Hex.parse("04519fac3d910ca7e7138f7013706f619fa8f033e6ec6e09370ea38cee6a7574")),
                new S256Field(Hex.parse("82b51eab8c27c66e26c858a079bcdf4f1ada34cec420cafc7eac1a42216fb6c4")));

        var s_inv = s.modPow(n.sub(Int.parse(2)), n);
        var u = z.mul(s_inv).mod(n);
        var v = r.mul(s_inv).mod(n);

        assertEquals(((S256Field) G.mul(u).add(point.mul(v)).getX()).getNum(), r);
    }

    @Test
    public void testVerificationMethod() {
        Signature sig = new Signature(Hex.parse("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6"),
                Hex.parse("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec"));
        S256Point point = new S256Point(new S256Field(Hex.parse("04519fac3d910ca7e7138f7013706f619fa8f033e6ec6e09370ea38cee6a7574")),
                new S256Field(Hex.parse("82b51eab8c27c66e26c858a079bcdf4f1ada34cec420cafc7eac1a42216fb6c4")));

        assertTrue(point.verify(Hex.parse("bc62d4b80d9e36da29c16c5d4d9f11731f36052c72401a76c23c0fb5a9b74423"), sig));
    }

    @Test
    public void testSignAndVerify() {
        var e = Hex.parse(SHA256Hasher.hash("my secret"));
        var z = Hex.parse(SHA256Hasher.hash("my message"));
        PrivateKey privateKey = new PrivateKey(e);
        Signature sig = privateKey.sign(z);
        S256Point point = S256Point.G.mul(e);

        assertTrue(point.verify(z, sig));
    }

    @Test
    public void testSec() {
        PrivateKey priv = new PrivateKey(Int.parse(5001));
        PrivateKey priv2 = new PrivateKey(Hex.parse("deadbeef54321"));

        assertEquals("0357a4f368868a8a6d572991e484e664810ff14c05c0fa023275251151fe0e53d1", HexFormat.of().formatHex(priv.getPublicKey().sec(true)));
        assertEquals("0296be5b1292f6c856b3c5654e886fc13511462059089cdf9c479623bfcbe77690", HexFormat.of().formatHex(priv2.getPublicKey().sec(true)));
    }

    @Test
    public void testDerFormat() {
        var r = Hex.parse("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6");
        var s = Hex.parse("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec");
        Signature sig = new Signature(r, s);

        assertEquals("3045022037206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c60221008ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec",
                HexFormat.of().formatHex(sig.der()));
    }

}
