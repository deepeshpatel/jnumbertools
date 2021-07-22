/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.math.BigInteger;

public class MathUtil {

    private MathUtil() { }

    public static long nCr(long n, long r) {
        if(n<r) return 0;
        long denominator = Math.min(r, n-r);
        double p = 1;
        for(int i=1; i<=denominator; i++) {
            p = p * n--/i;
        }
        return Math.round(p);
    }

    public static long nPr(int n, int r) {
        long p = 1;
        for(int i=1; i<=r; i++){
            p = p*n--;
        }
        return p;
    }

    public static BigInteger nPrBig(int n, int r){
        BigInteger p = BigInteger.ONE;
        BigInteger nBig = BigInteger.valueOf(n);
        for(int i=1; i<=r; i++){
            p = p.multiply(nBig);
            nBig = nBig.subtract(BigInteger.ONE);
        }
        return p;
    }

    public static BigInteger nCrBig(long n, long r) {
        if(n<r) return BigInteger.ZERO;
        long denominator = Math.min(r, n-r);

        BigInteger p = BigInteger.ONE;
        BigInteger nBig = BigInteger.valueOf(n);

        for(int i=1; i<=denominator; i++) {
            p = p.multiply(nBig).divide(BigInteger.valueOf(i));
            nBig = nBig.subtract(BigInteger.ONE);
        }
        return p;
    }

    public static BigInteger factorial(int n) {
        BigInteger product = BigInteger.ONE;
        for(int i=2; i<= n; i++) {
            product = product.multiply(BigInteger.valueOf(i));
        }
        return product;
    }
}