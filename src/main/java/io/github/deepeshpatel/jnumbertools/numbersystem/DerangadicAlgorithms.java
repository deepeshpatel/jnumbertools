/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.FenwickTree;

import java.math.BigInteger;
import java.util.*;

/**
 * Derangadic Number System for Derangements.
 * <p>
 * A combinatorial number system for fixed-point-free permutations (derangements).
 * Provides a bijective mapping between integers {@code [0, D_n - 1]} and derangements
 * of {@code n} elements in lexicographical order, where {@code D_n} is the number
 * of derangements (subfactorial !n).
 * </p>
 * <p>
 * The name "Derangadic" combines "Derangement" with "Combinadic" (combinatorial number system).
 * </p>
 *
 * <h3>Digit Array Representation and Trailing Zeros</h3>
 * <p>
 * The implementation uses variable-length encoding where the returned digit array has
 * length equal to the minimal effective size (actualN). This is the smallest number
 * with the same parity as {@code n} such that {@code D_actualN > rank}.
 * </p>
 * <p>
 * <strong>Important:</strong> Trailing zeros are preserved in the digit array to maintain
 * consistent length based on the minimal effective size calculation. When comparing
 * digit arrays for equality, trailing zeros should be ignored as they don't affect
 * the rank value. Use {@code arraysEqualIgnoringTrailingZeros()} for comparison.
 * </p>
 *
 * <h3>Example: n=4, D₄=9</h3>
 * <p>
 * For n=4, the minimal effective size (actualN) varies by rank:
 * </p>
 * <pre>
 * Rank 0 → actualN = 2 → digits [0, 0]           → derangement [1, 0, 3, 2]
 * Rank 1 → actualN = 4 → digits [0, 1, 1, 0]     → derangement [1, 2, 3, 0]
 * Rank 2 → actualN = 4 → digits [0, 0, 2, 0]     → derangement [1, 3, 0, 2]
 * Rank 3 → actualN = 4 → digits [0, 2, 1, 0]     → derangement [2, 0, 3, 1]
 * Rank 4 → actualN = 4 → digits [0, 2, 3, 1]     → derangement [2, 3, 0, 1]
 * Rank 5 → actualN = 4 → digits [1, 0, 1, 0]     → derangement [2, 3, 1, 0]
 * Rank 6 → actualN = 4 → digits [1, 0, 2, 1]     → derangement [3, 0, 1, 2]
 * Rank 7 → actualN = 4 → digits [1, 1, 1, 0]     → derangement [3, 2, 0, 1]
 * Rank 8 → actualN = 4 → digits [0, 1, 1, 2]     → derangement [3, 2, 1, 0]
 * </pre>
 * <p>
 * Note that trailing zeros are preserved (e.g., rank 1 has {@code [0,1,1,0]} not {@code [0,1,1]}).
 * When comparing digits, {@code [0,1,1,0]} is considered equal to {@code [0,1,1]} since
 * trailing zeros don't affect the rank value.
 * </p>
 *
 * @author Deepesh Patel & Aditya Patel
 * @since 3.0.2
 */
public final class DerangadicAlgorithms {

    // Precomputed derangement numbers: D[i] = !i
    private BigInteger[] derangementCounts;
    private Calculator calculator;

    // Cache for restricted derangements
    private final Map<String, BigInteger> restrictedCache;

    // Thresholds for array vs fenwick
    private static final int N_THRESHOLD = 100;
    private static final int DIGIT_THRESHOLD = 100;

    /**
     * Creates a DerangadicAlgorithms instance.
     */
    public DerangadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
        this.restrictedCache = new HashMap<>();
        initialize(1); // Initialize with !0 and !1
    }

    public DerangadicAlgorithms() {
        this(new Calculator());
    }
    // ==================== Core Combinatorial Functions ====================

    /**
     * Computes subfactorial !n using recurrence: !0=1, !1=0, !n=(n-1)*(!(n-1)+!(n-2))
     */
    private BigInteger subFactorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        ensureDerangementCapacity(n);
        return derangementCounts[n];
    }

    /**
     * Ensures derangementCounts array has capacity for n.
     */
    private void ensureDerangementCapacity(int n) {
        if (derangementCounts != null && derangementCounts.length > n) {
            return;
        }

        int start = (derangementCounts == null) ? 0 : derangementCounts.length;
        BigInteger[] newCache = new BigInteger[n + 1];

        if (derangementCounts != null) {
            System.arraycopy(derangementCounts, 0, newCache, 0, derangementCounts.length);
        }

        for (int i = start; i <= n; i++) {
            if (i == 0) {
                newCache[i] = BigInteger.ONE;
            } else if (i == 1) {
                newCache[i] = BigInteger.ZERO;
            } else {
                BigInteger prev1 = newCache[i - 1];
                BigInteger prev2 = newCache[i - 2];
                newCache[i] = BigInteger.valueOf(i - 1).multiply(prev1.add(prev2));
            }
        }

        derangementCounts = newCache;
    }

    /**
     * Precomputes derangement numbers up to {@code n}.
     */
    private void initialize(int n) {
        ensureDerangementCapacity(n);
    }

    /**
     * Computes restricted derangements using inclusion-exclusion:
     * D(n, k) = sum_{j=0}^{k} (-1)^j * C(k, j) * (n-j)!
     */
    private BigInteger restrictedDerangements(int total, int restricted) {
        if (restricted < 0) return BigInteger.ZERO;
        if (total == 0) return BigInteger.ONE;

        String key = total + "," + restricted;
        return restrictedCache.computeIfAbsent(key, k -> {
            BigInteger result = BigInteger.ZERO;
            for (int j = 0; j <= restricted; j++) {
                BigInteger term = calculator.nCr(restricted, j)
                        .multiply(calculator.factorial(total - j));
                if ((j & 1) == 0) {
                    result = result.add(term);
                } else {
                    result = result.subtract(term);
                }
            }
            return result;
        });
    }

    // ==================== Core Public API ====================

    /**
     * Returns the number of derangements for {@code n} elements.
     *
     * @param n number of elements
     * @return !n (subfactorial of n)
     */
    public BigInteger derangementCount(int n) {
        return subFactorial(n);
    }

    /**
     * Counts completions for the remaining structure.
     */
    private BigInteger countCompletions(Set<Integer> remainingPositions,
                                        Set<Integer> remainingElements) {
        int size = remainingPositions.size();
        int restrictedCount = 0;
        for (int pos : remainingPositions) {
            if (remainingElements.contains(pos)) {
                restrictedCount++;
            }
        }
        return restrictedDerangements(size, restrictedCount);
    }

    /**
     * Converts a decimal rank to Derangadic digits (variable-length array).
     * <p>
     * The returned array has length equal to the minimal effective size
     * (the smallest number with same parity as {@code n} where {@code D_len > rank}).
     * Missing positions (the first {@code n - actualN} positions) are implicitly zero.
     * </p>
     *
     * @param m decimal rank (0 ≤ m < D_n)
     * @param n order (number of elements)
     * @return Derangadic digit array of length actualN (≤ n)
     * @throws IllegalArgumentException if {@code m} is out of range
     */
    public int[] toDerangadic(BigInteger m, int n) {
        initialize(n);
        BigInteger max = derangementCount(n);
        if (m.signum() < 0 || m.compareTo(max) >= 0) {
            throw new IllegalArgumentException("m out of range");
        }

        int actualN = smallestN(n, m);
        int[] digits = new int[actualN];

        boolean[] elementUsed = new boolean[actualN];
        BigInteger currentM = m;

        for (int step = 0; step < actualN; step++) {
            int pos = step;
            int remainingSize = actualN - step;

            // Recalculate restrictedCount: How many i > step exist such that
            // position i is unfilled and element i is unused.
            int restrictedCount = 0;
            for (int i = step; i < actualN; i++) {
                if (!elementUsed[i]) {
                    restrictedCount++;
                }
            }

            int legalFoundCount = 0;
            BigInteger cumulative = BigInteger.ZERO;

            for (int candidate = 0; candidate < actualN; candidate++) {
                if (elementUsed[candidate] || candidate == pos) continue;

                boolean pickingRestricted = (candidate > step);
                boolean currentPosWasRestricted = (!elementUsed[pos]);

                int nextRestricted;
                if (pickingRestricted && currentPosWasRestricted) {
                    nextRestricted = restrictedCount - 2;
                } else if (pickingRestricted || currentPosWasRestricted) {
                    nextRestricted = restrictedCount - 1;
                } else {
                    nextRestricted = restrictedCount;
                }

                nextRestricted = Math.max(0, nextRestricted);
                BigInteger blockSize = restrictedDerangements(remainingSize - 1, nextRestricted);

                if (currentM.compareTo(cumulative.add(blockSize)) < 0) {
                    digits[actualN - 1 - step] = legalFoundCount;
                    elementUsed[candidate] = true;
                    currentM = currentM.subtract(cumulative);
                    break;
                }

                cumulative = cumulative.add(blockSize);
                legalFoundCount++;
            }
        }

        return digits;
    }

    /**
     * Converts a decimal rank to Derangadic digits (long version).
     *
     * @param m decimal rank (0 ≤ m < D_n)
     * @param n order
     * @return Derangadic digit array
     */
    public int[] toDerangadic(long m, int n) {
        return toDerangadic(BigInteger.valueOf(m), n);
    }
    /**
     * Converts variable-length Derangadic digits back to decimal rank.
     * <p>
     * The input array is treated as having implicit zeros padded at the end.
     * </p>
     *
     * @param digits Derangadic digit array (length = actualN)
     * @param n full order
     * @return decimal rank
     */
    public BigInteger fromDerangadic(int[] digits, int n) {
        initialize(n);
        ZeroPaddedList allDigits = new ZeroPaddedList(digits, n);

        Set<Integer> remainingElements = new HashSet<>();
        Set<Integer> remainingPositions = new HashSet<>();
        for (int i = 0; i < n; i++) {
            remainingElements.add(i);
            remainingPositions.add(i);
        }

        BigInteger result = BigInteger.ZERO;
        List<Integer> positionsList = new ArrayList<>(remainingPositions);
        Collections.sort(positionsList);

        for (int step = 0; step < n; step++) {
            int pos = positionsList.get(0);

            List<Integer> legal = new ArrayList<>();
            for (int e : remainingElements) {
                if (e != pos) legal.add(e);
            }
            Collections.sort(legal);

            int digit = allDigits.get(n - 1 - step);

            for (int idx = 0; idx < digit; idx++) {
                int candidate = legal.get(idx);

                Set<Integer> newRemainingElements = new HashSet<>(remainingElements);
                newRemainingElements.remove(candidate);

                Set<Integer> newRemainingPositions = new HashSet<>(remainingPositions);
                newRemainingPositions.remove(pos);

                result = result.add(countCompletions(newRemainingPositions, newRemainingElements));
            }

            int chosen = legal.get(digit);
            remainingElements.remove(chosen);
            remainingPositions.remove(pos);
            positionsList = new ArrayList<>(remainingPositions);
            Collections.sort(positionsList);
        }

        return result;
    }

    /**
     * Converts variable-length Derangadic digits to a derangement.
     * <p>
     * The input array is treated as having implicit zeros padded at the end.
     * </p>
     *
     * @param digits Derangadic digit array (length = actualN)
     * @param n full order
     * @return derangement array of length n
     */
    public int[] toDerangement(int[] digits, int n) {
        if (n < N_THRESHOLD || digits.length < DIGIT_THRESHOLD) {
            return toDerangementArray(digits, n);
        } else {
            return toDerangementFenwick(digits, n);
        }
    }

    /**
     * Array-based implementation for small n.
     * O(n²) but with low constant factor - best for n ≤ 100.
     */
    int[] toDerangementArray(int[] digits, int n) {
        int actualN = digits.length;
        int offset = n - actualN;

        int[] derangement = new int[n];
        boolean[] used = new boolean[n];

        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && used[nextCandidate]) {
                nextCandidate++;
            }

            int chosen = nextCandidate;
            if (chosen == pos) {
                int temp = nextCandidate + 1;
                while (temp < n && used[temp]) {
                    temp++;
                }
                chosen = temp;
            }

            derangement[pos] = chosen;
            used[chosen] = true;

            if (chosen == nextCandidate) {
                nextCandidate++;
            }
        }

        for (int step = 0; step < actualN; step++) {
            int pos = offset + step;
            int digit = digits[actualN - 1 - step];

            int count = 0;
            int chosen = -1;
            for (int e = 0; e < n; e++) {
                if (used[e]) continue;
                if (e == pos) continue;
                if (count == digit) {
                    chosen = e;
                    break;
                }
                count++;
            }

            derangement[pos] = chosen;
            used[chosen] = true;
        }

        return derangement;
    }

    /**
     * Fenwick Tree implementation for large n.
     * O(n + actualN × log n) - best for n > 100.
     */
    int[] toDerangementFenwick(int[] digits, int n) {
        int actualN = digits.length;
        int offset = n - actualN;

        int[] derangement = new int[n];
        FenwickTree availableElements = new FenwickTree(n);

        for (int i = 1; i <= n; i++) {
            availableElements.update(i, 1);
        }

        int nextAvailable = 1;
        for (int pos = 0; pos < offset; pos++) {
            while (nextAvailable <= n && availableElements.rsq(nextAvailable) - availableElements.rsq(nextAvailable - 1) == 0) {
                nextAvailable++;
            }

            int chosenIdx = nextAvailable;
            if (chosenIdx == pos + 1) {
                if (nextAvailable + 1 <= n) {
                    chosenIdx = nextAvailable + 1;
                    while (chosenIdx <= n && availableElements.rsq(chosenIdx) - availableElements.rsq(chosenIdx - 1) == 0) {
                        chosenIdx++;
                    }
                }
            }

            derangement[pos] = chosenIdx - 1;
            availableElements.update(chosenIdx, -1);

            if (chosenIdx == nextAvailable) {
                nextAvailable++;
                while (nextAvailable <= n && availableElements.rsq(nextAvailable) - availableElements.rsq(nextAvailable - 1) == 0) {
                    nextAvailable++;
                }
            }
        }

        for (int step = 0; step < actualN; step++) {
            int pos = offset + step;
            int digit = digits[actualN - 1 - step];

            int chosen;
            boolean posAvailable = (availableElements.rsq(pos + 1) - availableElements.rsq(pos)) == 1;

            if (posAvailable) {
                int posRank = availableElements.rsq(pos);
                if (digit < posRank) {
                    chosen = availableElements.findKth(digit + 1);
                } else {
                    chosen = availableElements.findKth(digit + 2);
                }
            } else {
                chosen = availableElements.findKth(digit + 1);
            }

            derangement[pos] = chosen - 1;
            availableElements.update(chosen, -1);
        }

        return derangement;
    }

    /**
     * Converts a derangement back to variable-length Derangadic digits.
     * <p>
     * Returns the minimal encoding by computing the full digit array and
     * trimming trailing zeros.
     * </p>
     *
     * @param derangement derangement array (full size n)
     * @param n order
     * @return Derangadic digit array of minimal length
     * @throws IllegalArgumentException if {@code derangement} is not a valid derangement
     */
    public int[] fromDerangement(int[] derangement, int n) {
        initialize(n);

        // Validate derangement
        for (int i = 0; i < n; i++) {
            if (derangement[i] == i) {
                throw new IllegalArgumentException(
                        "Not a derangement: fixed point at position " + i
                );
            }
        }

        // Compute full digits (length n)
        int[] fullDigits = new int[n];
        List<Integer> remainingElements = new ArrayList<>();
        for (int i = 0; i < n; i++) remainingElements.add(i);
        List<Integer> remainingPositions = new ArrayList<>();
        for (int i = 0; i < n; i++) remainingPositions.add(i);

        for (int step = 0; step < n; step++) {
            int pos = remainingPositions.get(0);

            List<Integer> legal = new ArrayList<>();
            for (int e : remainingElements) {
                if (e != pos) legal.add(e);
            }
            Collections.sort(legal);

            int chosen = derangement[pos];
            int digit = legal.indexOf(chosen);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid derangement at position " + pos);
            }
            fullDigits[n - 1 - step] = digit;

            remainingElements.remove(Integer.valueOf(chosen));
            remainingPositions.remove(0);
        }

        // Trim trailing zeros
        int lastNonZero = fullDigits.length - 1;
        while (lastNonZero >= 0 && fullDigits[lastNonZero] == 0) {
            lastNonZero--;
        }

        if (lastNonZero < 0) {
            return new int[]{0};
        }

        int[] result = new int[lastNonZero + 1];
        System.arraycopy(fullDigits, 0, result, 0, lastNonZero + 1);
        return result;
    }

    /**
     * Direct conversion: rank → derangement (without intermediate digits).
     *
     * @param rank the 0-based lexicographical rank (0 ≤ rank < D_n)
     * @param n the order (number of elements)
     * @return the derangement as an array of length n
     * @throws IllegalArgumentException if rank is out of range
     */
    public int[] unrank(BigInteger rank, int n) {
        int[] digits = toDerangadic(rank, n);
        return toDerangement(digits, n);
    }

    public int[] unrank(long rank, int n) {
        int[] digits = toDerangadic(rank, n);
        return toDerangement(digits, n);
    }

    /**
     * Direct conversion: derangement → rank (without intermediate digits).
     *
     * @param derangement the derangement array (full size n, no fixed points)
     * @param n the order (number of elements)
     * @return the 0-based lexicographical rank of the derangement
     * @throws IllegalArgumentException if derangement is not a valid derangement
     */
    public BigInteger rank(int[] derangement, int n) {
        int[] digits = fromDerangement(derangement, n);
        return fromDerangadic(digits, n);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Finds the smallest {@code actualN} with the same parity as {@code n}
     * such that {@code D[actualN] > m}.
     *
     * @param n original order
     * @param m rank
     * @return minimal effective size
     */
    private int smallestN(int n, BigInteger m) {
        int actualN = n;
        while (actualN > 2 && derangementCounts[actualN - 2].compareTo(m) > 0) {
            actualN -= 2;
        }
        return actualN;
    }

    /**
     * A list view that pads zeros at the end for indices beyond the backing array.
     */
    private static final class ZeroPaddedList {
        private final int[] digits;
        private final int n;

        ZeroPaddedList(int[] digits, int n) {
            this.digits = digits;
            this.n = n;
        }

        public int get(int index) {
            if (index < 0 || index >= n) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            if (index < digits.length) {
                return digits[index];
            }
            return 0;
        }
    }
}