package org.example.network;

import org.example.ecc.Hex;
import org.example.utils.Bytes;
import org.junit.Test;
import static org.junit.Assert.*;

public class GetHeadersMessageTest {

    @Test
    public void testSerialize() {
        var blockHex = Hex.parse("0000000000000000001237f46acddf58578a37e213d2a6edc4884a2fcad05ba3");
        var gh = new GetHeadersMessage(null, null, blockHex.toBytes(), null);
        assertArrayEquals(Bytes.hexStringToByteArray("7f11010001a35bd0ca2f4a88c4eda6d213e2378a5758dfcd6af437120000000000000000000000000000000000000000000000000000000000000000000000000000000000"),
                gh.serialize());
    }
}
