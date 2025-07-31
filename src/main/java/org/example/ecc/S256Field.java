package org.example.ecc;

import java.math.BigInteger;

public class S256Field extends FieldElement {

    public final static Int P = new Int(BigInteger.valueOf(2).pow(256).subtract(BigInteger.valueOf(2)
            .pow(32)).subtract(BigInteger.valueOf(977)));

    public S256Field(Int num) {
        super(num, P);
    }

    @Override
    public String toString() {
        return null;
    }

    /**
     * square root
     * @return {@link FieldElement}
     */
    public FieldElement sqrt() {
        return this.pow(P.add(Int.parse(1)).div(Int.parse(4)));
    }
}
