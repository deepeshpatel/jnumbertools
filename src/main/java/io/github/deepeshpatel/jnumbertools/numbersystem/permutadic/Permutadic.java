/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem.permutadic;

import java.math.BigInteger;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.getClone;
import static io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms.*;

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
    private final BigInteger decimalValue;

    private Permutadic(BigInteger decimalValue, int[] value, int size ) {
        this.size = size;
        this.value = value;
        this.decimalValue = decimalValue;
    }

    public static Permutadic of(BigInteger decimalValue, int size, int degree) {
        return new Permutadic(decimalValue, decimalToPermutadic(decimalValue,size,degree), size);
    }

    public int[] toNthPermutationWithoutBoundCheck() {
        return permutadicToNthPermutation(value,size);
    }

    public int[] toNthPermutationWithBoundCheck() {
        checkBounds(decimalValue,size,value.length);
        return permutadicToNthPermutation(value,size);
    }

    public static Permutadic fromNthPermutation(int[] nthPermutation, int size) {
        int[] permutadic = nthPermutationToPermutadic(nthPermutation, size);
        BigInteger decimalVal = permutadicToDecimal(permutadic,size);
        return new Permutadic(decimalVal, permutadic, size);
    }

    public BigInteger decimalValue() {
        return decimalValue;
    }

    public int[] getValue() {
        return getClone(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value) + '[' + size + ']';
    }
}
