package org.example.ecc;

import java.math.BigInteger;
import java.util.Objects;
import java.util.logging.Logger;

public class FieldElement {

    private final static Logger log = Logger.getLogger(FieldElement.class.getSimpleName());

    private final Int num;
    private final Int prime;

    public FieldElement(Int num, Int prime) {
        this.num = num;
        this.prime = prime;

        // Check if num is in range(0, prime - 1)
        if (num.compareTo(Int.parse(0)) < 0 || num.compareTo(prime) >= 0) {
            String error = String.format("Num %s not in the range from 0 to %s", num, prime.getBigInteger().subtract(BigInteger.ONE));
            log.severe(error);
            throw new IllegalArgumentException(error);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldElement other)) return false;
        return num.equals(other.num) && prime.equals(other.prime);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "FieldElement_" + prime + "(" + num + ")";
    }

    /**
     * ne
     * @param other {@link FieldElement}
     * @return boolean
     */
    public boolean ne(FieldElement other) {
        return !equals(other);
    }

    /**
     * add
     * @param other {@link FieldElement}
     * @return {@link FieldElement}
     */
    public FieldElement add(FieldElement other) {
        checkSameField(other);
        Int result = this.num.add(other.num).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * sub
     * @param other {@link FieldElement}
     * @return {@link FieldElement}
     */
    public FieldElement sub(FieldElement other) {
        checkSameField(other);
        Int result = this.num.sub(other.num).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * mul
     * @param other {@link FieldElement}
     * @return {@link FieldElement}
     */
    public FieldElement mul(FieldElement other) {
        checkSameField(other);
        Int result = this.num.mul(other.num).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * pow
     * @param exponent {@link FieldElement}
     * @return {@link FieldElement}
     */
    public FieldElement pow(Int exponent) {
        Int n = exponent.mod(prime.sub(Int.parse(1))); // Fermat’s little theorem
        Int result = this.num.modPow(n, prime);
        return new FieldElement(result, prime);
    }

    /**
     * div
     * @param other {@link FieldElement}
     * @return {@link FieldElement}
     */
    public FieldElement div(FieldElement other) {
        checkSameField(other);
        Int inverse = other.num.modPow(prime.sub(Int.parse(2)), prime);
        Int result = this.num.mul(inverse).mod(prime);
        return new FieldElement(result, prime);
    }

    /**
     * Check if given {@link FieldElement} is in same field
     * @param other {@link FieldElement}
     */
    private void checkSameField(FieldElement other) {
        if (!this.prime.equals(other.prime)) {
            throw new IllegalArgumentException("Field mismatch: " + this.prime + " vs " + other.prime);
        }
    }

    /**
     * Returns num
     * @return a {@link Int} object
     */
    public Int getNum() {return num;}

    /**
     * Returns prime
     * @return a {@link Int} object
     */
    public Int getPrime() {return prime;}

    /** {@inheritDoc} */
    @Override
    public int hashCode() {return Objects.hash(num, prime);}
}

