package org.example.network;

import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.example.utils.Bytes;
import org.junit.Test;
import static org.junit.Assert.*;

public class VersionMessageTest {

    @Test
    public void testSerialize() {
        var v = new VersionMessage(null, null, Int.parse(0), null, null,
                null, null, null, null,
                Hex.parse("0000000000000000").toBytes(), "/programmingbitcoin:0.1/",
                null, null);
        assertArrayEquals(Bytes.hexStringToByteArray("7f11010000000000000000000000000000000000000000000000000000000000000000000000ffff00000000208d000000000000000000000000000000000000ffff00000000208d0000000000000000182f70726f6772616d6d696e67626974636f696e3a302e312f0000000000"),
                v.serialize());
    }
}
