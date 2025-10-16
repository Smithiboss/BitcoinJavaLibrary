package org.smithiboss.network;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleNodeTest {

    @Test
    public void testHandshake() {
        var node = new SimpleNode("142.132.210.177", 8333, false, null);
        assertNotNull(node.handshake());
        node.close();
    }

}
