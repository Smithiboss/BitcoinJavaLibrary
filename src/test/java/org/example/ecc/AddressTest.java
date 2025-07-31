package org.example.ecc;

import org.junit.Test;

import java.math.BigInteger;

public class AddressTest {

    @Test
    public void testAddress() {
        PrivateKey privateKey = new PrivateKey(new BigInteger("123491872356756382918723649837259817234982371234234", 16));
        assertEquals("1F1Pn2y6pDb68E5nYJJeba4TLg2U7B6KF1", privateKey.getPublicKey().address(true));
    }

}
