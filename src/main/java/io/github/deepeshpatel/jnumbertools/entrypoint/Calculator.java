package io.github.deepeshpatel.jnumbertools.entrypoint;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculator required for combinatorics calculation. This calculator uses memoization
 * for nCr, nPr and factorial and hence the instance variable of this class must be set
 * to null once all calculations are done to dispose memoized values.
 */
public final class Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final List<BigInteger> fList = new ArrayList<>();

    public Calculator() {
        this(10,10,10);
    }

    public Calculator(int nCrCacheSize, int nPrCacheSize, int factorialCacheSize) {
        setupFactorialCache(factorialCacheSize);
        setupCombinationCache(nCrCacheSize);
        setupPermutationCache(nPrCacheSize);
    }

    private void setupFactorialCache(int cacheSize) {
        fList.add( BigInteger.ONE);
        fList.add( BigInteger.ONE);
        fList.add( BigInteger.TWO);
        fList.add( BigInteger.valueOf(6));
        factorial(cacheSize);
    }

    private void setupCombinationCache(int cacheSize) {
        for (int n = 4; n <= cacheSize; n++) {
            for (int r = 2; r <= n / 2; r++) {
                nCr(n, 3);
            }
        }
    }

    private void setupPermutationCache(int cacheSize) {
        for (int n = 3; n <= cacheSize; n++) {
            for (int r = 2; r <= n; r++) {
                nPr(n, 3);
            }
        }
    }

    public BigInteger nCr(int n, int r) {

        if(r == n || r == 0)    { return BigInteger.ONE; }
        if(n < r || r < 0)      { return BigInteger.ZERO; }
        if(r==1 || n-r == 1)    { return BigInteger.valueOf(n); }

        r = Math.min(r, n-r);
        BigInteger value = nCrMemo.get(n,r);
        return value != null ? value : nCrMemo.put(n,r, nCr(n-1,r-1).add(nCr(n-1,r)));
    }

    public BigInteger  nPr(int n, int r) {

        if(r == 0) return BigInteger.ONE;
        if(r == 1) return BigInteger.valueOf(n);

        BigInteger value = nPrMemo.get(n,r);
        return value != null ? value : nPrMemo.put(n,r, nPr(n-1, r-1).multiply(BigInteger.valueOf(n)));
    }

    public BigInteger factorial(int n) {
        if( n < fList.size()) {
            return fList.get(n);
        }

        BigInteger product = fList.get(fList.size() - 1);

        for(int i=fList.size(); i<=n; i++) {
            product = product.multiply(BigInteger.valueOf(i));
            fList.add(product);
        }
        return product;
    }

    public BigInteger pow(long base, long exponent) {
        return pow(BigInteger.valueOf(base), BigInteger.valueOf(exponent));
    }
    public BigInteger pow(BigInteger base, BigInteger exponent) {

        BigInteger result = BigInteger.ONE;
        while (exponent.signum() > 0) {
            if (exponent.testBit(0)) result = result.multiply(base);
            base = base.multiply(base);
            exponent = exponent.shiftRight(1);
        }
        return result;
    }

    public BigInteger totalSubsetsInRange(int from, int to, int noOfElements) {

        if(from==0 && to==noOfElements) return pow(2, noOfElements);

        BigInteger sum = BigInteger.ZERO;
        for(int i=from ; i<=to; i++){
            sum = sum.add(nCr(noOfElements, i));
        }
        return sum;
    }

    public BigInteger totalRepetitiveCombinations(int noOfElements, int r) {
        return nCr(noOfElements+r-1, r);
    }

    //Can be replaced by Google Guava Table. Using this because of
    // [1] defined constraint that JNumberTools library should not have any dependency
    // [2] very small code
    // [3] is fast
    private static class TwoLevelMap<K1,K2,V> extends HashMap<K1,Map<K2,V>> {

        public V get(K1 key1, K2 key2) {
            var map = get(key1);
            return map == null ?  null : map.get(key2);
        }

        public V put(K1 key1, K2 key2, V value) {
            computeIfAbsent(key1, (e) -> new HashMap<>()).put(key2, value);
            return value;
        }
    }
}
