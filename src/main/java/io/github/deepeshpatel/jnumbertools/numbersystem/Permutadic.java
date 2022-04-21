/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

/**
 *
 * Permutadic or Permutational-number-system is the term I introduced for a number system based on permutations.
 * Definition: Permutational-number-system of size s and degree k, for some positive integers s and k
 * where 1 ≤ k ≤ s , is a correspondence between natural numbers (starting from 0) and
 * k-permutations, represented as sequence [Ck−1, Ck−2, Ck−3 . . . C1, C0][s] where Ci ∈ N.
 * Permutational-number-system of size s and degree k also referred to as Permutadic(s, k) for short,
 * is a mixed radix number system that has a unique representation for all natural numbers.
 * The number n corresponding to the permutadic string -
 * [Ck−1, Ck−2, Ck−3 . . . C1, C0][s]
 *
 * is expressed by the following equation -
 * n = Summation[Permutation(s-i,k-i) * Ck-i] for i= 1 to k
 * Where s−iPk−i is the place value for the ith digit/number from right
 *
 * @author Deepesh Patel
 */
public class Permutadic {

    private final int size;
    private final int[] value;
    private final long decimalValue;

    public Permutadic(long decimalValue, int size, int degree) {
        if(degree> size) {
            throw new IllegalArgumentException(" degree " + degree + " should be <= size " + size);
        }
        this.size = size;
        this.value = permutadicOf(decimalValue, size, degree);
        this.decimalValue = decimalValue;
    }

    private Permutadic(long decimalValue, int[] value, int size) {
        this.size = size;
        this.value = value;
        this.decimalValue = decimalValue;
    }

    private static int[] permutadicOf(long decimalValue, int size, int degree) {

        int[] a = IntStream.range(0,degree).toArray();

        long j=size-degree+1L;
        for(int i=a.length-1; i>0; i--, j++) {
            a[i] = (int) (decimalValue % j);
            decimalValue =  decimalValue / j;
        }
        a[0] = (int) decimalValue;
        return a;
    }

    public int[] decodeToNthPermutation() {

        long maxSupported = nPr(size, value.length);
        if(decimalValue >= maxSupported) {
            throw new ArithmeticException("Out of range. Can't decode " + decimalValue + " to nth permutation as it is >= Permutation(size,degree).");
        }

        int[] a = new int[value.length];

        List<Integer> allValues = IntStream.range(0, size).boxed().collect(Collectors.toList());

        for(int i=0; i<a.length; i++) {
            a[i] = allValues.remove(value[i]);
        }
        return a;
    }

    public static Permutadic encodeNthPermutation(int[] nthPermutation, int size) {
        int[] a = new int[nthPermutation.length];

        List<Integer> allValues = IntStream.range(0, size).boxed().collect(Collectors.toList());
        allValues.removeAll(Arrays.stream(nthPermutation).boxed().collect(Collectors.toList()));

        for(int i=a.length-1; i>=0; i--) {
            int index = Collections.binarySearch(allValues,nthPermutation[i]);
            index = -(index+1);

            allValues.add(index,nthPermutation[i]);
            a[i] = index;
        }

        return new Permutadic(toDecimal(a,size), a,size);
    }

    public long toDecimal(){
        return decimalValue;
    }

    private static long toDecimal(int[] value, int size){
        long result = value[value.length-1];
        long startingMultiplier = size - value.length+1;
        long multiplier = startingMultiplier;
        for(int i=value.length-2; i>=0; i--) {
            result = result + (value[i] * multiplier);
            startingMultiplier++;
            multiplier = multiplier * startingMultiplier;
        }
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(value) + '[' + size + ']';
    }

    public int[] getValue() {
        int[] clone = new int[value.length];
        System.arraycopy(value,0, clone,0, value.length);
        return clone;
    }
}