package org.smithiboss.ecc;

import org.smithiboss.utils.Bytes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class Int {

    private final BigInteger bigInt;
    private final int bigIntLength;

    public Int(BigInteger bigInt) {
        this.bigInt = bigInt;
        this.bigIntLength = bigInt.toByteArray().length;
    }

    public Int(String s) {
        this.bigInt = new BigInteger(s);
        this.bigIntLength = s.length() / 2;
    }

    public Int(String s, int radix) {
        this.bigInt = new BigInteger(s, radix);
        this.bigIntLength = s.length() / 2;
    }


    public static Int parse(BigInteger bi) {
        return new Int(bi);
    }

    public static Int parse(String s) {
        return new Int(s);
    }

    public static Int parse(long l) {
        return new Int(String.valueOf(l));
    }

    public static Int parse(int i) {
        return new Int(String.valueOf(i));
    }

    /**
     * eq
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean eq(Int i) {
        if (i == null) return false;
        return bigInt.equals(i.bigInt);
    }

    /**
     * ne
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean ne(Int i) {
        return !eq(i);
    }

    /**
     * lt
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean lt(Int i) {
        if (i == null) return false;
        return bigInt.compareTo(i.bigInt) < 0;
    }

    /**
     * le
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean le(Int i) {
        if (i == null) return false;
        return bigInt.compareTo(i.bigInt) <= 0;
    }

    /**
     * gt
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean gt(Int i) {
        if (i == null) return false;
        return bigInt.compareTo(i.bigInt) > 0;
    }

    /**
     * ge
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean ge(Int i) {
        if (i == null) return false;
        return bigInt.compareTo(i.bigInt) >= 0;
    }

    /**
     * Add
     * @param i a {@link Int} object
     * @return a {@link Int} object
     */
    public Int add(Int i) {
        return Int.parse(bigInt.add(i.bigInt));
    }

    /**
     * Sub
     * @param i a {@link Int} object
     * @return a {@link Int} object
     */
    public Int sub(Int i) {
        return Int.parse(bigInt.subtract(i.bigInt));
    }

    /**
     * Mul
     * @param i a {@link Int} object
     * @return a {@link Int} object
     */
    public Int mul(Int i) {
        return Int.parse(bigInt.multiply(i.bigInt));
    }

    /**
     * Mul
     * @param coefficient an int
     * @return a {@link Int} object
     */
    public Int mul(int coefficient) {
        var result = Int.parse(0);
        for (int i = 0; i < coefficient; i++) {
            result = result.add(this);
        }
        return result;
    }

    /**
     * Pow
     * @param exponent a {@link Int} object
     * @return a {@link Int} object
     */
    public Int pow(Int exponent) {
        return Int.parse(bigInt.pow(exponent.bigInt.intValue()));
    }

    /**
     * Div
     * @param i a {@link Int} object
     * @return a {@link Int} object
     */
    public Int div(Int i) {
        return Int.parse(bigInt.divide(i.bigInt));
    }

    /**
     * Mod
     * @param divisor a {@link Int} object
     * @return a {@link Int} object
     */
    public Int mod(Int divisor) {
        return Int.parse(bigInt.mod(divisor.bigInt));
    }

    /**
     * Modpow
     * @param exponent a {@link Int} object
     * @param divisor a {@link Int} object
     * @return a {@link Int} object
     */
    public Int modPow(Int exponent, Int divisor) {
        return Int.parse(bigInt.modPow(exponent.bigInt, divisor.bigInt));
    }

    public byte[] toBytes() {
        return toBytes(bigIntLength);
    }

    /**
     * Converts the underlying integer value to a byte array of specified length.
     * If the byte array representation of the integer matches the specified length,
     * it is returned as is. If the representation is shorter, it is padded with leading zeros.
     * If the representation is longer, it is truncated. Throws an exception for unsupported cases.
     *
     * @param length the desired length of the resulting byte array
     * @return a byte array representation of the integer with the specified length
     * @throws IllegalStateException if the conversion cannot produce a valid result
     */
    public byte[] toBytes(int length) {
        byte[] bytes = bigInt.toByteArray();
        int len = bytes.length;
        if (len == length) return bytes;
        else if (len == length + 1) return Arrays.copyOfRange(bytes, 1, length + 1);
        else if (len < length) {
            // pad with leading zeros
            byte[] fill = Bytes.initFill(length - len, (byte) 0x00);
            return Bytes.concat(fill, bytes);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Bytes to little endian
     * @return byte array
     */
    public byte[] toBytesLittleEndian() {
        return Bytes.reverseOrder(toBytes(bigIntLength));
    }

    /**
     * Bytes to little endian
     * @param length int
     * @return byte array
     */
    public byte[] toBytesLittleEndian(int length) {
        return Bytes.reverseOrder(this.toBytes(length));
    }

    /**
     * Compares this {@link Int} object with the specified {@link Int} object for order.
     *
     * @param i the {@link Int} object to be compared
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object
     */
    public int compareTo(Int i) {
        return bigInt.compareTo(i.bigInt);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return bigInt.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        else if (o instanceof Int) {
            return this.eq((Int) o);
        } else return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(bigInt);
    }

    /**
     * Converts the current {@link Int} object to its {@link Hex} representation.
     *
     * @return a {@link Hex} object representing the hexadecimal value of the current integer.
     */
    public Hex toHex() {
        return Hex.parse(this.bigInt.toByteArray());
    }

    /**
     * Get the BigInteger
     * @return {@link BigInteger}
     */
    public BigInteger getBigInteger() {
        return bigInt;
    }

    /**
     * Get the long value
     * @return long
     */
    public long longValue() {
        return bigInt.longValue();
    }

    /**
     * Get the int value
     * @return int
     */
    public int intValue() {
        return bigInt.intValue();
    }
}
