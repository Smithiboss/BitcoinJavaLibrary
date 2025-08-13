package org.example.Utils;

import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.junit.Test;

import static org.junit.Assert.*;

public class HelperTest {

    @Test
    public void testLittleEndianToInt() {
        Int h = Hex.parse("99c3980000000000");
        Int want = Int.parse(10011545);
        assertEquals(want, Hex.parse(Bytes.reverseOrder(h.toBytes())));
    }

}
