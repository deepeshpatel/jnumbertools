/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

import java.math.BigInteger;

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
 * {@link #encode(BigInteger, int)} (LSD at index 0, MSD at the last index),
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
 * <h2>Intended use</h2>
 * <p>
 * <strong>Implementation class: intended to be used through {@link io.github.deepeshpatel.jnumbertools.base.RankOf} and
 * {@link io.github.deepeshpatel.jnumbertools.base.UnrankOf} (and the builders).</strong> Argument validation and its error signalling belong to those;
 * this class assumes valid input. Intrinsic conversion checks remain here: ranks outside
 * {@code [0, D_n)} cannot be encoded, and digit values outside the legal candidate range
 * cannot be decoded.
 * </p>
 * @author Deepesh Patel &amp; Aditya Patel
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
     * <p>Uses O(actualN²) candidate checks and O(actualN) auxiliary space.</p>
     *
     * @param m decimal rank ({@code 0 ≤ m < D_n})
     * @param n order (number of elements, {@code n ≥ 2})
     * @return Derangadic digit array of length {@code actualN ≤ n},
     *         stored LSD-first (i.e. {@code result[0] = D_0})
     * @throws IllegalArgumentException if {@code m} is negative or {@code m ≥ D_n}
     */
    public int[] encode(BigInteger m, int n) {
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

        // Number of unused elements that are also in the remaining position range [step, actualN).
        // This is maintained incrementally as positions and elements are consumed.
        int restrictedCount = actualN;

        for (int step = 0; step < actualN; step++) {
            int remainingSize = actualN - step;

            // The current position is no longer part of the remaining positions.
            if (!elementUsed[step]) {
                restrictedCount--;
            }

            int legalFoundCount = 0;
            BigInteger cumulative = BigInteger.ZERO;
            boolean selected = false;

            for (int candidate = 0; candidate < actualN; candidate++) {
                if (elementUsed[candidate] || candidate == step) {
                    continue;
                }

                // Selecting candidate removes it from the restricted set exactly when
                // candidate belongs to the remaining position range.
                int nextRestricted = restrictedCount - (candidate > step ? 1 : 0);
                BigInteger blockSize = calculator.restrictedDerangementCount(
                        remainingSize - 1, nextRestricted);

                if (currentM.compareTo(cumulative.add(blockSize)) < 0) {
                    // step 0 → index actualN-1 (MSD), final step → index 0 (LSD).
                    digits[actualNMinus1 - step] = legalFoundCount;
                    elementUsed[candidate] = true;
                    currentM = currentM.subtract(cumulative);
                    restrictedCount = nextRestricted;
                    selected = true;
                    break;
                }

                cumulative = cumulative.add(blockSize);
                legalFoundCount++;
            }

            if (!selected) {
                throw new IllegalStateException("Unable to encode rank at position " + step);
            }
        }

        return digits;
    }

    /**
     * Convenience overload of {@link #encode(BigInteger, int)} for {@code long} ranks.
     *
     * @param m decimal rank ({@code 0 ≤ m < D_n})
     * @param n order (number of elements)
     * @return Derangadic digit array, LSD-first ({@code result[0] = D_0})
     */
    public int[] encode(long m, int n) {
        return encode(BigInteger.valueOf(m), n);
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
     * <p>Uses O(n²) candidate checks and O(n) auxiliary space. Restricted
     * derangement counts are maintained incrementally, avoiding temporary
     * collection copies while considering candidate blocks.</p>
     *
     * @param digits Derangadic digit array of length {@code actualN ≤ n},
     *               stored LSD-first ({@code digits[0] = D_0})
     * @param n      full order ({@code n ≥ 2})
     * @return decimal rank corresponding to the given digit array
     * @throws IllegalArgumentException if a digit is outside the legal candidate range
     */
    public BigInteger decode(int[] digits, int n) {
        ZeroPaddedList allDigits = new ZeroPaddedList(digits, n);
        boolean[] elementUsed = new boolean[n];

        BigInteger result = BigInteger.ZERO;
        int restrictedCount = n;
        int nMinus1 = n - 1;

        for (int step = 0; step < n; step++) {
            // The current position leaves the remaining-position set.
            if (!elementUsed[step]) {
                restrictedCount--;
            }

            int digit = allDigits.get(nMinus1 - step);
            int remainingSize = n - step;
            int legalFoundCount = 0;
            boolean selected = false;

            // Visit legal candidates in ascending order, as required by lexicographic ranking.
            for (int candidate = 0; candidate < n; candidate++) {
                if (elementUsed[candidate] || candidate == step) {
                    continue;
                }

                int nextRestricted = restrictedCount - (candidate > step ? 1 : 0);
                BigInteger blockSize = calculator.restrictedDerangementCount(
                        remainingSize - 1, nextRestricted);

                if (legalFoundCount == digit) {
                    elementUsed[candidate] = true;
                    restrictedCount = nextRestricted;
                    selected = true;
                    break;
                }

                result = result.add(blockSize);
                legalFoundCount++;
            }

            if (!selected) {
                throw new IllegalArgumentException(
                        "Invalid Derangadic digit " + digit + " at position " + step);
            }
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
     * O((n + actualN) × log n) — preferred when {@code n > 100}.
     * Initializing the Fenwick tree costs O(n × log n).
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
        FenwickTree availableElements = FenwickTree.ofOnes(n);

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
     * <p>Uses O(n log n) time and O(n) auxiliary space.</p>
     *
     * @param derangement derangement array of length {@code n}, with no fixed points
     *                    and each value in {@code [0, n)} appearing exactly once
     * @param n           order (must equal {@code derangement.length})
     * @return Derangadic digit array of minimal length, stored LSD-first
     *         ({@code result[0] = D_0})
     * @throws IllegalArgumentException if an element is out of bounds, duplicated,
     *                                  or creates a fixed point
     */
    public int[] fromDerangement(int[] derangement, int n) {
        int[] fullDigits = new int[n];
        // Initially every value in [0, n) is available. FenwickTree uses
        // 1-based indices, so value v is stored at index v + 1.
        FenwickTree availableElements = FenwickTree.ofOnes(n);

        int nMinus1 = n - 1;
        for (int step = 0; step < n; step++) {
            int pos = step;
            int chosen = derangement[pos];

            if (chosen < 0 || chosen >= n) {
                throw new IllegalArgumentException(
                        "Invalid derangement: element " + chosen + " out of bounds at position " + pos);
            }
            if (chosen == pos) {
                throw new IllegalArgumentException(
                        "Invalid derangement: fixed point at position " + pos);
            }
            if (availableElements.get(chosen + 1) == 0) {
                throw new IllegalArgumentException(
                        "Invalid derangement: duplicate element " + chosen);
            }

            // Prefix sum through Fenwick index 'chosen' counts available
            // values [0, chosen), because value v is stored at v + 1.
            int availableLessThanChosen = availableElements.rsq(chosen);

            // Exclude the current position only when it is still unused and smaller than chosen.
            int digit = availableLessThanChosen
                    - (chosen > pos && availableElements.get(pos + 1) == 1 ? 1 : 0);

            fullDigits[nMinus1 - step] = digit;
            availableElements.update(chosen + 1, -1);
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
        int[] digits = encode(rank, n);
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
        int[] digits = encode(rank, n);
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
        return decode(digits, n);
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
        // Scan upward from the smallest candidate of the right parity: a small rank stops after a few steps,
        // and the cache is only asked for ascending values (a downward scan from n would need every D_k below n).
        int actualN = (n % 2 == 0) ? 2 : 3;
        while (actualN < n && calculator.subFactorial(actualN).compareTo(m) <= 0) {
            actualN += 2;
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
}
