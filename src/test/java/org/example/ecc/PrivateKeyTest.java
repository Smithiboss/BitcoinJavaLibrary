package org.example.ecc;

import org.example.utils.Hash;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrivateKeyTest {

    @Test
    public void testWifFormat() {
        PrivateKey privateKey = new PrivateKey(Hex.parse("54321deadbeef"));
        assertEquals("KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgiuQJv1h8Ytr2S53a", privateKey.wif(true, false));
    }

    @Test
    public void testAddress() {
        PrivateKey privateKey = new PrivateKey(Int.parse(5002));
        assertEquals("mmTPbXQFxboEtNRkwfh6K51jvdtHLxGeMA", privateKey.getPublicKey().address(false, true));
    }

    @Test
    public void testSign() {
        PrivateKey privateKey = new PrivateKey(Int.parse(12345));
        var z = Hex.parse(Hash.hash256("Programming Bitcoin!".getBytes()));
        var sig = privateKey.sign(z);
        assertTrue(privateKey.getPublicKey().verify(z, sig));
    }

}
