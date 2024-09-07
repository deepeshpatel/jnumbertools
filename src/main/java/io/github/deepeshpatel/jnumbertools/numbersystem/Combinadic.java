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
 * Object of this class encapsulates the Combinadic representation of a
 * positive integer for a given degree
 * @author Deepesh Patel
 */
public final class Combinadic implements Serializable {

    /**
     * List<Integer> containing the values of this Combinadic.
     * Length of list is equal to the degree of this Combinadic
     */
    public final transient List<Integer> combinadicValues;

    /**
     * decimal number equivalent of this Combinadic
     */
    public final BigInteger decimalValue;

    private Combinadic(List<Integer> combinadicValues, BigInteger decimalValue) {
        this.combinadicValues = combinadicValues;
        this.decimalValue = decimalValue;
    }

    /**
     * @param positiveNumber positive integer to be converted to Combinadic representation
     * @param degree degree of a Combinadic representation
     */
    public static Combinadic of(long positiveNumber, int degree, Calculator calculator) {
        return of(BigInteger.valueOf(positiveNumber), degree, calculator);
    }

    public static Combinadic of(BigInteger positiveNumber, int degree, Calculator calculator) {
        int[] values = new CombinadicAlgorithms(calculator).decimalToCombinadic(positiveNumber, degree);
        List<Integer> combinadicValues = IntStream.of(values).boxed().toList();
        return new Combinadic(combinadicValues, positiveNumber);
    }

    public int degree() {
        return combinadicValues.size();
    }

    /**
     * @return the next successive(plus 1) Combinadic of this Combinadic
     */
    public Combinadic next() {
        int[] next = CombinadicAlgorithms.nextCombinadic(combinadicValues);
        return new Combinadic(IntStream.of(next).boxed().toList(), decimalValue.add(BigInteger.ONE));
    }


//    /**
//     * @param k positive int value.
//     * @return next kth successive Combinadic
//     */
//    public Combinadic nextKthCombinadic(long k, NumericCalculator calculator) {
        //TODO: Find the algo for nextMthCombinadic without converting to decimal
        // You need to define "addition operation" rules for this number system
//        return Combinadic.of(decimalValue.add(BigInteger.valueOf(k)),combinadicValues.size(), calculator);
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