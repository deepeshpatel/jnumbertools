/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.FenwickTree;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Incremental Derangadic successor engine.
 * <p>
 * Provides amortized O(1) lexicographic traversal of derangements by maintaining
 * both encoded digits and materialized derangement array in a synchronized state.
 * Each call to {@link #increment(DerangadicState)} advances to the next rank
 * without recomputing from scratch.
 * <p>
 * <b>Design Intent:</b>
 * <ul>
 *   <li>Advance the digit array to the next rank via {@link #incrementEncoded(DerangadicState)}.</li>
 *   <li>Return the current derangement in O(1) via {@link #encodedToDerangement(DerangadicState)}.</li>
 *   <li>Keep both representations synchronized incrementally during suffix rebuilds.</li>
 * </ul>
 * <p>
 * <b>Key Structural Invariants:</b>
 * <ul>
 *   <li><b>LSD Never Carries:</b> {@code digits[0]} is always 0; pivot search starts at index 1.</li>
 *   <li><b>Dual-State Consistency:</b> Encoded digits and materialized {@code derangement[]} are
 *       kept synchronized at all times. {@code encodedToDerangement()} is O(1).</li>
 *   <li><b>Parity Expansion:</b> When all digits are maximal and {@code actualN < n}, the machine
 *       expands {@code actualN += 2} to cross parity-band boundaries, matching {@code !k} thresholds.</li>
 *   <li><b>Rollback & Rebuild:</b> Suffix updates are handled by returning elements to the availability
 *       structure, incrementing the pivot digit, resetting lower digits to {@code minDigit}, and
 *       greedily re-filling using order-statistic queries.</li>
 * </ul>
 * <p>
 * <b>Performance Characteristics:</b>
 * <ul>
 *   <li>Expected carry length converges to {@code ≈ e² ≈ 7.389} due to super-exponential decay of {@code k/!k}.</li>
 *   <li>Worst-case step: O(actualN log n) with Fenwick tree, but amortized O(1) per step.</li>
 *   <li>Thread-unsafe: state mutations are not synchronized.</li>
 * </ul>
 * <p>
 * <b>Digit Array Convention:</b>
 * <ul>
 *   <li>Arrays are stored <strong>LSD-first</strong>: {@code digits[0]} = {@code D_0} (least significant),
 *       {@code digits[k-1]} = {@code D_{k-1}} (most significant).</li>
 *   <li>For display/paper notation, digits are shown <strong>MSD-first</strong>: {@code [D_{k-1}, ..., D_0]}.</li>
 * </ul>
 *
 * @author Deepesh Patel & Aditya Patel
 * @version 3.0.2
 * @see DerangadicAlgorithms
 * @see Derangadic
 * @since 3.0.2
 */
public final class DerangadicIncrement {

    private final Calculator calculator;
    private final DerangadicAlgorithms alg;

    /**
     * Constructs a new instance with the given calculator.
     *
     * @param calculator memoizing calculator for factorial/subfactorial computations
     * @throws NullPointerException if {@code calculator} is {@code null}
     */
    public DerangadicIncrement(Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.alg = new DerangadicAlgorithms(calculator);
    }

    /**
     * Constructs a new instance with a default calculator.
     * <p>
     * <b>Note:</b> For repeated calls, prefer reusing a single {@link Calculator} instance
     * externally to benefit from memoization of factorials and subfactorials.
     */
    public DerangadicIncrement() {
        this(new Calculator());
    }

    // =========================================================================
    // Factory
    // =========================================================================

    /**
     * Creates state initialised at rank 0 for the given universe size.
     *
     * @param n universe size ({@code n ≥ 2})
     * @return initialised {@link DerangadicState} ready for iteration
     * @throws IllegalArgumentException if {@code n < 2}
     * @see #initialState(int, BigInteger)
     */
    DerangadicState initialState(int n) {
        return initialState(n, BigInteger.ZERO);
    }

    /**
     * Creates state initialised at the given rank for the given universe size.
     * <p>
     * Both digits and the derangement array are ready immediately on return.
     *
     * @param n    universe size ({@code n ≥ 2})
     * @param rank starting rank ({@code 0 ≤ rank < !n})
     * @return initialised {@link DerangadicState} ready for iteration
     * @throws IllegalArgumentException if {@code n < 2} or {@code rank} out of bounds
     * @throws NullPointerException     if {@code rank} is {@code null}
     */
    DerangadicState initialState(int n, BigInteger rank) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");
        Objects.requireNonNull(rank, "rank");

        int[] digits = alg.toDerangadic(rank, n);
        DerangadicState state = new DerangadicState(n, digits.length, digits);
        rebuildAllFromDigits(state);
        return state;
    }

    // =========================================================================
    // Three-level public API
    // =========================================================================

    /**
     * Advances the digit array to the next rank.
     * <p>
     * As a zero-marginal-cost side effect of the incremental suffix rebuild,
     * {@code derangement[]} is also kept current. Calling
     * {@link #encodedToDerangement(DerangadicState)} afterwards is therefore O(1).
     *
     * @param state mutable state object to advance
     * @return {@code true} if advanced; {@code false} if already at the last rank
     * @throws NullPointerException if {@code state} is {@code null}
     * @see #increment(DerangadicState)
     */
    public boolean incrementEncoded(DerangadicState state) {
        return doIncrement(state);
    }

    /**
     * Returns the derangement array for the current encoded state.
     * <p>
     * This is an O(1) operation: because {@link #incrementEncoded} keeps
     * {@code derangement[]} current during its incremental suffix rebuild,
     * no additional work is needed here.
     *
     * @param state current state object
     * @return live reference to {@code state.derangement} — clone if stability needed
     * @throws NullPointerException if {@code state} is {@code null}
     */
    public int[] encodedToDerangement(DerangadicState state) {
        return state.derangement; // always current; see class-level Javadoc
    }

    /**
     * Advances the state and returns {@code true} if successful.
     * <p>
     * Equivalent to {@link #incrementEncoded(DerangadicState)} — the derangement
     * is available via {@link DerangadicState#currentDerangement()} immediately.
     *
     * @param state mutable state object to advance
     * @return {@code true} if advanced; {@code false} if exhausted
     * @throws NullPointerException if {@code state} is {@code null}
     * @see #incrementEncoded(DerangadicState)
     */
    public boolean increment(DerangadicState state) {
        return doIncrement(state);
    }

    // =========================================================================
    // Single engine — shared by all three public entry points
    // =========================================================================

    /**
     * The hot path. Finds the leftmost digit that has headroom, increments it,
     * then calls {@link #rollbackAndRebuild} to restore consistency for the
     * suffix. On a parity-band boundary, expands the carrier length and does a
     * full rebuild (rare — O(n log n), but happens at most O(n/2) times total).
     *
     * @param state mutable state object to advance
     * @return {@code true} if advanced; {@code false} if exhausted
     * @implNote Pivot search starts at index 1 (LSD at index 0 never carries).
     */
    private boolean doIncrement(DerangadicState state) {
        int actualN = state.actualN;

        // Scan from index 1 (index 0 = LSD, always forced to 0 by Derangadic invariant).
        int p = -1;
        for (int i = 1; i < actualN; i++) {
            if (state.digits[i] < state.maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p != -1) {
            state.digits[p]++;
            rollbackAndRebuild(state, actualN - 1 - p);
            return true;
        }

        // All digits at maximum — cross the parity-band boundary.
        if (actualN >= state.n) return false;

        int newActualN = actualN + 2;
        BigInteger firstRank = calculator.subFactorial(actualN);
        int[] firstDigits = alg.toDerangadic(firstRank, state.n);
        state.resizeActualN(newActualN, firstDigits);
        rebuildAllFromDigits(state);
        return true;
    }

    // =========================================================================
    // Full rebuild — initialisation and parity-band expansion only
    // =========================================================================

    /**
     * Rebuilds the entire state (encoded + derangement) from {@code digits[]}
     * from scratch. O(n + actualN × log n). Called only at:
     * <ul>
     *   <li>initial state construction, and</li>
     *   <li>parity-band expansion (rare).</li>
     * </ul>
     *
     * @param state state object to rebuild
     * @implNote Greedy prefix positions never change during normal increments.
     */
    private void rebuildAllFromDigits(DerangadicState state) {
        int n = state.n;
        int actualN = state.actualN;
        int offset = n - actualN;

        // Reset encoded state
        Arrays.fill(state.eUsed, 0, actualN, false);

        // Reset derangement state
        Arrays.fill(state.usedFull, false);
        FenwickTree avail = state.availTree;
        for (int i = 1; i <= n; i++) {
            int v = avail.get(i);
            if (v != 1) avail.update(i, 1 - v);
        }

        // Fill prefix [0, offset) with the greedy minimum derangement.
        // These positions always receive digit 0 and never change during
        // normal increments — their values are constant across all ranks
        // in the current parity band.
        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && state.usedFull[nextCandidate]) nextCandidate++;
            int chosen = nextCandidate;
            if (chosen == pos) { // would be fixed point
                int temp = nextCandidate + 1;
                while (temp < n && state.usedFull[temp]) temp++;
                chosen = temp;
            }
            state.derangement[pos] = chosen;
            state.usedFull[chosen] = true;
            avail.update(chosen + 1, -1);
            if (chosen == nextCandidate) nextCandidate++;
        }

        // Fill suffix [offset, n) from the digit array.
        for (int step = 0; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(state, step);
            consumeAtStep(state, step, state.digits[di], avail, offset);
        }
    }

    // =========================================================================
    // Incremental suffix rollback — the hot path, O(suffix × log n)
    // =========================================================================

    /**
     * Releases positions {@code [changedStep, actualN)} from both the encoded
     * state and the Fenwick tree, then re-consumes them using the updated digit
     * at {@code changedStep} and greedy-minimum digits for later steps.
     * <p>
     * Both encoded state ({@code eUsed[]}, {@code consumedAtStep[]},
     * {@code digits[]}, {@code maxDigit[]}) and derangement state
     * ({@code usedFull[]}, {@code availTree}, {@code derangement[]}) are
     * updated in the same single pass — no separate second pass is needed.
     *
     * @param state        mutable state object
     * @param changedStep  MSD-first position where change begins
     * @implNote {@code changedStep} is in MSD-first indexing; suffix is {@code [changedStep, actualN)}.
     */
    private void rollbackAndRebuild(DerangadicState state, int changedStep) {
        int actualN = state.actualN;
        int offset = state.n - actualN;
        FenwickTree avail = state.availTree;

        // ----- Release suffix ----
        for (int step = changedStep; step < actualN; step++) {
            state.eUsed[state.consumedAtStep[step]] = false; // encoded
            int abs = state.derangement[offset + step];
            state.usedFull[abs] = false; // absolute
            avail.update(abs + 1, 1); // Fenwick
        }

        // ----- Rebuild suffix ----
        for (int step = changedStep; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(state, step);

            int digit;
            if (step == changedStep) {
                digit = state.digits[di]; // already incremented
            } else {
                digit = computeMinDigit(state, step);
                state.digits[di] = digit;
            }
            consumeAtStep(state, step, digit, avail, offset);
        }
    }

    // =========================================================================
    // Per-step consume — single method that keeps both encoded and absolute
    // state in sync
    // =========================================================================

    /**
     * Selects the element for step {@code step} using relative digit {@code digit},
     * and updates encoded state ({@code eUsed[]}, {@code consumedAtStep[]}) and
     * derangement state ({@code derangement[]}, {@code usedFull[]}, Fenwick tree)
     * atomically.
     * <p>
     * The relative digit indexes into the available relative candidates
     * (0..actualN-1 minus already-used and minus step itself). The absolute
     * element is derived from the Fenwick tree, skipping position {@code pos}
     * when it is still available so as not to create a fixed point in the full
     * n-element permutation.
     *
     * @param state   mutable state object
     * @param step    current step index (0-based, MSD-first within active window)
     * @param digit   target digit value for this step
     * @param avail   Fenwick tree tracking available elements (1-based indexing)
     * @param offset  global offset = {@code n - actualN}
     * @implNote Dead-end avoidance is applied when {@code remainingSize == 2}.
     */
    private static void consumeAtStep(DerangadicState state, int step, int digit,
                                      FenwickTree avail, int offset) {
        int actualN = state.actualN;

        // ---- Encoded (relative) selection with dead-end avoidance ----
        int seenRel = 0, chosenRel = -1;
        int remainingSize = actualN - step;

        for (int c = 0; c < actualN; c++) {
            if (state.eUsed[c] || c == step) continue;

            // Dead-end avoidance: when remaining == 2, skip candidate that forces fixed point
            if (remainingSize == 2) {
                int otherElem = -1;
                for (int x = 0; x < actualN; x++) {
                    if (!state.eUsed[x] && x != c) {
                        otherElem = x;
                        break;
                    }
                }
                if (otherElem == step + 1) {
                    seenRel++; // Skip this candidate but count it
                    continue;
                }
            }

            if (seenRel == digit) {
                chosenRel = c;
                break;
            }
            seenRel++;
        }

        if (chosenRel == -1) {
            throw new IllegalStateException("No valid candidate found at step " + step + " with digit " + digit);
        }

        state.eUsed[chosenRel] = true;
        state.consumedAtStep[step] = chosenRel;

        // ---- Absolute selection via Fenwick tree ----
        int pos = offset + step;
        boolean posAvail = (avail.rsq(pos + 1) - avail.rsq(pos)) == 1;
        int chosenAbs;
        if (posAvail) {
            int posRank = avail.rsq(pos);
            chosenAbs = (digit < posRank)
                    ? avail.findKth(digit + 1)
                    : avail.findKth(digit + 2);
        } else {
            chosenAbs = avail.findKth(digit + 1);
        }
        state.derangement[pos] = chosenAbs - 1;
        state.usedFull[chosenAbs - 1] = true;
        avail.update(chosenAbs, -1);
    }

    // =========================================================================
    // Digit constraint helpers (pure functions on encoded state)
    // =========================================================================

    /**
     * Computes the maximum valid digit at the given step.
     * <p>
     * If the current position is still available as an element, it cannot be chosen
     * (would create a fixed point), reducing the legal candidate count by 1.
     *
     * @param state current encoded state
     * @param step  current step index (0-based, MSD-first within active window)
     * @return maximum valid digit value for this step
     * @implNote Returns 0 if no legal candidates remain.
     */
    private static int computeMaxDigit(DerangadicState state, int step) {
        int unused = state.actualN - step;
        // If position 'step' is still available as an element, it cannot be chosen (would be fixed point)
        int legalCount = state.eUsed[step] ? unused : unused - 1;
        return legalCount > 0 ? legalCount - 1 : 0;
    }

    /**
     * Computes the minimum valid digit at the given step.
     * <p>
     * Accounts for dead-end avoidance: when exactly two positions remain,
     * candidates that would force the final element to map to its own index
     * are skipped, potentially shifting the minimum digit by +1.
     *
     * @param state current encoded state
     * @param step  current step index (0-based, MSD-first within active window)
     * @return minimum valid digit value for this step (usually 0)
     * @implNote Returns 0 if no candidates are valid (should not occur for valid states).
     */
    private static int computeMinDigit(DerangadicState state, int step) {
        int remainingSize = state.actualN - step;
        int seen = 0;
        for (int c = 0; c < state.actualN; c++) {
            if (state.eUsed[c] || c == step) continue;

            // Dead-end avoidance: when remaining == 2, skip candidate that forces fixed point
            boolean deadEnd = false;
            if (remainingSize == 2) {
                int otherElem = -1;
                for (int x = 0; x < state.actualN; x++) {
                    if (!state.eUsed[x] && x != c) {
                        otherElem = x;
                        break;
                    }
                }
                deadEnd = (otherElem == step + 1);
            }
            if (!deadEnd) return seen;
            seen++;
        }
        return 0;
    }

    /**
     * Advances the state and returns the carry length (number of digits modified).
     * @return carry length (>0) if successful, 0 if enumeration is complete.
     */
    public int incrementAndGetCarryLength(DerangadicState state) {
        int actualN = state.actualN;

        // Scan from index 1 (LSD at index 0 never carries)
        int p = -1;
        for (int i = 1; i < actualN; i++) {
            if (state.digits[i] < state.maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p != -1) {
            state.digits[p]++;
            rollbackAndRebuild(state, actualN - 1 - p);
            return p + 1; // Carry length = pivot index + 1
        }

        // All digits maximal: cross parity-band boundary
        if (actualN < state.n) {
            int newActualN = actualN + 2;
            BigInteger firstRank = calculator.subFactorial(actualN);
            int[] firstDigits = alg.toDerangadic(firstRank, state.n);
            state.resizeActualN(newActualN, firstDigits);
            rebuildAllFromDigits(state);
            return newActualN; // Expansion counts as full-length carry
        }

        return 0; // Enumeration complete
    }

    // =========================================================================
    // Inner state class — package-private
    // =========================================================================

    /**
     * Mutable state for one incremental Derangadic walk.
     * <p>
     * <b>Field Inventory:</b>
     * <pre>
     * ── Encoded state (relative, size actualN) ───────────────────────────────
     *   digits[]          LSD at index 0.
     *   maxDigit[]        Maximum valid digit at each index.
     *   consumedAtStep[]  Relative element chosen at each step.
     *   eUsed[]           Which relative elements are consumed.
     *   actualN           Carrier length.
     *
     * ── Derangement state (absolute, size n) ─────────────────────────────────
     *   derangement[]     Full permutation — always current after any public call.
     *   usedFull[]        Which absolute elements are used.
     *   availTree         Fenwick tree over [1, n] — always current.
     *
     * ── Invariant ────────────────────────────────────────────────────────────
     *   After any call to incrementEncoded(), increment(), or initialState(),
     *   BOTH groups are consistent and derangement[] is valid without further work.
     * </pre>
     * <p>
     * <b>Digit Ordering:</b> {@code digits[]} is LSD-first: {@code digits[0]} = {@code D_0}.
     */
    static final class DerangadicState {

        // encoded state
        int[] digits;
        int[] maxDigit;
        int[] consumedAtStep;
        boolean[] eUsed;
        int actualN;

        // derangement state
        int[] derangement;
        boolean[] usedFull;
        FenwickTree availTree;

        // immutable
        final int n;

        /**
         * Constructs a new state object.
         *
         * @param n        universe size
         * @param actualN  active encoding length
         * @param digits   initial digit array (LSD-first)
         */
        DerangadicState(int n, int actualN, int[] digits) {
            this.n = n;
            this.actualN = actualN;
            this.digits = digits;
            this.maxDigit = new int[actualN];
            this.consumedAtStep = new int[actualN];
            this.eUsed = new boolean[actualN];
            this.derangement = new int[n];
            this.usedFull = new boolean[n];
            this.availTree = new FenwickTree(n);
        }

        /**
         * Resizes the active encoding length and updates digit arrays.
         * <p>
         * Called when crossing a parity-band boundary ({@code actualN += 2}).
         *
         * @param newActualN new active length (must have same parity as original)
         * @param newDigits  new digit array for the expanded state
         */
        void resizeActualN(int newActualN, int[] newDigits) {
            this.actualN = newActualN;
            this.digits = newDigits;
            this.maxDigit = new int[newActualN];
            this.consumedAtStep = new int[newActualN];
            this.eUsed = new boolean[newActualN];
            // derangement[], usedFull[], availTree stay at size n
        }

        /**
         * Returns a defensive copy of the current digit array.
         *
         * @return digit array in LSD-first order
         */
        int[] getDigits() { return digits.clone(); }

        /**
         * Returns the current derangement array.
         * <p>
         * <b>Note:</b> Returns live reference; clone if immutability is required.
         *
         * @return derangement array of length {@code n}
         */
        int[] currentDerangement() { return derangement; }

        /**
         * Returns the current active encoding length.
         *
         * @return {@code actualN}
         */
        int getActualN() { return actualN; }

        @Override
        public String toString() {
            return "DerangadicState{digits=" + Arrays.toString(digits)
                    + ", max=" + Arrays.toString(maxDigit)
                    + ", actualN=" + actualN + '}';
        }
    }
}