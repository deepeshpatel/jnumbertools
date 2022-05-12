/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.combinadic;

import java.math.BigInteger;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.getClone;

/**
 * Object of this class encapsulates the Combinadic representation of a
 * positive integer for a given degree
 * @author Deepesh Patel
 */
public final class Combinadic {

    private final int[] readOnlyValues;
    private final BigInteger decimalValue;

    /**
     * @param positiveNumber positive integer to be converted to Combinadic representation
     * @param degree degree of a Combinadic representation
     */
    public Combinadic(long positiveNumber, int degree) {
        this(BigInteger.valueOf(positiveNumber), degree);
    }

    public Combinadic(BigInteger positiveNumber, int degree) {
        this(CombinadicAlgorithms.decimalToCombinadic(positiveNumber, degree), positiveNumber);
    }

    private Combinadic(int[] values, BigInteger decimalValue) {
        this.readOnlyValues = values;
        this.decimalValue = decimalValue;
    }

    /**
     * @return decimal number equivalent of this Combinadic
     */
    public BigInteger decimalValue() {
        return decimalValue;
    }

    /**
     * @return the next successive(plus 1) Combinadic of this Combinadic
     */
    public Combinadic nextCombinadic() {
        int[] next = CombinadicAlgorithms.nextCombinadic(readOnlyValues);
        return new Combinadic(next, decimalValue.add(BigInteger.ONE));
    }

    /**
     * @param k positive int value.
     * @return next kth successive Combinadic
     */
    public Combinadic nextKthCombinadic(long k) {
        //TODO: Find the fast algo for this
        return new Combinadic(decimalValue.add(BigInteger.valueOf(k)), readOnlyValues.length);
    }

    @Override
    public String toString() {
        return Arrays.toString(readOnlyValues);
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

    /**
     * @return int array containing the values of this Combinadic.
     * length of array is equal to the degree of this Combinadic
     */
    public int[] value() {
        return getClone(readOnlyValues);
    }
}