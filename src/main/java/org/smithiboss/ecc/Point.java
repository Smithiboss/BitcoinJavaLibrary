package org.smithiboss.ecc;

import java.util.Objects;

public class Point {
    private final Operators x;
    private final Operators y;
    private final Operators a;
    private final Operators b;

    public Point(Operators x, Operators y, Operators a, Operators b) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;

        // Point at infinity
        if (x == null && y == null) return;

        // y^2 = x^3 + ax + b
        if (!(y.pow(Int.parse(2)).eq(x.pow(Int.parse(3)).add(a.mul(x)).add(b)))) {
            throw new ArithmeticException("Point is not on the curve.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (x == null) return "Point(infinity)";
        return "Point(" + x + ", " + y + ")_" + a + "_" + b;
    }

    public boolean eq(Point other) {
        if (other == null) {
            return false;
        }
        if (this.x == null && this.y == null) {
            return other.x == null && other.y == null && this.a.eq(other.a) && this.b.eq(other.b);
        } else if (other.x == null && other.y == null) {
            return false;
        } else if (this.x == null) {
            throw new IllegalStateException();
        } else {
            return this.x.eq(other.x) && this.y.eq(other.y) && this.a.eq(other.a) && this.b.eq(other.b);
        }
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
        if (this.a.eq(other.a) && this.b.eq(other.b)) {
            // this at infinity
            if (x == null) return other;
            // other at infinity
            if (other.x == null) return this;

            // P + (-P) = 0
            if (x.eq(other.x) && y.ne(other.y)) return new Point(null, null, a, b);

            // x1 != x2
            if (!x.eq(other.x)) {
                Operators s = other.y.sub(y).div(other.x.sub(x));
                Operators x3 = s.pow(Int.parse(2)).sub(x).sub(other.x);
                Operators y3 = s.mul(x.sub(x3)).sub(y);
                return new Point(x3, y3, a, b);
            };

            // P + P = 2P
            if (this.eq(other)) {
                Operators s = (x.pow(Int.parse(2)).mul(3).add(a))
                        .div(y.mul(2));
                Operators x3 = s.pow(Int.parse(2)).sub(x.mul(2));
                Operators y3 = s.mul(x.sub(x3)).sub(y);
                return new Point(x3, y3, a, b);
            }

            // Tangent at vertical line -> point at infinity
            if (this.eq(other) && y.eq(x.mul(0))) return new Point(null, null, a, b);
        }
        throw new ArithmeticException("Points are not on the same curve.");
    }

    /**
     * Multiply a {@link Point} with a coefficient
     * @param coefficient {@link Int}
     * @return {@link Point}
     */
    public Point mul(Int coefficient) {
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

    public static Int getNum(Operators o) {
        if (o instanceof FieldElement) {
            return ((FieldElement) o).getNum();
        } else {
            return null;
        }
    }

    /**
     * Get x
     * @return {@link FieldElement}
     */
    public Operators getX() {return x;}

    /**
     * Get y
     * @return {@link FieldElement}
     */
    public Operators getY() {return y;}

    /**
     * Get a
     * @return {@link FieldElement}
     */
    public Operators getA() {return a;}

    /**
     * Get b
     * @return {@link FieldElement}
     */
    public Operators getB() {return b;}

}
