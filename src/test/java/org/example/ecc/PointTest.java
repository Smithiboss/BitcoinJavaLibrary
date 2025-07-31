package org.example.ecc;

import org.junit.Test;

import java.math.BigInteger;

public class PointTest {

    @Test
    public void testPointConstructor() {
    }

    @Test
    public void testPointAddition() {
        BigInteger prime = new BigInteger("223");
        FieldElement a = new FieldElement(new BigInteger("0"), prime);
        FieldElement b = new FieldElement(new BigInteger("7"), prime);
        FieldElement x1 = new FieldElement(new BigInteger("192"), prime);
        FieldElement y1 = new FieldElement(new BigInteger("105"), prime);
        FieldElement x2 = new FieldElement(new BigInteger("17"), prime);
        FieldElement y2 = new FieldElement(new BigInteger("56"), prime);

        Point p1 = new Point(x1, y1, a, b);
        Point p2 = new Point(x2, y2, a, b);

        Point res = new Point(new FieldElement(new BigInteger("170"), prime), new FieldElement(new BigInteger("142"), prime), a, b);
        assertEquals(res, p1.add(p2));
    }

    @Test
    public void testPointScalarMultiplication() {
        BigInteger prime = new BigInteger("223");

        FieldElement a = new FieldElement(new BigInteger("0"), prime);
        FieldElement b = new FieldElement(new BigInteger("7"), prime);
        FieldElement x1 = new FieldElement(new BigInteger("15"), prime);
        FieldElement y1 = new FieldElement(new BigInteger("86"), prime);

        Point p1 = new Point(x1, y1, a, b);
        Point res = new Point(null, null, a, b);
        assertEquals(res, p1.rMul(new BigInteger("7")));
    }

}
