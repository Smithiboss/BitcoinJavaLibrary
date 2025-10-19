package org.smithiboss.ecc;

import java.math.BigInteger;
import java.util.Objects;
import java.util.logging.Logger;

public class FieldElement implements Operator {

    private final static Logger log = Logger.getLogger(FieldElement.class.getSimpleName());

    final Int num;
    private final Int prime;

    public FieldElement(Int num, Int prime) {
        this.num = num;
        this.prime = prime;

        // Check if the num is in range(0, prime - 1)
        if (num.compareTo(Int.parse(0)) < 0 || num.compareTo(prime) >= 0) {
            String error = String.format("Num %s not in the range from 0 to %s", num, prime.getBigInteger().subtract(BigInteger.ONE));
            log.severe(error);
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * eq
     * @param otherOperator a {@link Operator} instance
     * @return a {@code boolean}
     */
    @Override
    public boolean eq(Operator otherOperator) {
        FieldElement other = (FieldElement) otherOperator;
        if (other == null) {
            return false;
        }
        return this.num.eq(other.num) && this.prime.eq(other.prime);
    }

    /**
     * ne
     * @param otherOperator a {@link Operator} instance
     * @return a {@code boolean}
     */
    @Override
    public boolean ne(Operator otherOperator) {
        return !this.eq(otherOperator);
    }

    /**
     * add
     * @param otherOperator a {@link Operator} instance
     * @return a {@link FieldElement} object
     */
    @Override
    public FieldElement add(Operator otherOperator) {
        FieldElement other = (FieldElement) otherOperator;
        if (other == null || this.prime.ne(other.prime)) {
            String error = "Cannot add two numbers in different Fields";
            throw new IllegalArgumentException(error);
        }
        Int result = this.num.add(other.num).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * sub
     * @param otherOperator a {@link Operator} instance
     * @return a {@link FieldElement} object
     */
    @Override
    public FieldElement sub(Operator otherOperator) {
        FieldElement other = (FieldElement) otherOperator;
        if (other == null || this.prime.ne(other.prime)) {
            String error = "Cannot add two numbers in different Fields";
            throw new IllegalArgumentException(error);
        }
        Int result = this.num.sub(other.num).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * mul
     * @param otherOperator a {@link Operator} instance
     * @return a {@link FieldElement} object
     */
    @Override
    public FieldElement mul(Operator otherOperator) {
        FieldElement other = (FieldElement) otherOperator;
        if (other == null || this.prime.ne(other.prime)) {
            String error = "Cannot add two numbers in different Fields";
            throw new IllegalArgumentException(error);
        }
        Int result = this.num.mul(other.num).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * mul
     * @param coefficient a {@code int}
     * @return a {@link FieldElement} object
     */
    @Override
    public FieldElement mul(int coefficient) {
        var result = new FieldElement(Int.parse(0), this.prime);
        for (int i = 0; i < coefficient; i++) {
            result = result.add(this);
        }
        return result;
    }

    /**
     * pow
     * @param exponent a {@link Int} object
     * @return a {@link FieldElement} object
     */
    public FieldElement pow(Int exponent) {
        Int n = exponent.mod(prime.sub(Int.parse(1))); // Fermat’s little theorem
        Int result = this.num.modPow(n, prime);
        return new FieldElement(result, prime);
    }

    /**
     * div
     * @param otherOperator a {@link Operator} instance
     * @return a {@link FieldElement} object
     */
    public FieldElement div(Operator otherOperator) {
        FieldElement other = (FieldElement) otherOperator;
        if (other == null || this.prime.ne(other.prime)) {
            String error = "Cannot add two numbers in different Fields";
            throw new IllegalArgumentException(error);
        }
        Int inverse = other.num.modPow(prime.sub(Int.parse(2)), prime);
        Int result = this.num.mul(inverse).mod(prime);
        return new FieldElement(result, prime);
    }

    @Override
    public FieldElement mod(Int other) {
        throw new IllegalStateException();
    }

    @Override
    public FieldElement powMod(Int exponent, Int divisor) {
        throw new IllegalStateException();
    }

    /**
     * Returns the num
     * @return a {@link Int} object
     */
    public Int getNum() {return num;}

    /**
     * Returns the prime
     * @return a {@link Int} object
     */
    public Int getPrime() {return prime;}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "FieldElement_" + prime + "(" + num + ")";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {return Objects.hash(num, prime);}
}

