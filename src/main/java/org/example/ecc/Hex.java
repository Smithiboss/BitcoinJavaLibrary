package org.example.ecc;

import java.math.BigInteger;

public class Hex extends Int{

    public Hex(String hex) {
        super(hex, 16);
    }

    private Hex(byte[] bytes) {
        super(new BigInteger(1, bytes));
    }

    public static Hex parse(String hex) {
        return new Hex(hex);
    }

    public static Hex parse(byte[] hex) {
        return new Hex(hex);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%x", super.getBigInteger()));
        if (builder.length() % 2 == 1) {
            builder.insert(0, "0");
        }
        return builder.toString();
    }

}
