/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.base;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 /**
 * Calculator required for combinatorics calculation. This calculator uses memoization
 * for nCr, nPr, factorial, and subfactorial values. All internal caches are implemented
 * using thread-safe collections and are properly synchronized.
 * <p>
 * Thread Safety: This class is thread safe. Instances of this class can be safely shared
 * among multiple threads. A public method {@code clearCaches()} is provided to allow users
 * to evict memoized values if needed. Note that eviction may result in temporary re-computation
 * overhead if other threads are concurrently accessing the caches.
 * </p>
 *
 *
 * @author Deepesh Patel
 */
public final class Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final List<BigInteger> factorialCache = Collections.synchronizedList(new ArrayList<>());
    private final List<BigInteger> subFactorialCache = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructs a new {@code Calculator} instance with default cache sizes.
     */
    public Calculator() {
        this(10, 10, 10,2);
    }

    /**
     * Constructs a new {@code Calculator} instance with specified cache sizes.
     *
     * @param nCrCacheSize The size of the cache for nCr values.
     * @param nPrCacheSize The size of the cache for nPr values.
     * @param factorialCacheSize The size of the cache for factorial values.
     * @param subFactorialCacheSize The size of the cache for sub-factorial values.
     */
    public Calculator(int nCrCacheSize, int nPrCacheSize, int factorialCacheSize, int subFactorialCacheSize) {
        setupFactorialCache(factorialCacheSize);
        setupSubfactorialCache(subFactorialCacheSize);
        setupCombinationCache(nCrCacheSize);
        setupPermutationCache(nPrCacheSize);
    }

    private void setupFactorialCache(int cacheSize) {
        factorialCache.add(BigInteger.ONE);
        factorial(cacheSize);
    }

    private void setupSubfactorialCache(int cacheSize) {
        subFactorialCache.add(BigInteger.ONE); // !0 = 1
        subFactorialCache.add(BigInteger.ZERO);  // !1 = 0
        subFactorial(cacheSize);
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
     * Finds the smallest n such that nCr(n, r) > max.
     *
     * @param r the number of elements to choose.
     * @param max the maximum value to exceed.
     * @return the smallest n such that nCr(n, r) > max.
     */
    public int nCrUpperBound(int r, BigInteger max) {
        int n = r;
        BigInteger nCr = nCr(n, r);

        while (nCr.compareTo(max) <= 0) {
            n++;
            nCr = nCr(n, r);
        }

        return n;
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
     * Returns the smallest integer n such that n! (n factorial) is greater than the specified non-negative integer.
     * <p>
     * This method iteratively computes factorial values starting from 1! and increments n until the computed factorial
     * exceeds {@code nonNegativeInt}. It then returns the current value of n.
     * </p>
     *
     * @param nonNegativeInt a {@code BigInteger} representing the threshold that n! must exceed; must be non-negative.
     * @return the smallest integer n for which factorial(n) > {@code nonNegativeInt}
     */
    public int factorialUpperBound(BigInteger nonNegativeInt){
        int i=1;
        while(true) {
            if(factorial(i).compareTo(nonNegativeInt) > 0) {
                return i;
            }
            i++;
        }
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
        synchronized (factorialCache) {
            while (factorialCache.size() <= n) {
                int i = factorialCache.size();
                BigInteger lastFactorial = factorialCache.get(i - 1);
                factorialCache.add(lastFactorial.multiply(BigInteger.valueOf(i)));
            }
            return factorialCache.get(n);
        }
    }

    /**
     * Computes the subfactorial (!n) using recursion with memoization.
     *
     * @param n The number for which to compute the subfactorial.
     * @return The subfactorial of n as a BigInteger.
     */
    public BigInteger subFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative input is not allowed.");
        }
        synchronized (subFactorialCache) {
            while (subFactorialCache.size() <= n) {
                int i = subFactorialCache.size();
                BigInteger value = BigInteger.valueOf(i - 1)
                        .multiply(subFactorialCache.get(i - 1).add(subFactorialCache.get(i - 2)));
                subFactorialCache.add(value);
            }
            return subFactorialCache.get(n);
        }
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

    /**
     * Calculates the multinomial coefficient for the given counts.
     * <p>
     * Given counts {n₁, n₂, ..., nₖ} where n = n₁ + n₂ + ... + nₖ, the multinomial
     * coefficient is given by:
     * <pre>
     *     multinomial = n! / (n₁! * n₂! * ... * nₖ!)
     * </pre>
     * This value represents the number of distinct permutations of a multiset
     * with the specified counts.
     * </p>
     *
     * @param counts an array of non-negative integers representing the counts of distinct items
     * @return the multinomial coefficient as a BigInteger
     * @throws IllegalArgumentException if any count is negative
     */
    public BigInteger multinomial(int... counts) {
        int total = 0;
        BigInteger denominator = BigInteger.ONE;
        for (int count : counts) {
            if (count < 0) {
                throw new IllegalArgumentException("Counts must be non-negative.");
            }
            total += count;
            denominator = denominator.multiply(factorial(count));
        }
        return factorial(total).divide(denominator);
    }

    /**
     * Computes the number of ways to select exactly s items from a multiset for all s in the range
     * [0, ⌊total⌋/2], where total is the sum of the frequencies.
     * <p>
     * The multiset is represented by an array of non-negative integers, where each element denotes
     * the frequency (available count) of a distinct item. This method uses dynamic programming to compute
     * the coefficients of the generating function:
     * <pre>
     *     ∏ (1 + x + x^2 + ... + x^(frequency))
     * </pre>
     * The returned array {@code dp} has length ⌊total/2⌋ + 1, and for each index s (0 ≤ s ≤ ⌊total/2⌋),
     * {@code dp[s]} is the number of ways to select exactly s items from the multiset.
     * </p>
     * <p>
     * Note: Due to symmetry in the generating function, the number of ways to select s items is equal to
     * the number of ways to select (total - s) items. Therefore, if you need the count for selections
     * where s > ⌊total/2⌋, you can obtain it by reading the value at index (total - s) in the full set of
     * coefficients.
     * </p>
     * <p>
     * Example: For frequencies = {2, 2, 3}, the total number of items is 2+2+3 = 7. This method returns
     * an array of size ⌊7/2⌋ + 1 = 4, where:
     * <ul>
     *   <li>dp[0] = 1   (1 way to select 0 items)</li>
     *   <li>dp[1] = 3   (3 ways to select 1 item)</li>
     *   <li>dp[2] = 6   (6 ways to select 2 items)</li>
     *   <li>dp[3] = 8   (8 ways to select 3 items)</li>
     * </ul>
     * The combinations for 4, 5, 6, and 7 items can be derived by symmetry:
     * <ul>
     *   <li>Ways to select 4 items = dp[7 - 4] = dp[3] = 8</li>
     *   <li>Ways to select 5 items = dp[7 - 5] = dp[2] = 6</li>
     *   <li>Ways to select 6 items = dp[7 - 6] = dp[1] = 3</li>
     *   <li>Ways to select 7 items = dp[7 - 7] = dp[0] = 1</li>
     * </ul>
     * </p>
     * <p>
     * If you need to calculate the number of combinations for selecting exactly a given number of items,
     * use {@link #multisetCombinationsCount(int, int...)} instead.
     * </p>
     *
     * @param frequencies an array of non-negative integers representing the available count of each item.
     * @return an array of integers where the value at index s (0 ≤ s ≤ ⌊total⌋/2) is the number of ways
     *         to select exactly s items from the multiset.
     * @see #multisetCombinationsCount(int, int...)
     */
    public static int[] multisetCombinationsCountAll(int... frequencies) {
        /*
            Note: Currently, the method iteratively updates a DP array by “convolving” with the polynomial
            (1 + x + x^2 + ... + x^freq) for each frequency. Thi is ok for small values but for very large values
            This can be optimized using FFT-based polynomial
         */

        // Initialize a DP array to store the coefficients of the generating function.
        int k = Arrays.stream(frequencies).sum() / 2;
        int[] dp = new int[k + 1];
        dp[0] = 1; //1 way to select 0 items

        for (int freq : frequencies) {
            // temp array to store the updated coefficients.
            int[] temp = new int[k + 1];

            for (int i = 0; i <= k; i++) {
                if (dp[i] == 0) continue; // no ways to select i items so far.
                for (int j = 0; j <= freq && i + j <= k; j++) {
                    temp[i + j] += dp[i];
                }
            }
            dp = temp;
        }
        return dp;
    }

    /**
     * Calculates the number of ways to select exactly k items from a multiset.
     * <p>
     * The multiset is represented by an array of non-negative integers, where each element
     * denotes the available count of a distinct item. This method uses an optimized recursive
     * dynamic programming approach with precomputed suffix sums and memoization.
     * </p>
     *
     * @param k the exact number of items to select from the multiset.
     * @param counts an array of non-negative integers representing the counts of each item type.
     * @return the total number of ways to select exactly k items from the multiset as an int.
     * @throws IllegalArgumentException if k is negative or any count is negative.
     */
    public static int multisetCombinationsCount(int k, int... counts) {
       return multisetCombinationsCountStartingFromIndex(k, 0, counts);
    }

    /**
     * Counts the number of ways to select exactly k items from a multiset defined by frequencies,
     * considering only the item types from the specified index onward.
     * <p>
     * This method uses an optimized recursive dynamic programming approach with precomputed suffix sums
     * and memoization.
     * </p>
     *
     * @param k      the exact number of items to select from the multiset.
     * @param index  the starting index from which to consider item types.
     * @param counts an array of non-negative integers representing the counts of each item type.
     * @return the total number of ways to select exactly k items from the multiset from index onward.
     * @throws IllegalArgumentException if k is negative, if index is out of range, or if any count is negative.
     */
    public static int multisetCombinationsCountStartingFromIndex(int k, int index, int... counts) {
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
        // Precompute suffix sum: suffixSum[i] = counts[i] + counts[i+1] + ... + counts[n-1]
        int[] suffixSum = new int[n + 1];
        suffixSum[n] = 0;
        for (int i = n - 1; i >= 0; i--) {
            suffixSum[i] = suffixSum[i + 1] + counts[i];
        }
        TwoLevelMap<Integer, Integer, Integer> memo = new TwoLevelMap<>();
        return multisetCombinationsHelper(counts, index, k, memo, suffixSum);
    }

    private static int multisetCombinationsHelper(int[] counts, int index, int k,
                                           TwoLevelMap<Integer, Integer, Integer> memo, int[] suffixSum) {
        if (k == 0) {
            return 1;
        }
        if (index == counts.length || k > suffixSum[index]) {
            return 0;
        }

        if (k == suffixSum[index]) {
            return 1;
        }
        Integer cached = memo.get(index, k);
        if (cached != null) {
            return cached;
        }
        int total = 0;
        int max = Math.min(counts[index], k);
        for (int i = 0; i <= max; i++) {
            total += multisetCombinationsHelper(counts, index + 1, k - i, memo, suffixSum);
        }
        memo.put(index, k, total);
        return total;
    }

    /**
     * Computes the Rencontres number, which represents the number of permutations
     * of size n with exactly k fixed points.
     *
     * @param n The size of the permutation.
     * @param k The number of fixed points.
     * @return The Rencontres number for (n, k) as a BigInteger.
     */
    public BigInteger rencontresNumber(int n, int k) {
        if (k > n || k < 0) {
            return BigInteger.ZERO; // No valid permutations
        }
        return nCr(n, k).multiply(subFactorial(n - k));
    }

    /**
     * Clears all memoization caches used by this Calculator.
     * <p>
     * This method clears the caches for nCr, nPr, factorial, and subfactorial values.
     * Note that if another thread is concurrently computing a value while this method is called,
     * that computation may be recomputed and re-cached. The Calculator remains thread safe,
     * but eviction may result in temporary re-computation overhead.
     * </p>
     */
    public void clearCaches() {
        nCrMemo.clear();
        nPrMemo.clear();
        setupCombinationCache(3);
        setupPermutationCache(3);

        synchronized (factorialCache) {
            factorialCache.clear();
            subFactorialCache.clear();
            setupFactorialCache(3);
            setupSubfactorialCache(3);
        }
    }

    /**
     * A simple two-level map for memoization.
     *
     * @param <K1> the type of the first key.
     * @param <K2> the type of the second key.
     * @param <V>  the type of the value.
     */
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
