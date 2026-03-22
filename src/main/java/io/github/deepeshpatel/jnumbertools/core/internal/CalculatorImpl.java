/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal;

import io.github.deepeshpatel.jnumbertools.core.external.Calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of {@link Calculator} with memoization.
 * Uses internal caches for factorials, subfactorials, and binomial/permutation coefficients.
 */
public final class CalculatorImpl implements Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final List<BigInteger> factorialCache = Collections.synchronizedList(new ArrayList<>());
    private final List<BigInteger> subFactorialCache = Collections.synchronizedList(new ArrayList<>());

    /**
     * Initializes caches with base values: 0! = 1, !0 = 1, !1 = 0.
     */
    public CalculatorImpl() {
        synchronized (factorialCache) {
            factorialCache.add(BigInteger.ONE); // 0! = 1
        }
        synchronized (subFactorialCache) {
            subFactorialCache.add(BigInteger.ONE);  // !0 = 1
            subFactorialCache.add(BigInteger.ZERO); // !1 = 0
        }
    }

    @Override
    public BigInteger nCrRepetitive(int n, int r) {
        return nCr(n + r - 1, r);
    }

    @Override
    public BigInteger nCr(int n, int r) {
        if (r == n || r == 0) return BigInteger.ONE;
        if (n < r || r < 0) return BigInteger.ZERO;

        r = Math.min(r, n - r);

        BigInteger value = nCrMemo.get(n, r);
        if (value != null) return value;

        value = BigInteger.ONE;
        for (int i = 1; i <= r; i++) {
            value = value.multiply(BigInteger.valueOf(n - r + i))
                    .divide(BigInteger.valueOf(i));
        }

        nCrMemo.put(n, r, value);
        return value;
    }

    @Override
    public int nCrUpperBound(int r, BigInteger max) {
        int n = r;
        BigInteger current = nCr(n, r);
        while (current.compareTo(max) <= 0) {
            n++;
            current = nCr(n, r);
        }
        return n;
    }

    @Override
    public BigInteger nPr(int n, int r) {
        if (r < 0 || n < r) return BigInteger.ZERO;
        if (r == 0) return BigInteger.ONE;

        BigInteger cached = nPrMemo.get(n, r);
        if (cached != null) return cached;

        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < r; i++) {
            result = result.multiply(BigInteger.valueOf(n - i));
        }

        nPrMemo.put(n, r, result);
        return result;
    }

    @Override
    public int factorialUpperBound(BigInteger value) {
        int n = 1;
        while (factorial(n).compareTo(value) <= 0) {
            n++;
        }
        return n;
    }

    @Override
    public BigInteger factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative input is not allowed.");
        synchronized (factorialCache) {
            while (factorialCache.size() <= n) {
                int i = factorialCache.size();
                BigInteger prev = factorialCache.get(i - 1);
                factorialCache.add(prev.multiply(BigInteger.valueOf(i)));
            }
            return factorialCache.get(n);
        }
    }

    @Override
    public BigInteger subFactorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative input is not allowed.");
        synchronized (subFactorialCache) {
            while (subFactorialCache.size() <= n) {
                int i = subFactorialCache.size();
                if (i == 2) {
                    subFactorialCache.add(BigInteger.ONE); // !2 = 1
                } else {
                    BigInteger prev1 = subFactorialCache.get(i - 1);
                    BigInteger prev2 = subFactorialCache.get(i - 2);
                    BigInteger value = BigInteger.valueOf(i - 1).multiply(prev1.add(prev2));
                    subFactorialCache.add(value);
                }
            }
            return subFactorialCache.get(n);
        }
    }

    @Override
    public BigInteger power(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0) {
            throw new IllegalArgumentException("Exponent must be non-negative.");
        }
        BigInteger result = BigInteger.ONE;
        BigInteger b = base;
        BigInteger e = exponent;
        while (e.signum() > 0) {
            if (e.testBit(0)) result = result.multiply(b);
            b = b.multiply(b);
            e = e.shiftRight(1);
        }
        return result;
    }

    @Override
    public BigInteger totalSubsetsInRange(int from, int to, int n) {
        if (from == 0 && to == n) return power(2, n);
        BigInteger sum = BigInteger.ZERO;
        for (int i = Math.max(0, from); i <= Math.min(n, to); i++) {
            sum = sum.add(nCr(n, i));
        }
        return sum;
    }

    @Override
    public BigInteger multinomial(int... counts) {
        int total = 0;
        BigInteger denom = BigInteger.ONE;
        for (int c : counts) {
            if (c < 0) throw new IllegalArgumentException("Counts must be non-negative.");
            total += c;
            denom = denom.multiply(factorial(c));
        }
        return factorial(total).divide(denom);
    }

    @Override
    public int[] multisetCombinationsCountAll(int... frequencies) {
        int total = Arrays.stream(frequencies).sum();
        int k = total / 2;
        int[] dp = new int[k + 1];
        dp[0] = 1;

        for (int freq : frequencies) {
            int[] next = new int[k + 1];
            for (int i = 0; i <= k; i++) {
                if (dp[i] == 0) continue;
                for (int j = 0; j <= freq && i + j <= k; j++) {
                    next[i + j] += dp[i];
                }
            }
            dp = next;
        }
        return dp;
    }

    @Override
    public BigInteger multisetCombinationsCount(int k, int... counts) {
        return multisetCombinationsCountStartingFromIndex(k, 0, counts);
    }

    @Override
    public BigInteger multisetCombinationsCountStartingFromIndex(int k, int index, int... counts) {
        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative.");
        }
        if (index < 0 || index > counts.length) {
            throw new IllegalArgumentException("index must be between 0 and counts.length inclusive.");
        }

        for (int count : counts) {
            if (count < 0) {
                throw new IllegalArgumentException("Counts must be non-negative.");
            }
        }

        int n = counts.length;
        int[] suffixSum = new int[n + 1];
        suffixSum[n] = 0;
        for (int i = n - 1; i >= 0; i--) {
            suffixSum[i] = suffixSum[i + 1] + counts[i];
        }

        TwoLevelMap<Integer, Integer, BigInteger> memo = new TwoLevelMap<>();
        return multisetCombinationsHelper(counts, index, k, memo, suffixSum);
    }

    private static BigInteger multisetCombinationsHelper(int[] counts, int index, int remain,
                                                         TwoLevelMap<Integer, Integer, BigInteger> memo,
                                                         int[] suffixSum) {
        if (remain == 0) return BigInteger.ONE;
        if (index == counts.length || remain > suffixSum[index]) return BigInteger.ZERO;
        if (remain == suffixSum[index]) return BigInteger.ONE;

        BigInteger cached = memo.get(index, remain);
        if (cached != null) return cached;

        BigInteger total = BigInteger.ZERO;
        int max = Math.min(counts[index], remain);
        for (int i = 0; i <= max; i++) {
            total = total.add(multisetCombinationsHelper(counts, index + 1, remain - i, memo, suffixSum));
        }

        memo.put(index, remain, total);
        return total;
    }

    @Override
    public BigInteger rencontresNumber(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        return nCr(n, k).multiply(subFactorial(n - k));
    }

    @Override
    public void clearCaches() {
        nCrMemo.clear();
        nPrMemo.clear();

        synchronized (factorialCache) {
            factorialCache.clear();
            factorialCache.add(BigInteger.ONE);
        }
        synchronized (subFactorialCache) {
            subFactorialCache.clear();
            subFactorialCache.add(BigInteger.ONE);
            subFactorialCache.add(BigInteger.ZERO);
        }
    }

    /**
     * Thread-safe two-level map for memoization.
     * Stores values indexed by two keys using nested ConcurrentHashMaps.
     */
    static class TwoLevelMap<K1, K2, V> extends ConcurrentHashMap<K1, Map<K2, V>> {

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
