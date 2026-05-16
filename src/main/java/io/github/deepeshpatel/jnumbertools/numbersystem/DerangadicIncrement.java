/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import java.math.BigInteger;

/**
 * Incremental (next-rank) generation of Derangadic digit encodings.
 * Optimized to prevent runtime object allocations during boundary crossings.
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @since 3.0.2
 */
public final class DerangadicIncrement {

    private final Calculator calculator;
    private final DerangadicAlgorithms alg; // Cached to prevent allocation leaks

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

        int[] maxDigit         = new int[actualN];
        int[] restrictedAtStep = new int[actualN];
        recomputeState(digits, actualN, maxDigit, restrictedAtStep);

        return new DerangadicState(digits, maxDigit, restrictedAtStep, n, actualN);
    }

    /**
     * Advances {@code state} in-place to the next rank.
     *
     * @param state current state (mutated)
     * @return {@code true} if advanced; {@code false} if already at the last rank
     */
    public boolean increment(DerangadicState state) {
        if (isLastRank(state)) return false;

        int[] digits   = state.digits;
        int[] maxDigit = state.maxDigit;
        int   actualN  = state.actualN;

        // Find the LOWEST index in [1, actualN-1] where digits[i] < maxDigit[i].
        int p = -1;
        for (int i = 1; i < actualN; i++) {
            if (digits[i] < maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p != -1) {
            digits[p]++;

            // Fill indices 1..p-1 with the minimum valid digits for the new prefix.
            fillMinimumSuffix(digits, actualN, p);
            recomputeState(digits, actualN, maxDigit, state.restrictedAtStep);

        } else {
            // All digits maxed → expand length by 2 using cached algorithms reference
            int newActualN = actualN + 2;
            BigInteger firstRank = calculator.subFactorial(actualN);
            int[] firstDigits = alg.toDerangadic(firstRank, state.n);

            int[] newMax        = new int[newActualN];
            int[] newRestricted = new int[newActualN];
            recomputeState(firstDigits, newActualN, newMax, newRestricted);

            state.digits           = firstDigits;
            state.maxDigit         = newMax;
            state.restrictedAtStep = newRestricted;
            state.actualN          = newActualN;
        }

        return true;
    }

    // =========================================================================
    // Minimum-suffix computation
    // =========================================================================

    private void fillMinimumSuffix(int[] digits, int actualN, int p) {
        int stepOfP = actualN - 1 - p;
        boolean[] elementUsed = new boolean[actualN];

        // Replay the fixed prefix (steps 0 .. stepOfP inclusive).
        for (int step = 0; step <= stepOfP; step++) {
            int di     = actualN - 1 - step;
            int target = digits[di];
            int seen   = 0;
            for (int c = 0; c < actualN; c++) {
                if (elementUsed[c] || c == step) continue;
                if (seen == target) {
                    elementUsed[c] = true;
                    break;
                }
                seen++;
            }
        }

        // Fill steps stepOfP+1 .. actualN-1 (i.e., digit indices p-1 .. 0)
        for (int step = stepOfP + 1; step < actualN; step++) {
            int di            = actualN - 1 - step;
            int remainingSize = actualN - step;

            int chosenDigit   = 0;
            int chosenElement = -1;
            int seen          = 0;

            for (int c = 0; c < actualN; c++) {
                if (elementUsed[c] || c == step) continue;

                boolean deadEnd = false;
                if (remainingSize == 2) {
                    int otherElement  = -1;
                    int otherPosition = step + 1; // Unfilled suffix index is immediately next

                    for (int x = 0; x < actualN; x++) {
                        if (!elementUsed[x] && x != c) {
                            otherElement = x;
                            break;
                        }
                    }
                    deadEnd = (otherElement == otherPosition);
                }

                if (!deadEnd) {
                    chosenDigit   = seen;
                    chosenElement = c;
                    break;
                }
                seen++;
            }

            digits[di] = chosenDigit;
            if (chosenElement != -1) elementUsed[chosenElement] = true;
        }
        digits[0] = 0;
    }

    // =========================================================================
    // Auxiliary-state computation
    // =========================================================================

    void recomputeState(int[] digits, int actualN, int[] maxDigit, int[] restrictedAtStep) {
        boolean[] elementUsed = new boolean[actualN];

        for (int step = 0; step < actualN; step++) {
            int di = actualN - 1 - step;

            int rc = 0;
            for (int i = step; i < actualN; i++) {
                if (!elementUsed[i]) rc++;
            }
            restrictedAtStep[di] = rc;

            int legalCount = 0;
            for (int c = 0; c < actualN; c++) {
                if (!elementUsed[c] && c != step) legalCount++;
            }
            maxDigit[di] = (legalCount > 0) ? legalCount - 1 : 0;

            int target = digits[di];
            int seen   = 0;
            for (int c = 0; c < actualN; c++) {
                if (elementUsed[c] || c == step) continue;
                if (seen == target) {
                    elementUsed[c] = true;
                    break;
                }
                seen++;
            }
        }
    }

    private boolean isLastRank(DerangadicState state) {
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
        int[] digits;
        int[] maxDigit;
        int[] restrictedAtStep;
        final int n;
        int actualN;

        DerangadicState(int[] digits, int[] maxDigit, int[] restrictedAtStep, int n, int actualN) {
            this.digits           = digits;
            this.maxDigit         = maxDigit;
            this.restrictedAtStep = restrictedAtStep;
            this.n                = n;
            this.actualN          = actualN;
        }

        public int[] getDigits() { return digits.clone(); }
        public int getActualN() { return actualN; }
        public int getN()       { return n; }

        @Override
        public String toString() {
            return "DerangadicState{digits=" + java.util.Arrays.toString(digits)
                    + ", max=" + java.util.Arrays.toString(maxDigit)
                    + ", actualN=" + actualN + '}';
        }
    }
}