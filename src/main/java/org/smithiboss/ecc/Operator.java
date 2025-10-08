package org.smithiboss.ecc;

public interface Operator {

    boolean eq(Operator otherPoint);

    boolean ne(Operator otherPoint);

    Operator add(Operator otherPoint);

    Operator sub(Operator otherPoint);

    Operator mul(Operator otherPoint);

    Operator mul(int coefficient);

    Operator pow(Int exponent);

    Operator div(Operator otherPoint);

    Operator mod(Int divisor);

    Operator powMod(Int exponent, Int divisor);

}
