/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.FenwickTree;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Incremental (next-rank) generation of Derangadic digit encodings.
 * <p>
 * Maintains the full derangement array in {@link DerangadicState} so that each
 * call to {@link #increment(DerangadicState)} only patches the suffix that
 * actually changed. For the common case where the incremented digit index
 * {@code p} is small (the low-order "odometer" position), only {@code p + 1}
 * positions of the derangement need to be rewritten.
 * </p>
 *
 * <h3>Algorithm</h3>
 * <p>The digit at index {@code di} corresponds to step {@code actualN - 1 - di}
 * of an internal build walk. When increment finds the lowest index {@code p}
 * with {@code digits[p] < maxDigit[p]} and bumps it:</p>
 * <ul>
 *   <li>Digits at indices {@code > p} (steps {@code < changedStep}) are untouched.</li>
 *   <li>The derangement positions {@code [offset, offset + changedStep)} therefore
 *       stay valid.</li>
 *   <li>State for steps {@code [changedStep, actualN)} is rolled back and rewalked,
 *       updating {@code maxDigit}, {@code restrictedAtStep}, the per-step element
 *       consumption arrays, and the derangement suffix in lockstep.</li>
 * </ul>
 *
 * <p>The state object exposes {@link DerangadicState#currentDerangement()} which
 * returns the live (non-copying) derangement array for hot loops.</p>
 *
 * <p>This class is part of the internal API; library users should prefer
 * {@link Derangadic}.</p>
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
        this.calculator = calculator;
        this.alg = new DerangadicAlgorithms(calculator);
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Creates a {@link DerangadicState} representing rank 0 of order {@code n}.
     *
     * @param n element count (n &ge; 2)
     * @return state representing rank 0
     */
    public DerangadicState initialState(int n) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");

        int[] digits  = alg.toDerangadic(0L, n);
        int   actualN = digits.length;

        DerangadicState s = new DerangadicState(n, actualN, digits);
        rebuildAllFromDigits(s);
        return s;
    }

    /**
     * Advances {@code state} in-place to the next rank.
     *
     * @param state current state (mutated)
     * @return {@code true} if advanced; {@code false} if already at the last rank
     */
    public boolean increment(DerangadicState state) {
        int   actualN  = state.actualN;
        int[] digits   = state.digits;
        int[] maxDigit = state.maxDigit;

        // Find the LOWEST index in [1, actualN-1] where digits[i] < maxDigit[i].
        // digit[0] is always at max (single remaining element at the last step),
        // so it never holds a non-maxed value; skipping i=0 is safe and means
        // this single scan doubles as the "is-last-rank" check.
        int p = -1;
        for (int i = 1; i < actualN; i++) {
            if (digits[i] < maxDigit[i]) { p = i; break; }
        }

        if (p != -1) {
            int changedStep = actualN - 1 - p;
            digits[p]++;
            rollbackAndRelax(state, changedStep);
            return true;
        }

        // All digits maxed for current actualN: either grow length by 2, or
        // we're truly at the global last rank.
        if (actualN >= state.n) return false;

        int newActualN = actualN + 2;
        BigInteger firstRank = calculator.subFactorial(actualN);
        int[] firstDigits = alg.toDerangadic(firstRank, state.n);
        state.resizeActualN(newActualN, firstDigits);
        rebuildAllFromDigits(state);
        return true;
    }

    // =========================================================================
    // Full rebuild (used by initialState and the length-expansion boundary)
    // =========================================================================

    /**
     * Walks step 0..actualN-1 using {@code state.digits[]} as authoritative,
     * fully populating {@code maxDigit}, {@code restrictedAtStep},
     * {@code consumedAtStep}, {@code eUsed}, {@code derangement}, {@code usedFull}.
     * Also fills the offset prefix of the derangement.
     */
    private void rebuildAllFromDigits(DerangadicState s) {
        int n = s.n, actualN = s.actualN;
        int offset = n - actualN;

        Arrays.fill(s.eUsed, false);
        Arrays.fill(s.usedFull, false);

        // Reset Fenwick tree: every position 1..n holds 1 (available).
        FenwickTree avail = s.availTree;
        for (int i = 1; i <= n; i++) {
            int slot = avail.get(i);
            if (slot != 1) avail.update(i, 1 - slot);
        }

        // Offset prefix: depends only on (n, actualN), not on digits.
        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && s.usedFull[nextCandidate]) nextCandidate++;
            int chosen = nextCandidate;
            if (chosen == pos) {
                int temp = nextCandidate + 1;
                while (temp < n && s.usedFull[temp]) temp++;
                chosen = temp;
            }
            s.derangement[pos] = chosen;
            s.usedFull[chosen] = true;
            avail.update(chosen + 1, -1);
            if (chosen == nextCandidate) nextCandidate++;
        }

        // Main walk (digits are authoritative).
        for (int step = 0; step < actualN; step++) {
            int di = actualN - 1 - step;
            s.maxDigit[di] = computeMaxDigit(s, step);
            consumeAtStep(s, step, s.digits[di]);
        }
    }

    // =========================================================================
    // Incremental rebuild: only the suffix from changedStep onward
    // =========================================================================

    /**
     * Caller guarantees: {@code digits[di_p]} (di_p = actualN-1-changedStep)
     * was just incremented; digits at indices {@code > p} (steps
     * {@code < changedStep}) are still correct; digits at indices
     * {@code < p} (steps {@code > changedStep}) are stale and will be overwritten
     * with their minimum legal values here.
     */
    private void rollbackAndRelax(DerangadicState s, int changedStep) {
        int actualN = s.actualN, n = s.n;
        int offset = n - actualN;
        FenwickTree avail = s.availTree;

        // 1) Roll back used-element state for steps in [changedStep, actualN).
        for (int step = changedStep; step < actualN; step++) {
            s.eUsed[s.consumedAtStep[step]] = false;
            int e = s.derangement[offset + step];
            s.usedFull[e] = false;
            avail.update(e + 1, +1);
        }

        // 2) Walk forward, fixing maxDigit, digits and derangement.
        for (int step = changedStep; step < actualN; step++) {
            int di = actualN - 1 - step;
            s.maxDigit[di] = computeMaxDigit(s, step);

            int digit;
            if (step == changedStep) {
                digit = s.digits[di];          // already incremented by caller
            } else {
                digit = computeMinDigit(s, step);
                s.digits[di] = digit;
            }
            consumeAtStep(s, step, digit);
        }
    }

    // =========================================================================
    // Per-step helpers
    // =========================================================================

    /**
     * O(1) replacement for the old two-scan {@code computeMaxAndRestricted}.
     * <p>
     * At the start of {@code step}, exactly {@code actualN - step} elements
     * remain unused in the actualN simulation (one consumed per previous step).
     * The set of <em>legal</em> elements excludes element {@code step} itself
     * (no fixed points), which is in the unused set iff {@code !eUsed[step]}.
     * </p>
     */
    private static int computeMaxDigit(DerangadicState s, int step) {
        int unused = s.actualN - step;
        int legalCount = s.eUsed[step] ? unused : unused - 1;
        return (legalCount > 0) ? legalCount - 1 : 0;
    }

    /**
     * Returns the smallest legal digit at {@code step} that does not lead to a
     * trivial dead end. Preserves the original {@code remainingSize == 2}
     * dead-end check.
     */
    private static int computeMinDigit(DerangadicState s, int step) {
        int actualN = s.actualN;
        boolean[] eUsed = s.eUsed;
        int remainingSize = actualN - step;
        int seen = 0;

        for (int c = 0; c < actualN; c++) {
            if (eUsed[c] || c == step) continue;

            boolean deadEnd = false;
            if (remainingSize == 2) {
                int otherElement = -1;
                int otherPosition = step + 1;
                for (int x = 0; x < actualN; x++) {
                    if (!eUsed[x] && x != c) { otherElement = x; break; }
                }
                deadEnd = (otherElement == otherPosition);
            }

            if (!deadEnd) return seen;
            seen++;
        }
        return 0;
    }

    /**
     * Consume the {@code digit}-th legal element in both the actualN simulation
     * (used for the digit-encoding state) and the full-n derangement walk
     * (which feeds {@link DerangadicState#currentDerangement()}).
     */
    private static void consumeAtStep(DerangadicState s, int step, int digit) {
        int actualN = s.actualN, n = s.n;
        int offset = n - actualN;

        // actualN simulation: actualN is typically small (D_actualN grows like
        // !actualN, so actualN rarely exceeds ~20 even for rank in the trillions),
        // so a linear scan is fine and beats Fenwick overhead.
        int seenA = 0, chosenA = -1;
        boolean[] eUsed = s.eUsed;
        for (int c = 0; c < actualN; c++) {
            if (eUsed[c] || c == step) continue;
            if (seenA == digit) { chosenA = c; break; }
            seenA++;
        }
        eUsed[chosenA] = true;
        s.consumedAtStep[step] = chosenA;

        // Full-n derangement: O(log n) via Fenwick (mirrors
        // DerangadicAlgorithms.toDerangementFenwick.getChosen).
        int pos = offset + step;
        FenwickTree avail = s.availTree;
        int chosenFull;
        // Is element `pos` (1-based pos+1 in the tree) still available?
        int upTo = avail.rsq(pos + 1);
        int below = avail.rsq(pos);
        boolean posAvailable = (upTo - below) == 1;

        if (posAvailable) {
            // `below` = number of available elements with index < pos.
            // If digit < below, the answer lies before pos; otherwise after pos
            // (so skip it by adding 1 to the rank we look up).
            if (digit < below) chosenFull = avail.findKth(digit + 1) - 1;
            else               chosenFull = avail.findKth(digit + 2) - 1;
        } else {
            chosenFull = avail.findKth(digit + 1) - 1;
        }

        s.derangement[pos] = chosenFull;
        s.usedFull[chosenFull] = true;
        avail.update(chosenFull + 1, -1);
    }

    private static boolean isLastRank(DerangadicState state) {
        // Retained for completeness; no longer on the hot path (increment() now
        // detects last-rank via its own digit scan).
        if (state.actualN < state.n) return false;
        for (int i = 0; i < state.actualN; i++) {
            if (state.digits[i] < state.maxDigit[i]) return false;
        }
        return true;
    }

    // =========================================================================
    // State object
    // =========================================================================

    public static final class DerangadicState {
        // Package-private mutable state — accessed by DerangadicIncrement and a
        // couple of internal tests.
        int[]  digits;
        int[]  maxDigit;
        int[]  consumedAtStep;     // element index consumed at each step (actualN sim)
        boolean[] eUsed;           // mirrors actualN-sized "elementUsed" at END of walk
        int[]  derangement;        // length n, mirrors what toDerangement(digits, n) returns
        boolean[] usedFull;        // length n, "elementUsed" for the full-n walk
        FenwickTree availTree;     // length n, available[i+1]=1 iff !usedFull[i]
        final int n;
        int       actualN;

        DerangadicState(int n, int actualN, int[] digits) {
            this.n                = n;
            this.actualN          = actualN;
            this.digits           = digits;
            this.maxDigit         = new int[actualN];
            this.consumedAtStep   = new int[actualN];
            this.eUsed            = new boolean[actualN];
            this.derangement      = new int[n];
            this.usedFull         = new boolean[n];
            this.availTree        = new FenwickTree(n);
        }

        /** Resize internal actualN-sized arrays for a length expansion (+2). */
        void resizeActualN(int newActualN, int[] newDigits) {
            this.actualN          = newActualN;
            this.digits           = newDigits;
            this.maxDigit         = new int[newActualN];
            this.consumedAtStep   = new int[newActualN];
            this.eUsed            = new boolean[newActualN];
            // derangement, usedFull and availTree remain length n;
            // rebuildAllFromDigits resets them.
        }

        /** @return a defensive copy of the digit array. */
        public int[] getDigits() { return digits.clone(); }

        /**
         * Returns the live, internally-maintained derangement array of length
         * {@link #getN()}.
         * <p><strong>The caller must not modify this array.</strong> It is owned
         * by {@link DerangadicIncrement} and will be mutated on the next
         * {@link DerangadicIncrement#increment(DerangadicState) increment}. If
         * you need a stable snapshot, clone it.</p>
         *
         * @return the live derangement array
         */
        public int[] currentDerangement() { return derangement; }

        public int getActualN() { return actualN; }
        public int getN()       { return n; }

        @Override
        public String toString() {
            return "DerangadicState{digits=" + Arrays.toString(digits)
                    + ", max=" + Arrays.toString(maxDigit)
                    + ", actualN=" + actualN + '}';
        }
    }
}

