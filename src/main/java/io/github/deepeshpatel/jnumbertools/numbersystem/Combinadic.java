/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.math.BigInteger;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCrBig;

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
        this(combinadicOf(positiveNumber, degree), positiveNumber);
    }


    private Combinadic(int[] values, BigInteger decimalValue) {
        this.readOnlyValues = values;
        this.decimalValue = decimalValue;
    }

    /**
     * @return decimal number equivalent of a this Combinadic
     */
    public BigInteger decimalValue() {
        return decimalValue;
    }

    /**
     * @return the next successive(plus 1) Combinadic of this Combinadic
     */
    public Combinadic nextCombinadic() {
        int[] next = nextCombinadic(readOnlyValues);
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

    private static int[] combinadicOf(BigInteger value, int degree) {

        int[] combinadic = new int[degree];

        int r = degree;
        BigInteger max = value;

        for(int i=0; i<combinadic.length; i++) {

            BigInteger next = nCrBig(0,r);
            int n=1;
            BigInteger result = BigInteger.ZERO;
            while(next.compareTo(max) <= 0) {
                result= next;
                next = nCrBig(n++, r);
            }

            combinadic[i] = n-2;
            max = max.subtract(result);
            r--;
        }
        return combinadic;
    }

    /**
     * @return int array containing the values of this Combinadic.
     * length of array is equal to the degree of this Combinadic
     */
    public int[] value() {
        int[] a = new int[readOnlyValues.length];
        System.arraycopy(readOnlyValues,0,a,0, a.length);
        return a;
    }

    //This is faster than nextKthCombinadic. So must be used for +1
    private static int[] nextCombinadic(int[] combinadic) {

        int[] result = new int[combinadic.length];
        System.arraycopy(combinadic,0, result, 0, combinadic.length);

        int k=0;
        for(int i=result.length-1; i>0; i--) {

            result[i] = result[i] + 1;
            if(result[i] < result[i-1] ) {
                return result;
            }
            result[i] = k++;
        }

        result[0]++;
        return result;
    }
}