/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Algorithms for ranking and unranking involutions using the <em>Involutadic</em>
 * number system.
 *
 * <h2>What is an involution?</h2>
 * <p>An <em>involution</em> of {@code [n] = {0, 1, ..., n-1}} is a permutation
 * {@code π} such that {@code π(π(i)) = i} for all {@code i}.  Equivalently, every
 * element is either a <em>fixed point</em> ({@code π(i) = i}) or part of a
 * <em>2-cycle</em> ({@code π(i) = j}, {@code π(j) = i}, {@code i ≠ j}).
 * The total number of involutions of {@code [n]} is the telephone number
 * {@code T(n)} (OEIS A000085).
 *
 * <h2>The Involutadic number system</h2>
 * <p>The Involutadic system assigns each involution a unique fixed-width array of
 * length {@code n}, called its <em>Involutadic representation</em>.  Positions are
 * indexed {@code 0, 1, ..., n-1} and processed left-to-right (MSD-first, index 0
 * is the most-significant decision).
 *
 * <p>At each position {@code i} exactly one of three things is true:
 * <ul>
 *   <li><b>{@code digits[i] = -1}</b> — position {@code i} was already consumed as
 *       the <em>right element</em> of a 2-cycle decided at some earlier position
 *       {@code j < i}.  No independent choice is made here; the value is forced.</li>
 *   <li><b>{@code digits[i] =  0}</b> — position {@code i} is a <em>fixed point</em>
 *       ({@code π(i) = i}).  One choice is available: stay.</li>
 *   <li><b>{@code digits[i] ≥  1}</b> — position {@code i} is the <em>left
 *       (active) element</em> of a 2-cycle.  The value is the 1-based index of the
 *       chosen right-partner among all positions {@code > i} that have not yet been
 *       consumed.</li>
 * </ul>
 *
 * <p>The {@code -1} entries are structurally determined — they are not free choices —
 * but they are included in the array so that every involution maps to an array of the
 * same length {@code n}, making position-by-position comparison well-defined.
 *
 * <h2>Complexity</h2>
 * <p>Two implementations back every operation. For small {@code n}
 * ({@code n < N_THRESHOLD}) the linear-scan version runs in O(n²) with a low
 * constant. For large {@code n} a Fenwick (binary-indexed) tree provides
 * order-statistic partner lookup and free-count queries in O(log n), reducing the
 * element-placement work to O(n log n). Note that for an arbitrary rank near
 * {@code T(n)} the rank itself is an Θ(n log n)-bit integer, so the O(n)
 * big-integer divisions against telephone numbers dominate; the Fenwick tree
 * removes the O(n²) scan term but the arbitrary-rank cost remains super-linear
 * because of that unavoidable big-integer arithmetic.
 */
public class InvolutadicAlgorithms{

    private final Calculator calculator;

    /**
     * Below this universe size the linear-scan implementation is used (low constant,
     * O(n²)); at or above it the Fenwick-tree implementation (O(n log n) placement)
     * is used. Both produce bit-identical output; the threshold is purely a
     * performance switch.
     */
    static final int N_THRESHOLD = 100;

    /**
     * Constructs an {@code InvolutadicAlgorithms} instance backed by the given
     * {@link Calculator} for telephone-number arithmetic.
     *
     * @param calculator the calculator used for T(n) computations; must not be null
     */
    public InvolutadicAlgorithms(Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    // =========================================================================
    // 1. Decimal (rank) ↔ Encoded digits
    // =========================================================================

    /**
     * Converts a decimal rank to its Involutadic digit representation.
     *
     * @param rank the 0-based lexicographic rank (0 ≤ rank &lt; T(n))
     * @param n    the order (n ≥ 1)
     * @return Involutadic digit array of length {@code n}, MSD-first
     * @throws IllegalArgumentException if {@code rank} is out of range
     */
    public int[] encode(BigInteger rank, int n) {
        Objects.requireNonNull(rank, "rank");
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);

        BigInteger[] T = calculator.telephoneTable(n);
        BigInteger total = T[n];
        if (rank.signum() < 0 || rank.compareTo(total) >= 0) {
            throw new IllegalArgumentException(
                    "rank " + rank + " out of range [0, " + total + ")");
        }
        return (n < N_THRESHOLD) ? encodeScan(rank, n, T) : encodeFenwick(rank, n, T);
    }

    /**
     * Convenience overload.
     *
     * @param rank the 0-based lexicographic rank (0 ≤ rank &lt; T(n))
     * @param n    the order (n ≥ 1)
     * @return Involutadic digit array of length {@code n}, MSD-first
     */
    public int[] encode(long rank, int n) {
        return encode(BigInteger.valueOf(rank), n);
    }

    /** Linear-scan encode (original; O(n²)). Preferred for small {@code n}. */
    private int[] encodeScan(BigInteger rank, int n, BigInteger[] T) {
        int[] digits = new int[n];
        boolean[] used = new boolean[n];
        BigInteger rem = rank;

        for (int pos = 0; pos < n; pos++) {
            if (used[pos]) {
                digits[pos] = -1;
                continue;
            }

            int free = countFree(used, pos + 1, n);
            BigInteger tFixed = T[free];

            if (rem.compareTo(tFixed) < 0) {
                digits[pos] = 0;
                used[pos] = true;
            } else {
                rem = rem.subtract(tFixed);
                BigInteger tPair = T[free - 1];
                BigInteger[] qr = rem.divideAndRemainder(tPair);
                int partnerIndex = qr[0].intValueExact();
                rem = qr[1];

                digits[pos] = partnerIndex + 1;  // 1-based partner index
                int partner = findKthFree(used, pos, partnerIndex + 1, n);
                used[pos] = true;
                used[partner] = true;
            }
        }
        return digits;
    }

    /**
     * Fenwick-tree encode (O(n log n) placement). Preferred for large {@code n}.
     *
     * <p>Element value {@code e} maps to 1-based Fenwick index {@code e+1}.
     * The tree holds 1 at index {@code e+1} while element {@code e} is unconsumed.
     * The number of unconsumed positions strictly greater than {@code pos} is
     * {@code rsq(n) - rsq(pos+1)}, and the {@code k}-th such position is
     * {@code findKth(rsq(pos+1) + k) - 1} (starting the cumulative rank at
     * {@code rsq(pos+1)} automatically excludes {@code pos} and all smaller indices).
     */
    private int[] encodeFenwick(BigInteger rank, int n, BigInteger[] T) {
        int[] digits = new int[n];
        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);
        BigInteger rem = rank;

        for (int pos = 0; pos < n; pos++) {
            int upto = avail.rsq(pos + 1);            // available elements in [0, pos]
            boolean posAvailable = (upto - avail.rsq(pos)) == 1;
            if (!posAvailable) {
                digits[pos] = -1;
                continue;
            }

            int free = avail.rsq(n) - upto;           // available elements > pos
            BigInteger tFixed = T[free];

            if (rem.compareTo(tFixed) < 0) {
                digits[pos] = 0;
                avail.update(pos + 1, -1);
            } else {
                rem = rem.subtract(tFixed);
                BigInteger tPair = T[free - 1];
                BigInteger[] qr = rem.divideAndRemainder(tPair);
                int partnerIndex = qr[0].intValueExact();
                rem = qr[1];

                digits[pos] = partnerIndex + 1;       // 1-based partner index
                int partner = avail.findKth(upto + partnerIndex + 1) - 1;
                avail.update(pos + 1, -1);
                avail.update(partner + 1, -1);
            }
        }
        return digits;
    }

    /**
     * Converts an Involutadic digit array back to its decimal rank.
     *
     * @param digits the Involutadic digit array of length {@code n}, MSD-first
     * @return the 0-based rank as a {@link BigInteger}
     * @throws IllegalArgumentException if {@code digits} is not a valid representation
     */
    public BigInteger decode(int[] digits) {
        Objects.requireNonNull(digits, "digits");
        int n = digits.length;
        BigInteger[] T = calculator.telephoneTable(n);
        return (n < N_THRESHOLD) ? decodeScan(digits, n, T) : decodeFenwick(digits, n, T);
    }

    /** Linear-scan decode (original; O(n²)). */
    private BigInteger decodeScan(int[] digits, int n, BigInteger[] T) {
        BigInteger result = BigInteger.ZERO;
        boolean[] consumed = new boolean[n];

        for (int pos = 0; pos < n; pos++) {
            int d = digits[pos];

            if (d == -1) {
                if (!consumed[pos]) {
                    throw new IllegalArgumentException(
                            "digit -1 at position " + pos + " but position was not consumed");
                }
                continue;
            }

            if (consumed[pos]) {
                throw new IllegalArgumentException(
                        "position " + pos + " is already consumed but digit is " + d);
            }

            int free = countFree(consumed, pos + 1, n);

            if (d == 0) {
                consumed[pos] = true;
            } else if (d > 0) {
                BigInteger tFixed = T[free];
                BigInteger tPair = T[free - 1];
                result = result.add(tFixed);
                result = result.add(BigInteger.valueOf(d - 1).multiply(tPair));

                int partner = findKthFree(consumed, pos, d, n);
                consumed[pos] = true;
                consumed[partner] = true;
            } else {
                throw new IllegalArgumentException("Invalid digit: " + d + " at position " + pos);
            }
        }

        return result;
    }

    /** Fenwick-tree decode (O(n log n)). */
    private BigInteger decodeFenwick(int[] digits, int n, BigInteger[] T) {
        BigInteger result = BigInteger.ZERO;
        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);

        for (int pos = 0; pos < n; pos++) {
            int d = digits[pos];
            int upto = avail.rsq(pos + 1);
            boolean posAvailable = (upto - avail.rsq(pos)) == 1;

            if (d == -1) {
                if (posAvailable) {
                    throw new IllegalArgumentException(
                            "digit -1 at position " + pos + " but position was not consumed");
                }
                continue;
            }

            if (!posAvailable) {
                throw new IllegalArgumentException(
                        "position " + pos + " is already consumed but digit is " + d);
            }

            int free = avail.rsq(n) - upto;

            if (d == 0) {
                avail.update(pos + 1, -1);
            } else if (d > 0) {
                BigInteger tFixed = T[free];
                BigInteger tPair = T[free - 1];
                result = result.add(tFixed);
                result = result.add(BigInteger.valueOf(d - 1).multiply(tPair));

                int partner = avail.findKth(upto + d) - 1;
                avail.update(pos + 1, -1);
                avail.update(partner + 1, -1);
            } else {
                throw new IllegalArgumentException("Invalid digit: " + d + " at position " + pos);
            }
        }
        return result;
    }

    // =========================================================================
    // 2. Encoded digits ↔ Involution
    // =========================================================================

    /**
     * Converts an Involutadic digit array to its corresponding involution.
     *
     * @param digits the Involutadic digit array of length {@code n}, MSD-first
     * @return the decoded involution array of length {@code n}
     * @throws IllegalArgumentException if {@code digits} is not a valid representation
     */
    public int[] toInvolution(int[] digits) {
        Objects.requireNonNull(digits, "digits");
        int n = digits.length;
        return (n < N_THRESHOLD) ? toInvolutionScan(digits, n) : toInvolutionFenwick(digits, n);
    }

    /** Linear-scan digits→involution (original; O(n²)). */
    private int[] toInvolutionScan(int[] digits, int n) {
        int[] involution = new int[n];
        boolean[] used = new boolean[n];

        for (int pos = 0; pos < n; pos++) {
            int d = digits[pos];

            if (d == -1) {
                if (!used[pos]) {
                    throw new IllegalArgumentException(
                            "digit -1 at position " + pos + " but position was not consumed");
                }
            } else if (d == 0) {
                if (used[pos]) {
                    throw new IllegalArgumentException(
                            "digit 0 at position " + pos + " but position was already consumed");
                }
                involution[pos] = pos;
                used[pos] = true;
            } else if (d > 0) {
                if (used[pos]) {
                    throw new IllegalArgumentException(
                            "digit " + d + " at position " + pos + " but position was already consumed");
                }
                int partner = findKthFree(used, pos, d, n);
                involution[pos] = partner;
                involution[partner] = pos;
                used[pos] = true;
                used[partner] = true;
            } else {
                throw new IllegalArgumentException("Invalid digit: " + d + " at position " + pos);
            }
        }

        return involution;
    }

    /** Fenwick-tree digits→involution (O(n log n)). */
    private int[] toInvolutionFenwick(int[] digits, int n) {
        int[] involution = new int[n];
        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);

        for (int pos = 0; pos < n; pos++) {
            int d = digits[pos];
            int upto = avail.rsq(pos + 1);
            boolean posAvailable = (upto - avail.rsq(pos)) == 1;

            if (d == -1) {
                if (posAvailable) {
                    throw new IllegalArgumentException(
                            "digit -1 at position " + pos + " but position was not consumed");
                }
            } else if (d == 0) {
                if (!posAvailable) {
                    throw new IllegalArgumentException(
                            "digit 0 at position " + pos + " but position was already consumed");
                }
                involution[pos] = pos;
                avail.update(pos + 1, -1);
            } else if (d > 0) {
                if (!posAvailable) {
                    throw new IllegalArgumentException(
                            "digit " + d + " at position " + pos + " but position was already consumed");
                }
                int partner = avail.findKth(upto + d) - 1;
                involution[pos] = partner;
                involution[partner] = pos;
                avail.update(pos + 1, -1);
                avail.update(partner + 1, -1);
            } else {
                throw new IllegalArgumentException("Invalid digit: " + d + " at position " + pos);
            }
        }
        return involution;
    }

    /**
     * Converts an involution to its Involutadic digit representation.
     *
     * @param involution a valid involution of {@code [n]}, length exactly {@code n}
     * @return Involutadic digit array of length {@code n}, MSD-first
     * @throws IllegalArgumentException if {@code involution} is not a valid involution
     */
    public int[] fromInvolution(int[] involution) {
        Objects.requireNonNull(involution, "involution");
        int n = involution.length;
        validateInvolution(involution, n);
        return (n < N_THRESHOLD) ? fromInvolutionScan(involution, n)
                : fromInvolutionFenwick(involution, n);
    }

    /** Linear-scan involution→digits (original; O(n²)). */
    private int[] fromInvolutionScan(int[] involution, int n) {
        int[] digits = new int[n];
        boolean[] consumed = new boolean[n];

        for (int pos = 0; pos < n; pos++) {
            if (consumed[pos]) {
                digits[pos] = -1;
                continue;
            }

            int target = involution[pos];
            if (target == pos) {
                digits[pos] = 0;
                consumed[pos] = true;
            } else {
                digits[pos] = partnerRank(consumed, pos, target, n);
                consumed[pos] = true;
                consumed[target] = true;
            }
        }
        return digits;
    }

    /** Fenwick-tree involution→digits (O(n log n)). */
    private int[] fromInvolutionFenwick(int[] involution, int n) {
        int[] digits = new int[n];
        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);

        for (int pos = 0; pos < n; pos++) {
            int upto = avail.rsq(pos + 1);
            boolean posAvailable = (upto - avail.rsq(pos)) == 1;
            if (!posAvailable) {
                digits[pos] = -1;
                continue;
            }

            int target = involution[pos];
            if (target == pos) {
                digits[pos] = 0;
                avail.update(pos + 1, -1);
            } else {
                // 1-based rank of target among available elements > pos
                digits[pos] = avail.rsq(target + 1) - upto;
                avail.update(pos + 1, -1);
                avail.update(target + 1, -1);
            }
        }
        return digits;
    }

    // =========================================================================
    // 3. Decimal (rank) ↔ Involution
    // =========================================================================

    /**
     * Converts a decimal rank to its corresponding involution.
     *
     * @param rank the 0-based lexicographic rank (0 ≤ rank &lt; T(n))
     * @param n    the order (n ≥ 1)
     * @return the involution array of length {@code n}
     * @throws IllegalArgumentException if {@code rank} is out of range
     */
    public int[] unrank(BigInteger rank, int n) {
        Objects.requireNonNull(rank, "rank");
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);

        BigInteger[] T = calculator.telephoneTable(n);
        BigInteger total = T[n];
        if (rank.signum() < 0 || rank.compareTo(total) >= 0) {
            throw new IllegalArgumentException(
                    "rank " + rank + " out of range [0, " + total + ")");
        }
        return (n < N_THRESHOLD) ? unrankScan(rank, n, T) : unrankFenwick(rank, n, T);
    }

    /**
     * Convenience overload.
     *
     * @param rank the 0-based lexicographic rank (0 ≤ rank &lt; T(n))
     * @param n    the order (n ≥ 1)
     * @return the involution array of length {@code n}
     */
    public int[] unrank(long rank, int n) {
        return unrank(BigInteger.valueOf(rank), n);
    }

    /** Linear-scan rank→involution (original; O(n²)). */
    private int[] unrankScan(BigInteger rank, int n, BigInteger[] T) {
        int[] involution = new int[n];
        boolean[] used = new boolean[n];
        BigInteger rem = rank;

        for (int pos = 0; pos < n; pos++) {
            if (used[pos]) continue;

            int free = countFree(used, pos + 1, n);
            BigInteger tFixed = T[free];

            if (rem.compareTo(tFixed) < 0) {
                involution[pos] = pos;
                used[pos] = true;
            } else {
                rem = rem.subtract(tFixed);
                BigInteger tPair = T[free - 1];
                BigInteger[] qr = rem.divideAndRemainder(tPair);
                int partnerIndex = qr[0].intValueExact();
                rem = qr[1];

                int partner = findKthFree(used, pos, partnerIndex + 1, n);
                involution[pos] = partner;
                involution[partner] = pos;
                used[pos] = true;
                used[partner] = true;
            }
        }
        return involution;
    }

    /** Fenwick-tree rank→involution (O(n log n) placement). */
    private int[] unrankFenwick(BigInteger rank, int n, BigInteger[] T) {
        int[] involution = new int[n];
        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);
        BigInteger rem = rank;

        for (int pos = 0; pos < n; pos++) {
            int upto = avail.rsq(pos + 1);
            boolean posAvailable = (upto - avail.rsq(pos)) == 1;
            if (!posAvailable) continue;

            int free = avail.rsq(n) - upto;
            BigInteger tFixed = T[free];

            if (rem.compareTo(tFixed) < 0) {
                involution[pos] = pos;
                avail.update(pos + 1, -1);
            } else {
                rem = rem.subtract(tFixed);
                BigInteger tPair = T[free - 1];
                BigInteger[] qr = rem.divideAndRemainder(tPair);
                int partnerIndex = qr[0].intValueExact();
                rem = qr[1];

                int partner = avail.findKth(upto + partnerIndex + 1) - 1;
                involution[pos] = partner;
                involution[partner] = pos;
                avail.update(pos + 1, -1);
                avail.update(partner + 1, -1);
            }
        }
        return involution;
    }

    /**
     * Converts an involution to its decimal rank.
     *
     * @param involution a valid involution of {@code [n]}, length exactly {@code n}
     * @return the 0-based rank as a {@link BigInteger}
     * @throws IllegalArgumentException if {@code involution} is not a valid involution
     */
    public BigInteger rank(int[] involution) {
        Objects.requireNonNull(involution, "involution");
        int n = involution.length;
        validateInvolution(involution, n);
        BigInteger[] T = calculator.telephoneTable(n);
        return (n < N_THRESHOLD) ? rankScan(involution, n, T) : rankFenwick(involution, n, T);
    }

    /** Linear-scan involution→rank (original; O(n²)). */
    private BigInteger rankScan(int[] involution, int n, BigInteger[] T) {
        BigInteger result = BigInteger.ZERO;
        boolean[] consumed = new boolean[n];

        for (int pos = 0; pos < n; pos++) {
            if (consumed[pos]) continue;

            int target = involution[pos];
            int free = countFree(consumed, pos + 1, n);

            if (target == pos) {
                consumed[pos] = true;
            } else {
                int partnerRankValue = partnerRank(consumed, pos, target, n);
                BigInteger tFixed = T[free];
                BigInteger tPair = T[free - 1];
                result = result.add(tFixed);
                result = result.add(BigInteger.valueOf(partnerRankValue - 1).multiply(tPair));

                consumed[pos] = true;
                consumed[target] = true;
            }
        }
        return result;
    }

    /** Fenwick-tree involution→rank (O(n log n)). */
    private BigInteger rankFenwick(int[] involution, int n, BigInteger[] T) {
        BigInteger result = BigInteger.ZERO;
        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);

        for (int pos = 0; pos < n; pos++) {
            int upto = avail.rsq(pos + 1);
            boolean posAvailable = (upto - avail.rsq(pos)) == 1;
            if (!posAvailable) continue;

            int target = involution[pos];
            int free = avail.rsq(n) - upto;

            if (target == pos) {
                avail.update(pos + 1, -1);
            } else {
                int partnerRankValue = avail.rsq(target + 1) - upto;  // 1-based rank > pos
                BigInteger tFixed = T[free];
                BigInteger tPair = T[free - 1];
                result = result.add(tFixed);
                result = result.add(BigInteger.valueOf(partnerRankValue - 1).multiply(tPair));

                avail.update(pos + 1, -1);
                avail.update(target + 1, -1);
            }
        }
        return result;
    }

    // =========================================================================
    // Private helper methods (linear-scan path)
    // =========================================================================

    /**
     * Returns the 1-based rank of {@code target} among unconsumed positions
     * strictly greater than {@code pos}.
     */
    private int partnerRank(boolean[] consumed, int pos, int target, int n) {
        int rank = 0;
        for (int i = pos + 1; i < n; i++) {
            if (!consumed[i]) {
                rank++;
                if (i == target) return rank;
            }
        }
        throw new IllegalStateException(
                "target " + target + " not found among free positions after " + pos);
    }

    /**
     * Returns the position of the {@code k}-th (1-based) unconsumed element
     * strictly greater than {@code pos}.
     */
    private int findKthFree(boolean[] consumed, int pos, int k, int n) {
        int count = 0;
        for (int i = pos + 1; i < n; i++) {
            if (!consumed[i]) {
                count++;
                if (count == k) return i;
            }
        }
        throw new IllegalArgumentException(
                "Not enough free positions to the right of " + pos +
                        " to find " + k + "-th partner (only " + count + " available)");
    }

    /**
     * Returns the number of unconsumed positions in {@code [from, n)}.
     */
    private int countFree(boolean[] consumed, int from, int n) {
        int count = 0;
        for (int i = from; i < n; i++) {
            if (!consumed[i]) count++;
        }
        return count;
    }

    /**
     * Validates that {@code inv} is a valid involution of {@code [n]}.
     */
    private void validateInvolution(int[] inv, int n) {
        for (int i = 0; i < n; i++) {
            if (inv[i] < 0 || inv[i] >= n) {
                throw new IllegalArgumentException(
                        "involution[" + i + "] = " + inv[i] + " out of range [0," + n + ")");
            }
            if (inv[inv[i]] != i) {
                throw new IllegalArgumentException(
                        "Not self-inverse at position " + i +
                                ": inv[" + i + "]=" + inv[i] + " but inv[" + inv[i] + "]=" + inv[inv[i]]);
            }
        }
    }
}