package org.smithiboss.script;

import org.smithiboss.ecc.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

public class ScriptTest {

    @Test
    public void testParse() {
        var scriptPubKey = new ByteArrayInputStream(Hex.parse("6a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937").toBytes());
        var script = Script.parse(scriptPubKey);
        var want = Hex.parse("304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a71601");
        assertEquals(want, Hex.parse(script.getCmds().getFirst().getElement()));
        var want2 = Hex.parse("035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937");
        assertEquals(want2, Hex.parse(script.getCmds().get(1).getElement()));
    }

    @Test
    public void testSerialize() {
        var want = Hex.parse("6a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937");
        var scriptPubkey = new ByteArrayInputStream(want.toBytes());
        var script = Script.parse(scriptPubkey);
        assertEquals(want, Hex.parse(script.serialize()));
    }

}
