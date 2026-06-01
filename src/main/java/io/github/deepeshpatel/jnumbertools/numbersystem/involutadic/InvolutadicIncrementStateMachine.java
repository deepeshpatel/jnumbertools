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
 * in O(1) via {@link #involution()}.
 *
 * <h2>Increment Algorithm</h2>
 * <ol>
 *   <li><b>Find pivot.</b> In the LSD-first digit array, scan from index 0 upward
 *       to find the smallest index {@code p} where {@code digits[p] < maxDigit[p]}.</li>
 *   <li><b>Suffix increment.</b> If pivot {@code p} exists:
 *     <ol type="a">
 *       <li>Increment {@code digits[p]}.</li>
 *       <li>Reset {@code digits[0..p-1]} to 0.</li>
 *       <li>Rollback and rebuild the involution from the pivot onward.</li>
 *     </ol>
 *   </li>
 *   <li><b>Exhaustion.</b> If no pivot exists, all involutions have been enumerated.</li>
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
public final class InvolutadicIncrementStateMachine {

    private final InvolutadicAlgorithms alg;

    // ── Internal state (never exposed) ───────────────────────────────────────
    private final int n; //universe size.
    private int[]    digits; //Current digit array, LSD-first. Length = number of decisions.
    private int[]    maxDigit; //Maximum digit at each LSD-first position.
    private int[]    decisionPos; //Position in [n] handled by the decision at each LSD-first index
    private boolean[] placed; //Which positions have been placed.
    private int[]    involution; //Live. always consistent with digits

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Constructs a new state machine starting at rank 0.
     *
     * @param n          universe size (n ≥ 1)
     * @param calculator memoising calculator
     */
    public InvolutadicIncrementStateMachine(int n, Calculator calculator) {
        this(n, BigInteger.ZERO, calculator);
    }

    /**
     * Constructs a new state machine starting at the given rank.
     *
     * @param n          universe size (n ≥ 1)
     * @param rank       starting rank (0 ≤ rank < T(n))
     * @param calculator memoising calculator
     */
    public InvolutadicIncrementStateMachine(int n, long rank, Calculator calculator) {
        this(n, BigInteger.valueOf(rank), calculator);
    }

    /**
     * Constructs a new state machine starting at the given rank.
     *
     * @param n          universe size (n ≥ 1)
     * @param rank       starting rank (0 ≤ rank < T(n))
     * @param calculator memoising calculator
     */
    public InvolutadicIncrementStateMachine(int n, BigInteger rank, Calculator calculator) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1, got " + n);
        Objects.requireNonNull(rank, "rank");
        Objects.requireNonNull(calculator, "calculator");

        this.n   = n;
        this.alg = new InvolutadicAlgorithms(calculator);

        int[] initialDigits = alg.toInvolutadic(rank, n);
        initState(initialDigits);
        rebuildAllFromDigits();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns the current involution array.
     *
     * <p><strong>Performance note:</strong> Returns a live reference — do NOT modify.
     * Clone if mutation is required.</p>
     *
     * @return live reference to the current involution of length {@code n}
     */
    public int[] involution() {
        return involution;
    }

    /**
     * Returns a snapshot of the current digit array (LSD-first).
     *
     * @return copy of the digit array
     */
    public int[] getDigits() {
        return Arrays.copyOf(digits, digits.length);
    }

    /**
     * Returns the current number of decisions (= digit array length).
     * This varies with the involution's structure (fixed points vs 2-cycles).
     */
    public int decisionCount() {
        return digits.length;
    }

    /**
     * Advances to the next involution in lexicographic order.
     *
     * @return {@code true} if successfully advanced; {@code false} if all
     *         involutions have been enumerated
     */
    public boolean increment() {
        int k = digits.length;

        // Find pivot: smallest LSD-first index p where digits[p] < maxDigit[p]
        int p = -1;
        for (int i = 0; i < k; i++) {
            if (digits[i] < maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p == -1) return false;

        digits[p]++;
        Arrays.fill(digits, 0, p, 0);

        // Rollback and rebuild from the pivot (MSD-first index = k-1-p)
        rollbackAndRebuild(k - 1 - p);
        return true;
    }

    /**
     * Advances and returns the carry length (number of digit positions modified).
     * Returns 0 if enumeration is complete.
     *
     * @return carry length (&gt; 0) on success; 0 if exhausted
     */
    public int incrementAndGetCarryLength() {
        int k = digits.length;

        int p = -1;
        for (int i = 0; i < k; i++) {
            if (digits[i] < maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p == -1) return 0;

        digits[p]++;
        Arrays.fill(digits, 0, p, 0);
        rollbackAndRebuild(k - 1 - p);
        return p + 1;
    }

    // =========================================================================
    // Internal initialisation
    // =========================================================================

    private void initState(int[] initialDigits) {
        int k = initialDigits.length;
        this.digits      = Arrays.copyOf(initialDigits, k);
        this.maxDigit    = new int[k];
        this.decisionPos = new int[k];
        Arrays.fill(this.decisionPos, -1);
        this.placed      = new boolean[n];
        this.involution  = new int[n];
        for (int i = 0; i < n; i++) this.involution[i] = i;
    }

    // =========================================================================
    // Core rebuild logic
    // =========================================================================

    private void rebuildAllFromDigits() {
        int k = digits.length;
        Arrays.fill(placed, false);
        Arrays.fill(involution, 0);
        for (int i = 0; i < n; i++) involution[i] = i;

        for (int di = k - 1; di >= 0; di--) {
            int digit = digits[di];
            int pos   = InvolutadicAlgorithms.firstUnplaced(placed, n);
            int unplaced = InvolutadicAlgorithms.countUnplaced(placed, n);

            maxDigit[di]    = unplaced - 1;
            decisionPos[di] = pos;

            applyDecision(pos, digit);
        }
    }

    private void rollbackAndRebuild(int pivotMsdIdx) {
        int k = digits.length;

        // Undo from pivotMsdIdx onward (MSD order)
        for (int msdIdx = pivotMsdIdx; msdIdx < k; msdIdx++) {
            int lsdIdx = k - 1 - msdIdx;
            int pos = decisionPos[lsdIdx];
            if (pos < 0) continue;

            int partner = involution[pos];
            placed[pos] = false;
            if (partner != pos) {
                placed[partner] = false;
                involution[partner] = partner;
            }
            involution[pos] = pos;
        }

        // Rebuild from pivotMsdIdx onward
        for (int msdIdx = pivotMsdIdx; msdIdx < k; msdIdx++) {
            int lsdIdx = k - 1 - msdIdx;

            int pos = InvolutadicAlgorithms.firstUnplaced(placed, n);
            if (pos < 0) {
                trimTo(msdIdx);
                return;
            }

            int unplaced = InvolutadicAlgorithms.countUnplaced(placed, n);
            int maxD = unplaced - 1;
            maxDigit[lsdIdx]    = maxD;
            decisionPos[lsdIdx] = pos;

            int digit = digits[lsdIdx];
            if (digit > maxD) digit = 0;
            digits[lsdIdx] = digit;

            applyDecision(pos, digit);
        }

        // If positions remain after k decisions, expand
        if (InvolutadicAlgorithms.firstUnplaced(placed, n) >= 0) {
            expandToComplete();
        }
    }

    private void applyDecision(int pos, int digit) {
        if (digit == 0) {
            involution[pos] = pos;
            placed[pos] = true;
        } else {
            int partner = InvolutadicAlgorithms.findKthPartner(placed, pos, digit - 1, n);
            involution[pos]     = partner;
            involution[partner] = pos;
            placed[pos]         = true;
            placed[partner]     = true;
        }
    }

    private void expandToComplete() {
        int pos;
        while ((pos = InvolutadicAlgorithms.firstUnplaced(placed, n)) >= 0) {
            int unplaced = InvolutadicAlgorithms.countUnplaced(placed, n);
            appendDecision(pos, 0, unplaced - 1);
            involution[pos] = pos;
            placed[pos] = true;
        }
    }

    // =========================================================================
    // Array resize helpers
    // =========================================================================

    private void appendDecision(int pos, int digit, int maxD) {
        int k = digits.length;
        digits      = Arrays.copyOf(digits,      k + 1);
        maxDigit    = Arrays.copyOf(maxDigit,    k + 1);
        decisionPos = Arrays.copyOf(decisionPos, k + 1);
        // Shift right by 1 (new LSD at index 0)
        System.arraycopy(digits,      0, digits,      1, k);
        System.arraycopy(maxDigit,    0, maxDigit,    1, k);
        System.arraycopy(decisionPos, 0, decisionPos, 1, k);
        digits[0]      = digit;
        maxDigit[0]    = maxD;
        decisionPos[0] = pos;
    }

    private void trimTo(int newMsdCount) {
        int k = digits.length;
        if (newMsdCount >= k) return;
        int drop = k - newMsdCount;
        digits      = Arrays.copyOfRange(digits,      drop, k);
        maxDigit    = Arrays.copyOfRange(maxDigit,    drop, k);
        decisionPos = Arrays.copyOfRange(decisionPos, drop, k);
    }
}
