/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;


import io.github.deepeshpatel.jnumbertools.datastructure.BinomialCoefficientMap;
import io.github.deepeshpatel.jnumbertools.datastructure.TwoLevelMap;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Calculator for combinatorial computations with memoization.
 * Provides thread-safe methods for calculating binomial coefficients (ⁿCᵣ), permutations (ⁿPₖ),
 * factorials (n!), sub-factorials (!n), multinomial coefficients, multiset combinations, and more.
 * Uses memoization to cache frequently computed values in thread-safe collections.
 * @author Deepesh Patel
 */
public final class Calculator {

    private final BinomialCoefficientMap nCrMemo = new BinomialCoefficientMap();
    private final TwoLevelMap<Integer, Integer, BigInteger> nPrMemo = new TwoLevelMap<>();
    private final TwoLevelMap<Integer, Integer, BigInteger> restrictedDerangementMemo = new TwoLevelMap<>();
    private final List<BigInteger> factorialCache = new ArrayList<>();
    private final List<BigInteger> subFactorialCache = new ArrayList<>();

    /**
     * Factorials and subfactorials up to this value are kept in dense lists (every value cached).
     * Above it only every {@link #CHECKPOINT_INTERVAL}-th value is kept, because a dense cache holds
     * O(n²) bits in total: about 290 MB at n = 20 000 and over 1 GB at n = 40 000.
     * Must be a multiple of {@link #CHECKPOINT_INTERVAL}.
     */
    private static final int DENSE_LIMIT = 2000;

    /** Spacing of the cached checkpoints above {@link #DENSE_LIMIT}; any other value costs at most this many small steps. */
    private static final int CHECKPOINT_INTERVAL = 20;

    private final ConcurrentSkipListMap<Integer, BigInteger> factorialCheckpoints = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Integer, BigInteger> subFactorialCheckpoints = new ConcurrentSkipListMap<>();

    /**
     * Constructs a new Calculator instance.
     * Base cases (0!, !0, !1) are initialized automatically.
     * No synchronization is needed during construction since the instance
     * is not accessible to other threads until construction completes.
     */
    public Calculator() {
        factorialCache.add(BigInteger.ONE); // 0! = 1
        subFactorialCache.add(BigInteger.ONE);  // !0 = 1
        subFactorialCache.add(BigInteger.ZERO); // !1 = 0
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
     * @return the binomial coefficient as a BigInteger (returns 0 if r > n or r &lt; 0)
     */
    public BigInteger nCr(int n, int r) {
        if (r == n || r == 0) return BigInteger.ONE;
        if (n < r || r < 0) return BigInteger.ZERO;
        r = Math.min(r, n - r);
        if (r == 1) return BigInteger.valueOf(n);
        if (r == 2) {
            return BigInteger.valueOf(n).multiply(BigInteger.valueOf(n - 1)).divide(BigInteger.TWO);
        }

        return nCrMemo.computeIfAbsent(n, r);
    }

    /**
     * Finds the smallest integer n such that ⁿCᵣ > max.
     * <p>
     * ⁿCᵣ is non-decreasing in n (for n ≥ r), so the answer is located by doubling an upper
     * probe until ⁿCᵣ exceeds {@code max} and then bisecting. This needs O(log n) evaluations of
     * ⁿCᵣ instead of the O(n) a linear scan would need, and avoids filling the memo with one
     * entry per scanned n.
     * </p>
     *
     * @param r  the number of items to choose (r ≥ 0)
     * @param max the threshold value to exceed
     * @return the smallest n where binomial(n, r) > max
     * @throws IllegalArgumentException if r &lt; 0, or if r == 0 and max ≥ 1 (ⁿC₀ = 1 for every n,
     *                                  so no such n exists)
     * @throws ArithmeticException      if the result does not fit in an {@code int}
     */
    public int nCrUpperBound(int r, BigInteger max) {
        if (r < 0) {
            throw new IllegalArgumentException("r must be ≥ 0, found " + r);
        }
        // Smallest possible n is r, where ʳCᵣ = 1.
        if (BigInteger.ONE.compareTo(max) > 0) {
            return r;
        }
        if (r == 0) {
            throw new IllegalArgumentException("No n satisfies ⁿC₀ > " + max + " because ⁿC₀ = 1 for all n");
        }

        // Invariant: nCr(lo, r) <= max < nCr(hi, r)
        // Probe in long arithmetic and cap at Integer.MAX_VALUE: doubling may overshoot the int range even
        // when the answer itself fits (e.g. r = 2, answer ≈ 1.4·10⁹ while the last doubling reaches 2³¹).
        long probe = Math.max(r + 1L, 2L * r);
        int lo = r;
        while (probe <= Integer.MAX_VALUE && nCr((int) probe, r).compareTo(max) <= 0) {
            lo = (int) probe;
            probe *= 2;
        }
        int hi;
        if (probe <= Integer.MAX_VALUE) {
            hi = (int) probe;
        } else {
            hi = Integer.MAX_VALUE;
            if (nCr(hi, r).compareTo(max) <= 0) {
                throw new ArithmeticException("nCrUpperBound(" + r + ", " + max + ") does not fit in an int");
            }
        }
        while (hi - lo > 1) {
            int mid = lo + ((hi - lo) >>> 1);
            if (nCr(mid, r).compareTo(max) > 0) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return hi;
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
        if (r == 1) return BigInteger.valueOf(n);
        if (r == 2) return BigInteger.valueOf(n).multiply(BigInteger.valueOf(n - 1));


        return nPrMemo.computeIfAbsent(n, r, (nVal, rVal) -> {
            BigInteger result = BigInteger.ONE;
            for (int i = nVal; i > nVal - rVal; i--) {
                result = result.multiply(BigInteger.valueOf(i));
            }
            return result;
        });
    }

    /**
     * Finds the smallest integer n such that n! > value.
     * <p>
     * The search starts at {@code n = 1}. Consequently, for any {@code value < 1}
     * (including zero and negatives) the result is {@code 1}, because {@code 1! = 1}
     * is the first factorial strictly greater than those values under this convention.
     * This matches the behaviour relied on by ranking/unranking helpers and by the unit tests.
     * </p>
     * @param value the value to exceed (must be non-negative)
     * @return the smallest n where n! > value
     */
    public int factorialUpperBound(BigInteger value) {
        for (int n = 1; n <= DENSE_LIMIT; n++) {
            if (factorial(n).compareTo(value) > 0) {
                return n;
            }
        }
        // Above the dense range, roll the product forward instead of asking the cache for every n,
        // which would make it retain one value per n again.
        BigInteger f = factorial(DENSE_LIMIT);
        int n = DENSE_LIMIT;
        while (f.compareTo(value) <= 0) {
            n++;
            f = f.multiply(BigInteger.valueOf(n));
        }
        return n;
    }

    /**
     * Calculates n! (factorial of n).
     * <p>
     * Values up to {@value #DENSE_LIMIT} are cached individually. Above that only every
     * {@value #CHECKPOINT_INTERVAL}-th value is cached, so memory stays proportional to the number of
     * checkpoints touched and any value is at most {@value #CHECKPOINT_INTERVAL} small multiplications
     * away from a checkpoint.
     * </p>
     * @param n non-negative integer
     * @return n! as a BigInteger
     * @throws IllegalArgumentException if n is negative
     */
    public BigInteger factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative input is not allowed.");
        if (n <= DENSE_LIMIT) return denseFactorial(n);

        int checkpoint = n - n % CHECKPOINT_INTERVAL;
        BigInteger value = factorialCheckpoint(checkpoint);
        for (int i = checkpoint + 1; i <= n; i++) {
            value = value.multiply(BigInteger.valueOf(i));
        }
        return value;
    }

    private BigInteger denseFactorial(int n) {
        synchronized (factorialCache) {
            while (factorialCache.size() <= n) {
                int i = factorialCache.size();
                BigInteger prev = factorialCache.get(i - 1);
                factorialCache.add(prev.multiply(BigInteger.valueOf(i)));
            }
            return factorialCache.get(n);
        }
    }

    /** Returns {@code c!} for a multiple {@code c} of the checkpoint interval, deriving it from the nearest cached neighbour. */
    private BigInteger factorialCheckpoint(int c) {
        if (c <= DENSE_LIMIT) return denseFactorial(c);
        BigInteger cached = factorialCheckpoints.get(c);
        if (cached != null) return cached;

        Map.Entry<Integer, BigInteger> below = factorialCheckpoints.lowerEntry(c);
        Map.Entry<Integer, BigInteger> above = factorialCheckpoints.higherEntry(c);
        int belowKey = below == null ? DENSE_LIMIT : below.getKey();

        BigInteger result;
        if (above != null && above.getKey() - c < c - belowKey) {
            // The next checkpoint up is closer: c! = hi! / ((c + 1) · … · hi), an exact division.
            result = above.getValue().divide(rangeProduct(c + 1, above.getKey()));
        } else {
            BigInteger start = below == null ? denseFactorial(DENSE_LIMIT) : below.getValue();
            result = start.multiply(rangeProduct(belowKey + 1, c));
        }
        factorialCheckpoints.put(c, result);
        return result;
    }

    /** Product of the integers {@code lo..hi} (1 if the range is empty), by balanced splitting so that large ranges use fast multiplication. */
    private static BigInteger rangeProduct(int lo, int hi) {
        if (lo > hi) return BigInteger.ONE;
        if (hi - lo < 8) {
            BigInteger product = BigInteger.valueOf(lo);
            for (int i = lo + 1; i <= hi; i++) {
                product = product.multiply(BigInteger.valueOf(i));
            }
            return product;
        }
        int mid = (lo + hi) >>> 1;
        return rangeProduct(lo, mid).multiply(rangeProduct(mid + 1, hi));
    }

    /**
     * Calculates !n (subfactorial / derangement number) — number of permutations of n items with no fixed points.
     * <p>
     * Cached like {@link #factorial(int)}: individually up to {@value #DENSE_LIMIT}, then at every
     * {@value #CHECKPOINT_INTERVAL}-th value. Between checkpoints it steps with
     * {@code !k = k·!(k−1) + (−1)^k}, which needs only the previous value.
     * </p>
     * @param n non-negative integer
     * @return !n as a BigInteger
     * @throws IllegalArgumentException if n is negative
     */
    public BigInteger subFactorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative input is not allowed.");
        if (n <= DENSE_LIMIT) return denseSubFactorial(n);

        int checkpoint = n - n % CHECKPOINT_INTERVAL;
        BigInteger value = subFactorialCheckpoint(checkpoint);
        for (int k = checkpoint + 1; k <= n; k++) {
            value = nextSubFactorial(value, k);
        }
        return value;
    }

    private BigInteger denseSubFactorial(int n) {
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

    /** Returns {@code !c} for a multiple {@code c} of the checkpoint interval, deriving it from the nearest cached neighbour. */
    private BigInteger subFactorialCheckpoint(int c) {
        if (c <= DENSE_LIMIT) return denseSubFactorial(c);
        BigInteger cached = subFactorialCheckpoints.get(c);
        if (cached != null) return cached;

        Map.Entry<Integer, BigInteger> below = subFactorialCheckpoints.lowerEntry(c);
        Map.Entry<Integer, BigInteger> above = subFactorialCheckpoints.higherEntry(c);
        int belowKey = below == null ? DENSE_LIMIT : below.getKey();

        BigInteger value;
        if (above != null && above.getKey() - c < c - belowKey) {
            value = above.getValue();
            for (int k = above.getKey(); k > c; k--) {
                value = previousSubFactorial(value, k);
            }
        } else {
            value = below == null ? denseSubFactorial(DENSE_LIMIT) : below.getValue();
            for (int k = belowKey + 1; k <= c; k++) {
                value = nextSubFactorial(value, k);
            }
        }
        subFactorialCheckpoints.put(c, value);
        return value;
    }

    /** {@code !k} from {@code !(k−1)}: {@code !k = k·!(k−1) + (−1)^k}. */
    private static BigInteger nextSubFactorial(BigInteger previous, int k) {
        BigInteger value = previous.multiply(BigInteger.valueOf(k));
        return (k & 1) == 0 ? value.add(BigInteger.ONE) : value.subtract(BigInteger.ONE);
    }

    /** {@code !(k−1)} from {@code !k}: {@code !(k−1) = (!k − (−1)^k) / k}, an exact division. */
    private static BigInteger previousSubFactorial(BigInteger current, int k) {
        BigInteger value = (k & 1) == 0 ? current.subtract(BigInteger.ONE) : current.add(BigInteger.ONE);
        return value.divide(BigInteger.valueOf(k));
    }

    /**
     * Counts permutations of {@code total} elements where a specific set of
     * {@code restricted} positions cannot contain their matching element.
     * <p>
     * All other positions have no restrictions.
     * </p>
     *
     * Uses inclusion-exclusion principle: iterates over all possible fixed point counts
     * and applies the complement rule. The direct subFactorial(total) optimization is
     * only beneficial when restricted == total AND total is very large; for mixed values,
     * the inclusion-exclusion approach with memoization is more efficient.
     *
     * @param total total number of elements
     * @param restricted number of specific forbidden positions
     * @return number of valid permutations
     */
    public BigInteger restrictedDerangementCount(int total, int restricted) {
        if (restricted < 0) return BigInteger.ZERO;
        if (total == 0) return BigInteger.ONE;
        if (restricted == 0) return factorial(total);

        return restrictedDerangementMemo.computeIfAbsent(total, restricted, (t, r) -> {
            BigInteger result = BigInteger.ZERO;
            BigInteger c = BigInteger.ONE; // C(r, j), stepped along the row: O(r) instead of O(r^2), and no nCr cache growth
            for (int j = 0; j <= r; j++) {
                BigInteger term = c.multiply(factorial(t - j));
                result = (j & 1) == 0 ? result.add(term) : result.subtract(term);
                c = c.multiply(BigInteger.valueOf(r - j)).divide(BigInteger.valueOf(j + 1));
            }
            return result;
        });
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
    public static int[] multisetCombinationsCountAll(int... frequencies) {
        int total = 0;
        for (int f : frequencies) {
            total += f;
        }
        int k = total / 2;

        int[] dp = new int[k + 1];
        int[] next = new int[k + 1];
        dp[0] = 1;

        // Adding a type with multiplicity f: next[s] = dp[s - f] + ... + dp[s], kept as a sliding window sum.
        // O(types * k) instead of O(types * k * f); int arithmetic wraps exactly as the direct sum would.
        for (int freq : frequencies) {
            if (freq <= 0) {
                continue; // contributes nothing (zero) or is outside the documented domain
            }
            int window = 0;
            for (int s = 0; s <= k; s++) {
                window += dp[s];
                int expired = s - freq - 1;
                if (expired >= 0) {
                    window -= dp[expired];
                }
                next[s] = window;
            }
            int[] swap = dp;
            dp = next;
            next = swap;
        }

        return dp;
    }

    /**
     * Computes the number of ways to choose exactly k items from a multiset defined by counts.
     * @param k      number of items to choose (k ≥ 0)
     * @param counts non-negative multiplicities of each distinct type
     * @return number of ways to choose exactly k items as a BigInteger
     * @throws IllegalArgumentException if k &lt; 0 or any count &lt; 0
     */
    public static BigInteger multisetCombinationsCount(int k, int... counts) {
        return multisetCombinationsCountStartingFromIndex(k, 0, counts);
    }

    /**
     * Hot-path for multiset unranking.
     * Caller MUST ensure: k >= 0, index valid, counts non-negative, suffixSum correct.
     * Caller owns the memo map lifecycle.
     * @see #multisetCombinationsCountStartingFromIndex(int, int, int...)
     */
    public static BigInteger multisetCombinationsCountWithMemo(
            int k, int index, int[] counts,
            TwoLevelMap<Integer, Integer, BigInteger> memo,
            int[] suffixSum) {

        // Base cases (inlined from helper for speed)
        if (k == 0) return BigInteger.ONE;
        if (index == counts.length || k > suffixSum[index]) return BigInteger.ZERO;
        if (k == suffixSum[index]) return BigInteger.ONE;

        return memo.computeIfAbsent(index, k, (idx, rem) -> {
            BigInteger total = BigInteger.ZERO;
            int max = Math.min(counts[idx], rem);
            for (int i = 0; i <= max; i++) {
                total = total.add(multisetCombinationsCountWithMemo(rem - i, idx + 1, counts, memo, suffixSum));
            }
            return total;
        });
    }


    /**
     * Calculates the number of ways to select exactly k items from a multiset, starting from a given index.
     * The multiset is defined by multiplicities, considering item types from the specified index onward.
     * Use #multisetCombinationsCountWithMemo(int, int, int[], TwoLevelMap, int[]) for hot-path with caller-managed memoization and suffix sums.
     * @param k the number of items to select (k ≥ 0)
     * @param index the starting index of item types to consider (0 ≤ index ≤ counts.length)
     * @param counts an array of non-negative integers representing the multiplicities of item types
     * @return the number of ways to select k items as a BigInteger
     * @throws IllegalArgumentException if k is negative, index is out of range, or any count is negative
     * @see #multisetCombinationsCountWithMemo(int, int, int[], TwoLevelMap, int[])
     */
    public static BigInteger multisetCombinationsCountStartingFromIndex(int k, int index, int... counts) {
        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative.");
        }
        if (index < 0 || index > counts.length) {
            throw new IllegalArgumentException(String.format(
                    "index must be between 0 and %d (counts.length), got %d",
                    counts.length, index));
        }

        for (int count : counts) {
            if (count < 0) {
                throw new IllegalArgumentException("Counts must be non-negative.");
            }
        }

        long total = 0;
        for (int i = index; i < counts.length; i++) {
            total += counts[i];
        }
        if (k > total) {
            return BigInteger.ZERO;
        }
        // Symmetry: choosing k items is equivalent to leaving k' = total - k items out.
        int target = (int) Math.min(k, total - k);
        BigInteger[] ways = multisetWays(counts, index, target);
        return ways[target];
    }

    /**
     * Number of ways to choose exactly j items, for every j in [0, maxK], from the multiset formed by
     * {@code counts[from..]}. Bottom-up over the item types with a sliding window sum: O((types) * maxK)
     * big-number additions, no recursion, O(maxK) memory.
     *
     * @param counts non-negative multiplicities
     * @param from   first item type to include
     * @param maxK   largest number of items of interest (maxK ≥ 0)
     * @return array of length maxK + 1 where element j is the number of ways to choose j items
     */
    static BigInteger[] multisetWays(int[] counts, int from, int maxK) {
        BigInteger[] current = new BigInteger[maxK + 1];
        BigInteger[] next = new BigInteger[maxK + 1];
        java.util.Arrays.fill(current, BigInteger.ZERO);
        current[0] = BigInteger.ONE;
        int reach = 0; // highest index that can be non-zero so far

        for (int type = from; type < counts.length; type++) {
            int c = counts[type];
            if (c == 0) {
                continue;
            }
            int newReach = (int) Math.min((long) maxK, (long) reach + c);
            // next[j] = sum of current[j-c .. j]; maintained as a window over j.
            BigInteger window = BigInteger.ZERO;
            for (int j = 0; j <= newReach; j++) {
                if (j <= reach) {
                    window = window.add(current[j]);
                }
                if (j - c - 1 >= 0 && j - c - 1 <= reach) {
                    window = window.subtract(current[j - c - 1]);
                }
                next[j] = window;
            }
            for (int j = newReach + 1; j <= maxK; j++) {
                next[j] = BigInteger.ZERO;
            }
            BigInteger[] swap = current;
            current = next;
            next = swap;
            reach = newReach;
        }
        return current;
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
     *         returns {@code BigInteger.ZERO} if k &lt; 0 or k > n
     * @see <a href="https://en.wikipedia.org/wiki/Rencontres_numbers">Rencontres numbers</a>
     */
    public BigInteger rencontresNumber(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        return nCr(n, k).multiply(subFactorial(n - k));
    }

    /**
     * Returns the nth Telephone Number (number of involutions of size n).
     *
     * <p>Recurrence:
     * T(0)=1, T(1)=1
     * T(n)=T(n-1)+(n-1)T(n-2)
     *
     * @param n n >= 0
     * @return T(n)
     * @throws IllegalArgumentException if n &lt; 0
     * @see <a href="https://en.wikipedia.org/wiki/Telephone_number_(mathematics)">Telephone Number (mathematics)</a>
     */
    public BigInteger telephoneNumber(int n) {

        // No caching required. Iterative version is fast enough for n up to 500 or more, and it avoids synchronization overhead of memoization.
        if (n < 0) { throw new IllegalArgumentException("n must be >= 0"); }
        if (n <= 1) { return BigInteger.ONE; }

        BigInteger t0 = BigInteger.ONE;
        BigInteger t1 = BigInteger.ONE;

        for (int i = 2; i <= n; i++) {
            BigInteger current = t1.add(BigInteger.valueOf(i - 1).multiply(t0));
            t0 = t1;
            t1 = current;
        }
        return t1;
    }

    /**
     * Builds the telephone-number table {@code T[0..n]} in a single O(n) pass.
     *
     * <p>Replaces per-iteration {@code calculator.telephoneNumber(free)} calls,
     * each of which recomputes the whole recurrence in O(free) big-integer
     * operations. Calling it inside the n-step encode/unrank loop costs O(n²)
     * big-integer operations; precomputing the table once and indexing into it is
     * O(n) and removes that dominant term. The remaining O(n) big-integer divisions
     * on the Θ(n log n)-bit rank are unavoidable.
     */
    public BigInteger[] telephoneTable(int n) {
        BigInteger[] T = new BigInteger[n + 1];
        T[0] = BigInteger.ONE;
        if (n >= 1) T[1] = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            T[i] = T[i - 1].add(BigInteger.valueOf(i - 1).multiply(T[i - 2]));
        }
        return T;
    }

    /**
     * Computes the greatest common divisor (GCD) of one or more BigInteger values.
     * @param nums one or more BigInteger values (null values are ignored)
     * @return the GCD of all non-null inputs, or ZERO if no valid inputs
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static BigInteger gcd(BigInteger... nums) {
        if (nums == null || nums.length == 0) throw new IllegalArgumentException("At least one number required");
        BigInteger res = nums[0] != null ? nums[0].abs() : BigInteger.ZERO;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] != null) {
                res = res.gcd(nums[i].abs());
            }
        }
        return res;
    }

    /**
     * Computes the least common multiple (LCM) of one or more BigInteger values.
     * @param nums one or more BigInteger values
     * @return the LCM of all inputs, or ZERO if any input is zero
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static BigInteger lcm(BigInteger... nums) {
        if (nums == null || nums.length == 0) throw new IllegalArgumentException("At least one number required");
        BigInteger res = BigInteger.ONE;
        for (BigInteger num : nums) {
            if (num == null || num.signum() == 0) return BigInteger.ZERO;
            res = res.multiply(num.abs()).divide(res.gcd(num.abs()));
        }
        return res;
    }

    /**
     * Clears all memoization caches (binomial, permutation, factorial, subfactorial).
     * Caches will rebuild lazily on next use.
     */
    public void  clearCaches() {
        nCrMemo.clear();
        nPrMemo.clear();
        restrictedDerangementMemo.clear();   // ← Add this

        factorialCheckpoints.clear();
        subFactorialCheckpoints.clear();
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
}
