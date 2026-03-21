/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.api.Calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculator for combinatorial computations with memoization.
 * Provides thread-safe methods for calculating binomial coefficients (ⁿCᵣ), permutations (ⁿPₖ),
 * factorials (n!), sub-factorials (!n), multinomial coefficients, multiset combinations, and more.
 * Uses memoization to cache frequently computed values in thread-safe collections.
 * @author Deepesh Patel
 */
public final class CalculatorImpl implements Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final List<BigInteger> factorialCache = Collections.synchronizedList(new ArrayList<>());
    private final List<BigInteger> subFactorialCache = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructs a new CalculatorImpl instance.
     * Base cases (0!, !0, !1) are initialized automatically.
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

    /**
     * Calculates the binomial coefficient with replacement (ⁿ⁺ʳ⁻¹Cᵣ / "stars and bars").
     * Represents the number of ways to choose r items from n types allowing repetition.
     * @param n number of distinct types (n ≥ 0)
     * @param r number of items to choose (r ≥ 0)
     * @return the number of multi-combinations as a BigInteger
     */
    public BigInteger nCrRepetitive(int n, int r) {
        return nCr(n + r - 1, r);
    }

    /**
     * Calculates the binomial coefficient ⁿCᵣ (n choose r).
     * Represents the number of ways to choose r distinct items from n distinct items without regard to order.
     * @param n the total number of items (n ≥ 0)
     * @param r the number of items to choose (r ≥ 0)
     * @return the binomial coefficient as a BigInteger (returns 0 if r > n or r < 0)
     */
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

    /**
     * Finds the smallest integer n such that ⁿCᵣ > max.
     * @param r  the number of items to choose (r ≥ 0)
     * @param max the threshold value to exceed
     * @return the smallest n where binomial(n, r) > max
     */
    public int nCrUpperBound(int r, BigInteger max) {
        int n = r;
        BigInteger current = nCr(n, r);
        while (current.compareTo(max) <= 0) {
            n++;
            current = nCr(n, r);
        }
        return n;
    }

    /**
     * Calculates the number of k-permutations ⁿPₖ (permutations of n things taken k at a time).
     * @param n total number of distinct items (n ≥ 0)
     * @param r number of items to arrange (0 ≤ r ≤ n)
     * @return the number of k-permutations as a BigInteger
     */
    public BigInteger nPr(int n, int r) {
        if (r < 0 || n < r) return BigInteger.ZERO;
        if (r == 0) return BigInteger.ONE;

        BigInteger cached = nPrMemo.get(n, r);
        if (cached != null) return cached;

        // Calculate iteratively
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < r; i++) {
            result = result.multiply(BigInteger.valueOf(n - i));
        }

        nPrMemo.put(n, r, result);
        return result;
    }

    /**
     * Finds the smallest integer n such that n! > value.
     * @param value the value to exceed (must be non-negative)
     * @return the smallest n where n! > value
     */
    public int factorialUpperBound(BigInteger value) {
        int n = 1;
        while (factorial(n).compareTo(value) <= 0) {
            n++;
        }
        return n;
    }

    /**
     * Calculates n! (factorial of n).
     * @param n non-negative integer
     * @return n! as a BigInteger
     * @throws IllegalArgumentException if n is negative
     */
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

    /**
     * Calculates !n (subfactorial / derangement number) — number of permutations of n items with no fixed points.
     * @param n non-negative integer
     * @return !n as a BigInteger
     * @throws IllegalArgumentException if n is negative
     */
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

    /**
     * Calculates base raised to the power of exponent (base^exponent).
     * @param base     the base number
     * @param exponent the non-negative exponent
     * @return base^exponent as a BigInteger
     * @throws IllegalArgumentException if exponent is negative
     */
    public BigInteger power(long base, long exponent) {
        return power(BigInteger.valueOf(base), BigInteger.valueOf(exponent));
    }

    /**
     * Calculates base raised to the power of exponent using binary exponentiation.
     * @param base     the base number
     * @param exponent the non-negative exponent
     * @return base^exponent as a BigInteger
     * @throws IllegalArgumentException if exponent is negative
     */
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

    /**
     * Calculates the sum of binomial coefficients ⁿCᵣ for r in [from, to].
     * Special case: if from = 0 and to = n, returns 2ⁿ efficiently.
     * @param from lower bound (inclusive, ≥ 0)
     * @param to   upper bound (inclusive, ≤ n)
     * @param n    total number of elements
     * @return sum of ⁿCᵣ from r=from to r=to as a BigInteger
     */
    public BigInteger totalSubsetsInRange(int from, int to, int n) {
        if (from == 0 && to == n) return power(2, n);
        BigInteger sum = BigInteger.ZERO;
        for (int i = Math.max(0, from); i <= Math.min(n, to); i++) {
            sum = sum.add(nCr(n, i));
        }
        return sum;
    }

    /**
     * Calculates the multinomial coefficient n! / (k₁! × k₂! × … × kₘ!)
     * where n = k₁ + k₂ + … + kₘ.
     * <p>
     * Represents the number of distinct permutations of a multiset with the given frequencies.
     * </p>
     * <p>
     * Example: multinomial(2, 1, 1) = 4!/(2!×1!×1!) = 12, which is the number of distinct
     * permutations of the multiset [A, A, B, C].
     * </p>
     *
     * @param counts non-negative integers representing frequencies of each distinct type
     * @return the multinomial coefficient as a BigInteger
     * @throws IllegalArgumentException if any count is negative
     */
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

    /**
     * Computes the number of ways to choose exactly s items from a multiset
     * for every s from 0 to floor(total/2), using bottom-up dynamic programming.
     * Due to symmetry, ways(total - s) = ways(s).
     *
     * @param frequencies array of non-negative frequencies/multiplicities
     * @return array dp where dp[s] = number of ways to choose exactly s items (s ≤ total/2)
     */
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

    /**
     * Computes the number of ways to choose exactly k items from a multiset defined by counts.
     * @param k      number of items to choose (k ≥ 0)
     * @param counts non-negative multiplicities of each distinct type
     * @return number of ways to choose exactly k items as a BigInteger
     * @throws IllegalArgumentException if k < 0 or any count < 0
     */
    public BigInteger multisetCombinationsCount(int k, int... counts) {
        return multisetCombinationsCountStartingFromIndex(k, 0, counts);
    }

    /**
     * Calculates the number of ways to select exactly k items from a multiset, starting from a given index.
     * The multiset is defined by multiplicities, considering item types from the specified index onward.
     * @param k the number of items to select (k ≥ 0)
     * @param index the starting index of item types to consider (0 ≤ index ≤ counts.length)
     * @param counts an array of non-negative integers representing the multiplicities of item types
     * @return the number of ways to select k items as a BigInteger
     * @throws IllegalArgumentException if k is negative, index is out of range, or any count is negative
     */
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

    /**
     * Calculates the Rencontres number D(n,k) = ⁿCₖ × !(n-k).
     * <p>
     * Represents the number of permutations of n elements with exactly k fixed points
     * (elements that map to themselves).
     * </p>
     * <p>
     * Example: rencontresNumber(4, 1) = 8, meaning there are 8 permutations of 4 elements
     * where exactly one element stays in its original position.
     * </p>
     *
     * @param n size of the set (n ≥ 0)
     * @param k number of fixed points (0 ≤ k ≤ n)
     * @return the Rencontres number as a BigInteger;
     *         returns {@code BigInteger.ZERO} if k < 0 or k > n
     */
    public BigInteger rencontresNumber(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        return nCr(n, k).multiply(subFactorial(n - k));
    }

    /**
     * Clears all memoization caches (binomial, permutation, factorial, subfactorial).
     * Caches will rebuild lazily on next use.
     */
    public void clearCaches() {
        nCrMemo.clear();
        nPrMemo.clear();

        synchronized (factorialCache) {
            factorialCache.clear();
            factorialCache.add(BigInteger.ONE); // restore 0!
        }
        synchronized (subFactorialCache) {
            subFactorialCache.clear();
            subFactorialCache.add(BigInteger.ONE);  // !0
            subFactorialCache.add(BigInteger.ZERO); // !1
        }
    }

    /**
     * A thread-safe two-level map for memoization of two-key values.
     * <p>
     * This map stores values indexed by a primary key (K1) and secondary key (K2).
     * It extends ConcurrentHashMap and uses nested ConcurrentHashMaps for thread safety.
     * </p>
     * <p>
     * Primarily used internally by {@link CalculatorImpl} for caching combinatorial values
     * like binomial coefficients C(n, k) where n is the primary key and k is the secondary key.
     * </p>
     *
     * @param <K1> the type of the first-level key
     * @param <K2> the type of the second-level key
     * @param <V> the type of the stored value
     * @author Deepesh Patel
     * @see <a href="https://en.wikipedia.org/wiki/Memoization">Wikipedia: Memoization</a>
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