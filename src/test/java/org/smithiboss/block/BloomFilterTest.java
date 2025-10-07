package org.smithiboss.block;

import org.smithiboss.ecc.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

public class BloomFilterTest {

    @Test
    public void testAdd() {
        var bf = new BloomFilter(10, 5, 99);
        var item = Hex.parse("Hello World".getBytes()).toBytes();
        bf.add(item);
        var expected = Hex.parse("0000000a080000000140").toBytes();
        assertArrayEquals(expected, bf.filterBytes());
        item = Hex.parse("Goodbye!".getBytes()).toBytes();
        bf.add(item);
        expected = Hex.parse("4000600a080000010940").toBytes();
        assertArrayEquals(expected, bf.filterBytes());
    }

    @Test
    public void testFilterload() {
        var bf = new BloomFilter(10, 5, 99);
        var item = Hex.parse("Hello World".getBytes()).toBytes();
        bf.add(item);
        item = Hex.parse("Goodbye!".getBytes()).toBytes();
        bf.add(item);
        var expected = Hex.parse("0a4000600a080000010940050000006300000001").toBytes();
        assertArrayEquals(expected, bf.filterload(null).serialize());
    }

}
