/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.util.Arrays;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

/**
 * Permutadic or Permutational-number-system or Deep's representation is the term I introduced for a number system based on permutations.
 * This is similar to Combinadic
 *
 * The number N corresponding to (Ck, ..., C2, C1) is given by
 * N= [ nPr(s-1,k-1) * Ck ] + [ nPr(s-2,k-2) * Ck-1 ] + .. [ nPr(s-k,k-k) * C1 ]
 *
 *  for k=s, the representation is same as factorial-number-system.
 *
 *  for k<s there is one to one mapping between Permutadic and nth K-permutation
 *
 * @author Deepesh Patel
 */
public class Permutadic {

    final long decimalValue;
    final int n;
    final int r;
    final int[] value;

    private Permutadic(int n, int r, long decimalValue) {
        this.n = n;
        this.r = r;
        this.decimalValue = decimalValue;
        value = permutadicOf(n,r,decimalValue);
    }

    public static Permutadic valueOf(int n, int r, long decimalValue) {
        if(r>n) {
            throw new IllegalArgumentException(" r should be >=n");
        }

        return new Permutadic(n,r, decimalValue);
    }

    private int[] permutadicOf(int n, int r, long decimalValue) {

        int[] a = IntStream.range(0,r).toArray();
        long nPr = nPr(n,r);

        long divisor = nPr/n;
        int next = n-1;

        long remainingValue = decimalValue;

        for(int i=0; i<a.length; i++) {
            int quotient = (int) (remainingValue / divisor);
            remainingValue = remainingValue % divisor;
            a[i] = quotient;
            if(next !=0) {
                divisor = divisor / next;
                next--;
            }
        }
        return a;
    }

    public static long decimalOf(int[] v, int n){
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
        return "Permutadic{" +
                "decimalValue=" + decimalValue +
                ", n=" + n +
                ", r=" + r +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}