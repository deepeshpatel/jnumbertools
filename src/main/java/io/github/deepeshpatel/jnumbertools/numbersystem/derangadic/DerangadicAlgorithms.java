/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.FenwickTree;

import java.math.BigInteger;
import java.util.*;

import static javax.management.openmbean.SimpleType.BIGINTEGER;

/**
 * Core algorithms for the <strong>Derangadic</strong> number system — a combinatorial
 * number system for fixed-point-free permutations (derangements).
 *
 * <p>Provides a bijective mapping between integers {@code [0, D_n − 1]} and the
 * {@code D_n} derangements of {@code n} elements in lexicographical order, where
 * {@code D_n = !n} (the subfactorial of {@code n}).</p>
 *
 * <p>The name "Derangadic" blends "Derangement" with "Combinadic" (the combinatorial
 * number system).</p>
 *
 * <h2>Array index convention — LSD at {@code a[0]}, MSD at {@code a[a.length-1]}</h2>
 * <p>
 * Every digit array produced or consumed by this class is stored in
 * <strong>Least-Significant-Digit-first (LSD-first)</strong> order:
 * </p>
 * <pre>
 *   a[0]          = D_0          — Least Significant Digit (LSD)
 *   a[1]          = D_1
 *   ...
 *   a[a.length-1] = D_{k-1}     — Most Significant Digit (MSD)
 * </pre>
 * <p>
 * This matches the paper's positional notation, which defines the Derangadic
 * representation as {@code (D_{k-1}, D_{k-2}, …, D_1, D_0)} — MSD written first
 * (on the left), LSD written last (on the right). The in-memory array simply reverses
 * that written order: index&nbsp;0 holds the rightmost (least-significant) digit of the
 * paper notation.
 * </p>
 * <p>
 * When printing in human-readable (MSD-first) form, iterate the array from
 * {@code a[a.length-1]} down to {@code a[0]}.
 * </p>
 *
 * <h2>Variable-length encoding and the parity invariant</h2>
 * <p>
 * The digit array has length equal to the <em>minimal carrier length</em>
 * {@code actualN}: the smallest integer with the same parity as {@code n} for
 * which {@code D_actualN > rank}. Positions logically corresponding to indices
 * {@code actualN} through {@code n-1} in the full {@code n}-element encoding are
 * implicitly zero — they appear as leading zeros in MSD-first (paper) notation and
 * are not stored in the array.
 * </p>
 * <p>
 * Because {@code D_k} grows super-exponentially, the carrier length increases
 * in steps of two (parity-preserving jumps), reflecting the fact that the
 * Derangadic encoding of a rank depends on the parity of {@code n} but not
 * on {@code n} itself beyond parity.
 * </p>
 *
 * <h2>Example: n = 12 (even), ranks 0–8</h2>
 * <p>
 * The table below shows rank, the digit array as returned by
 * {@link #toDerangadic(BigInteger, int)} (LSD at index 0, MSD at the last index),
 * and the same digits displayed MSD-first as in the paper.
 * The MSD-first form is obtained by reversing the array before printing.
 * </p>
 * <pre>
 *  Rank  array stored LSD-first (a[0]=D_0 … a[k-1]=D_{k-1})   MSD-first display
 *  0     [0, 0]                                                  [0, 0]
 *  1     [0, 1, 1, 0]                                            [0, 1, 1, 0]
 *  2     [0, 0, 2, 0]                                            [0, 2, 0, 0]
 *  3     [0, 1, 0, 1]                                            [1, 0, 1, 0]
 *  4     [0, 0, 1, 1]                                            [1, 1, 0, 0]
 *  5     [0, 1, 1, 1]                                            [1, 1, 1, 0]
 *  6     [0, 0, 0, 2]                                            [2, 0, 0, 0]
 *  7     [0, 0, 1, 2]                                            [2, 1, 0, 0]
 *  8     [0, 1, 1, 2]                                            [2, 1, 1, 0]
 * </pre>
 * <p>
 * <strong>Trailing zeros</strong> in the stored (LSD-first) array appear at low indices
 * (e.g. {@code a[0] = 0} for ranks 0–8 with n=12). They represent the LSD digits
 * {@code D_0, D_1, …} and are structurally meaningful — they must not be stripped
 * from the low end.  MSD-side leading zeros (at the high-index end) arise when
 * {@code actualN < n}: those positions are trimmed by {@link #fromDerangement}.
 * Use {@code arraysEqualIgnoringTrailingZeros()} only when comparing arrays that
 * may differ in MSD-side padding.
 * </p>
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @since 3.0.2
 */
public final class DerangadicAlgorithms {

    private final Calculator calculator;

    private static final int N_THRESHOLD = 100;
    private static final int DIGIT_THRESHOLD = 100;

    public DerangadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    public DerangadicAlgorithms() {
        this(new Calculator());
    }

    // ==================== Core Public API ====================

    /**
     * Returns the total number of derangements of {@code n} elements, i.e.
     * the subfactorial {@code !n = D_n}.
     *
     * @param n number of elements ({@code n ≥ 0})
     * @return {@code D_n = !n}
     */
    public BigInteger derangementCount(int n) {
        return calculator.subFactorial(n);
    }

    /**
     * Counts the derangement completions for the remaining structure.
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
        return calculator.restrictedDerangements(size, restrictedCount);
    }

    /**
     * Converts a decimal rank to its Derangadic digit array.
     *
     * <h3>Array index convention</h3>
     * <p>
     * The returned array is <strong>LSD-first</strong>:
     * </p>
     * <pre>
     *   result[0]            = D_0       (Least Significant Digit)
     *   result[1]            = D_1
     *   ...
     *   result[result.length-1] = D_{k-1}  (Most Significant Digit)
     * </pre>
     * <p>
     * The array length equals the minimal carrier length {@code actualN}: the
     * smallest integer with the same parity as {@code n} such that
     * {@code D_actualN > rank}. Digits for positions logically above {@code actualN-1}
     * in the full {@code n}-element encoding are implicitly zero and are not stored
     * (they would appear as leading zeros in MSD-first paper notation).
     * </p>
     * <p>
     * To display in MSD-first (paper) order, iterate the array from
     * {@code result.length-1} down to {@code 0}.
     * </p>
     *
     * @param m decimal rank ({@code 0 ≤ m < D_n})
     * @param n order (number of elements, {@code n ≥ 2})
     * @return Derangadic digit array of length {@code actualN ≤ n},
     *         stored LSD-first (i.e. {@code result[0] = D_0})
     * @throws IllegalArgumentException if {@code m} is negative or {@code m ≥ D_n}
     */
    public int[] toDerangadic(BigInteger m, int n) {
        BigInteger max = derangementCount(n);
        if (m.signum() < 0 || m.compareTo(max) >= 0) {
            throw new IllegalArgumentException("m out of range");
        }

        int actualN = smallestN(n, m);
        int[] digits = new int[actualN];
        boolean[] elementUsed = new boolean[actualN];
        BigInteger currentM = m;

        // Pre-calculate actualNMinus1 for array index calculations
        int actualNMinus1 = actualN - 1;

        for (int step = 0; step < actualN; step++) {
            int remainingSize = actualN - step;
            int restrictedCount = 0;

            // Optimized loop: count unused elements
            for (int i = step; i < actualN; i++) {
                if (!elementUsed[i]) {
                    restrictedCount++;
                }
            }

            int legalFoundCount = 0;
            BigInteger cumulative = BigInteger.ZERO;

            boolean stepNotUsed = !elementUsed[step];

            for (int candidate = 0; candidate < actualN; candidate++) {
                if (elementUsed[candidate] || candidate == step) continue;

                boolean pickingRestricted = (candidate > step);
                int decrement = (pickingRestricted ? 1 : 0) + (stepNotUsed ? 1 : 0);
                int nextRestricted = restrictedCount > decrement ? restrictedCount - decrement : 0;

                BigInteger blockSize = calculator.restrictedDerangements(remainingSize - 1, nextRestricted);

                if (currentM.compareTo(cumulative.add(blockSize)) < 0) {
                    // step 0 → index actualN-1 (MSD), step actualN-1 → index 0 (LSD = D_0)
                    digits[actualNMinus1 - step] = legalFoundCount;
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
     * Convenience overload of {@link #toDerangadic(BigInteger, int)} for {@code long} ranks.
     *
     * @param m decimal rank ({@code 0 ≤ m < D_n})
     * @param n order (number of elements)
     * @return Derangadic digit array, LSD-first ({@code result[0] = D_0})
     */
    public int[] toDerangadic(long m, int n) {
        return toDerangadic(BigInteger.valueOf(m), n);
    }

    /**
     * Converts a Derangadic digit array back to its decimal rank.
     *
     * <h3>Array index convention</h3>
     * <p>
     * The input array must be <strong>LSD-first</strong>:
     * {@code digits[0] = D_0} (Least Significant Digit) and
     * {@code digits[digits.length-1] = D_{k-1}} (Most Significant Digit).
     * Any logical digit positions beyond {@code digits.length-1} up to {@code n-1}
     * are treated as implicit zeros (i.e. the MSD end of the full encoding is
     * zero-padded as needed).
     * </p>
     *
     * @param digits Derangadic digit array of length {@code actualN ≤ n},
     *               stored LSD-first ({@code digits[0] = D_0})
     * @param n      full order ({@code n ≥ 2})
     * @return decimal rank corresponding to the given digit array
     */
    public BigInteger fromDerangadic(int[] digits, int n) {
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

        int nMinus1 = n - 1;
        for (int step = 0; step < n; step++) {
            int pos = positionsList.get(0);

            List<Integer> legal = new ArrayList<>();
            for (int e : remainingElements) {
                if (e != pos) legal.add(e);
            }
            Collections.sort(legal);

            // Digit for this step: step 0 → index n-1 (MSD = D_{n-1}),
            // step n-1 → index 0 (LSD = D_0).
            int digit = allDigits.get(nMinus1 - step);

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
     * Converts a Derangadic digit array to the corresponding derangement.
     *
     * <h3>Array index convention</h3>
     * <p>
     * The input array must be <strong>LSD-first</strong>:
     * {@code digits[0] = D_0} (Least Significant Digit) and
     * {@code digits[digits.length-1] = D_{k-1}} (Most Significant Digit).
     * Logical digit positions beyond {@code digits.length-1} are treated as implicit zeros.
     * </p>
     *
     * @param digits Derangadic digit array of length {@code actualN ≤ n},
     *               stored LSD-first ({@code digits[0] = D_0})
     * @param n      full order ({@code n ≥ 2})
     * @return derangement array of length {@code n}
     */
    public int[] toDerangement(int[] digits, int n) {
        if (n < N_THRESHOLD || digits.length < DIGIT_THRESHOLD) {
            return toDerangementArray(digits, n);
        }
        return toDerangementFenwick(digits, n);
    }

    /**
     * Array-based implementation for small {@code n}.
     * O(n²) with a low constant factor — preferred when {@code n ≤ 100}.
     *
     * <p>The {@code digits} array must be LSD-first: {@code digits[0] = D_0} (Least
     * Significant Digit) and {@code digits[digits.length-1] = D_{k-1}} (Most
     * Significant Digit). Step {@code k} within the active window reads digit
     * {@code digits[actualN-1-k]}, where step 0 reads the MSD and step
     * {@code actualN-1} reads the LSD.</p>
     */
    int[] toDerangementArray(int[] digits, int n) {
        int actualN = digits.length;
        int offset = n - actualN;

        int[] derangement = new int[n];
        boolean[] used = new boolean[n];
        int actualNMinus1 = actualN - 1;

        // Fill the prefix [0, offset) with the greedy minimum derangement.
        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && used[nextCandidate]) {
                nextCandidate++;
            }
            int chosen = nextCandidate;
            if (chosen == pos) { // would be a fixed point
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

        // Fill suffix [offset, n) from the digit array.
        // Step k within the active window reads digits[actualN-1-k]:
        //   step 0  → digits[actualN-1] = D_{actualN-1} (MSD of active window)
        //   step actualN-1 → digits[0] = D_0 (LSD)
        for (int step = 0; step < actualN; step++) {
            int pos = offset + step;
            int digit = digits[actualNMinus1 - step];

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
     * Fenwick-tree-based implementation for large {@code n}.
     * O(n + actualN × log n) — preferred when {@code n > 100}.
     *
     * <p>The {@code digits} array must be LSD-first: {@code digits[0] = D_0} (Least
     * Significant Digit) and {@code digits[digits.length-1] = D_{k-1}} (Most
     * Significant Digit). Step {@code k} within the active window reads digit
     * {@code digits[actualN-1-k]}, where step 0 reads the MSD and step
     * {@code actualN-1} reads the LSD.</p>
     */
    int[] toDerangementFenwick(int[] digits, int n) {
        int actualN = digits.length;
        int offset = n - actualN;

        int[] derangement = new int[n];
        FenwickTree availableElements = new FenwickTree(n);
        for (int i = 1; i <= n; i++) {
            availableElements.update(i, 1);
        }

        int actualNMinus1 = actualN - 1;
        int nextAvailable = 1;
        for (int pos = 0; pos < offset; pos++) {
            while (nextAvailable <= n &&
                    availableElements.rsq(nextAvailable) - availableElements.rsq(nextAvailable - 1) == 0) {
                nextAvailable++;
            }
            int chosenIdx = nextAvailable;
            if (chosenIdx == pos + 1) {
                if (nextAvailable + 1 <= n) {
                    chosenIdx = nextAvailable + 1;
                    while (chosenIdx <= n &&
                            availableElements.rsq(chosenIdx) - availableElements.rsq(chosenIdx - 1) == 0) {
                        chosenIdx++;
                    }
                }
            }
            derangement[pos] = chosenIdx - 1;
            availableElements.update(chosenIdx, -1);
            if (chosenIdx == nextAvailable) {
                do {
                    nextAvailable++;
                } while (nextAvailable <= n &&
                        availableElements.rsq(nextAvailable) - availableElements.rsq(nextAvailable - 1) == 0);
            }
        }

        for (int step = 0; step < actualN; step++) {
            int pos = offset + step;
            int digit = digits[actualNMinus1 - step];

            int chosen = getChosen(availableElements, pos, digit);
            derangement[pos] = chosen - 1;
            availableElements.update(chosen, -1);
        }

        return derangement;
    }

    private static int getChosen(FenwickTree availableElements, int pos, int digit) {
        int pos1 = pos + 1;
        int posRank = availableElements.rsq(pos);
        boolean posAvailable = (availableElements.rsq(pos1) - posRank) == 1;

        if (posAvailable) {
            return (digit < posRank) ?
                    availableElements.findKth(digit + 1) :
                    availableElements.findKth(digit + 2);
        } else {
            return availableElements.findKth(digit + 1);
        }
    }

    /**
     * Converts a derangement back to its minimal Derangadic digit array.
     *
     * <h3>Array index convention of the returned array</h3>
     * <p>
     * The returned array is <strong>LSD-first</strong>:
     * {@code result[0] = D_0} (Least Significant Digit) and
     * {@code result[result.length-1] = D_{k-1}} (Most Significant Digit).
     * MSD-side leading zeros (at high indices) are trimmed so the returned length
     * equals the minimal carrier length {@code actualN}.
     * </p>
     * <p>
     * To display in MSD-first (paper) order, iterate from
     * {@code result.length-1} down to {@code 0}.
     * </p>
     *
     * @param derangement derangement array of length {@code n} (no fixed points,
     *                    values in {@code [0, n)}, all distinct); correctness is
     *                    not fully validated
     * @param n           order (must equal {@code derangement.length})
     * @return Derangadic digit array of minimal length, stored LSD-first
     *         ({@code result[0] = D_0})
     * @throws IllegalArgumentException if {@code derangement} contains a fixed point
     *                                  or an element that is not in the legal candidate list
     */
    public int[] fromDerangement(int[] derangement, int n) {
        int[] fullDigits = new int[n];

        List<Integer> remainingElements = new ArrayList<>();
        List<Integer> remainingPositions = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            remainingElements.add(i);
            remainingPositions.add(i);
        }

        int nMinus1 = n - 1;
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

            // step 0 → index n-1 (MSD = D_{n-1}), step n-1 → index 0 (LSD = D_0).
            fullDigits[nMinus1 - step] = digit;

            remainingElements.remove(Integer.valueOf(chosen));
            remainingPositions.remove(0);
        }

        // Trim MSD-side leading zeros (high indices) to produce minimal carrier length.
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
     * Direct conversion: rank → derangement (bypassing the intermediate digit array).
     *
     * @param rank the 0-based lexicographical rank ({@code 0 ≤ rank < D_n})
     * @param n    the order (number of elements, {@code n ≥ 2})
     * @return the derangement as an array of length {@code n}
     * @throws IllegalArgumentException if {@code rank} is out of range
     */
    public int[] unrank(BigInteger rank, int n) {
        int[] digits = toDerangadic(rank, n);
        return toDerangement(digits, n);
    }

    /**
     * Convenience overload of {@link #unrank(BigInteger, int)} for {@code long} ranks.
     *
     * @param rank the 0-based lexicographical rank ({@code 0 ≤ rank < D_n})
     * @param n    the order (number of elements, {@code n ≥ 2})
     * @return the derangement as an array of length {@code n}
     */
    public int[] unrank(long rank, int n) {
        int[] digits = toDerangadic(rank, n);
        return toDerangement(digits, n);
    }

    /**
     * Direct conversion: derangement → rank (bypassing the intermediate digit array).
     *
     * @param derangement the derangement array (length {@code n}, no fixed points,
     *                    values in {@code [0, n)}, all distinct)
     * @param n           the order (must equal {@code derangement.length})
     * @return the 0-based lexicographical rank of the derangement
     * @throws IllegalArgumentException if {@code derangement} is not a valid derangement
     */
    public BigInteger rank(int[] derangement, int n) {
        int[] digits = fromDerangement(derangement, n);
        return fromDerangadic(digits, n);
    }

    /**
     * Returns the smallest integer {@code actualN} with the same parity as {@code n}
     * such that {@code D_actualN > m}.
     * <p>
     * This is the minimal carrier length: the minimum number of digits needed to
     * encode rank {@code m} within a universe of size {@code n}. Because the encoding
     * depends only on the parity of {@code n} (not on {@code n} itself beyond parity),
     * {@code actualN} may be strictly less than {@code n}.
     * </p>
     *
     * @param n the universe size
     * @param m the rank to encode
     * @return minimal carrier length {@code actualN ≤ n} with {@code actualN ≡ n (mod 2)}
     *         and {@code D_actualN > m}
     */
    private int smallestN(int n, BigInteger m) {
        int actualN = n;
        while (actualN > 2 && calculator.subFactorial(actualN - 2).compareTo(m) > 0) {
            actualN -= 2;
        }
        return actualN;
    }

    /**
     * A virtual list view over a digit array that returns {@code 0} for any index beyond
     * the backing array's length, effectively zero-padding the MSD end.
     *
     * <p>
     * Because the backing array is LSD-first, "padding the MSD end" means padding at
     * high indices: {@code get(i)} returns {@code 0} whenever {@code i ≥ digits.length}.
     * Low indices ({@code 0 … digits.length-1}) are served directly from the array.
     * </p>
     */
    private record ZeroPaddedList(int[] digits, int n) {
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
