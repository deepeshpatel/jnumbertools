package io.github.deepeshpatel.jnumbertools.entrypoint;

import java.math.BigInteger;
import java.util.*;

/**
 * Calculator required for combinatorics calculation. This calculator uses memoization
 * for nCr, nPr and factorial and hence the instance variable of this class must be set
 * to null once all calculations are done to dispose memoized values.
 */
public class Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final List<BigInteger> fList = new ArrayList<>();

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
        if(r == n || r == 0)    return BigInteger.ONE;
        if(n < r || r < 0)      return BigInteger.ZERO;
        if(r==1 || n-r == 1)    return BigInteger.valueOf(n);

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

        BigInteger product = fList.get(fList.size()-1);

        for(int i=fList.size(); i<= n; i++) {
            product = product.multiply(BigInteger.valueOf(i));
            fList.add(product);
        }
        return product;
    }

    //Can be replaced by Google Guava Table. Using this because of the
    // defined constraint that JNumberTools library should not have any dependency
    private static class TwoLevelMap<K1,K2,V> extends HashMap<K1,Map<K2,V>>{

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
