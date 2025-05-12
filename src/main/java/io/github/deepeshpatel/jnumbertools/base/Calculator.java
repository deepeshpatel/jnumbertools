/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import java.math.BigInteger;
import java.util.*;

/**
 * Calculator for combinatorial computations with memoization.
 *
 * Provides thread-safe methods for calculating binomial coefficients (`ⁿCᵣ`, `ⁿ⁺ᵣ⁻¹Cᵣ`),
 * permutations (`ⁿPₖ`), factorials (`n!`), subfactorials (`!n`), multinomial coefficients
 * (`n! / Π(nᵢ!)`), multiset combinations, and more. Uses memoization to cache frequently
 * computed values (`ⁿCᵣ`, `ⁿPₖ`, `n!`, `!n`) in thread-safe collections with synchronized
 * access. The `clearCaches()` method allows cache eviction, which may cause temporary
 * re-computation overhead if accessed concurrently.
 *
 * Example usage:
 * ```
 * Calculator calc = new Calculator();
 * BigInteger nCr = calc.nCr(5, 2); // 10
 * BigInteger factorial = calc.factorial(5); // 120
 * BigInteger multinomial = calc.multinomial(2, 2, 1); // 30
 * ```
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class Calculator {

    private final TwoLevelMap<Integer, Integer, BigInteger> nCrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final List<BigInteger> factorialCache = Collections.synchronizedList(new ArrayList<>());
    private final List<BigInteger> subFactorialCache = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructs a new Calculator instance with default cache sizes.
     */
    public Calculator() {
        this(10, 10, 10, 2);
    }

    /**
     * Constructs a new Calculator instance with specified cache sizes.
     *
     * @param nCrCacheSize the size of the cache for `ⁿCᵣ` values
     * @param nPrCacheSize the size of the cache for `ⁿPₖ` values
     * @param factorialCacheSize the size of the cache for `n!` values
     * @param subFactorialCacheSize the size of the cache for `!n` values
     */
    public Calculator(int nCrCacheSize, int nPrCacheSize, int factorialCacheSize, int subFactorialCacheSize) {
        setupFactorialCache(factorialCacheSize);
        setupSubFactorialCache(subFactorialCacheSize);
        setupCombinationCache(nCrCacheSize);
        setupPermutationCache(nPrCacheSize);
    }

    private void setupFactorialCache(int cacheSize) {
        factorialCache.add(BigInteger.ONE);
        factorial(cacheSize);
    }

    private void setupSubFactorialCache(int cacheSize) {
        subFactorialCache.add(BigInteger.ONE); // !0 = 1
        subFactorialCache.add(BigInteger.ZERO); // !1 = 0
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
     * Calculates the binomial coefficient with replacement (`ⁿ⁺ᵣ⁻¹Cᵣ`).
     *
     * Represents the number of ways to choose r items from n distinct types, allowing repetition.
     *
     * @param n the number of distinct item types (n ≥ 0)
     * @param r the number of items to choose (r ≥ 0)
     * @return the number of combinations with replacement as a BigInteger
     */
    public BigInteger nCrRepetitive(int n, int r) {
        return nCr(n + r - 1, r);
    }

    /**
     * Calculates the binomial coefficient (`ⁿCᵣ`) for unique combinations.
     *
     * Represents the number of ways to choose r distinct items from n distinct items without regard to order.
     *
     * @param n the number of distinct items (n ≥ 0)
     * @param r the number of items to choose (0 ≤ r ≤ n)
     * @return the number of unique combinations as a BigInteger
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
     * Finds the smallest n such that `ⁿCᵣ` exceeds max.
     *
     * Used to determine the minimal set size for combination ranking where the number of combinations exceeds a threshold.
     *
     * @param r the number of items to choose (r ≥ 0)
     * @param max the threshold to exceed (max ≥ 0)
     * @return the smallest n such that `ⁿCᵣ` > max
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
     * Calculates the number of k-permutations (`ⁿPₖ`).
     *
     * Represents the number of ways to arrange k distinct items from n distinct items, where order matters.
     *
     * @param n the number of distinct items (n ≥ 0)
     * @param r the number of items to arrange (0 ≤ r ≤ n)
     * @return the number of k-permutations as a BigInteger
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
     * Finds the smallest n such that n! exceeds the specified value.
     *
     * Iteratively computes factorials until n! > value, used for permutation ranking thresholds.
     *
     * @param nonNegativeInt the threshold to exceed (nonNegativeInt ≥ 0)
     * @return the smallest n such that n! > nonNegativeInt
     */
    public int factorialUpperBound(BigInteger nonNegativeInt) {
        int i = 1;
        while (true) {
            if (factorial(i).compareTo(nonNegativeInt) > 0) {
                return i;
            }
            i++;
        }
    }

    /**
     * Calculates the factorial of a non-negative integer (`n!`).
     *
     * Represents the product of all positive integers up to n.
     *
     * @param n the integer to calculate the factorial for (n ≥ 0)
     * @return the factorial of n as a BigInteger
     * @throws IllegalArgumentException if n is negative
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
     * Calculates the subfactorial of a non-negative integer (`!n`).
     *
     * Represents the number of derangements (permutations with no fixed points) of n items.
     *
     * @param n the integer to calculate the subfactorial for (n ≥ 0)
     * @return the subfactorial of n as a BigInteger
     * @throws IllegalArgumentException if n is negative
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
     * Calculates the power of a base raised to an exponent (`baseᵉˣᵖᵒⁿᵉⁿᵗ`).
     *
     * @param base the base number
     * @param exponent the exponent (exponent ≥ 0)
     * @return the result of base^exponent as a BigInteger
     */
    public BigInteger power(long base, long exponent) {
        return power(BigInteger.valueOf(base), BigInteger.valueOf(exponent));
    }

    /**
     * Calculates the power of a base raised to an exponent (`baseᵉˣᵖᵒⁿᵉⁿᵗ`).
     *
     * @param base the base number
     * @param exponent the exponent (exponent ≥ 0)
     * @return the result of base^exponent as a BigInteger
     * @throws IllegalArgumentException if exponent is negative
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
     * Calculates the total number of subsets of sizes in the range [from, to] for n elements (`∑ⁿCᵣ`).
     *
     * Represents the sum of binomial coefficients `ⁿCᵣ` for r from `from` to `to`, or 2ⁿ if from=0 and to=n.
     *
     * @param from the minimum subset size (from ≥ 0)
     * @param to the maximum subset size (to ≤ n)
     * @param n the total number of elements (n ≥ 0)
     * @return the total number of subsets as a BigInteger
     */
    public BigInteger totalSubsetsInRange(int from, int to, int n) {
        if (from == 0 && to == n) return power(2, n);

        BigInteger sum = BigInteger.ZERO;
        for (int i = from; i <= to; i++) {
            sum = sum.add(nCr(n, i));
        }
        return sum;
    }

    /**
     * Calculates the multinomial coefficient (`n! / Π(nᵢ!)`) for the given counts.
     *
     * Represents the number of distinct permutations of a multiset with counts {n₁, n₂, ..., nₖ},
     * where n = n₁ + n₂ + ... + nₖ.
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
     * Calculates the total number of multiset permutations for every m-th rank.
     *
     * Used to determine the number of permutations between rank `start` and the last rank, stepping by `m`.
     *
     * @param start the starting rank (start ≥ 0)
     * @param m the step size (m ≥ 1)
     * @param counts an array of non-negative integers representing the counts of distinct items
     * @return the total number of permutations as a BigInteger
     */
    public BigInteger totalMthMultinomial(long start, long m, int... counts) {
        return multinomial(counts)
                .subtract(BigInteger.ONE)
                .subtract(BigInteger.valueOf(start))
                .divide(BigInteger.valueOf(m))
                .add(BigInteger.ONE);
    }

    /**
     * Calculates the number of ways to select exactly s items from a multiset for all s in [0, ⌊total/2⌋].
     *
     * The multiset is defined by multiplicities, where each integer represents the count of a distinct item type.
     * Uses dynamic programming to compute coefficients of the generating function Π(1 + x + x² + ... + xᶠʳᵉᵠᵘᵉⁿᶜʸ).
     * Returns an array dp of length ⌊total/2⌋ + 1, where dp[s] is the number of ways to select exactly s items.
     * Due to symmetry, ways to select (total - s) items equals dp[s].
     *
     * Example: For multiplicities {2, 2, 3} (total=7), returns dp[0..3]:
     * - dp[0] = 1 (select 0 items)
     * - dp[1] = 3 (select 1 item)
     * - dp[2] = 6 (select 2 items)
     * - dp[3] = 8 (select 3 items)
     * Symmetry gives: ways for 4=dp[3], 5=dp[2], 6=dp[1], 7=dp[0].
     *
     * Use `multisetCombinationsCount` for a specific s.
     *
     * @param frequencies an array of non-negative integers representing the multiplicities of item types
     * @return an array where dp[s] is the number of ways to select s items (0 ≤ s ≤ ⌊total/2⌋)
     * @see #multisetCombinationsCount(int, int...)
     */
    public static int[] multisetCombinationsCountAll(int... frequencies) {
        /*
         * Note: Currently, the method iteratively updates a DP array by “convolving” with the polynomial
         * (1 + x + x^2 + ... + x^freq) for each frequency. This is ok for small values but for very large values
         * this can be optimized using FFT-based polynomial.
         */

        // Initialize a DP array to store the coefficients of the generating function.
        int k = Arrays.stream(frequencies).sum() / 2;
        int[] dp = new int[k + 1];
        dp[0] = 1; // 1 way to select 0 items

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
     *
     * The multiset is defined by multiplicities, where each integer represents the count of a distinct item type.
     * Uses recursive dynamic programming with memoization and precomputed suffix sums for efficiency.
     *
     * @param k the number of items to select (k ≥ 0)
     * @param counts an array of non-negative integers representing the multiplicities of item types
     * @return the number of ways to select k items as a BigInteger
     * @throws IllegalArgumentException if k or any count is negative
     */
    public static BigInteger multisetCombinationsCount(int k, int... counts) {
        return multisetCombinationsCountStartingFromIndex(k, 0, counts);
    }

    /**
     * Calculates the number of ways to select exactly k items from a multiset, starting from a given index.
     *
     * The multiset is defined by multiplicities, considering item types from the specified index onward.
     * Uses recursive dynamic programming with memoization and precomputed suffix sums for efficiency.
     *
     * @param k the number of items to select (k ≥ 0)
     * @param index the starting index of item types to consider (0 ≤ index ≤ counts.length)
     * @param counts an array of non-negative integers representing the multiplicities of item types
     * @return the number of ways to select k items as a BigInteger
     * @throws IllegalArgumentException if k is negative, index is out of range, or any count is negative
     */
    public static BigInteger multisetCombinationsCountStartingFromIndex(int k, int index, int... counts) {
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
        TwoLevelMap<Integer, Integer, BigInteger> memo = new TwoLevelMap<>();
        return multisetCombinationsHelper(counts, index, k, memo, suffixSum);
    }

    private static BigInteger multisetCombinationsHelper(int[] counts, int index, int k,
                                                         TwoLevelMap<Integer, Integer, BigInteger> memo, int[] suffixSum) {
        if (k == 0) {
            return BigInteger.ONE;
        }
        if (index == counts.length || k > suffixSum[index]) {
            return BigInteger.ZERO;
        }

        if (k == suffixSum[index]) {
            return BigInteger.ONE;
        }
        BigInteger cached = memo.get(index, k);
        if (cached != null) {
            return cached;
        }
        BigInteger total = BigInteger.ZERO;
        int max = Math.min(counts[index], k);
        for (int i = 0; i <= max; i++) {
            total = total.add(multisetCombinationsHelper(counts, index + 1, k - i, memo, suffixSum));
        }
        memo.put(index, k, total);
        return total;
    }

    /**
     * Calculates the Rencontres number (`ⁿCₖ · !(n-k)`).
     *
     * Represents the number of permutations of n items with exactly k fixed points (elements mapped to themselves).
     *
     * @param n the size of the permutation (n ≥ 0)
     * @param k the number of fixed points (0 ≤ k ≤ n)
     * @return the Rencontres number as a BigInteger
     */
    public BigInteger rencontresNumber(int n, int k) {
        if (k > n || k < 0) {
            return BigInteger.ZERO; // No valid permutations
        }
        return nCr(n, k).multiply(subFactorial(n - k));
    }

    /**
     * Calculates the Greatest Common Divisor (GCD) of multiple BigInteger numbers.
     *
     * Uses the binary GCD algorithm iteratively. Returns 0 if all inputs are 0, the absolute value of a single input,
     * or throws an exception if the input array is empty or null.
     *
     * @param a variable number of BigInteger inputs
     * @return the GCD of all inputs as a BigInteger
     * @throws IllegalArgumentException if the input array is null or empty
     */
    public static BigInteger gcd(BigInteger... a) {
        BigInteger result = a[0] == null ? BigInteger.ZERO : a[0].abs();
        for (int i = 1; i < a.length; i++) {
            if (a[i] != null) {
                result = result.gcd(a[i].abs());
            }
        }
        return result;
    }

    /**
     * Calculates the Least Common Multiple (LCM) of multiple BigInteger numbers.
     *
     * Computes LCM iteratively using LCM(a, b) = |a * b| / GCD(a, b). Returns 0 if any input is 0,
     * the absolute value of a single input, or throws an exception if the input array is empty or null.
     *
     * @param a variable number of BigInteger inputs
     * @return the LCM of all inputs as a BigInteger
     * @throws IllegalArgumentException if the input array is null or empty
     */
    public static BigInteger lcm(BigInteger... a) {
        return lcmTree(a, 0, a.length - 1);
    }

    private static BigInteger lcmTree(BigInteger[] arr, int start, int end) {
        if (end - start + 1 <= 10) { // THRESHOLD to switch to iterative
            return iterativeLCM(arr, start, end);
        }

        int mid = (start + end) / 2;
        BigInteger left = lcmTree(arr, start, mid);
        BigInteger right = lcmTree(arr, mid + 1, end);
        return lcmPair(left, right);
    }

    private static BigInteger iterativeLCM(BigInteger[] arr, int start, int end) {
        BigInteger result = BigInteger.ONE;
        for (int i = start; i <= end; i++) {
            result = lcmPair(result, arr[i]);
        }
        return result;
    }

    /**
     * Helper method to compute LCM of two BigInteger numbers.
     */
    private static BigInteger lcmPair(BigInteger a, BigInteger b) {
        return a.abs().multiply(b.abs()).divide(a.gcd(b));
    }

    /**
     * Clears all memoization caches (`ⁿCᵣ`, `ⁿPₖ`, `n!`, `!n`).
     *
     * Eviction may cause temporary re-computation overhead if accessed concurrently. The Calculator remains thread-safe.
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
            setupSubFactorialCache(3);
        }
    }
}