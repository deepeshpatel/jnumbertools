/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Stateful lexicographic successor engine for involutions (Involutadic increment machine).
 *
 * <p>Maintains a dual-state representation — a digit array and a materialised involution
 * array — that are always kept consistent. Advancing to the next involution in
 * lexicographic order costs <b>amortised O(1)</b>; the involution array is available
 * in O(1) via {@link InvolutadicState#currentInvolution()}.
 *
 * <h2>Increment Algorithm</h2>
 *
 * <ol>
 *   <li><b>Find pivot.</b> In the LSD-first digit array, scan from index 0 upward
 *       to find the smallest index {@code p} where {@code digits[p] < maxDigit[p]}.
 *       Note: {@code maxDigit[0]} is 0 only when the last decision is a fixed point
 *       (1 unplaced position remaining); it can be &gt; 0 when the last decision is a
 *       2-cycle choice among multiple candidates, so the search must include index 0.</li>
 *   <li><b>Suffix increment.</b> If pivot {@code p} exists:
 *       <ol type="a">
 *         <li>Increment {@code digits[p]}.</li>
 *         <li>Reset {@code digits[0..p-1]} to 0 (minimum = fixed point).</li>
 *         <li>Rollback: undo the involution assignments for all decisions after
 *             (and including) the changed position.</li>
 *         <li>Rebuild: re-apply decisions greedily with new digits.</li>
 *       </ol>
 *   </li>
 *   <li><b>Exhaustion.</b> If no pivot exists, all involutions of order {@code n}
 *       have been enumerated; {@link #increment} returns {@code false}.</li>
 * </ol>
 *
 * <h2>Amortised O(1) Proof Sketch</h2>
 *
 * <p>Define the potential Φ = (number of trailing decisions where digit == maxDigit).
 * When the last decision is a fixed point, maxDigit[0] = 0 and that decision is
 * always saturated, so Φ ≥ 1; when the last decision is a 2-cycle, maxDigit[0] ≥ 1
 * and it contributes to Φ only if the digit is also at its max.
 * An increment with carry length {@code C = p+1} decreases Φ by at least {@code p-1}
 * and increases it by at most 1, giving amortised cost ≤ 3. The expected carry length
 * under uniform random increments converges to a finite constant governed by
 * {@code ∑ k/T(k)} (analogous to Derangadic's {@code e²} bound), because
 * {@code T(k) ~ k!/e·√(2)} grows super-exponentially.
 *
 * <h2>Usage</h2>
 * <pre>
 *   InvolutadicIncrement engine = new InvolutadicIncrement();
 *   InvolutadicState state = engine.initialState(6);             // start at rank 0
 *   // or: engine.initialState(6, BigInteger.valueOf(30));        // start at rank 30
 *   do {
 *       int[] inv = state.currentInvolution();
 *       // process inv ...
 *   } while (engine.increment(state));
 * </pre>
 *
 * @author Deepesh Patel and Aditya Patel
 * @since 3.0.2
 */
public final class InvolutadicIncrementStateMachine {

    private final Calculator calculator;
    private final InvolutadicAlgorithms alg;

    public InvolutadicIncrementStateMachine() {
        this(new Calculator());
    }

    public InvolutadicIncrementStateMachine(Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.alg = new InvolutadicAlgorithms(calculator);
    }

    // ==================== Public API ====================

    /**
     * Creates the initial state for rank 0.
     *
     * @param n number of elements (n ≥ 1)
     * @return initial state representing the lexicographically smallest involution
     */
    public InvolutadicState initialState(int n) {
        return initialState(n, BigInteger.ZERO);
    }

    /**
     * Creates the initial state for the given rank.
     *
     * @param n    number of elements (n ≥ 1)
     * @param rank starting rank (0 ≤ rank < T(n))
     * @return initial state representing the involution at the given rank
     */
    public InvolutadicState initialState(int n, BigInteger rank) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);
        Objects.requireNonNull(rank, "rank");
        int[] digits = alg.toInvolutadic(rank, n);
        InvolutadicState state = new InvolutadicState(n, digits);
        rebuildAllFromDigits(state);
        return state;
    }

    /**
     * Advances the state to the next involution in lexicographic order.
     *
     * @param state the current state (modified in-place)
     * @return {@code true} if the state was advanced; {@code false} if all
     *         involutions have been enumerated (state is left at the last involution)
     */
    public boolean increment(InvolutadicState state) {
        int k = state.digits.length;

        // Find pivot: smallest LSD-first index p >= 0 where digits[p] < maxDigit[p]
        // Note: index 0 is the last decision. For all-fixed-point involutions maxDigit[0]=0,
        // but for involutions ending in a 2-cycle, maxDigit[0] can be > 0, so we must
        // include index 0 in the pivot search.
        int p = -1;
        for (int i = 0; i < k; i++) {
            if (state.digits[i] < state.maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p == -1) {
            // All decisions are at their maximum -> check whether any 2-cycle could
            // also be expressed differently. In Involutadic, when all maxDigits are
            // reached, the entire space T(n) has been exhausted.
            return false;
        }

        // Increment pivot digit
        state.digits[p]++;

        // Reset all less-significant decisions (indices 0..p-1) to 0 (fixed point)
        Arrays.fill(state.digits, 0, p, 0);

        // Rollback decisions from the pivot onward, then rebuild
        // Decision index p in LSD-first = decision (k-1-p) in MSD-first order
        int pivotMsdIdx = k - 1 - p;
        rollbackAndRebuild(state, pivotMsdIdx);
        return true;
    }

    // ==================== Internal logic ====================

    /**
     * Builds the full involution and maxDigit arrays from scratch using current digits.
     * Called once at initialisation.
     */
    private void rebuildAllFromDigits(InvolutadicState state) {
        int n = state.n;
        int k = state.digits.length;
        Arrays.fill(state.placed, false);
        Arrays.fill(state.involution, 0);

        // Iterate decisions MSD-first (index k-1 down to 0)
        for (int di = k - 1; di >= 0; di--) {
            int digit = state.digits[di];
            int pos = InvolutadicAlgorithms.firstUnplaced(state.placed, n);
            int unplaced = InvolutadicAlgorithms.countUnplaced(state.placed, n);

            // Record maxDigit for this decision.
            // The digits array is LSD-first, so digits[k-1] = first/MSD decision and
            // digits[0] = last/LSD decision. The loop variable di runs k-1..0 (MSD to LSD),
            // reading digits[di], so di is already the correct LSD-first storage index.
            state.maxDigit[di] = unplaced - 1;

            applyDecision(state, pos, digit, n);

            // Record which position this decision handled (for rollback)
            state.decisionPos[di] = pos;
        }
    }

    /**
     * Rolls back decisions from MSD index {@code pivotMsdIdx} onward,
     * increments the pivot (already done by caller), resets earlier decisions
     * to 0, and rebuilds involution from {@code pivotMsdIdx} onward.
     */
    private void rollbackAndRebuild(InvolutadicState state, int pivotMsdIdx) {
        int n = state.n;
        int k = state.digits.length;

        // Undo involution assignments from pivotMsdIdx onward (MSD order)
        // pivotMsdIdx in MSD = LSD index p = k-1-pivotMsdIdx
        // All decisions at MSD indices >= pivotMsdIdx need rollback
        int pivotLsd = k - 1 - pivotMsdIdx;

        // Un-place all positions that were set by decisions at MSD index >= pivotMsdIdx
        for (int msdIdx = pivotMsdIdx; msdIdx < k; msdIdx++) {
            int pos = state.decisionPos[k - 1 - msdIdx]; // LSD index = k-1-msdIdx
            if (pos < 0) continue; // not yet set
            int partner = state.involution[pos];
            state.placed[pos] = false;
            if (partner != pos) {
                state.placed[partner] = false;
                state.involution[partner] = partner; // reset to identity
            }
            state.involution[pos] = pos;
        }

        // May need to resize the digit array: after incrementing the pivot digit,
        // the number of remaining decisions can change (a 2-cycle consumes 2 positions,
        // so the suffix may now have fewer decisions).
        // Recompute the expected number of decisions from pivotMsdIdx onward.
        // The simplest correct approach: rebuild greedily with digits = 0 for suffix.

        // Rebuild from pivotMsdIdx onward
        for (int msdIdx = pivotMsdIdx; msdIdx < k; msdIdx++) {
            int lsdIdx = k - 1 - msdIdx;
            int digit = state.digits[lsdIdx]; // 0 for suffix (reset), pivot value for pivot
            int pos = InvolutadicAlgorithms.firstUnplaced(state.placed, n);

            if (pos < 0) {
                // No more positions to place: trim the digit array to msdIdx decisions
                state.trimTo(msdIdx);
                return;
            }

            int unplaced = InvolutadicAlgorithms.countUnplaced(state.placed, n);
            int maxD = unplaced - 1;
            state.maxDigit[lsdIdx] = maxD;
            state.decisionPos[lsdIdx] = pos;

            // Clamp digit to maxD (shouldn't exceed, but guard against stale values)
            if (digit > maxD) digit = 0;
            state.digits[lsdIdx] = digit;

            applyDecision(state, pos, digit, n);
        }

        // If there are still unplaced positions after k decisions, expand the array
        int pos = InvolutadicAlgorithms.firstUnplaced(state.placed, n);
        if (pos >= 0) {
            expandToComplete(state, n);
        }
    }

    /**
     * Applies one decision (digit) at {@code pos} and updates the involution and placed arrays.
     */
    private static void applyDecision(InvolutadicState state, int pos, int digit, int n) {
        if (digit == 0) {
            state.involution[pos] = pos;
            state.placed[pos] = true;
        } else {
            int partner = InvolutadicAlgorithms.findKthPartner(state.placed, pos, digit - 1, n);
            state.involution[pos] = partner;
            state.involution[partner] = pos;
            state.placed[pos] = true;
            state.placed[partner] = true;
        }
    }

    /**
     * Expands the digit array with additional fixed-point decisions (digit=0)
     * until all positions are placed. Called when a 2-cycle choice in the suffix
     * shrinks the number of remaining decisions below the current array size.
     */
    private static void expandToComplete(InvolutadicState state, int n) {
        int pos;
        while ((pos = InvolutadicAlgorithms.firstUnplaced(state.placed, n)) >= 0) {
            int unplaced = InvolutadicAlgorithms.countUnplaced(state.placed, n);
            // Append one decision (digit=0, fixed point) to the LSD end
            state.appendDecision(pos, 0, unplaced - 1);
            state.involution[pos] = pos;
            state.placed[pos] = true;
        }
    }

    // ==================== State class ====================

    /**
     * Mutable state for the Involutadic increment machine.
     *
     * <p>The digit array is LSD-first. Its length equals the number of decisions,
     * which can change during structural transitions (when a 2-cycle replaces two
     * fixed-point decisions or vice versa).
     */
    public static final class InvolutadicState {

        /** The universe size. */
        final int n;

        /** Current digit array, LSD-first. Length = number of decisions. */
        int[] digits;

        /** Maximum digit at each LSD-first position. */
        int[] maxDigit;

        /**
         * Position in [n] handled by the decision at each LSD-first index.
         * Needed for rollback.
         */
        int[] decisionPos;

        /** Which positions have been placed. */
        boolean[] placed;

        /** Current involution (always consistent with digits). */
        int[] involution;

        InvolutadicState(int n, int[] digits) {
            this.n = n;
            int k = digits.length;
            this.digits = Arrays.copyOf(digits, k);
            this.maxDigit = new int[k];
            this.decisionPos = new int[k];
            Arrays.fill(this.decisionPos, -1);
            this.placed = new boolean[n];
            this.involution = new int[n];
            for (int i = 0; i < n; i++) this.involution[i] = i;
        }

        /** Returns a snapshot of the current digit array (LSD-first). */
        public int[] getDigits() {
            return Arrays.copyOf(digits, digits.length);
        }

        /** Returns a snapshot of the current involution. Always O(n). */
        public int[] currentInvolution() {
            return Arrays.copyOf(involution, n);
        }

        /** Returns the current number of decisions (= digit array length). */
        public int decisionCount() {
            return digits.length;
        }

        /**
         * Appends one new decision at the LSD end (used by {@code expandToComplete}).
         */
        void appendDecision(int pos, int digit, int maxD) {
            // Insert at front of LSD-first array (i.e. new LSD position 0, shift rest up)
            int k = digits.length;
            digits = Arrays.copyOf(digits, k + 1);
            maxDigit = Arrays.copyOf(maxDigit, k + 1);
            decisionPos = Arrays.copyOf(decisionPos, k + 1);
            // Shift everything right by 1
            System.arraycopy(digits,     0, digits,     1, k);
            System.arraycopy(maxDigit,   0, maxDigit,   1, k);
            System.arraycopy(decisionPos,0, decisionPos,1, k);
            digits[0]      = digit;
            maxDigit[0]    = maxD;
            decisionPos[0] = pos;
        }

        /**
         * Trims the digit array to {@code newMsdCount} decisions (MSD-first count),
         * keeping the first {@code newMsdCount} decisions.
         * In LSD-first storage, keeps the last {@code newMsdCount} entries.
         */
        void trimTo(int newMsdCount) {
            int k = digits.length;
            if (newMsdCount >= k) return;
            int drop = k - newMsdCount; // entries to drop from LSD end (index 0..drop-1)
            int[] newDigits      = new int[newMsdCount];
            int[] newMaxDigit    = new int[newMsdCount];
            int[] newDecisionPos = new int[newMsdCount];
            System.arraycopy(digits,      drop, newDigits,      0, newMsdCount);
            System.arraycopy(maxDigit,    drop, newMaxDigit,    0, newMsdCount);
            System.arraycopy(decisionPos, drop, newDecisionPos, 0, newMsdCount);
            digits      = newDigits;
            maxDigit    = newMaxDigit;
            decisionPos = newDecisionPos;
        }

        @Override
        public String toString() {
            return "InvolutadicState{n=" + n
                    + ", digits=" + Arrays.toString(digits)
                    + ", maxDigit=" + Arrays.toString(maxDigit)
                    + ", involution=" + Arrays.toString(involution) + '}';
        }
    }
}
