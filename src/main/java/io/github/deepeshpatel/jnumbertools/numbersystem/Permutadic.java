/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

/**
 * Permutadic or Permutational-number-system is the term I introduced for a number system based on permutations.
 * Unlike Combinadic or Combinatorial Number System, it is a mixed radix system
 *
 * The number N corresponding to (Ck, ..., C2, C1) is given by
 * N= [ nPr(s-1,k-1) * Ck ] + [ nPr(s-2,k-2) * Ck-1 ] + .. [ nPr(s-k,k-k) * C1 ]
 *
 *  for k=s, the representation is same as factorial-number-system.
 *
 *  for k<=s there is one to one mapping between Permutadic and nth K-permutation
 *
 * @author Deepesh Patel
 */
public class Permutadic {

    private final int n;
    private final int[] value;

    public Permutadic(long decimalValue, int n, int r) {
        if(r>n) {
            throw new IllegalArgumentException(" r should be >=n");
        }
        this.n = n;

        this.value = permutadicOf(n,r,decimalValue);
    }

    //decimal to permutadic
    public static int[] permutadicOf(int s, int r, long decimalValue) {

        int[] a = IntStream.range(0,r).toArray();

        long j=s-r+1L;
        for(int i=a.length-1; i>=0; i--, j++) {
            a[i] = (int) (decimalValue % j);
            decimalValue =  decimalValue / j;
        }
        return a;
    }

    public static int[] decodePermutadicToNthPermutation(int[] v, int n) {
        int[] a = new int[v.length];

        List<Integer> allValues = IntStream.range(0, n).boxed().collect(Collectors.toList());

        for(int i=0; i<a.length; i++) {
            a[i] = allValues.remove(v[i]);
        }
        return a;
    }

    public static int[] encodeNthPermutationToPermutadic(int[] nthPermutation, int n) {
        int[] a = new int[nthPermutation.length];

        List<Integer> allValues = IntStream.range(0, n).boxed().collect(Collectors.toList());
        allValues.removeAll(Arrays.stream(nthPermutation).boxed().collect(Collectors.toList()));

        for(int i=a.length-1; i>=0; i--) {
            int index = Collections.binarySearch(allValues,nthPermutation[i]);
            index = -(index+1);

            allValues.add(index,nthPermutation[i]);
            a[i] = index;
        }
        return a;
    }

    public static long toDecimal(int[] v, int n){
        int r= v.length;
        long multiplier = nPr(n,r)/n;
        int divisor = n-1;
        long result = 0;
        for(int value: v){
            result = result + (value * multiplier);
            if(divisor != 0) {
                multiplier = multiplier/divisor;
                divisor--;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(value) + '[' + n + ']';
    }

    public int[] getValue() {
        int[] clone = new int[value.length];
        System.arraycopy(value,0, clone,0, value.length);
        return clone;
    }
}