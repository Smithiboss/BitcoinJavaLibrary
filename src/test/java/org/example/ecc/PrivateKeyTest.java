package org.example.ecc;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class PrivateKeyTest {

    @Test
    public void testWifFormat() {
        PrivateKey privateKey = new PrivateKey(new BigInteger("54321deadbeef", 16));
        assertEquals("KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgiuQJv1h8Ytr2S53a", privateKey.wif(true, false));
    }

}
