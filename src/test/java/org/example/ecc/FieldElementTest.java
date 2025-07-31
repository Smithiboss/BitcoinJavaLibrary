package org.example.ecc;

import org.junit.Test;

import java.math.BigInteger;

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
        assertTrue(exception.getMessage().contains("not in field range"));
    }

    @Test
    public void testEqualsAndHashCode() {
        FieldElement a = new FieldElement(Int.parse(3), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(3), Int.parse(7));
        FieldElement c = new FieldElement(Int.parse(4), Int.parse(7));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    public void testToString() {
        FieldElement fe = new FieldElement(Int.parse(5), Int.parse(19));
        String expected = "FieldElement_19(5)";
        assertEquals(expected, fe.toString());
    }

    @Test
    public void testNotEqualsMethod() {
        FieldElement a = new FieldElement(Int.parse(2), Int.parse(7));
        FieldElement b = new FieldElement(Int.parse(2), Int.parse(7));
        FieldElement c = new FieldElement(Int.parse(3), Int.parse(7));

        assertFalse(a.ne(b));
        assertTrue(a.ne(c));
    }

    @Test
    public void testAddition() {
        FieldElement a = new FieldElement(BigInteger.valueOf(5), BigInteger.valueOf(7));
        FieldElement b = new FieldElement(BigInteger.valueOf(6), BigInteger.valueOf(7));
        FieldElement expected = new FieldElement(BigInteger.valueOf(4), BigInteger.valueOf(7));
        assertEquals(expected, a.add(b));
    }

    @Test
    public void testSubtraction() {
        FieldElement a = new FieldElement(BigInteger.valueOf(5), BigInteger.valueOf(7));
        FieldElement b = new FieldElement(BigInteger.valueOf(6), BigInteger.valueOf(7));
        FieldElement expected = new FieldElement(BigInteger.valueOf(6), BigInteger.valueOf(7));
        assertEquals(expected, a.sub(b));
    }

    @Test
    public void testMultiplication() {
        FieldElement a = new FieldElement(BigInteger.valueOf(5), BigInteger.valueOf(7));
        FieldElement b = new FieldElement(BigInteger.valueOf(6), BigInteger.valueOf(7));
        FieldElement expected = new FieldElement(BigInteger.valueOf(2), BigInteger.valueOf(7));
        assertEquals(expected, a.mul(b));
    }

    @Test
    public void testPower() {
        FieldElement a = new FieldElement(BigInteger.valueOf(3), BigInteger.valueOf(13));
        FieldElement expected = new FieldElement(BigInteger.valueOf(9), BigInteger.valueOf(13));
        assertEquals(expected, a.pow(BigInteger.valueOf(-4)));
    }

    @Test
    public void testDivision() {
        FieldElement a = new FieldElement(BigInteger.valueOf(2), BigInteger.valueOf(19));
        FieldElement b = new FieldElement(BigInteger.valueOf(7), BigInteger.valueOf(19));
        FieldElement expected = new FieldElement(BigInteger.valueOf(3), BigInteger.valueOf(19));
        assertEquals(expected, a.div(b));
    }
}
