package org.example.ecc;

import org.example.Utils.Bytes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class Int {

    private final BigInteger bigInt;
    private final int bigIntLength;

    public Int(BigInteger bigInt) {
        this.bigInt = bigInt;
        this.bigIntLength = bigInt.bitLength();
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
     * Equals
     * @param i a {@link Int} object
     * @return a boolean
     */
    public boolean eq(Int i) {
        if (i == null) return false;
        return bigInt.equals(i.bigInt);
    }

    /**
     * Not equals
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
     * Mod pow
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
     * {@link Int} to byte array
     * @param length int
     * @return byte array
     */
    public byte[] toBytes(int length) {
        byte[] bytes = bigInt.toByteArray();
        int len = bytes.length;
        if (len == length) return bytes;
        else if (len == length + 1) return Arrays.copyOfRange(bytes, 1, length + 1);
        else if (len < length) {
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
        return Bytes.reverseOrder(toBytesLittleEndian(bigIntLength));
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
     * Copy of {@link BigInteger} compareTo
     * @param i a {@link Int} object
     * @return int
     */
    public int compareTo(Int i) {
        return bigInt.compareTo(i.bigInt);
    }

    @Override
    public String toString() {
        return bigInt.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        else if (o instanceof Int) {
            return this.eq((Int) o);
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bigInt);
    }

    /**
     * To hex
     *
     * @return a {@link Hex} object
     */
    public Hex toHex() {
        return Hex.parse(this.bigInt.toByteArray());
    }

    /**
     * Get BigInteger
     * @return {@link BigInteger}
     */
    public BigInteger getBigInteger() {
        return bigInt;
    }

    /**
     * Get long value
     * @return long
     */
    public long longValue() {
        return bigInt.longValue();
    }

    /**
     * Get int value
     * @return int
     */
    public int intValue() {
        return bigInt.intValue();
    }
}
