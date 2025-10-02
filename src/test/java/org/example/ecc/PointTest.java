package org.example.ecc;

import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {

    @Test
    public void testPointConstructor() {
    }

    @Test
    public void testPointAddition() {
        Int prime = Int.parse("223");
        FieldElement a = new FieldElement(Int.parse("0"), prime);
        FieldElement b = new FieldElement(Int.parse("7"), prime);
        FieldElement x1 = new FieldElement(Int.parse("192"), prime);
        FieldElement y1 = new FieldElement(Int.parse("105"), prime);
        FieldElement x2 = new FieldElement(Int.parse("17"), prime);
        FieldElement y2 = new FieldElement(Int.parse("56"), prime);

        Point p1 = new Point(x1, y1, a, b);
        Point p2 = new Point(x2, y2, a, b);

        Point res = new Point(new FieldElement(Int.parse("170"), prime), new FieldElement(Int.parse("142"), prime), a, b);
        assertTrue(res.eq(p1.add(p2)));
    }

    @Test
    public void testPointScalarMultiplication() {
        Int prime = Int.parse("223");

        FieldElement a = new FieldElement(Int.parse("0"), prime);
        FieldElement b = new FieldElement(Int.parse("7"), prime);
        FieldElement x1 = new FieldElement(Int.parse("15"), prime);
        FieldElement y1 = new FieldElement(Int.parse("86"), prime);

        Point p1 = new Point(x1, y1, a, b);
        Point res = new Point(null, null, a, b);
        assertTrue(res.eq(p1.mul(Int.parse("7"))
        ));
    }

}
