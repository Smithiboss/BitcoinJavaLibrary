package org.example.ecc;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntTest {

    @Test
    public void testToBytesLittleEndian() {
        Int n = Int.parse(1);
        byte[] want = new byte[]{0x01, 0x00, 0x00, 0x00};
        assertArrayEquals(want, n.toBytesLittleEndian(4));
    }

}
