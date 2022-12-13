/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.math.BigInteger;

import static java.lang.Math.min;

public class MathUtil {

    private static final int factorialCacheSize = 40;
    private static final BigInteger[] factorialList = new BigInteger[factorialCacheSize];

    static {

        factorialList[0] = BigInteger.ONE;
        factorialList[1] = BigInteger.ONE;

        BigInteger product = BigInteger.ONE;

        for(int i=2; i<factorialCacheSize; i++) {
            product = product.multiply(BigInteger.valueOf(i));
            factorialList[i] = product;
        }
    }

    private MathUtil() { }

    public static long nCr(long n, long r) {
        if(n<r) return 0;
        long denominator = min(r, n-r);
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
        if(r == 1) return BigInteger.valueOf(n);
        if(r == 0) return BigInteger.ZERO;
        return factorial(n).divide(factorial(n-r));
    }

    public static BigInteger nCrBig(int n, int r) {
        if(n==r) return BigInteger.ONE;
        if(n<r) return BigInteger.ZERO;
        if(r==1 || n-r ==1) return BigInteger.valueOf(n);
        return factorial(n).divide(factorial(r)).divide(factorial(n-r));
    }

    public static BigInteger factorial(int n) {

        if( n < factorialList.length) {
            return factorialList[n];
        }

        BigInteger product = factorialList[factorialList.length-1];

        for(int i=factorialCacheSize; i<= n; i++) {
            product = product.multiply(BigInteger.valueOf(i));
        }
        return product;
    }

    public static long GCD(long a, long b) {
        long mod = a % b;
        return mod == 0 ? b : GCD(b, mod);
    }

    public static long LCM(long a, long b) {
        return (a * b)/GCD(a,b);
    }
}