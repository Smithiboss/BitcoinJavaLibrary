package org.example.ecc;


import java.math.BigInteger;
import java.util.Objects;

public class Point {
    private final FieldElement x;
    private final FieldElement y;
    private final FieldElement a;
    private final FieldElement b;

    public Point(FieldElement x, FieldElement y, FieldElement a, FieldElement b) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;

        // Point at infinity
        if (x == null && y == null) return;

        // y^2 = x^3 + ax + b
        if (!(y.pow(Int.parse(2)).equals(x.pow(Int.parse(3)).add(a.mul(x)).add(b)))) {
            throw new ArithmeticException("Test");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (x == null) return "Point(infinity)";
        return "Point(" + x + ", " + y + ")_" + a + "_" + b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point other)) return false;
        return Objects.equals(x, other.x) &&
                Objects.equals(y, other.y) &&
                Objects.equals(a, other.a) &&
                Objects.equals(b, other.b);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {return Objects.hash(x, y, a, b);}

    public boolean notEquals(Point point) {
        return !this.equals(point);
    }

    /**
     * add
     * @param other {@link Point}
     * @return {@link Point}
     */
    public Point add(Point other) {
        if (Objects.equals(a.getNum(), other.a.getNum()) && Objects.equals(b.getNum(), other.b.getNum())) {
            // this at infinity
            if (x == null) return other;
            // other at infinity
            if (other.x == null) return this;

            // P + (-P) = 0
            if (x.equals(other.x) && !Objects.equals(y, other.y)) return new Point(null, null, a, b);

            // x1 != x2
            if (!x.equals(other.x)) {
                FieldElement s = other.y.sub(y).div(other.x.sub(x));
                FieldElement x3 = s.pow(Int.parse(2)).sub(x).sub(other.x);
                FieldElement y3 = s.mul(x.sub(x3)).sub(y);
                return new Point(x3, y3, a, b);
            };

            // P + P = 2P
            if (this.equals(other)) {
                FieldElement s = (x.pow(Int.parse(2)).mul(new FieldElement(Int.parse(3), x.getPrime())).add(a))
                        .div(y.mul(new FieldElement(Int.parse(2), x.getPrime())));
                FieldElement x3 = s.pow(Int.parse(2)).sub(x.mul(new FieldElement(Int.parse(2), x.getPrime())));
                FieldElement y3 = s.mul(x.sub(x3)).sub(y);
                return new Point(x3, y3, a, b);
            }

            // Tangent at vertical line -> point at infinity
            if (this.equals(other) && y.equals(new FieldElement(Int.parse(1), x.getPrime()))) return new Point(null, null, a, b);
        }
        throw new ArithmeticException("Points are not on the same curve.");
    }

    /**
     * Multiply a {@link Point} with a coefficient
     * @param coefficient {@link Int}
     * @return {@link Point}
     */
    public Point rMul(Int coefficient) {
        Point current = this;
        // Point at infinity
        Point result = new Point(null, null, a, b);

        Int k = coefficient;

        while (k.gt(Int.parse(0))) {
            // check if k is uneven
            if (k.getBigInteger().testBit(0)) {
                result = result.add(current);
            }
            // double current
            current = current.add(current);
            // equivalent to div by 2
            k = Int.parse(k.getBigInteger().shiftRight(1));
        }
        return result;
    }

    /**
     * Get x
     * @return {@link FieldElement}
     */
    public FieldElement getX() {return x;}

    /**
     * Get y
     * @return {@link FieldElement}
     */
    public FieldElement getY() {return y;}

    /**
     * Get a
     * @return {@link FieldElement}
     */
    public FieldElement getA() {return a;}

    /**
     * Get b
     * @return {@link FieldElement}
     */
    public FieldElement getB() {return b;}

}
