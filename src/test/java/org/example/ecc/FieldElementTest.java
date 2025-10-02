package org.example.ecc;

import org.junit.Test;

import static org.junit.Assert.*;

public class FieldElementTest {

    @Test
    public void testConstructorValid() {
        FieldElement fe = new FieldElement(Int.parse(2), Int.parse(7));
        assertEquals(Int.parse(2), fe.getNum());
        assertEquals(Int.parse(7), fe.getPrime());
    }

    @Test
    public void testConstructorInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new FieldElement(Int.parse(8), Int.parse(7));
        });
        assertTrue(exception.getMessage().contains("not in the range"));
    }

    @Test
    public void testEqAndHashCode() {
        FieldElement a = new FieldElement(Int.parse(3), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(3), Int.parse(7));
        FieldElement c = new FieldElement(Int.parse(4), Int.parse(7));

        assertTrue(a.eq(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.eq(c));
    }

    @Test
    public void testToString() {
        FieldElement fe = new FieldElement(Int.parse(5), Int.parse(19));
        String expected = "FieldElement_19(5)";
        assertEquals(expected, fe.toString());
    }

    @Test
    public void testNeMethod() {
        FieldElement a = new FieldElement(Int.parse(2), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(2), Int.parse(7));
        FieldElement c = new FieldElement(Int.parse(3), Int.parse(7));

        assertFalse(a.ne(b));
        assertTrue(a.ne(c));
    }

    @Test
    public void testAddition() {
        FieldElement a = new FieldElement(Int.parse(5), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(6), Int.parse(7));
        FieldElement expected = new FieldElement(Int.parse(4), Int.parse(7));
        assertTrue(expected.eq(a.add(b)));
    }

    @Test
    public void testSubtraction() {
        FieldElement a = new FieldElement(Int.parse(5), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(6), Int.parse(7));
        FieldElement expected = new FieldElement(Int.parse(6), Int.parse(7));
        assertTrue(expected.eq(a.sub(b)));
    }

    @Test
    public void testMultiplication() {
        FieldElement a = new FieldElement(Int.parse(5), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(6), Int.parse(7));
        FieldElement expected = new FieldElement(Int.parse(2), Int.parse(7));
        assertTrue(expected.eq(a.mul(b)));
    }

    @Test
    public void testPower() {
        FieldElement a = new FieldElement(Int.parse(3), Int.parse(13));
        FieldElement expected = new FieldElement(Int.parse(9), Int.parse(13));
        assertTrue(expected.eq(a.pow(Int.parse(-4))));
    }

    @Test
    public void testDivision() {
        FieldElement a = new FieldElement(Int.parse(2), Int.parse(19));
        FieldElement b = new FieldElement(Int.parse(7), Int.parse(19));
        FieldElement expected = new FieldElement(Int.parse(3), Int.parse(19));
        assertTrue(expected.eq(a.div(b)));
    }
}
