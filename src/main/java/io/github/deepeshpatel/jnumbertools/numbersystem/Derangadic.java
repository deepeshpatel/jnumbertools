/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a Derangadic number, a combinatorial number system for derangements.
 * <p>
 * The Derangadic number system provides a bijective mapping between integers {@code [0, D_n - 1]}
 * and derangements (fixed-point-free permutations) of {@code n} elements in lexicographical order,
 * where {@code D_n} is the number of derangements (subfactorial !n).
 * </p>
 * <p>
 * This class encapsulates the Derangadic representation (digits) and its decimal value (rank).
 * </p>
 *
 * <h3>Digit Representation and Trailing Zeros</h3>
 * <p>
 * The digit array has variable length equal to the minimal effective size (actualN),
 * which is the smallest number with the same parity as {@code n} where {@code D_actualN > rank}.
 * Trailing zeros are preserved in the representation to maintain consistent length.
 * </p>
 * <p>
 * When comparing Derangadic instances or digit arrays, trailing zeros should be ignored
 * as they don't affect the rank value. For example, {@code [0,1,1,0]} and {@code [0,1,1]}
 * represent the same rank.
 * </p>
 *
 * <h3>Example: n=4, D₄=9</h3>
 * <pre>
 * Derangadic d0 = Derangadic.of(0, 4);
 * System.out.println(d0);                 // [0, 0](4)
 * System.out.println(d0.decimalValue());  // 0
 * System.out.println(Arrays.toString(d0.toDerangement())); // [1, 0, 3, 2]
 *
 * Derangadic d1 = Derangadic.of(1, 4);
 * System.out.println(d1);                 // [0, 1, 1, 0](4)
 * System.out.println(d1.decimalValue());  // 1
 *
 * Derangadic d8 = Derangadic.of(8, 4);
 * System.out.println(d8);                 // [0, 1, 1, 2](4)
 * System.out.println(d8.decimalValue());  // 8
 *
 * // From derangement
 * int[] der = {1, 0, 3, 2};
 * Derangadic fromDer = Derangadic.fromDerangement(der, 4);
 * System.out.println(fromDer.decimalValue()); // 0
 * </pre>
 *
 * <p>
 * Note: The digit arrays include trailing zeros. When comparing, use
 * {@code Arrays.equals()} for exact match or custom logic to ignore trailing zeros
 * if needed.
 * </p>
 *
 * @author Deepesh Patel & Aditya Patel
 * @see DerangadicAlgorithms
 * @since 3.0.2
 */
public final class Derangadic implements Serializable {

    private final BigInteger decimalValue;
    private final int[] derangadicValues;
    private final int order;

    private Derangadic(BigInteger decimalValue, int[] derangadicValues, int order) {
        this.decimalValue = decimalValue;
        this.derangadicValues = derangadicValues.clone();
        this.order = order;
    }

    /**
     * Creates a {@link Derangadic} instance from a decimal rank and order.
     *
     * @param rank the decimal rank to convert (0 ≤ rank < D_n)
     * @param n the order (number of elements)
     * @return a Derangadic instance representing the given rank and order
     * @throws IllegalArgumentException if rank is out of range
     */
    public static Derangadic of(BigInteger rank, int n) {
        DerangadicAlgorithms algorithms = new DerangadicAlgorithms(new Calculator());
        int[] digits = algorithms.toDerangadic(rank, n);
        return new Derangadic(rank, digits, n);
    }

    /**
     * Creates a {@link Derangadic} instance from a decimal rank and order (long version).
     *
     * @param rank the decimal rank to convert (0 ≤ rank < D_n)
     * @param n the order (number of elements)
     * @return a Derangadic instance representing the given rank and order
     * @throws IllegalArgumentException if rank is out of range
     */
    public static Derangadic of(long rank, int n) {
        return of(BigInteger.valueOf(rank), n);
    }

    /**
     * Creates a {@link Derangadic} instance from a derangement array.
     *
     * @param derangement the derangement array (full size n, no fixed points)
     * @param n the order (number of elements)
     * @return a Derangadic instance representing the given derangement
     * @throws IllegalArgumentException if derangement is not a valid derangement
     */
    public static Derangadic fromDerangement(int[] derangement, int n) {
        DerangadicAlgorithms algorithms = new DerangadicAlgorithms(new Calculator());
        int[] digits = algorithms.fromDerangement(derangement, n);
        BigInteger rank = algorithms.fromDerangadic(digits, n);
        return new Derangadic(rank, digits, n);
    }

    /**
     * Returns the derangement corresponding to this Derangadic number.
     *
     * @return an array representing the derangement (full size n)
     */
    public int[] toDerangement() {
        DerangadicAlgorithms algorithms = new DerangadicAlgorithms(new Calculator());
        return algorithms.toDerangement(derangadicValues, order);
    }

    /**
     * Returns the decimal rank of this Derangadic number.
     *
     * @return the rank as a BigInteger
     */
    public BigInteger decimalValue() {
        return decimalValue;
    }

    /**
     * Returns the Derangadic digit representation.
     *
     * @return a copy of the digit array
     */
    public int[] derangadicValues() {
        return derangadicValues.clone();
    }

    /**
     * Returns the order (n) of this Derangadic number.
     *
     * @return the order
     */
    public int order() {
        return order;
    }

    /**
     * Returns the minimal effective size of this Derangadic representation.
     *
     * @return the length of the digit array
     */
    public int getMinimalSize() {
        return derangadicValues.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < derangadicValues.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(derangadicValues[i]);
        }
        sb.append("](").append(order).append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Derangadic that = (Derangadic) o;
        return order == that.order &&
                decimalValue.equals(that.decimalValue) &&
                Arrays.equals(derangadicValues, that.derangadicValues);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(decimalValue, order);
        result = 31 * result + Arrays.hashCode(derangadicValues);
        return result;
    }
}