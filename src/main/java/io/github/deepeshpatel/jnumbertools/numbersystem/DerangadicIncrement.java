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
 *
 * <h2>Design intent</h2>
 * <p>
 * Produces the <em>next</em> Derangadic digit array without re-encoding
 * {@code rank + 1} from scratch via {@link DerangadicAlgorithms#toDerangadic}.
 * </p>
 *
 * <h2>Three-level public API</h2>
 * <pre>
 *   incrementEncoded(state)     // advance digits[] — derangement[] also kept current
 *   encodedToDerangement(state) // return derangement[] — O(1), always already current
 *   increment(state)            // identical to incrementEncoded; kept for readability
 * </pre>
 *
 * <h2>Performance invariant — why the Fenwick tree is always live</h2>
 * <p>
 * All three paths above maintain the Fenwick tree ({@code availTree}), the
 * {@code usedFull[]} bitmap, and {@code derangement[]} incrementally during the
 * suffix rollback-and-rebuild.  This is O(suffix × log n) per step, amortised
 * O(log n) per step on average (because suffix ≈ 1 for most steps).
 * </p>
 * <p>
 * <b>Why not rebuild from scratch in {@code encodedToDerangement}?</b><br>
 * A from-scratch rebuild costs O(n) just to reset the Fenwick tree, plus
 * O(offset × log n) to replay the prefix.  For n = 50 000 with the starting
 * rank near the middle, that is ≈ 50 000 operations on every single call —
 * making it slower than brute force.  Keeping the tree live during the suffix
 * rebuild costs zero extra work, because we were already touching those same
 * tree nodes as part of the rollback.
 * </p>
 *
 * <h2>State encapsulation</h2>
 * <p>
 * {@link DerangadicState} is package-private so {@link Derangadic} can hold a
 * reference without it becoming part of the public API contract.
 * </p>
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @since 3.0.2
 */
public final class DerangadicIncrement {

    private final Calculator calculator;
    private final DerangadicAlgorithms alg;

    public DerangadicIncrement() {
        this(new Calculator());
    }

    public DerangadicIncrement(Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.alg = new DerangadicAlgorithms(calculator);
    }

    // =========================================================================
    // Factory
    // =========================================================================

    /** Creates state initialised at rank 0. */
    DerangadicState initialState(int n) {
        return initialState(n, BigInteger.ZERO);
    }

    /**
     * Creates state initialised at the given rank.
     * Both digits and the derangement array are ready immediately on return.
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
     *
     * <p>As a zero-marginal-cost side effect of the incremental suffix rebuild,
     * {@code derangement[]} is also kept current.  Calling
     * {@link #encodedToDerangement(DerangadicState)} afterwards is therefore O(1).</p>
     *
     * @return {@code true} if advanced; {@code false} if already at the last rank
     */
    public boolean incrementEncoded(DerangadicState state) {
        return doIncrement(state);
    }

    /**
     * Returns the derangement array for the current encoded state.
     *
     * <p>This is an O(1) operation: because {@link #incrementEncoded} keeps
     * {@code derangement[]} current during its incremental suffix rebuild,
     * no additional work is needed here.</p>
     *
     * @return live reference to {@code state.derangement} — clone if stability needed
     */
    public int[] encodedToDerangement(DerangadicState state) {
        return state.derangement;   // always current; see class-level Javadoc
    }

    /**
     * Advances the state and returns {@code true} if successful.
     * Equivalent to {@link #incrementEncoded(DerangadicState)} — the derangement
     * is available via {@link DerangadicState#currentDerangement()} immediately.
     *
     * @return {@code true} if advanced; {@code false} if exhausted
     */
    public boolean increment(DerangadicState state) {
        return doIncrement(state);
    }

    // =========================================================================
    // Single engine — shared by all three public entry points
    // =========================================================================

    /**
     * The hot path.  Finds the leftmost digit that has headroom, increments it,
     * then calls {@link #rollbackAndRebuild} to restore consistency for the
     * suffix.  On a parity-band boundary, expands the carrier length and does a
     * full rebuild (rare — O(n log n), but happens at most O(n/2) times total).
     */
    private boolean doIncrement(DerangadicState state) {
        int actualN = state.actualN;

        // Scan from index 1 (index 0 = LSD, always forced to 0).
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
     * from scratch.  O(n + actualN × log n).  Called only at:
     * <ul>
     *   <li>initial state construction, and</li>
     *   <li>parity-band expansion (rare).</li>
     * </ul>
     */
    private void rebuildAllFromDigits(DerangadicState state) {
        int n       = state.n;
        int actualN = state.actualN;
        int offset  = n - actualN;

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
            if (chosen == pos) {                         // would be fixed point
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
     *
     * <p>Both encoded state ({@code eUsed[]}, {@code consumedAtStep[]},
     * {@code digits[]}, {@code maxDigit[]}) and derangement state
     * ({@code usedFull[]}, {@code availTree}, {@code derangement[]}) are
     * updated in the same single pass — no separate second pass is needed.</p>
     */
    private void rollbackAndRebuild(DerangadicState state, int changedStep) {
        int actualN = state.actualN;
        int offset  = state.n - actualN;
        FenwickTree avail = state.availTree;

        // ---- Release suffix ----
        for (int step = changedStep; step < actualN; step++) {
            state.eUsed[state.consumedAtStep[step]] = false;          // encoded
            int abs = state.derangement[offset + step];
            state.usedFull[abs] = false;                              // absolute
            avail.update(abs + 1, 1);                                 // Fenwick
        }

        // ---- Rebuild suffix ----
        for (int step = changedStep; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(state, step);

            int digit;
            if (step == changedStep) {
                digit = state.digits[di];               // already incremented
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
     *
     * <p>The relative digit indexes into the available relative candidates
     * (0..actualN-1 minus already-used and minus step itself).  The absolute
     * element is derived from the Fenwick tree, skipping position {@code pos}
     * when it is still available so as not to create a fixed point in the full
     * n-element permutation.</p>
     */
    private static void consumeAtStep(DerangadicState state, int step, int digit,
                                      FenwickTree avail, int offset) {
        int actualN = state.actualN;

        // --- Encoded (relative) selection ---
        int seenRel = 0, chosenRel = -1;
        for (int c = 0; c < actualN; c++) {
            if (state.eUsed[c] || c == step) continue;
            if (seenRel == digit) { chosenRel = c; break; }
            seenRel++;
        }
        state.eUsed[chosenRel]       = true;
        state.consumedAtStep[step]   = chosenRel;

        // --- Absolute selection via Fenwick tree ---
        int pos          = offset + step;
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
        state.derangement[pos]      = chosenAbs - 1;
        state.usedFull[chosenAbs-1] = true;
        avail.update(chosenAbs, -1);
    }

    // =========================================================================
    // Digit constraint helpers (pure functions on encoded state)
    // =========================================================================

    private static int computeMaxDigit(DerangadicState state, int step) {
        int unused     = state.actualN - step;
        int legalCount = state.eUsed[step] ? unused : unused - 1;
        return legalCount > 0 ? legalCount - 1 : 0;
    }

    private static int computeMinDigit(DerangadicState state, int step) {
        int remainingSize = state.actualN - step;
        int seen = 0;
        for (int c = 0; c < state.actualN; c++) {
            if (state.eUsed[c] || c == step) continue;
            boolean deadEnd = false;
            if (remainingSize == 2) {
                int otherElem = -1;
                for (int x = 0; x < state.actualN; x++) {
                    if (!state.eUsed[x] && x != c) { otherElem = x; break; }
                }
                deadEnd = (otherElem == step + 1);
            }
            if (!deadEnd) return seen;
            seen++;
        }
        return 0;
    }

    // =========================================================================
    // Inner state class — package-private
    // =========================================================================

    /**
     * Mutable state for one incremental Derangadic walk.
     *
     * <h3>Field inventory</h3>
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
     */
    static final class DerangadicState {

        // encoded state
        int[]       digits;
        int[]       maxDigit;
        int[]       consumedAtStep;
        boolean[]   eUsed;
        int         actualN;

        // derangement state
        int[]       derangement;
        boolean[]   usedFull;
        FenwickTree availTree;

        // immutable
        final int n;

        DerangadicState(int n, int actualN, int[] digits) {
            this.n              = n;
            this.actualN        = actualN;
            this.digits         = digits;
            this.maxDigit       = new int[actualN];
            this.consumedAtStep = new int[actualN];
            this.eUsed          = new boolean[actualN];
            this.derangement    = new int[n];
            this.usedFull       = new boolean[n];
            this.availTree      = new FenwickTree(n);
        }

        void resizeActualN(int newActualN, int[] newDigits) {
            this.actualN        = newActualN;
            this.digits         = newDigits;
            this.maxDigit       = new int[newActualN];
            this.consumedAtStep = new int[newActualN];
            this.eUsed          = new boolean[newActualN];
            // derangement[], usedFull[], availTree stay at size n
        }

        int[] getDigits()          { return digits.clone(); }
        int[] currentDerangement() { return derangement; }
        int   getActualN()         { return actualN; }

        @Override
        public String toString() {
            return "DerangadicState{digits=" + Arrays.toString(digits)
                    + ", max=" + Arrays.toString(maxDigit)
                    + ", actualN=" + actualN + '}';
        }
    }
}