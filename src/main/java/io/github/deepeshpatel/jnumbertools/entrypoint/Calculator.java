package io.github.deepeshpatel.jnumbertools.entrypoint;

import java.math.BigInteger;
import java.util.*;

/**
 * Calculator required for combinatorics calculation. This calculator uses memoization
 * for nCr, nPr and factorial and hence the instance variable of this class must be set
 * to null once all calculations are done to dispose memoized values.
 */
public class Calculator {

    private final Map<String, BigInteger> nCrMemo = new HashMap<>();
    private final Map<String, BigInteger> nPrMemo = new HashMap<>();
    private final List<BigInteger> fList = new ArrayList<>();
    private boolean nCrMemoized = false;

    public Calculator() {
        setupCache();
    }

    private void setupCache() {
        fList.add(BigInteger.ONE);
        fList.add(BigInteger.ONE);
        fList.add(BigInteger.TWO);
        fList.add(BigInteger.valueOf(6));
        fList.add(BigInteger.valueOf(24));
        fList.add(BigInteger.valueOf(120));
    }

    public static long GCD(long greater, long smaller) {
        long mod = greater % smaller;
        return mod == 0 ? smaller : GCD(smaller, mod);
    }

    public static long LCM(long a, long b) {
        return (a * b)/ GCD(a,b);
    }

    public BigInteger nCr(int n, int r) {


        if(r == n || r == 0) return BigInteger.ONE;
        if(n < r || r < 0) return BigInteger.ZERO;

        if(n>5000 && !nCrMemoized) {
            nCrMemoized = true;
            memoizeAllCombinations(1,n);
        }

        r = Math.min(r, n-r);
        String key = n+"_" + r;
        BigInteger value = nCrMemo.get(key);
        if(value != null) {
            return value;
        }

        BigInteger result = nCr(n-1,r-1).add(nCr(n-1,r));
        nCrMemo.put(key, result);
        return result;
    }

    public BigInteger  nPr(int n, int r) {

        if(r == 0) return BigInteger.ONE;
        if(r == 1) return BigInteger.valueOf(n);

        String key = n+"_" + r;
        BigInteger value = nPrMemo.get(key);
        if(value != null) {
            return value;
        }

        BigInteger nPr = nPr(n-1, r-1).multiply(BigInteger.valueOf(n));
        nPrMemo.put(key, nPr);
        return nPr;
    }

    public BigInteger factorial(int n) {

        if( n < fList.size()) {
            return fList.get(n);
        }

        BigInteger product = fList.get(fList.size()-1);

        for(int i=fList.size(); i<= n; i++) {
            product = product.multiply(BigInteger.valueOf(i));
            fList.add(product);
        }
        return product;
    }

    public void memoizeAllCombinations( int from, int to) {
        for(int n=from; n<=to; n++) {
                nCr(n, n-1);
        }
    }
}
