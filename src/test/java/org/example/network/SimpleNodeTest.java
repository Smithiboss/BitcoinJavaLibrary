package org.example.network;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleNodeTest {

    @Test
    public void testHandshake() {
        var node = new SimpleNode("57.129.83.51", 8333, false, null);
        assertNotNull(node.handshake());
    }

}
