package org.smithiboss.ecc;

public interface Operator {

    /**
     * Compares the current operator instance with another operator instance for equality.
     *
     * @param otherPoint the {@link Operator} instance to compare with the current instance
     * @return true if the current operator is equal to the specified operator, false otherwise
     */
    boolean eq(Operator otherPoint);

    /**
     * Determines if the current operator is not equal to the specified operator.
     *
     * @param otherPoint the {@link Operator} instance to compare with the current instance
     * @return true if the current operator is not equal to the specified operator, false otherwise
     */
    boolean ne(Operator otherPoint);

    /**
     * Adds the current {@link Operator} instance to another {@link Operator} instance.
     *
     * @param otherPoint the {@link Operator} instance to be added to the current instance
     * @return a new {@link Operator} instance representing the result of the addition
     */
    Operator add(Operator otherPoint);

    /**
     * Subtracts the specified {@link Operator} instance from the current {@link Operator} instance.
     *
     * @param otherPoint the {@link Operator} instance to be subtracted from the current instance
     * @return a new {@link Operator} instance representing the result of the subtraction
     */
    Operator sub(Operator otherPoint);

    /**
     * Multiplies the current {@link Operator} instance with another {@link Operator} instance.
     *
     * @param otherPoint the {@link Operator} instance to multiply with the current instance
     * @return a new {@link Operator} instance representing the result of the multiplication
     */
    Operator mul(Operator otherPoint);

    /**
     * Multiplies the current {@link Operator} instance by the specified integer coefficient.
     *
     * @param coefficient the integer value by which the current {@link Operator} instance is multiplied
     * @return a new {@link Operator} instance representing the result of the multiplication
     */
    Operator mul(int coefficient);

    /**
     * Raises the current {@link Operator} instance to the specified exponent.
     *
     * @param exponent the {@link Int} representing the exponent to which the current {@link Operator} instance is raised
     * @return a new {@link Operator} instance representing the result of the exponentiation
     */
    Operator pow(Int exponent);

    /**
     * Divides the current {@link Operator} instance by the specified {@link Operator} instance.
     *
     * @param otherPoint the {@link Operator} instance to divide the current instance by
     * @return a new {@link Operator} instance representing the result of the division
     */
    Operator div(Operator otherPoint);

    /**
     * Computes the remainder of the division of the current {@link Operator} instance by the specified integer divisor.
     *
     * @param divisor the {@link Int} representing the divisor used to compute the remainder
     * @return a new {@link Operator} instance representing the remainder of the division
     */
    Operator mod(Int divisor);

    /**
     * Raises the current {@link Operator} instance to the given exponent and computes the result modulo the given divisor.
     *
     * @param exponent the {@link Int} representing the exponent to which the current {@link Operator} instance is raised
     * @param divisor the {@link Int} representing the divisor for computing the modulus of the result
     * @return a new {@link Operator} instance representing the result of raising the current instance to the power
     *         of the specified exponent, modulo the specified divisor
     */
    Operator powMod(Int exponent, Int divisor);

}
