package org.example.ecc;

public interface Operators {

    boolean eq(Operators otherPoint);

    boolean ne(Operators otherPoint);

    Operators add(Operators otherPoint);

    Operators sub(Operators otherPoint);

    Operators mul(Operators otherPoint);

    Operators mul(int coefficient);

    Operators pow(Int exponent);

    Operators div(Operators otherPoint);

    Operators mod(Int divisor);

    Operators powMod(Int exponent, Int divisor);

}
