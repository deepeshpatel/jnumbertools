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

/** Internal incremental Derangadic successor engine. */
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

    DerangadicState initialState(int n) {
        return initialState(n, BigInteger.ZERO);
    }

    DerangadicState initialState(int n, BigInteger rank) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");
        Objects.requireNonNull(rank, "rank");
        int[] digits = alg.toDerangadic(rank, n);
        DerangadicState state = new DerangadicState(n, digits.length, digits);
        rebuildAllFromDigits(state);
        return state;
    }

    boolean increment(DerangadicState state) {
        int actualN = state.actualN;
        int p = -1;
        for (int i = 1; i < actualN; i++) {
            if (state.digits[i] < state.maxDigit[i]) {
                p = i;
                break;
            }
        }

        if (p != -1) {
            int changedStep = actualN - 1 - p;
            state.digits[p]++;
            rollbackAndRelax(state, changedStep);
            return true;
        }

        if (actualN >= state.n) return false;

        int newActualN = actualN + 2;
        BigInteger firstRank = calculator.subFactorial(actualN);
        int[] firstDigits = alg.toDerangadic(firstRank, state.n);
        state.resizeActualN(newActualN, firstDigits);
        rebuildAllFromDigits(state);
        return true;
    }

    private void rebuildAllFromDigits(DerangadicState state) {
        int n = state.n;
        int actualN = state.actualN;
        int offset = n - actualN;

        Arrays.fill(state.eUsed, false);
        Arrays.fill(state.usedFull, false);

        FenwickTree avail = state.availTree;
        for (int i = 1; i <= n; i++) {
            int slot = avail.get(i);
            if (slot != 1) avail.update(i, 1 - slot);
        }

        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && state.usedFull[nextCandidate]) nextCandidate++;
            int chosen = nextCandidate;
            if (chosen == pos) {
                int temp = nextCandidate + 1;
                while (temp < n && state.usedFull[temp]) temp++;
                chosen = temp;
            }
            state.derangement[pos] = chosen;
            state.usedFull[chosen] = true;
            avail.update(chosen + 1, -1);
            if (chosen == nextCandidate) nextCandidate++;
        }

        for (int step = 0; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(state, step);
            consumeAtStep(state, step, state.digits[di]);
        }
    }

    private void rollbackAndRelax(DerangadicState state, int changedStep) {
        int actualN = state.actualN;
        int offset = state.n - actualN;
        FenwickTree avail = state.availTree;

        for (int step = changedStep; step < actualN; step++) {
            state.eUsed[state.consumedAtStep[step]] = false;
            int element = state.derangement[offset + step];
            state.usedFull[element] = false;
            avail.update(element + 1, 1);
        }

        for (int step = changedStep; step < actualN; step++) {
            int di = actualN - 1 - step;
            state.maxDigit[di] = computeMaxDigit(state, step);
            int digit;
            if (step == changedStep) {
                digit = state.digits[di];
            } else {
                digit = computeMinDigit(state, step);
                state.digits[di] = digit;
            }
            consumeAtStep(state, step, digit);
        }
    }

    private static int computeMaxDigit(DerangadicState state, int step) {
        int unused = state.actualN - step;
        int legalCount = state.eUsed[step] ? unused : unused - 1;
        return legalCount > 0 ? legalCount - 1 : 0;
    }

    private static int computeMinDigit(DerangadicState state, int step) {
        int remainingSize = state.actualN - step;
        int seen = 0;
        for (int candidate = 0; candidate < state.actualN; candidate++) {
            if (state.eUsed[candidate] || candidate == step) continue;
            boolean deadEnd = false;
            if (remainingSize == 2) {
                int otherElement = -1;
                int otherPosition = step + 1;
                for (int x = 0; x < state.actualN; x++) {
                    if (!state.eUsed[x] && x != candidate) {
                        otherElement = x;
                        break;
                    }
                }
                deadEnd = otherElement == otherPosition;
            }
            if (!deadEnd) return seen;
            seen++;
        }
        return 0;
    }

    private static void consumeAtStep(DerangadicState state, int step, int digit) {
        int actualN = state.actualN;
        int offset = state.n - actualN;

        int seenActual = 0;
        int chosenActual = -1;
        for (int candidate = 0; candidate < actualN; candidate++) {
            if (state.eUsed[candidate] || candidate == step) continue;
            if (seenActual == digit) {
                chosenActual = candidate;
                break;
            }
            seenActual++;
        }
        state.eUsed[chosenActual] = true;
        state.consumedAtStep[step] = chosenActual;

        int pos = offset + step;
        FenwickTree avail = state.availTree;
        int upTo = avail.rsq(pos + 1);
        int below = avail.rsq(pos);
        boolean posAvailable = upTo - below == 1;
        int chosenFull;
        if (posAvailable) {
            chosenFull = avail.findKth(digit < below ? digit + 1 : digit + 2) - 1;
        } else {
            chosenFull = avail.findKth(digit + 1) - 1;
        }

        state.derangement[pos] = chosenFull;
        state.usedFull[chosenFull] = true;
        avail.update(chosenFull + 1, -1);
    }

    static final class DerangadicState {
        int[] digits;
        int[] maxDigit;
        int[] consumedAtStep;
        boolean[] eUsed;
        int[] derangement;
        boolean[] usedFull;
        FenwickTree availTree;
        final int n;
        int actualN;

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

        void resizeActualN(int newActualN, int[] newDigits) {
            this.actualN = newActualN;
            this.digits = newDigits;
            this.maxDigit = new int[newActualN];
            this.consumedAtStep = new int[newActualN];
            this.eUsed = new boolean[newActualN];
        }

        int[] getDigits() {
            return digits.clone();
        }

        int[] currentDerangement() {
            return derangement;
        }

        int getActualN() {
            return actualN;
        }

        @Override
        public String toString() {
            return "DerangadicState{digits=" + Arrays.toString(digits)
                    + ", max=" + Arrays.toString(maxDigit)
                    + ", actualN=" + actualN + '}';
        }
    }
}
