package io.github.deepeshpatel.jnumbertools.base;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculator required for combinatorics calculation. This calculator uses memoization
 * for nCr, nPr, and factorial, and hence the instance variable of this class must be set
 * to null once all calculations are done to dispose of memoized values.
 */
public final class Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final ConcurrentHashMap<Integer, BigInteger> factorialCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code Calculator} instance with default cache sizes.
     */
    public Calculator() {
        this(10, 10, 10);
    }

    /**
     * Constructs a new {@code Calculator} instance with specified cache sizes.
     *
     * @param nCrCacheSize The size of the cache for nCr values.
     * @param nPrCacheSize The size of the cache for nPr values.
     * @param factorialCacheSize The size of the cache for factorial values.
     */
    public Calculator(int nCrCacheSize, int nPrCacheSize, int factorialCacheSize) {
        setupFactorialCache(factorialCacheSize);
        setupCombinationCache(nCrCacheSize);
        setupPermutationCache(nPrCacheSize);
    }

    private void setupFactorialCache(int cacheSize) {
        factorialCache.put(0, BigInteger.ONE);
        factorial(cacheSize);
        //factorial(0);
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

    /**
     * Calculates the binomial coefficient with repetition allowed.
     *
     * @param n The total number of distinct objects.
     * @param r The number of objects to choose.
     * @return The number of ways to choose r objects from n with repetition.
     */
    public BigInteger nCrRepetitive(int n, int r) {
        return nCr(n + r - 1, r);
    }

    /**
     * Calculates the number of ways to choose a sample of r elements from a set of n distinct objects.
     *
     * @param n The total number of distinct objects.
     * @param r The number of objects to choose.
     * @return The number of ways to choose r objects from n without repetition.
     */
    public BigInteger nCr(int n, int r) {

        if (r == n || r == 0) {
            return BigInteger.ONE;
        }
        if (n < r || r < 0) {
            return BigInteger.ZERO;
        }
        if (r == 1 || n - r == 1) {
            return BigInteger.valueOf(n);
        }

        r = Math.min(r, n - r);
        BigInteger value = nCrMemo.get(n, r);
        return value != null ? value : nCrMemo.put(n, r, nCr(n - 1, r - 1).add(nCr(n - 1, r)));
    }

    /**
     * Calculates the number of ways to arrange r elements from a set of n distinct objects where order matters.
     *
     * @param n The total number of distinct objects.
     * @param r The number of objects to arrange.
     * @return The number of ways to arrange r objects from n.
     */
    public BigInteger nPr(int n, int r) {
        if (r < 0 || n < r) {
            return BigInteger.ZERO;
        }
        if (r == 0) {
            return BigInteger.ONE;
        }
        if (r == 1) {
            return BigInteger.valueOf(n);
        }

        BigInteger value = nPrMemo.get(n, r);
        return value != null ? value : nPrMemo.put(n, r, nPr(n - 1, r - 1).multiply(BigInteger.valueOf(n)));
    }

    /**
     * Calculates the factorial of a non-negative integer n.
     *
     * @param n The integer to calculate the factorial for.
     * @return The factorial of n.
     * @throws IllegalArgumentException If n is negative.
     */
    public BigInteger factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative input is not allowed.");
        }

        BigInteger cachedValue = factorialCache.get(n);
        if(cachedValue != null) {
            return cachedValue;
        }
        return computeAndCacheFactorial(n);
    }

    private BigInteger computeAndCacheFactorial(int n) {
        int size = factorialCache.size();
        BigInteger product = factorialCache.get(size-1);
        for(int i=size; i<=n; i++) {
            product = product.multiply(BigInteger.valueOf(i));
            factorialCache.put(i, product);
        }
        return product;
    }


    /**
     * Calculates the power of a base raised to an exponent.
     *
     * @param base The base number.
     * @param exponent The exponent to raise the base to.
     * @return The result of base^exponent.
     */
    public BigInteger power(long base, long exponent) {
        return power(BigInteger.valueOf(base), BigInteger.valueOf(exponent));
    }

    /**
     * Calculates the power of a base raised to an exponent.
     *
     * @param base The base number.
     * @param exponent The exponent to raise the base to.
     * @return The result of base^exponent.
     * @throws IllegalArgumentException If exponent is negative.
     */
    public BigInteger power(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0) {
            throw new IllegalArgumentException("Exponent must be non-negative.");
        }
        BigInteger result = BigInteger.ONE;
        while (exponent.signum() > 0) {
            if (exponent.testBit(0)) result = result.multiply(base);
            base = base.multiply(base);
            exponent = exponent.shiftRight(1);
        }
        return result;
    }

    /**
     * Calculates the total number of possible subsets of size in the range [from, to] for a given number of elements.
     *
     * @param from The minimum subset size.
     * @param to The maximum subset size.
     * @param noOfElements The total number of elements.
     * @return The total number of possible subsets.
     */
    public BigInteger totalSubsetsInRange(int from, int to, int noOfElements) {

        if (from == 0 && to == noOfElements) return power(2, noOfElements);

        BigInteger sum = BigInteger.ZERO;
        for (int i = from; i <= to; i++) {
            sum = sum.add(nCr(noOfElements, i));
        }
        return sum;
    }

    private static class TwoLevelMap<K1, K2, V> extends ConcurrentHashMap<K1, Map<K2, V>> {

        public V get(K1 key1, K2 key2) {
            var map = get(key1);
            return map == null ? null : map.get(key2);
        }

        public V put(K1 key1, K2 key2, V value) {
            computeIfAbsent(key1, (e) -> new ConcurrentHashMap<>()).put(key2, value);
            return value;
        }
    }
}
