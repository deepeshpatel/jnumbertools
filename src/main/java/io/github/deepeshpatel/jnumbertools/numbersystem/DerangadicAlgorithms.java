/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.FenwickTree;

import java.math.BigInteger;
import java.util.*;

/**
 * Algorithms for the Derangadic Number System.
 * <p>
 * The Derangadic system establishes a canonical bijection between ranks {@code [0, !n - 1]}
 * and lexicographically ordered derangements of {@code n} elements, where {@code !n} is the
 * subfactorial (number of derangements). Unlike the Factoradic system, Derangadic intrinsically
 * enforces the fixed-point-free constraint ({@code π(i) ≠ i}) through dynamic restricted
 * derangement counts.
 * <p>
 * <b>Mathematical Definition:</b>
 * A rank {@code r < !n} is represented as a mixed-radix expansion:
 * <pre>
 *   r = Σ_{s=0}^{k-1} D_s · B_s
 * </pre>
 * where {@code k = actualN(n,r)} is the minimal active length with {@code k ≡ n (mod 2)} and
 * {@code !k > r}, {@code D_s} are digits satisfying {@code 0 ≤ D_s ≤ maxDigit(s)}, and
 * {@code B_s = RD(remaining-1, nextRestricted)} are block sizes computed via restricted
 * derangement counts.
 * <p>
 * <b>Restricted Derangements:</b>
 * {@code RD(m, r)} denotes permutations of {@code m} elements avoiding fixed points at
 * {@code r} specified positions:
 * <pre>
 *   RD(m, r) = Σ_{j=0}^{r} (-1)^j · C(r, j) · (m - j)!
 * </pre>
 * <p>
 * <b>Key Structural Invariants:</b>
 * <ul>
 *   <li><b>LSD is always 0:</b> {@code digits[0]} (least-significant digit) is strictly 0 for all valid encodings.
 *       This follows from the derangement constraint forcing a unique completion when only two positions remain.</li>
 *   <li><b>Parity-Locked Stabilisation:</b> Active length {@code actualN} preserves the parity of {@code n}.
 *       Encodings depend only on rank and parity, not on the global universe size.</li>
 *   <li><b>Dead-End Avoidance:</b> When exactly two positions remain, candidates that would force the final element
 *       to map to its own index are skipped, effectively shifting {@code minDigit} by +1 for that step.</li>
 *   <li><b>Dynamic Restricted Count:</b> {@code c_s} is recomputed at each step by counting unfilled positions
 *       {@code j ≥ s} where element {@code j} is still unused.</li>
 * </ul>
 * <p>
 * <b>Digit Array Convention:</b>
 * <ul>
 *   <li>Arrays are stored <strong>LSD-first</strong> in the implementation: {@code digits[0]} = {@code D_0}
 *       (least significant), {@code digits[k-1]} = {@code D_{k-1}} (most significant).</li>
 *   <li>For display/paper notation, digits are shown <strong>MSD-first</strong>: {@code [D_{k-1}, ..., D_0]}.</li>
 *   <li>Length equals {@code actualN} (minimal carrier length), not necessarily {@code n}.</li>
 *   <li>Trailing zeros beyond the last non-zero digit do not affect the rank value.</li>
 * </ul>
 * <p>
 * <b>Example (n=4, !4=9):</b>
 * <pre>
 *   Rank 0 → digits [0, 0] (actualN=2) → derangement [1, 0, 3, 2]
 *   Rank 1 → digits [0, 1, 1, 0] (actualN=4) → derangement [1, 2, 3, 0]
 *   Rank 2 → digits [0, 2, 0, 0] (actualN=4) → derangement [1, 3, 0, 2]
 *   Rank 3 → digits [1, 0, 1, 0] (actualN=4) → derangement [2, 0, 3, 1]
 *   Rank 4 → digits [1, 1, 0, 0] (actualN=4) → derangement [2, 3, 0, 1]
 *   Rank 5 → digits [1, 1, 1, 0] (actualN=4) → derangement [2, 3, 1, 0]
 *   Rank 6 → digits [2, 0, 0, 0] (actualN=4) → derangement [3, 0, 1, 2]
 *   Rank 7 → digits [2, 1, 0, 0] (actualN=4) → derangement [3, 2, 0, 1]
 *   Rank 8 → digits [2, 1, 1, 0] (actualN=4) → derangement [3, 2, 1, 0]
 * </pre>
 * <p>
 * <b>Relationship to Other Systems:</b>
 * <ul>
 *   <li><b>Factoradic:</b> The unrestricted limit of Derangadic when restricted count {@code c_s = 0} at all steps,
 *       collapsing {@code RD(m,0) = m!} and recovering factorial place values.</li>
 *   <li><b>Permutadic:</b> Derangadic encodes permutations with zero fixed points; Permutadic encodes
 *       k-permutations of s elements with degree {@code d = s - k}.</li>
 * </ul>
 *
 * @author Deepesh Patel & Aditya Patel
 * @version 3.0.2
 * @see <a href="https://ssrn.com/abstract=4174035">Derangadic: A Combinatorial Number System for Derangements</a>
 * @see FactoradicAlgorithms
 * @see DerangadicIncrementStateMachine
 * @since 3.0.2
 */
public final class DerangadicAlgorithms {

    private final Calculator calculator;


    private static final int N_THRESHOLD = 100;
    private static final int DIGIT_THRESHOLD = 100;

    public DerangadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    // ==================== Core Public API ====================

    /**
     * Returns the number of derangements of {@code n} elements (subfactorial {@code !n}).
     *
     * @param n number of elements ({@code n ≥ 0})
     * @return {@code !n}, the count of fixed-point-free permutations
     * @implNote Delegates to {@link Calculator#subFactorial(int)}
     */
    public BigInteger derangementCount(int n) {
        return calculator.subFactorial(n);
    }

    /**
     * Counts completions for the remaining structure.
     */
    private BigInteger countCompletions(Set<Integer> remainingPositions,
                                        Set<Integer> remainingElements) {
        int size = remainingPositions.size();
        int restrictedCount = 0;
        for (int pos : remainingPositions) {
            if (remainingElements.contains(pos)) {
                restrictedCount++;
            }
        }
        return calculator.restrictedDerangements(size, restrictedCount);
    }

    /**
     * Encodes a rank into its Derangadic digit representation.
     * <p>
     * <b>Algorithm:</b>
     * <ol>
     *   <li>Compute {@code k = actualN(n, rank)}</li>
     *   <li>For each position {@code s = 0..k-1} (MSD to LSD):
     *     <ul>
     *       <li>Recompute restricted count {@code c_s} dynamically by counting unfilled positions j ≥ s where element j is unused</li>
     *       <li>Iterate legal candidates, compute block sizes via {@code RD(remaining-1, nextRestricted)}</li>
     *       <li>Select candidate whose cumulative block covers current remainder</li>
     *       <li>Handle dead-end avoidance when {@code remaining == 2}</li>
     *     </ul>
     *   </li>
     *   <li>Store digits LSD-first ({@code digits[0]} = last position)</li>
     * </ol>
     * <p>
     * <b>Digit Ordering:</b> Result array is LSD-first: {@code result[0]} = {@code D_0} (least significant).
     * <p>
     * <b>Complexity:</b> O(k²) where {@code k = actualN}, due to dynamic restricted count recomputation.
     *
     * @param rank the rank to encode ({@code 0 ≤ rank < !n})
     * @param n    universe size ({@code n ≥ 2})
     * @return Derangadic digit array in LSD-first order, length = {@code actualN}
     * @throws IllegalArgumentException if {@code rank} out of bounds or {@code n < 2}
     *
     * @apiNote
     * <ul>
     *   <li>{@code digits[0]} is guaranteed to be 0 (LSD Invariant)</li>
     *   <li>Max digit at step {@code s}: {@code max = unused - 1} if position {@code s} is available, else {@code unused}</li>
     *   <li>Dead-end avoidance skips candidates that would force a fixed point when {@code remaining == 2}</li>
     * </ul>
     */
    public int[] toDerangadic(BigInteger rank, int n) {
        BigInteger max = derangementCount(n);
        if (rank.signum() < 0 || rank.compareTo(max) >= 0) {
            throw new IllegalArgumentException("m out of range");
        }

        int actualN = smallestN(n, rank);
        int[] digits = new int[actualN];

        boolean[] elementUsed = new boolean[actualN];
        BigInteger currentM = rank;

        for (int step = 0; step < actualN; step++) {
            int remainingSize = actualN - step;

            // Recalculate restrictedCount: How many i > step exist such that
            // position i is unfilled and element i is unused.
            int restrictedCount = 0;
            for (int i = step; i < actualN; i++) {
                if (!elementUsed[i]) {
                    restrictedCount++;
                }
            }

            int legalFoundCount = 0;
            BigInteger cumulative = BigInteger.ZERO;

            for (int candidate = 0; candidate < actualN; candidate++) {
                if (elementUsed[candidate] || candidate == step) continue;

                boolean pickingRestricted = (candidate > step);
                boolean currentPosWasRestricted = (!elementUsed[step]);

                int nextRestricted;
                if (pickingRestricted && currentPosWasRestricted) {
                    nextRestricted = restrictedCount - 2;
                } else if (pickingRestricted || currentPosWasRestricted) {
                    nextRestricted = restrictedCount - 1;
                } else {
                    nextRestricted = restrictedCount;
                }

                nextRestricted = Math.max(0, nextRestricted);
                BigInteger blockSize = calculator.restrictedDerangements(remainingSize - 1, nextRestricted);

                if (currentM.compareTo(cumulative.add(blockSize)) < 0) {
                    digits[actualN - 1 - step] = legalFoundCount;
                    elementUsed[candidate] = true;
                    currentM = currentM.subtract(cumulative);
                    break;
                }

                cumulative = cumulative.add(blockSize);
                legalFoundCount++;
            }
        }

        return digits;
    }

    /**
     * Convenience overload for long ranks.
     *
     * @param rank the rank to encode ({@code 0 ≤ rank < !n})
     * @param n    universe size ({@code n ≥ 2})
     * @return Derangadic digit array in LSD-first order
     * @see #toDerangadic(BigInteger, int)
     */
    public int[] toDerangadic(long rank, int n) {
        return toDerangadic(BigInteger.valueOf(rank), n);
    }


    /**
     * Decodes a Derangadic digit tuple back to its decimal rank.
     * <p>
     * Computes {@code rank = Σ D_i · B_i} where {@code B_i} are block sizes at each step.
     * Uses the same dynamic restricted count logic as {@link #toDerangadic}.
     * <p>
     * <b>Digit Ordering:</b> Input array is LSD-first: {@code digits[0]} = {@code D_0}.
     * <p>
     * <b>Complexity:</b> O(k²) where {@code k = digits.length}.
     *
     * @param digits Derangadic digits in LSD-first order
     * @param n      universe size ({@code n ≥ digits.length})
     * @return the decoded rank as {@link BigInteger}
     * @throws IllegalArgumentException if any digit violates constraints
     */
    public BigInteger fromDerangadic(int[] digits, int n) {
        ZeroPaddedList allDigits = new ZeroPaddedList(digits, n);

        Set<Integer> remainingElements = new HashSet<>();
        Set<Integer> remainingPositions = new HashSet<>();
        for (int i = 0; i < n; i++) {
            remainingElements.add(i);
            remainingPositions.add(i);
        }

        BigInteger result = BigInteger.ZERO;
        List<Integer> positionsList = new ArrayList<>(remainingPositions);
        Collections.sort(positionsList);

        for (int step = 0; step < n; step++) {
            int pos = positionsList.get(0);

            List<Integer> legal = new ArrayList<>();
            for (int e : remainingElements) {
                if (e != pos) legal.add(e);
            }
            Collections.sort(legal);

            int digit = allDigits.get(n - 1 - step);

            for (int idx = 0; idx < digit; idx++) {
                int candidate = legal.get(idx);

                Set<Integer> newRemainingElements = new HashSet<>(remainingElements);
                newRemainingElements.remove(candidate);

                Set<Integer> newRemainingPositions = new HashSet<>(remainingPositions);
                newRemainingPositions.remove(pos);

                result = result.add(countCompletions(newRemainingPositions, newRemainingElements));
            }

            int chosen = legal.get(digit);
            remainingElements.remove(chosen);
            remainingPositions.remove(pos);
            positionsList = new ArrayList<>(remainingPositions);
            Collections.sort(positionsList);
        }

        return result;
    }

    /**
     * Converts Derangadic digits (LSD-first) to a full derangement array.
     * <p>
     * Handles the greedy prefix for positions {@code 0..(n-k-1)} and uses digits
     * to select elements for the active window {@code [n-k..n-1]}.
     * <p>
     * The active window operates on the set of elements that were NOT used in the prefix.
     * We maintain a mapping: active index {@code c ∈ [0..k-1]} → global element {@code elem ∈ [0..n-1]}.
     * <p>
     * <b>Implementation Dispatch:</b> Uses array-based method for {@code n < 100}, Fenwick-tree for larger.
     * <p>
     * <b>Complexity:</b> O(n²) array-based, O(n + k log n) Fenwick-based.
     *
     * @param digits Derangadic digits in LSD-first order, length = {@code actualN}
     * @param n      universe size
     * @return derangement array of length {@code n}
     * @throws IllegalArgumentException if {@code digits.length > n}
     */
    public int[] toDerangement(int[] digits, int n) {
        if (n < N_THRESHOLD || digits.length < DIGIT_THRESHOLD) {
            return toDerangementArray(digits, n);
        }
//some how fixed values of N_THRESHOLD and DIGIT_THRESHOLD provided better optimization than math below
//        long arrayComplexity = (long) n * digits.length;
//        long fenwickComplexity = (long) digits.length * (long)(Math.log(n) / Math.log(2));
//
//        if (arrayComplexity <= fenwickComplexity) {
//            return toDerangementArray(digits, n);
//        } else {
//            return toDerangementFenwick(digits, n);
//        }

        return toDerangementFenwick(digits, n);
    }

    /**
     * Array-based implementation for small n.
     * O(n²) but with low constant factor - best for n ≤ 100.
     */
    int[] toDerangementArray(int[] digits, int n) {
        int actualN = digits.length;
        int offset = n - actualN;

        int[] derangement = new int[n];
        boolean[] used = new boolean[n];

        int nextCandidate = 0;
        for (int pos = 0; pos < offset; pos++) {
            while (nextCandidate < n && used[nextCandidate]) {
                nextCandidate++;
            }

            int chosen = nextCandidate;
            if (chosen == pos) {
                int temp = nextCandidate + 1;
                while (temp < n && used[temp]) {
                    temp++;
                }
                chosen = temp;
            }

            derangement[pos] = chosen;
            used[chosen] = true;

            if (chosen == nextCandidate) {
                nextCandidate++;
            }
        }

        for (int step = 0; step < actualN; step++) {
            int pos = offset + step;
            int digit = digits[actualN - 1 - step];

            int count = 0;
            int chosen = -1;
            for (int e = 0; e < n; e++) {
                if (used[e]) continue;
                if (e == pos) continue;
                if (count == digit) {
                    chosen = e;
                    break;
                }
                count++;
            }

            derangement[pos] = chosen;
            used[chosen] = true;
        }

        return derangement;
    }

    /**
     * Fenwick Tree implementation for large n.
     * O(n + actualN × log n) - best for n > 100.
     */
    int[] toDerangementFenwick(int[] digits, int n) {
        int actualN = digits.length;
        int offset = n - actualN;

        int[] derangement = new int[n];
        FenwickTree availableElements = new FenwickTree(n);

        for (int i = 1; i <= n; i++) {
            availableElements.update(i, 1);
        }

        int nextAvailable = 1;
        for (int pos = 0; pos < offset; pos++) {
            while (nextAvailable <= n && availableElements.rsq(nextAvailable) - availableElements.rsq(nextAvailable - 1) == 0) {
                nextAvailable++;
            }

            int chosenIdx = nextAvailable;
            if (chosenIdx == pos + 1) {
                if (nextAvailable + 1 <= n) {
                    chosenIdx = nextAvailable + 1;
                    while (chosenIdx <= n && availableElements.rsq(chosenIdx) - availableElements.rsq(chosenIdx - 1) == 0) {
                        chosenIdx++;
                    }
                }
            }

            derangement[pos] = chosenIdx - 1;
            availableElements.update(chosenIdx, -1);

            if (chosenIdx == nextAvailable) {
                do {
                    nextAvailable++;
                } while (nextAvailable <= n && availableElements.rsq(nextAvailable) - availableElements.rsq(nextAvailable - 1) == 0);
            }
        }

        for (int step = 0; step < actualN; step++) {
            int pos = offset + step;
            int digit = digits[actualN - 1 - step];

            int chosen = getChosen(availableElements, pos, digit);

            derangement[pos] = chosen - 1;
            availableElements.update(chosen, -1);
        }

        return derangement;
    }

    private static int getChosen(FenwickTree availableElements, int pos, int digit) {
        int chosen;
        boolean posAvailable = (availableElements.rsq(pos + 1) - availableElements.rsq(pos)) == 1;

        if (posAvailable) {
            int posRank = availableElements.rsq(pos);
            if (digit < posRank) {
                chosen = availableElements.findKth(digit + 1);
            } else {
                chosen = availableElements.findKth(digit + 2);
            }
        } else {
            chosen = availableElements.findKth(digit + 1);
        }
        return chosen;
    }


    /**
     * Converts a derangement back to its Derangadic digit representation.
     * <p>
     * Computes digits directly in a single O(n) pass by simulating the encoding process.
     * Returns the minimal-length array with trailing zeros trimmed.
     * <p>
     * <b>Optimization:</b> Does NOT compute rank first or delegate to toDerangadic.
     * Generates digits on-the-fly using identical candidate selection & dead-end logic.
     * <p>
     * <b>Complexity:</b> O(n²) due to dynamic restricted count recomputation.
     *
     * @param derangement derangement array of length {@code n}
     * @param n           order (must equal {@code derangement.length})
     * @return Derangadic digit array of minimal length (LSD-first)
     * @throws IllegalArgumentException if {@code derangement} is not a valid derangement
     */
    public int[] fromDerangement(int[] derangement, int n) {
        // Compute full digits (length n)
        int[] fullDigits = new int[n];
        List<Integer> remainingElements = new ArrayList<>();
        for (int i = 0; i < n; i++) remainingElements.add(i);
        List<Integer> remainingPositions = new ArrayList<>();
        for (int i = 0; i < n; i++) remainingPositions.add(i);

        for (int step = 0; step < n; step++) {
            int pos = remainingPositions.get(0);

            List<Integer> legal = new ArrayList<>();
            for (int e : remainingElements) {
                if (e != pos) legal.add(e);
            }
            Collections.sort(legal);

            int chosen = derangement[pos];
            int digit = legal.indexOf(chosen);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid derangement at position " + pos);
            }
            fullDigits[n - 1 - step] = digit;

            remainingElements.remove(Integer.valueOf(chosen));
            remainingPositions.remove(0);
        }

        // Trim trailing zeros
        int lastNonZero = fullDigits.length - 1;
        while (lastNonZero >= 0 && fullDigits[lastNonZero] == 0) {
            lastNonZero--;
        }

        if (lastNonZero < 0) {
            return new int[]{0};
        }

        int[] result = new int[lastNonZero + 1];
        System.arraycopy(fullDigits, 0, result, 0, lastNonZero + 1);
        return result;
    }

    /**
     * @see #unrank(long, int)
     */
    public int[] unrank(BigInteger rank, int n) {
        int[] digits = toDerangadic(rank, n);
        return toDerangement(digits, n);
    }

    /**
     * Direct conversion: rank → derangement (without intermediate digits).
     * <p>
     * Equivalent to {@code toDerangement(toDerangadic(rank, n), n)}.
     *
     * @param rank the 0-based lexicographical rank ({@code 0 ≤ rank < !n})
     * @param n    order
     * @return derangement array of length {@code n}
     * @throws IllegalArgumentException if rank is out of range
     */


    public int[] unrank(long rank, int n) {
        int[] digits = toDerangadic(rank, n);
        return toDerangement(digits, n);
    }

    /**
     * Direct conversion: derangement → rank (without intermediate digits).
     * <p>
     * Equivalent to {@code fromDerangadic(fromDerangement(derangement, n), n)}.
     *
     * @param derangement derangement array of length {@code n}
     * @param n           order
     * @return the 0-based lexicographical rank
     * @throws IllegalArgumentException if derangement is not a valid derangement
     */
    public BigInteger rank(int[] derangement, int n) {
        int[] digits = fromDerangement(derangement, n);
        return fromDerangadic(digits, n);
    }

    private int smallestN(int n, BigInteger m) {
        //Finds the smallest actualN with the same parity as n such thatD[actualN] > m
        int actualN = n;
        while (actualN > 2 && calculator.subFactorial(actualN - 2).compareTo(m) > 0) {
            actualN -= 2;
        }
        return actualN;
    }

    /**
     *  A list view that pads zeros at the end for indices beyond the backing array.
     */
    private record ZeroPaddedList(int[] digits, int n) {

    public int get(int index) {
            if (index < 0 || index >= n) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            if (index < digits.length) {
                return digits[index];
            }
            return 0;
        }
    }
}
