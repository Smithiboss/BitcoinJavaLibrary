package org.smithiboss.ecc;

import java.math.BigInteger;

public class S256Field extends FieldElement {

    public final static Int P = new Int(BigInteger.valueOf(2).pow(256).subtract(BigInteger.valueOf(2)
            .pow(32)).subtract(BigInteger.valueOf(977)));

    public S256Field(Int num) {
        super(num, P);
    }

    /**
     * Adds the current {@link S256Field} instance to another {@link Operator} instance.
     *
     * @param otherOperator the {@link Operator} instance to be added to the current instance
     * @return a new {@link S256Field} instance representing the result of the addition
     */
    @Override
    public S256Field add(Operator otherOperator) {
        return new S256Field(super.add(otherOperator).getNum());
    }

    /**
     * Raises the current {@link S256Field} element to the power specified by the given divisor.
     *
     * @param divisor an {@link Int} representing the power to which the current element should be raised
     * @return a new {@link S256Field} instance representing the result of the exponentiation
     */
    @Override
    public S256Field pow(Int divisor) {
        return new S256Field(super.pow(divisor).getNum());
    }

    /**
     * Computes the square root of the current {@code S256Field} instance using modular arithmetic.
     *
     * @return a new {@code S256Field} instance representing the square root of the current value.
     */
    public S256Field sqrt() {
        return new S256Field(this.pow(P.add(Int.parse(1)).div(Int.parse(4))).getNum());
    }

    /**
     * Returns a string representation of the current {@code S256Field} instance.
     *
     * @return a {@code String} representing this {@code S256Field} instance, typically including its numeric value.
     */
    @Override
    public String toString() {
        return null;
    }
}
