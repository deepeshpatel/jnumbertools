/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents a combinadic number system for a given degree. This class encapsulates
 * the Combinadic representation of a positive integer and provides methods to convert
 * between decimal and combinadic formats, as well as generate successive combinadic values.
 * <p>
 * The Combinadic representation is a unique way to represent numbers where each position
 * in the representation is determined by combinatorial numbers, providing a way to
 * enumerate combinations with repetitions.
 *
 * @author Deepesh Patel
 */
public final class Combinadic implements Serializable {

    /**
     * List of integers representing the Combinadic values. The length of the list is equal
     * to the degree of this Combinadic representation.
     */
    public final transient List<Integer> combinadicValues;

    /**
     * Decimal number equivalent of this Combinadic representation.
     */
    public final BigInteger decimalValue;

    private Combinadic(List<Integer> combinadicValues, BigInteger decimalValue) {
        this.combinadicValues = combinadicValues;
        this.decimalValue = decimalValue;
    }

    /**
     * Creates a {@link Combinadic} object for the specified positive integer and degree.
     *
     * @param positiveNumber the positive integer to be converted to Combinadic representation.
     * @param degree the degree of the Combinadic representation.
     * @param calculator the calculator used for combinatorial calculations.
     * @return a {@link Combinadic} object representing the given positive integer.
     */
    public static Combinadic of(long positiveNumber, int degree, Calculator calculator) {
        return of(BigInteger.valueOf(positiveNumber), degree, calculator);
    }

    /**
     * Creates a {@link Combinadic} object for the specified positive integer (in BigInteger format) and degree.
     *
     * @param positiveNumber the positive integer to be converted to Combinadic representation.
     * @param degree the degree of the Combinadic representation.
     * @param calculator the calculator used for combinatorial calculations.
     * @return a {@link Combinadic} object representing the given positive integer.
     */
    public static Combinadic of(BigInteger positiveNumber, int degree, Calculator calculator) {
        int[] values = new CombinadicAlgorithms(calculator).decimalToCombinadic(positiveNumber, degree);
        List<Integer> combinadicValues = IntStream.of(values).boxed().toList();
        return new Combinadic(combinadicValues, positiveNumber);
    }

    /**
     * Returns the degree of this Combinadic representation, which is the length of the combinadicValues list.
     *
     * @return the degree of this Combinadic representation.
     */
    public int degree() {
        return combinadicValues.size();
    }

    /**
     * Returns the next successive Combinadic value by incrementing the current decimal value by 1.
     *
     * @return a new {@link Combinadic} object representing the next successive Combinadic value.
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
