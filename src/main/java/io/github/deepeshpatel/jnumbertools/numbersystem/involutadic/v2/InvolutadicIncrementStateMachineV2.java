/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.v2;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

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
 * The array is MSD-first (index 0 = most-significant decision), has fixed length
 * {@code n}, and lexicographic order on the array equals rank order on involutions
 * (with {@code -1 < 0 < 1 < 2 ...}).
 *
 * <h2>Increment Algorithm</h2>
 * <p>Because the representation is MSD-first and fixed-width (with structural
 * {@code -1} entries), incrementing works right-to-left on the <em>free</em>
 * (non-{@code -1}) positions:
 * <ol>
 *   <li><b>Find pivot.</b> Scan from the rightmost free position leftward to find
 *       the rightmost free position {@code p} where {@code digits[p] < maxDigit[p]}.</li>
 *   <li><b>Increment pivot.</b> Increment {@code digits[p]}.</li>
 *   <li><b>Reset suffix.</b> For every free position {@code q > p}, set
 *       {@code digits[q] = 0}; recompute the {@code -1} entries and {@code maxDigit}
 *       values for the suffix by replaying decisions from {@code p} onward.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>
 * var engine = new InvolutadicIncrementStateMachine(6, 0L, calculator);
 * do {
 *     int[] inv = engine.involution();
 *     // process inv ...
 * } while (engine.increment());
 * </pre>
 *
 * @author Deepesh Patel and Aditya Patel
 * @since 3.0.2
 */
public final class InvolutadicIncrementStateMachineV2 {

    private final InvolutadicAlgorithmsV2 alg;
    private final int n;

    /**
     * MSD-first digit array of length {@code n}.
     * Values: -1 (consumed), 0 (fixed point), >=1 (left element of 2-cycle).
     */
    private int[] digits;

    /**
     * Maximum legal digit at each position.
     * {@code maxDigit[i] == -1} when {@code digits[i] == -1} (no choice).
     * Otherwise {@code maxDigit[i]} == number of unconsumed positions to the
     * right of {@code i} at the time position {@code i} is decided.
     */
    private int[] maxDigit;

    /** Live involution array, always consistent with {@code digits}. */
    private int[] involution;

    // =========================================================================
    // Performance Optimization: Versioned "Consumed" Tracking
    // =========================================================================
    // Instead of allocating a new boolean[n] array on EVERY increment (which causes
    // massive GC pressure and hidden O(n) clearing costs), we use an int[] array
    // paired with a global version counter. "Clearing" the board is now O(1).

    private int[] consumedVersion;
    private int currentVersion = 0;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Constructs a new state machine starting at rank 0.
     *
     * @param n          universe size (n ≥ 1)
     * @param calculator memoising calculator
     */
    public InvolutadicIncrementStateMachineV2(int n, Calculator calculator) {
        this(n, BigInteger.ZERO, calculator);
    }

    /**
     * Constructs a new state machine starting at the given rank.
     *
     * @param n          universe size (n ≥ 1)
     * @param rank       starting rank (0 ≤ rank < T(n))
     * @param calculator memoising calculator
     */
    public InvolutadicIncrementStateMachineV2(int n, long rank, Calculator calculator) {
        this(n, BigInteger.valueOf(rank), calculator);
    }

    /**
     * Constructs a new state machine starting at the given rank.
     *
     * @param n          universe size (n ≥ 1)
     * @param rank       starting rank (0 ≤ rank < T(n))
     * @param calculator memoising calculator
     */
    public InvolutadicIncrementStateMachineV2(int n, BigInteger rank, Calculator calculator) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);
        Objects.requireNonNull(rank,       "rank");
        Objects.requireNonNull(calculator, "calculator");

        this.n   = n;
        this.alg = new InvolutadicAlgorithmsV2(calculator);

        this.digits     = alg.encode(rank, n);
        this.maxDigit   = new int[n];
        this.involution = new int[n];
        this.consumedVersion = new int[n]; // Allocated exactly once!

        rebuildFromDigits();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns the current involution array.
     *
     * <p><strong>Performance note:</strong> Returns a live reference — do NOT modify.
     * Clone if mutation is required.
     *
     * @return live reference to the current involution of length {@code n}
     */
    public int[] involution() {
        return involution;
    }

    /**
     * Returns a snapshot of the current digit array (MSD-first, length {@code n}).
     *
     * @return copy of the digit array
     */
    public int[] getDigits() {
        return Arrays.copyOf(digits, n);
    }

    /**
     * Advances to the next involution in lexicographic order.
     *
     * @return {@code true} if successfully advanced; {@code false} if all
     *         involutions have been enumerated
     */
    public boolean increment() {
        int pivot = -1;
        for (int i = n - 1; i >= 0; i--) {
            if (digits[i] != -1 && digits[i] < maxDigit[i]) {
                pivot = i;
                break;
            }
        }
        if (pivot == -1) return false;   // already at last involution

        digits[pivot]++;
        rebuildSuffixFrom(pivot + 1);
        return true;
    }

    /**
     * Advances and returns the number of digit positions modified (carry length).
     * Returns 0 if enumeration is complete.
     *
     * @return number of positions changed (&gt; 0) on success; 0 if exhausted
     */
    public int incrementAndGetCarryLength() {
        int pivot = -1;
        for (int i = n - 1; i >= 0; i--) {
            if (digits[i] != -1 && digits[i] < maxDigit[i]) {
                pivot = i;
                break;
            }
        }
        if (pivot == -1) return 0;

        digits[pivot]++;
        rebuildSuffixFrom(pivot + 1);
        return n - pivot;
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private boolean isConsumed(int i) {
        return consumedVersion[i] == currentVersion;
    }

    private void setConsumed(int i) {
        consumedVersion[i] = currentVersion;
    }

    /**
     * Full rebuild: recomputes {@code involution} and {@code maxDigit} from
     * {@code digits[0..n-1]} in a single left-to-right pass.
     * Called once at construction.
     */
    private void rebuildFromDigits() {
        for (int i = 0; i < n; i++) involution[i] = i;

        incrementVersion(); // O(1) clear

        for (int pos = 0; pos < n; pos++) {
            int d = digits[pos];
            if (d == -1) {
                maxDigit[pos] = -1;
            } else {
                maxDigit[pos] = countFreeFrom(pos + 1);
                applyDecision(pos, d);
            }
        }
    }

    /**
     * Partial rebuild: resets and recomputes everything from position {@code from}
     * to {@code n-1}, leaving positions {@code 0..from-1} intact.
     */
    private void rebuildSuffixFrom(int from) {
        // O(1) clear of the "consumed" state by bumping the version!
        incrementVersion();

        // Step 1: replay prefix to reconstruct consumed state up to `from`.
        for (int pos = 0; pos < from; pos++) {
            int d = digits[pos];
            if (d == -1) continue;
            applyDecision(pos, d);
        }

        // Step 2: Count total free positions from `from` to end
        int freeCount = countFreeFrom(from);

        // Step 3: Walk suffix left-to-right.
        // (Note: The original code had a redundant loop here to reset involution[pos] = pos,
        // but applyDecision(pos, 0) already does exactly that, so it was removed for speed).
        for (int pos = from; pos < n; pos++) {
            if (isConsumed(pos)) {
                digits[pos]   = -1;
                maxDigit[pos] = -1;
            } else {
                digits[pos]   = 0;
                maxDigit[pos] = freeCount - 1;
                applyDecision(pos, 0);
                freeCount--;
            }
        }
    }

    /**
     * Safely increments the version counter, handling integer overflow gracefully.
     */
    private void incrementVersion() {
        if (currentVersion == Integer.MAX_VALUE) {
            // Only happens once every 2.1 billion increments. O(n) cost is negligible here.
            Arrays.fill(consumedVersion, 0);
            currentVersion = 1;
        } else {
            currentVersion++;
        }
    }

    /**
     * Applies a single decision at {@code pos} to both the consumed state and
     * {@code involution[]}.
     */
    private void applyDecision(int pos, int digit) {
        if (digit == 0) {
            involution[pos] = pos;
            setConsumed(pos);
        } else {
            int partner = findKthFree(pos, digit);
            involution[pos]     = partner;
            involution[partner] = pos;
            setConsumed(pos);
            setConsumed(partner);
        }
    }

    /**
     * Returns the {@code k}-th (1-based) unconsumed position strictly greater
     * than {@code pos}.
     */
    private int findKthFree(int pos, int k) {
        int count = 0;
        for (int i = pos + 1; i < n; i++) {
            if (!isConsumed(i)) {
                count++;
                if (count == k) return i;
            }
        }
        throw new IllegalStateException(
                "Cannot find " + k + "-th free partner for pos=" + pos);
    }

    /**
     * Counts unconsumed positions in {@code [from, n)}.
     */
    private int countFreeFrom(int from) {
        int count = 0;
        for (int i = from; i < n; i++) {
            if (!isConsumed(i)) count++;
        }
        return count;
    }
}