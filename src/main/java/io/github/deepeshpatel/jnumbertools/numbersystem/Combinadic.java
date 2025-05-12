/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents a combinadic number system for a given degree.
 * <p>
 * A combinadic representation uniquely maps a non-negative integer (in decimal) to a combination
 * of a fixed size (degree) from a set of items. This class encapsulates such a representation,
 * providing methods to convert a decimal number into its combinadic format and to generate successive
 * combinadic values. The combinadic number system is particularly useful for directly computing the
 * nth combination in lexicographical order without enumerating all previous combinations.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class Combinadic implements Serializable {

    /**
     * The combinadic values represented as a list of integers.
     * The length of this list equals the degree of the combinadic representation.
     */
    public final transient List<Integer> combinadicValues;

    /**
     * The decimal equivalent of this combinadic representation.
     */
    public final BigInteger decimalValue;

    private Combinadic(List<Integer> combinadicValues, BigInteger decimalValue) {
        this.combinadicValues = combinadicValues;
        this.decimalValue = decimalValue;
    }

    /**
     * Creates a {@link Combinadic} object for the specified positive integer (as a {@code long})
     * and degree.
     *
     * @param positiveNumber the positive integer to convert to combinadic representation.
     * @param degree the degree (length) of the combinadic representation.
     * @param calculator the calculator used for combinatorial calculations.
     * @return a {@link Combinadic} object representing the given positive integer.
     */
    public static Combinadic of(long positiveNumber, int degree, Calculator calculator) {
        return of(BigInteger.valueOf(positiveNumber), degree, calculator);
    }

    /**
     * Creates a {@link Combinadic} object for the specified positive integer (as a {@code BigInteger})
     * and degree.
     *
     * @param positiveNumber the positive integer to convert to combinadic representation.
     * @param degree the degree (length) of the combinadic representation.
     * @param calculator the calculator used for combinatorial calculations.
     * @return a {@link Combinadic} object representing the given positive integer.
     */
    public static Combinadic of(BigInteger positiveNumber, int degree, Calculator calculator) {
        int[] values = new CombinadicAlgorithms(calculator).decimalToCombinadic(positiveNumber, degree);
        List<Integer> combinadicValues = IntStream.of(values).boxed().toList();
        return new Combinadic(combinadicValues, positiveNumber);
    }

    /**
     * Returns the degree of this combinadic representation.
     * <p>
     * This is the length of the {@code combinadicValues} list.
     * </p>
     *
     * @return the degree of this combinadic representation.
     */
    public int degree() {
        return combinadicValues.size();
    }

    /**
     * Returns the next successive combinadic value.
     * <p>
     * This method increments the current decimal value by one and computes the corresponding
     * combinadic representation.
     * </p>
     *
     * @return a new {@link Combinadic} object representing the next combinadic value.
     */
    public Combinadic next() {
        int[] next = CombinadicAlgorithms.nextCombinadic(combinadicValues);
        return new Combinadic(IntStream.of(next).boxed().toList(), decimalValue.add(BigInteger.ONE));
    }

//    public Combinadic nextKthCombinadic(long k, NumericCalculator calculator) {
//        //TODO: Find the algorithm for nextKthCombinadic without converting to decimal
//        // You need to define "addition operation" rules for this number system
//        return Combinadic.of(decimalValue.add(BigInteger.valueOf(k)), combinadicValues.size(), calculator);
//    }

    @Override
    public String toString() {
        return combinadicValues.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Combinadic that = (Combinadic) o;
        return decimalValue.equals(that.decimalValue);
    }

    @Override
    public int hashCode() {
        return decimalValue.hashCode();
    }
}
