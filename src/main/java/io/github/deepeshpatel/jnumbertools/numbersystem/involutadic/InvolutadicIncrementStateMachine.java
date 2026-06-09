/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Stateful lexicographic successor engine for involutions (Involutadic increment machine).
 *
 * <p>Maintains a dual-state representation — a V2 Involutadic digit array (MSD-first,
 * fixed width {@code n}, with {@code -1} for consumed positions) and a materialised
 * involution array — that are always kept consistent. Advancing to the next involution
 * in lexicographic order costs <b>amortised O(1)</b>; the involution array is available
 * in O(1) via {@link #involution()}.
 *
 * <h2>Digit-array conventions (V2)</h2>
 * <ul>
 *   <li>{@code digits[i] == -1} — position {@code i} was consumed as the right element
 *       of a 2-cycle decided at some earlier position; no free choice here.</li>
 *   <li>{@code digits[i] ==  0} — position {@code i} is a fixed point.</li>
 *   <li>{@code digits[i] >=  1} — position {@code i} is the left element of a 2-cycle;
 *       the value is the 1-based index of the chosen right-partner among all unconsumed
 *       positions {@code > i}.</li>
 * </ul>
 *
 * @author Deepesh Patel and Aditya Patel
 * @since 3.0.2
 */
public final class InvolutadicIncrementStateMachine {

    private final InvolutadicAlgorithms alg;
    private final int n;

    private int[] digits;
    private int[] maxDigit;
    private int[] involution;

    // Persistent consumed state to avoid O(n) prefix replay
    private boolean[] consumed;

    public InvolutadicIncrementStateMachine(int n, Calculator calculator) {
        this(n, BigInteger.ZERO, calculator);
    }

    public InvolutadicIncrementStateMachine(int n, long rank, Calculator calculator) {
        this(n, BigInteger.valueOf(rank), calculator);
    }

    public InvolutadicIncrementStateMachine(int n, BigInteger rank, Calculator calculator) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);
        Objects.requireNonNull(rank,       "rank");
        Objects.requireNonNull(calculator, "calculator");

        this.n   = n;
        this.alg = new InvolutadicAlgorithms(calculator);

        this.digits     = alg.encode(rank, n);
        this.maxDigit   = new int[n];
        this.involution = new int[n];
        this.consumed   = new boolean[n];

        rebuildFromDigits();
    }

    public int[] involution() {
        return involution;
    }

    public int[] getDigits() {
        return Arrays.copyOf(digits, n);
    }

    public boolean increment() {
        int pivot = findPivot();
        if (pivot == -1) return false;

        digits[pivot]++;
        rollbackSuffix(pivot);
        rebuildSuffix(pivot);
        return true;
    }

    public int incrementAndGetCarryLength() {
        int pivot = findPivot();
        if (pivot == -1) return 0;

        // Count BEFORE modifying anything — non-(-1) in [pivot, n) = decisions in suffix
        // This is identical to the original definition; -1 entries are not decisions.
        int carry = 0;
        for (int i = pivot; i < n; i++) {
            if (digits[i] != -1) carry++;
        }

        digits[pivot]++;
        rollbackSuffix(pivot);
        rebuildSuffix(pivot);
        return carry;
    }

    // =========================================================================
    // Core Increment Logic (Zero-Allocation, Amortised O(1))
    // =========================================================================

    private int findPivot() {
        for (int i = n - 1; i >= 0; i--) {
            if (digits[i] != -1 && digits[i] < maxDigit[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Selectively un-consumes only the positions in the suffix that were
     * consumed by the suffix itself (or the pivot).
     * Positions consumed by the prefix have their partner < pivot, so they are untouched.
     */
    private void rollbackSuffix(int pivot) {
        for (int i = n - 1; i >= pivot; i--) {
            if (consumed[i]) {
                int partner = involution[i];
                if (partner >= pivot) {
                    consumed[i] = false;
                    consumed[partner] = false;
                    involution[i] = i;
                    involution[partner] = partner;
                }
            }
        }
    }

    /**
     * Rebuilds the suffix from the pivot onwards.
     * Uses a dynamic totalFree counter to compute maxDigit perfectly in a single pass.
     */
    private void rebuildSuffix(int pivot) {
        // Count total free positions in the suffix before making any new decisions
        int totalFree = 0;
        for (int i = pivot; i < n; i++) {
            if (!consumed[i]) totalFree++;
        }

        // Left-to-right pass to apply decisions and compute maxDigit dynamically
        for (int i = pivot; i < n; i++) {
            if (consumed[i]) {
                // Consumed by prefix OR consumed by a suffix decision at j < i
                digits[i] = -1;
                maxDigit[i] = -1;
                continue;
            }

            // i is a decision point. Available partners = totalFree - 1
            maxDigit[i] = totalFree - 1;

            if (i > pivot) {
                digits[i] = 0; // Reset suffix to fixed points
            }

            int d = digits[i];
            if (d == 0) {
                involution[i] = i;
                consumed[i] = true;
                totalFree--;
            } else {
                int partner = findKthFree(i, d);
                involution[i] = partner;
                involution[partner] = i;
                consumed[i] = true;
                consumed[partner] = true;
                totalFree -= 2;
            }
        }
    }

    // =========================================================================
    // Initialization & Helpers
    // =========================================================================

    /**
     * Cold-start build of the full state from {@link #digits}.
     *
     * <p>Uses a Fenwick (binary-indexed) tree for O(log n) order-statistic partner
     * lookup, giving O(n log n) construction instead of the O(n²) cost of repeated
     * linear partner scans. Element value {@code e} occupies 1-based Fenwick index
     * {@code e+1}; the tree stores 1 while the element is unconsumed. The number of
     * unconsumed positions in {@code [pos, n)} (the live {@code totalFree}) is
     * {@code rsq(n) - rsq(pos)}, and the {@code d}-th unconsumed partner strictly
     * greater than {@code pos} is {@code findKth(rsq(pos+1) + d) - 1}.
     *
     * <p>Produces {@code digits} (with {@code -1} in consumed slots), {@code maxDigit},
     * {@code involution}, and {@code consumed} bit-identically to a left-to-right
     * linear-scan build, so the subsequent {@link #increment()} behaviour is
     * unchanged. The steady-state increment path ({@link #rebuildSuffix}) keeps its
     * local scan, which is O(carry length) ≈ amortised O(1) and not a bottleneck.
     */
    private void rebuildFromDigits() {
        for (int i = 0; i < n; i++) involution[i] = i;
        Arrays.fill(consumed, false);

        FenwickTree avail = new FenwickTree(n);
        for (int i = 1; i <= n; i++) avail.update(i, 1);

        for (int pos = 0; pos < n; pos++) {
            int uptoPrev = avail.rsq(pos);                 // available in [0, pos)
            boolean posAvailable = (avail.rsq(pos + 1) - uptoPrev) == 1;
            if (!posAvailable) {
                digits[pos] = -1;
                maxDigit[pos] = -1;
                continue;
            }

            int totalFree = avail.rsq(n) - uptoPrev;       // unconsumed in [pos, n)
            maxDigit[pos] = totalFree - 1;
            int d = digits[pos];

            if (d == 0) {
                involution[pos] = pos;
                consumed[pos] = true;
                avail.update(pos + 1, -1);
            } else {
                int upto = avail.rsq(pos + 1);             // available in [0, pos]
                int partner = avail.findKth(upto + d) - 1; // d-th unconsumed > pos
                involution[pos] = partner;
                involution[partner] = pos;
                consumed[pos] = true;
                consumed[partner] = true;
                avail.update(pos + 1, -1);
                avail.update(partner + 1, -1);
            }
        }
    }

    private int findKthFree(int pos, int k) {
        int count = 0;
        for (int i = pos + 1; i < n; i++) {
            if (!consumed[i]) {
                count++;
                if (count == k) return i;
            }
        }
        throw new IllegalStateException("Cannot find " + k + "-th free partner for pos=" + pos);
    }
}