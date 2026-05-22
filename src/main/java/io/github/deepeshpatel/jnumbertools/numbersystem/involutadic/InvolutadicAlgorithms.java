/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Involutadic Number System for Involutions.
 *
 * <p>An <em>involution</em> of {@code [n]} is a permutation {@code π} satisfying
 * {@code π(π(i)) = i} for every {@code i}. Equivalently, it consists only of
 * fixed points ({@code π(i) = i}) and transpositions (2-cycles, {@code π(i) = j}
 * and {@code π(j) = i}). Involutions of {@code [n]} are counted by the
 * <em>telephone numbers</em> (OEIS A000085):
 * <pre>
 *   T(0)=1, T(1)=1, T(2)=2, T(3)=4, T(4)=10, T(5)=26, T(6)=76, ...
 * </pre>
 * satisfying the recurrence {@code T(n) = T(n-1) + (n-1)·T(n-2)}.
 *
 * <h2>The Involutadic Number System</h2>
 *
 * <p>The Involutadic system establishes a canonical bijection between the
 * integers {@code {0, 1, ..., T(n)-1}} and the involutions of {@code [n]}
 * in lexicographic order, via a <em>decision-based mixed-radix encoding</em>.
 *
 * <h3>Encoding Algorithm</h3>
 *
 * <p>Process positions {@code pos = 0, 1, ..., n-1} left-to-right. Skip
 * positions already claimed as a 2-cycle partner. At each <em>active</em>
 * position (one not yet placed), make exactly one decision:
 * <ul>
 *   <li><b>Digit 0</b>: place a <em>fixed point</em> ({@code π(pos) = pos}).
 *       Block size = {@code T(remaining − 1)}, where {@code remaining} is the
 *       count of unplaced positions (including {@code pos} itself).</li>
 *   <li><b>Digit k ≥ 1</b>: form a <em>2-cycle</em> with the k-th smallest
 *       available element {@code e > pos} (1-indexed). This simultaneously sets
 *       {@code π(pos) = e} and {@code π(e) = pos}, consuming both positions.
 *       Block size = {@code T(remaining − 2)}.</li>
 * </ul>
 * The maximum digit at a step with {@code remaining} unplaced positions is
 * {@code remaining − 1} (one fixed-point option plus {@code remaining − 1}
 * 2-cycle partners).
 *
 * <h3>Digit Array Layout</h3>
 *
 * <p>The digit array is stored <b>LSD-first</b> to match the Derangadic
 * convention: {@code digits[0]} is the last decision made (least significant),
 * and {@code digits[len-1]} is the first decision (most significant). The
 * array length equals the number of decisions, which varies from
 * {@code ⌈n/2⌉} (all 2-cycles) to {@code n} (all fixed points).
 *
 * <h3>Example: n = 4, T(4) = 10</h3>
 * <pre>
 * Rank  Involution    Decisions (MSD→LSD)   Digits (LSD-first)
 *   0   [0,1,2,3]    fp,fp,fp,fp            [0,0,0,0]
 *   1   [0,1,3,2]    fp,fp,2c(2,3)          [1,0,0]
 *   2   [0,2,1,3]    fp,2c(1,2),fp          [0,1,0]
 *   3   [0,3,2,1]    fp,2c(1,3),fp          [0,2,0]
 *   4   [1,0,2,3]    2c(0,1),fp,fp          [0,0,1]
 *   5   [1,0,3,2]    2c(0,1),2c(2,3)        [1,1]
 *   6   [2,1,0,3]    2c(0,2),fp,fp          [0,0,2]
 *   7   [2,3,0,1]    2c(0,2),2c(1,3)        [1,2]
 *   8   [3,1,2,0]    2c(0,3),fp,fp          [0,0,3]
 *   9   [3,2,1,0]    2c(0,3),2c(1,2)        [1,3]
 * </pre>
 *
 * @author Deepesh Patel and Aditya Patel
 * @since 3.0.2
 */


//TODO
// Documentation Clarifications Needed
//Digit semantics: Explicitly document that:
//digit = 0 → fixed point at current position
//digit = k ≥ 1 → 2-cycle with k-th smallest available partner > pos
//maxDigit = remaining - 1 (one fixed-point option + remaining-1 partners)
//Variable digit array length: Clarify that length ∈ [⌈n/2⌉, n] depending on involution structure (all 2-cycles vs. all fixed points). This is a feature, not a bug.
//No parity stabilization: Unlike Derangadic, Involutadic has no parity-locked active window. The digit array can be as large as n. This is correct but should be noted.

public final class InvolutadicAlgorithms {

    private final Calculator calculator;

    public InvolutadicAlgorithms(Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    // ==================== Public API ====================

    /**
     * Returns the number of involutions of {@code n} elements (the telephone number T(n)).
     *
     * @param n number of elements (n ≥ 0)
     * @return T(n)
     */
    public BigInteger involutionCount(int n) {
        return calculator.telephoneNumber(n);
    }

    /**
     * Converts a rank to its Involutadic digit array.
     *
     * <p>Returns an {@code int[]} of length equal to the number of decisions made
     * (between {@code ⌈n/2⌉} and {@code n}), stored LSD-first.
     *
     * @param rank the 0-based lexicographic rank (0 ≤ rank < T(n))
     * @param n    the order (number of elements, n ≥ 1)
     * @return Involutadic digit array, LSD-first
     * @throws IllegalArgumentException if rank is out of range
     */
    public int[] toInvolutadic(long rank, int n) {
        return toInvolutadic(BigInteger.valueOf(rank), n);
    }

    /**
     * Converts a rank to its Involutadic digit array.
     *
     * @param rank the 0-based lexicographic rank (0 ≤ rank < T(n))
     * @param n    the order (number of elements, n ≥ 1)
     * @return Involutadic digit array, LSD-first
     * @throws IllegalArgumentException if rank is out of range
     */
    public int[] toInvolutadic(BigInteger rank, int n) {
        Objects.requireNonNull(rank, "rank");
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);
        BigInteger total = calculator.telephoneNumber(n);
        if (rank.signum() < 0 || rank.compareTo(total) >= 0) {
            throw new IllegalArgumentException(
                    "rank " + rank + " out of range [0, T(" + n + ")=" + total + ")");
        }

        // Collect decisions MSD-first, then reverse to LSD-first
        int[] msdDecisions = new int[n]; // at most n decisions
        int decisionCount = 0;

        boolean[] placed = new boolean[n];
        BigInteger remaining = rank;

        for (int pos = 0; pos < n; pos++) {
            if (placed[pos]) continue;

            int unplaced = countUnplaced(placed, n);
            // Block size for fixed point: T(unplaced - 1)
            BigInteger fixedBlock = calculator.telephoneNumber(unplaced - 1);

            if (remaining.compareTo(fixedBlock) < 0) {
                // Choose fixed point: digit 0
                msdDecisions[decisionCount++] = 0;
                placed[pos] = true;
            } else {
                // Choose a 2-cycle
                remaining = remaining.subtract(fixedBlock);
                BigInteger cycleBlock = calculator.telephoneNumber(unplaced - 2);

                // Find which partner (1-indexed among elements > pos not yet placed)
                BigInteger[] divRem = remaining.divideAndRemainder(cycleBlock);
                int partnerIndex = divRem[0].intValue(); // 0-indexed among partners
                remaining = divRem[1];

                msdDecisions[decisionCount++] = partnerIndex + 1; // 1-indexed digit

                // Resolve the actual partner element
                int partner = findKthPartner(placed, pos, partnerIndex, n);
                placed[pos] = true;
                placed[partner] = true;
            }
        }

        // Reverse to LSD-first
        int[] result = new int[decisionCount];
        for (int i = 0; i < decisionCount; i++) {
            result[i] = msdDecisions[decisionCount - 1 - i];
        }
        return result;
    }

    /**
     * Converts an Involutadic digit array back to its lexicographic rank.
     *
     * @param digits the digit array (LSD-first), length = number of decisions
     * @param n      the order (number of elements)
     * @return the 0-based lexicographic rank
     */
    public BigInteger fromInvolutadic(int[] digits, int n) {
        Objects.requireNonNull(digits, "digits");
        if (n < 1) throw new IllegalArgumentException("n must be >= 1");

        // digits is LSD-first; decisions are MSD-first (reversed)
        int k = digits.length;
        BigInteger rank = BigInteger.ZERO;
        boolean[] placed = new boolean[n];

        for (int di = k - 1; di >= 0; di--) { // iterate MSD to LSD
            int digit = digits[di];

            // Find active position (first unplaced)
            int pos = firstUnplaced(placed, n);
            int unplaced = countUnplaced(placed, n);

            BigInteger fixedBlock = calculator.telephoneNumber(unplaced - 1);

            if (digit == 0) {
                // Fixed point: contributes 0 offset
                placed[pos] = true;
            } else {
                // 2-cycle: skip the fixed-point block, then skip (digit-1) cycle blocks
                BigInteger cycleBlock = calculator.telephoneNumber(unplaced - 2);
                rank = rank.add(fixedBlock);
                rank = rank.add(cycleBlock.multiply(BigInteger.valueOf(digit - 1)));

                int partner = findKthPartner(placed, pos, digit - 1, n);
                placed[pos] = true;
                placed[partner] = true;
            }
        }
        return rank;
    }

    /**
     * Converts an Involutadic digit array to the corresponding involution array.
     *
     * @param digits the digit array (LSD-first)
     * @param n      the order (number of elements)
     * @return involution array of length n
     */
    public int[] toInvolution(int[] digits, int n) {
        Objects.requireNonNull(digits, "digits");
        int[] involution = new int[n];
        for (int i = 0; i < n; i++) involution[i] = i; // initialise as identity

        boolean[] placed = new boolean[n];
        int k = digits.length;

        for (int di = k - 1; di >= 0; di--) { // MSD to LSD
            int digit = digits[di];
            int pos = firstUnplaced(placed, n);

            if (digit == 0) {
                involution[pos] = pos; // fixed point
                placed[pos] = true;
            } else {
                int partner = findKthPartner(placed, pos, digit - 1, n);
                involution[pos] = partner;
                involution[partner] = pos;
                placed[pos] = true;
                placed[partner] = true;
            }
        }
        return involution;
    }

    /**
     * Converts an involution array to its Involutadic digit array (LSD-first).
     *
     * @param involution the involution array of length n
     * @param n          the order
     * @return Involutadic digit array, LSD-first
     * @throws IllegalArgumentException if the input is not a valid involution
     */
    public int[] fromInvolution(int[] involution, int n) {
        Objects.requireNonNull(involution, "involution");
        validateInvolution(involution, n);

        int[] msdDecisions = new int[n];
        int decisionCount = 0;
        boolean[] placed = new boolean[n];

        for (int pos = 0; pos < n; pos++) {
            if (placed[pos]) continue;
            int target = involution[pos];

            if (target == pos) {
                msdDecisions[decisionCount++] = 0;
                placed[pos] = true;
            } else {
                // 2-cycle: find index of target among partners of pos
                int partnerIdx = partnerRank(placed, pos, target, n);
                msdDecisions[decisionCount++] = partnerIdx + 1;
                placed[pos] = true;
                placed[target] = true;
            }
        }

        // Reverse to LSD-first
        int[] result = new int[decisionCount];
        for (int i = 0; i < decisionCount; i++) {
            result[i] = msdDecisions[decisionCount - 1 - i];
        }
        return result;
    }

    /**
     * Direct conversion: rank → involution (without storing intermediate digits).
     *
     * @param rank the 0-based lexicographic rank
     * @param n    the order
     * @return involution array of length n
     */
    public int[] unrank(long rank, int n) {
        return toInvolution(toInvolutadic(rank, n), n);
    }

    /**
     * Direct conversion: rank → involution.
     *
     * @param rank the 0-based lexicographic rank
     * @param n    the order
     * @return involution array of length n
     */
    public int[] unrank(BigInteger rank, int n) {
        return toInvolution(toInvolutadic(rank, n), n);
    }

    /**
     * Direct conversion: involution → rank.
     *
     * @param involution the involution array of length n
     * @param n          the order
     * @return the 0-based lexicographic rank
     */
    public BigInteger rank(int[] involution, int n) {
        return fromInvolutadic(fromInvolution(involution, n), n);
    }

    // ==================== Package-private helpers (used by InvolutadicIncrement) ====================

    /**
     * Returns the index of the first unplaced position.
     */
    static int firstUnplaced(boolean[] placed, int n) {
        for (int i = 0; i < n; i++) {
            if (!placed[i]) return i;
        }
        return -1;
    }

    /**
     * Counts how many positions are not yet placed.
     */
    static int countUnplaced(boolean[] placed, int n) {
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!placed[i]) count++;
        }
        return count;
    }

    /**
     * Returns the element at 0-based rank {@code k} among available partners
     * of {@code pos}: elements {@code e > pos} with {@code !placed[e]}.
     */
    static int findKthPartner(boolean[] placed, int pos, int k, int n) {
        int seen = 0;
        for (int e = pos + 1; e < n; e++) {
            if (!placed[e]) {
                if (seen == k) return e;
                seen++;
            }
        }
        throw new IllegalStateException(
                "No " + k + "-th partner for pos=" + pos + " (logic error)");
    }

    /**
     * Returns the 0-based rank of {@code target} among partners of {@code pos}.
     */
    static int partnerRank(boolean[] placed, int pos, int target, int n) {
        int rank = 0;
        for (int e = pos + 1; e < n; e++) {
            if (!placed[e]) {
                if (e == target) return rank;
                rank++;
            }
        }
        throw new IllegalArgumentException(
                "target=" + target + " is not a valid partner for pos=" + pos);
    }

    // ==================== Validation ====================

    private static void validateInvolution(int[] pi, int n) {
        if (pi.length != n) {
            throw new IllegalArgumentException(
                    "involution length " + pi.length + " != n=" + n);
        }
        for (int i = 0; i < n; i++) {
            if (pi[i] < 0 || pi[i] >= n) {
                throw new IllegalArgumentException(
                        "involution[" + i + "]=" + pi[i] + " out of range [0," + n + ")");
            }
            if (pi[pi[i]] != i) {
                throw new IllegalArgumentException(
                        "Not an involution: π(π(" + i + ")) != " + i);
            }
        }
    }
}
