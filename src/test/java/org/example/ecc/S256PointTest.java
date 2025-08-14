package org.example.ecc;

import org.junit.Test;

import static org.junit.Assert.*;

public class S256PointTest {

    @Test
    public void testVerify() {
        var px = Hex.parse("887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e4da568744d06c");
        var py = Hex.parse("61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34");
        var p = new S256Point(new S256Field(px), new S256Field(py));

        var z = Hex.parse("ec208baa0fc1c19f708a9ca96fdeff3ac3f230bb4a7ba4aede4942ad003c0f60");
        var r = Hex.parse("ac8d1c87e51d0d441be8b3dd5b05c8795b48875dffe00b7ffcfac23010d3a395");
        var s = Hex.parse("68342ceff8935ededd102dd876ffd6ba72d6a427a3edb13d26eb0781cb423c4");
        assertTrue(p.verify(z, new Signature(r, s)));
    }

}
