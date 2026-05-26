/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.FenwickTree;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Incremental Derangadic successor engine with encapsulated state.
 *
 * <p>
 * Provides amortised O(1) lexicographic traversal of derangements by maintaining
 * both the encoded digit array and the materialised derangement array in a
 * continuously synchronised state. Each call to {@link #increment()} advances to
 * the next rank without recomputing from scratch.
 * </p>
 *
 * <h2>Digit array convention — LSD at {@code digits[0]}, MSD at {@code digits[actualN-1]}</h2>
 * <p>
 * All digit arrays in this class are stored <strong>LSD-first</strong>:
 * </p>
 * <pre>
 *   digits[0]          = D_0          — Least Significant Digit (LSD)
 *   digits[1]          = D_1
 *   ...
 *   digits[actualN-1]  = D_{actualN-1} — Most Significant Digit (MSD)
 * </pre>
 * <p>
 * This matches {@link DerangadicAlgorithms}: the paper writes digits as
 * {@code (D_{k-1}, …, D_1, D_0)} (MSD on the left), and the in-memory array
 * simply reverses that order. The pivot scan in {@link #findPivot()} starts at
 * index&nbsp;1 and scans upward because {@code digits[0] = D_0} (the LSD) never
 * generates a carry — the carry always originates at a higher-index (more
 * significant) digit.
 * </p>
 * <p>
 * {@link #encoded()} returns the live internal array in this LSD-first order.
 * To display in the MSD-first notation used by the paper, iterate from
 * {@code digits[actualN-1]} down to {@code digits[0]}.
 * </p>
 *
 * <h2>Design intent</h2>
 * <ul>
 *   <li>Encapsulates both the digit array and derangement state in a single object.</li>
 *   <li>Returns the current derangement in O(1) via {@link #derangement()}.</li>
 *   <li>Keeps both representations synchronised incrementally during suffix rebuilds.</li>
 * </ul>
 *
 * <h2>Key structural invariants</h2>
 * <ul>
 *   <li><strong>LSD never carries:</strong> {@code digits[0] = D_0} is always 0;
 *       the pivot search starts at index&nbsp;1 ({@code D_1}).</li>
 *   <li><strong>Dual-state consistency:</strong> encoded digits and the materialised
 *       {@code derangement[]} array are kept synchronised at all times.
 *       {@link #derangement()} is O(1).</li>
 *   <li><strong>Parity expansion:</strong> when all digits are maximal and
 *       {@code actualN < n}, the machine expands {@code actualN += 2} to cross
 *       parity-band boundaries, matching {@code !k} thresholds.</li>
 *   <li><strong>Rollback &amp; rebuild:</strong> suffix updates release elements from the
 *       availability structure, increment the pivot digit, reset lower-significance
 *       digits ({@code D_0 … D_{pivot-1}}) to their minimum values, and greedily
 *       re-fill using order-statistic queries on a Fenwick tree.</li>
 * </ul>
 *
 * <h2>Performance characteristics</h2>
 * <ul>
 *   <li>Expected carry length converges to {@code ≈ e² ≈ 7.389} due to
 *       factorial decay of {@code k / !k}.</li>
 *   <li>Worst-case step: O(actualN log n) with Fenwick tree; amortised O(1) per step.</li>
 *   <li>Thread-unsafe: state mutations are not synchronised.</li>
 * </ul>
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @version 3.0.2
 * @see DerangadicAlgorithms
 * @see Derangadic
 * @since 3.0.2
 */
public final class DerangadicIncrementStateMachine {

    private final Calculator calculator;
    private final DerangadicAlgorithms alg;
    private DerangadicState state;

    /**
     * Constructs a new state machine for the given universe size and starting rank.
     *
     * @param n          universe size ({@code n ≥ 2})
     * @param rank       starting rank ({@code 0 ≤ rank < !n})
     * @param calculator memoising calculator for factorial/subfactorial computations
     * @throws IllegalArgumentException if {@code n < 2} or {@code rank} is out of bounds
     * @throws NullPointerException     if {@code calculator} is {@code null}
     */
    public DerangadicIncrementStateMachine(int n, long rank, Calculator calculator) {
        this(n, BigInteger.valueOf(rank), calculator);
    }

    /**
     * Constructs a new state machine for the given universe size and starting rank.
     *
     * @param n          universe size ({@code n ≥ 2})
     * @param rank       starting rank ({@code 0 ≤ rank < !n})
     * @param calculator memoising calculator for factorial/subfactorial computations
     * @throws IllegalArgumentException if {@code n < 2} or {@code rank} is out of bounds
     * @throws NullPointerException     if {@code rank} or {@code calculator} is {@code null}
     */
    public DerangadicIncrementStateMachine(int n, BigInteger rank, Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.alg = new DerangadicAlgorithms(calculator);
        this.state = initialState(n, rank);
    }

    /**
     * Creates the initial state for the given universe size and rank.
     *
     * @param n    universe size ({@code n ≥ 2})
     * @param rank starting rank
     * @return initialised state ready for iteration
     */
    private DerangadicState initialState(int n, BigInteger rank) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");
        Objects.requireNonNull(rank, "rank");
        int[] digits = alg.toDerangadic(rank, n);
        this.state = new DerangadicState(n, digits.length, digits);
        rebuildAllFromDigits();
        return state;
    }

    /**
     * Returns the current derangement array.
     *
     * <p><strong>Performance note:</strong> Returns a live reference to the internal array
     * for zero-copy performance. Callers must NOT modify the returned array.
     * Clone it explicitly if mutation is required.</p>
     *
     * @return live reference to the current derangement array of length {@code n}
     */
    public int[] derangement() {
        return state.derangement;
    }

    /**
     * Returns the current encoded digit array in <strong>LSD-first</strong> order:
     * {@code encoded()[0] = D_0} (Least Significant Digit) and
     * {@code encoded()[actualN-1] = D_{actualN-1}} (Most Significant Digit).
     *
     * <p>
     * To display in the MSD-first notation used by the paper, iterate from
     * {@code encoded()[actualN-1]} down to {@code encoded()[0]}.
     * </p>
     *
     * <p><strong>Performance note:</strong> Returns a live reference to the internal array
     * for zero-copy performance. Callers must NOT modify the returned array.
     * Clone it explicitly if mutation is required.</p>
     *
     * @return live reference to the digit array in LSD-first order ({@code result[0] = D_0})
     */
    public int[] encoded() {
        return state.digits;
    }

    /**
     * Advances the state to the next derangement in lexicographic order.
     *
     * <p>After a successful call, {@link #derangement()} returns the next derangement
     * and {@link #encoded()} returns its LSD-first digit array.</p>
     *
     * @return {@code true} if successfully advanced; {@code false} if already at the last rank
     */
    public boolean increment() {
        int carry = incrementAndGetCarryLength();
        return carry > 0;
    }

    public int actualN() {
        return state.actualN;
    }


    /**
     * Advances the state and returns the carry length (number of digit positions modified).
     *
     * <p>
     * The carry length is the index of the pivot digit plus one, i.e. the number of
     * digit positions (counting from the LSD end, {@code D_0} upward) that were
     * updated. For a parity-band expansion, the carry length equals the new
     * {@code actualN}. Returns 0 if enumeration is complete.
     * </p>
     *
     * <p>Useful for performance analysis and debugging.</p>
     *
     * @return carry length ({@code > 0}) if the advance was successful; {@code 0} if
     *         all derangements have been enumerated
     */
    public int incrementAndGetCarryLength() {
        int actualN = state.actualN;

        // Scan from index 1 upward (digits[0] = D_0 = LSD never carries)
        int p = findPivot();

        if (p != -1) {
            state.digits[p]++;
            rollbackAndRebuild(actualN - 1 - p);
            return p + 1; // Carry length = pivot index + 1
        }

        // All digits maximal: cross parity-band boundary
        if (actualN < state.n) {
            int newActualN = actualN + 2;
            BigInteger firstRank = calculator.subFactorial(actualN);
            int[] firstDigits = alg.toDerangadic(firstRank, state.n);
            state.resizeActualN(newActualN, firstDigits);
            rebuildAllFromDigits();
            return newActualN; // Expansion counts as full-length carry
        }

        return 0; // Enumeration complete
    }

    // =========================================================================
    // Core rebuild and rollback methods (private)
    // =========================================================================

    /**
     * Rebuilds the entire state (encoded digits and derangement) from {@code digits[]}
     * from scratch. O(n + actualN × log n). Called only at:
     * <ul>
     *   <li>initial state construction, and</li>
     *   <li>parity-band expansion (rare).</li>
     * </ul>
     * <p>
     * The prefix {@code [0, offset)} of the derangement (positions not covered by
     * the active digit window) is filled with the greedy minimum derangement.
     * The suffix {@code [offset, n)} is filled by consuming digits
     * {@code digits[actualN-1]} (MSD = {@code D_{actualN-1}}) down to
     * {@code digits[0]} (LSD = {@code D_0}).
     * </p>
     */
    private void rebuildAllFromDigits() {
        int n = state.n;
        int actualN = state.actualN;
        int offset = n - actualN;

        // Reset encoded state
        boolean[] eUsed = state.eUsed;
        Arrays.fill(eUsed, 0, actualN, false);

        // Reset derangement state
        boolean[] usedFull = state.usedFull;
        Arrays.fill(usedFull, false);
        FenwickTree avail = state.availTree;
        for (int i = 1; i <= n; i++) {
            int v = avail.get(i);
            if (v != 1) avail.update(i, 1 - v);
        }

        int[] derangement = state.derangement;

        // Fill prefix [0, offset) with the greedy minimum derangement.
        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && usedFull[nextCandidate]) nextCandidate++;
            int chosen = nextCandidate;
            if (chosen == pos) { // would be fixed point
                int temp = nextCandidate + 1;
                while (temp < n && usedFull[temp]) temp++;
                chosen = temp;
            }
            derangement[pos] = chosen;
            usedFull[chosen] = true;
            avail.update(chosen + 1, -1);
            if (chosen == nextCandidate) nextCandidate++;
        }

        // Fill suffix [offset, n) from the digit array.
        // Step k reads digits[actualN-1-k]: step 0 → MSD (D_{actualN-1}),
        // step actualN-1 → LSD (D_0).

        for (int step = 0; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(step);
            consumeAtStep(step, state.digits[di], avail, offset);
        }
    }


    /**
     * Releases all digit positions from {@code changedStep} onward (i.e. the suffix of
     * the active window starting at the changed MSD-first step), then re-consumes
     * them: the pivot position uses its already-incremented digit and all subsequent
     * positions use their greedy-minimum digit.
     *
     * <p>
     * "Releasing" a step means marking the corresponding relative element unused in
     * {@code eUsed} and restoring it to the Fenwick tree. "Re-consuming" rebuilds both
     * the encoded and absolute state atomically via {@link #consumeAtStep}.
     * </p>
     *
     * @param changedStep the first MSD-first step index (0-based within the active
     *                    window) where a digit change occurred; corresponds to the
     *                    pivot at array index {@code actualN-1-changedStep}
     */
    private void rollbackAndRebuild(int changedStep) {
        int actualN = state.actualN;
        int offset = state.n - actualN;
        FenwickTree avail = state.availTree;

        // ----- Release suffix -----
        for (int step = changedStep; step < actualN; step++) {
            state.eUsed[state.consumedAtStep[step]] = false;       // encoded
            int abs = state.derangement[offset + step];
            state.usedFull[abs] = false;                           // absolute
            avail.update(abs + 1, 1);                              // Fenwick
        }

        // ----- Rebuild suffix -----
        for (int step = changedStep; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(step);

            int digit;
            if (step == changedStep) {
                digit = state.digits[di]; // already incremented by caller
            } else {
                digit = computeMinDigit(step);
                state.digits[di] = digit;
            }

            consumeAtStep(step, digit, avail, offset);
        }
    }

    /**
     * Selects the element for MSD-first step {@code step} using relative digit
     * {@code digit}, and updates both the encoded state and the derangement state
     * atomically.
     *
     * <p>
     * The relative digit indexes into the available relative candidates
     * (elements {@code 0…actualN-1} that are neither already used nor equal to
     * {@code step} itself, which would create a fixed point in the relative
     * encoding). Dead-end avoidance skips candidates that would force the last
     * remaining element to map to its own position.
     * The absolute element is derived from the Fenwick tree, which tracks
     * availability over the full {@code n}-element universe.
     * </p>
     *
     * @param step   current MSD-first step index (0-based within the active window),
     *               where step 0 processes the MSD ({@code D_{actualN-1}}) and
     *               step {@code actualN-1} processes the LSD ({@code D_0})
     * @param digit  target digit value (relative rank of the chosen element among legal candidates)
     * @param avail  Fenwick tree tracking available absolute elements (1-based indexing)
     * @param offset global offset {@code n - actualN}; maps step index to absolute position
     */
    private void consumeAtStep(int step, int digit, FenwickTree avail, int offset) {
        int actualN = state.actualN;

        // ---- Encoded (relative) selection with dead-end avoidance ----
        int seenRel = 0, chosenRel = -1;
        int remainingSize = actualN - step;
        for (int c = 0; c < actualN; c++) {
            if (state.eUsed[c] || c == step) continue;
            if (isDeadEndCandidate(step, c, remainingSize)) {
                seenRel++;
                continue;
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
    // Digit constraint helpers
    // =========================================================================

    /**
     * Computes the maximum valid digit at MSD-first step {@code step}.
     *
     * <p>
     * The number of legal candidates at a given step is the number of unused relative
     * elements minus the one that would create a fixed point (if position {@code step}
     * itself is still available as an element).
     * </p>
     *
     * @param step current MSD-first step index (0-based within the active window)
     * @return maximum valid digit value ({@code ≥ 0})
     */
    private int computeMaxDigit(int step) {
        int unused = state.actualN - step;
        // If position 'step' is still available as an element, it cannot be chosen
        // (would create a fixed point in the relative encoding).
        int legalCount = state.eUsed[step] ? unused : unused - 1;
        return legalCount > 0 ? legalCount - 1 : 0;
    }

    /**
     * Computes the minimum valid digit at MSD-first step {@code step}.
     *
     * <p>
     * Normally this is {@code 0}. However, dead-end avoidance may force the minimum
     * to {@code 1} when exactly two positions remain and the first available candidate
     * would force the final element to map to its own position (a fixed point).
     * </p>
     *
     * @param step current MSD-first step index (0-based within the active window)
     * @return minimum valid digit value (usually {@code 0}, occasionally {@code 1})
     */
    private int computeMinDigit(int step) {
        int remainingSize = state.actualN - step;
        int seen = 0;
        for (int c = 0; c < state.actualN; c++) {
            if (state.eUsed[c] || c == step) continue;
            if (isDeadEndCandidate(step, c, remainingSize)) {
                seen++;
                continue;
            }
            return seen;
        }
        return 0;
    }

    /**
     * Returns {@code true} if choosing candidate {@code c} at MSD-first step
     * {@code step} would leave the final remaining element with no valid placement
     * (i.e. the only remaining position would be its own index, creating a fixed point).
     *
     * <p>This check applies only when exactly {@code 2} positions remain
     * ({@code remainingSize == 2}).</p>
     *
     * @param step          current MSD-first step index
     * @param c             candidate element being evaluated
     * @param remainingSize number of positions not yet assigned in the active window
     * @return {@code true} if choosing {@code c} leads to an unavoidable dead end
     */
    private boolean isDeadEndCandidate(int step, int c, int remainingSize) {
        if (remainingSize != 2) {
            return false;
        }
        int otherElem = -1;
        for (int x = 0; x < state.actualN; x++) {
            if (!state.eUsed[x] && x != c) {
                otherElem = x;
                break;
            }
        }
        return otherElem == step + 1;
    }

    /**
     * Finds the pivot: the lowest-index digit (highest-significance position) that
     * can still be incremented (i.e. {@code digits[i] < maxDigit[i]}).
     *
     * <p>
     * The scan starts at index&nbsp;1 because {@code digits[0] = D_0} (the LSD) is
     * always&nbsp;0 and never carries — it is always reset to its minimum value as
     * part of a suffix rebuild triggered by a carry at a higher position.
     * </p>
     *
     * @return the index of the pivot digit, or {@code -1} if no pivot exists
     *         (all digits are at their maxima, requiring a parity-band expansion)
     */
    private int findPivot() {
        int actualN = state.actualN;
        for (int i = 1; i < actualN; i++) { // start from 1: digits[0] = D_0 (LSD) never carries
            if (state.digits[i] < state.maxDigit[i]) {
                return i;
            }
        }
        return -1;
    }

    // =========================================================================
    // Inner state class
    // =========================================================================

    /**
     * Mutable state for one incremental Derangadic walk.
     *
     * <h3>Field inventory</h3>
     * <pre>
     * ── Encoded state (relative, active window of size actualN) ──────────────
     *   digits[]          Digit array, LSD-first: digits[0] = D_0 (LSD),
     *                     digits[actualN-1] = D_{actualN-1} (MSD).
     *   maxDigit[]        Maximum valid digit at each index (same indexing as digits[]).
     *   consumedAtStep[]  Relative element chosen at each MSD-first step.
     *   eUsed[]           Which relative elements (0…actualN-1) are consumed.
     *   actualN           Carrier length (active digit window size).
     *
     * ── Derangement state (absolute, full universe of size n) ─────────────────
     *   derangement[]     Full permutation — always current after any public call.
     *   usedFull[]        Which absolute elements (0…n-1) are used.
     *   availTree         Fenwick tree over [1, n] — always current.
     *
     * ── Invariant ─────────────────────────────────────────────────────────────
     *   After any call to increment(), BOTH groups are consistent and
     *   derangement[] is valid without further work.
     * </pre>
     *
     * <h3>Digit ordering reminder</h3>
     * <p>
     * {@code digits[]} is LSD-first: {@code digits[0] = D_0} (Least Significant
     * Digit), {@code digits[actualN-1] = D_{actualN-1}} (Most Significant Digit).
     * To display in MSD-first paper notation, iterate from {@code digits[actualN-1]}
     * down to {@code digits[0]}.
     * </p>
     */
    public static final class DerangadicState {

        // encoded state
        int[] digits;
        int[] maxDigit;
        int[] consumedAtStep;
        boolean[] eUsed;
        int actualN;

        // derangement state
        final int[] derangement;
        final boolean[] usedFull;
        final FenwickTree availTree;

        // immutable
        final int n;

        /**
         * Constructs a new state object.
         *
         * @param n        universe size
         * @param actualN  active encoding length (carrier length)
         * @param digits   initial digit array, LSD-first:
         *                 {@code digits[0] = D_0}, {@code digits[actualN-1] = D_{actualN-1}}
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
         * Resizes the active encoding length and replaces the digit arrays.
         *
         * <p>Called when crossing a parity-band boundary ({@code actualN += 2}).
         * The {@code derangement[]}, {@code usedFull[]}, and {@code availTree}
         * fields remain at their original size {@code n} and are rebuilt separately
         * by {@link DerangadicIncrementStateMachine#rebuildAllFromDigits()}.</p>
         *
         * @param newActualN new active carrier length (must have the same parity as
         *                   the original {@code n} and satisfy {@code newActualN ≤ n})
         * @param newDigits  new digit array of length {@code newActualN}, LSD-first:
         *                   {@code newDigits[0] = D_0},
         *                   {@code newDigits[newActualN-1] = D_{newActualN-1}}
         */
        void resizeActualN(int newActualN, int[] newDigits) {
            this.actualN = newActualN;
            this.digits = newDigits;
            this.maxDigit = new int[newActualN];
            this.consumedAtStep = new int[newActualN];
            this.eUsed = new boolean[newActualN];
            // derangement[], usedFull[], availTree stay at size n
        }

        @Override
        public String toString() {
            return "DerangadicState{digits=" + Arrays.toString(digits)
                    + ", max=" + Arrays.toString(maxDigit)
                    + ", actualN=" + actualN + '}';
        }
    }
}
