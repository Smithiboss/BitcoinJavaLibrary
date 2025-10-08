package org.smithiboss.ecc;

import java.math.BigInteger;

public class S256Field extends FieldElement {

    public final static Int P = new Int(BigInteger.valueOf(2).pow(256).subtract(BigInteger.valueOf(2)
            .pow(32)).subtract(BigInteger.valueOf(977)));

    public S256Field(Int num) {
        super(num, P);
    }

    @Override
    public S256Field add(Operator otherOperator) {
        return new S256Field(super.add(otherOperator).getNum());
    }

    @Override
    public S256Field pow(Int divisor) {
        return new S256Field(super.pow(divisor).getNum());
    }

    @Override
    public String toString() {
        return null;
    }

    /**
     * square root
     * @return {@link FieldElement}
     */
    public S256Field sqrt() {
        return new S256Field(this.pow(P.add(Int.parse(1)).div(Int.parse(4))).getNum());
    }
}
