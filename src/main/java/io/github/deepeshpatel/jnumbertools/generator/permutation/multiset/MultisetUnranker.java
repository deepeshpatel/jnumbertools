/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

import java.math.BigInteger;

/**
 * Unranks multiset permutations; one instance per iterator (it reuses mutable scratch state).
 * <p>
 * With S items left and N = their number of distinct arrangements, the arrangements that start with type i
 * number N·cᵢ/S exactly, so each position is decided by finding the type whose cumulative count range holds the
 * rank. That search is the only part with two possible implementations:
 * </p>
 * <ul>
 *   <li>up to {@value #LINEAR_MAX_TYPES} types, when the whole count fits the {@code long} path: a linear scan
 *       over the types. It needs no extra structure; with random ranks it was measured faster than the Fenwick
 *       tree for 4 and 8 types; at 16 it was level or slower ({@code OptimisationCandidatesBenchmark});</li>
 *   <li>otherwise a Fenwick tree over the remaining counts, O(log types) per position. With {@code BigInteger}
 *       counts every scanned type costs a multiplication and a division, so the scan is about 6 times slower than
 *       the tree for 64 types and only level with it for 4.</li>
 * </ul>
 * Both give the same permutation for every rank.
 */
final class MultisetUnranker {

    /** Largest number of types searched linearly; above it the Fenwick tree is used. */
    static final int LINEAR_MAX_TYPES = 8;

    private final int[] frequencies;
    private final int totalLength;
    private final BigInteger total;
    private final long longTotal;     // total as a long when every product fits (see totalForLongArithmetic), else -1
    private final int[] remaining;    // scratch: remaining count of every type
    private final FenwickTree tree;   // over remaining; null in linear mode

    MultisetUnranker(int[] frequencies, int totalLength, BigInteger total, long longTotal) {
        this.frequencies = frequencies;
        this.totalLength = totalLength;
        this.total = total;
        this.longTotal = longTotal;
        this.remaining = frequencies.clone();
        if (frequencies.length > LINEAR_MAX_TYPES || longTotal < 0) {
            tree = new FenwickTree(frequencies.length);
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i] > 0) tree.update(i + 1, frequencies[i]);
            }
        } else {
            tree = null;
        }
    }

    /** Type index at every position of the permutation with the given rank (0 ≤ m &lt; total, not checked). */
    int[] unrank(BigInteger m) {
        if (tree == null) {
            System.arraycopy(frequencies, 0, remaining, 0, frequencies.length);
            return linearLong(m.longValue());
        }
        int[] output = longTotal >= 0 ? fenwickLong(m.longValue()) : fenwickBig(m);
        // Put the consumed items back so the next rank starts from the full multiset.
        for (int index : output) {
            remaining[index]++;
            tree.update(index + 1, 1);
        }
        return output;
    }

    /** Every product is at most total x totalLength, which fits in a long when longTotal ≥ 0. */
    private int[] linearLong(long m) {
        int[] output = new int[totalLength];
        long sum = totalLength;
        long totalPossible = longTotal;
        for (int j = 0; j < output.length; j++) {
            long permutationsBefore = 0;
            for (int i = 0; i < remaining.length; i++) {
                if (remaining[i] > 0) {
                    long perms = totalPossible * remaining[i] / sum;
                    long rangeEnd = permutationsBefore + perms;
                    if (m < rangeEnd) {
                        output[j] = i;
                        m -= permutationsBefore;
                        remaining[i]--;
                        sum--;
                        totalPossible = perms;
                        break;
                    }
                    permutationsBefore = rangeEnd;
                }
            }
        }
        return output;
    }

    /**
     * The type for rank m is the one whose cumulative count first exceeds floor(m·S/N), found with a Fenwick
     * search instead of scanning every type.
     */
    private int[] fenwickLong(long m) {
        int[] output = new int[totalLength];
        long sum = totalLength;
        long totalPossible = longTotal;
        for (int j = 0; j < output.length; j++) {
            int target = (int) (m * sum / totalPossible) + 1;
            int position = tree.findKth(target); // 1-based type index
            int type = position - 1;

            long permutationsBefore = totalPossible * tree.rsq(position - 1) / sum;
            totalPossible = totalPossible * remaining[type] / sum;

            output[j] = type;
            m -= permutationsBefore;
            remaining[type]--;
            tree.update(position, -1);
            sum--;
        }
        return output;
    }

    private int[] fenwickBig(BigInteger m) {
        int[] output = new int[totalLength];
        long sum = totalLength;
        BigInteger totalPossible = total;
        for (int j = 0; j < output.length; j++) {
            BigInteger bigSum = BigInteger.valueOf(sum);
            int target = m.multiply(bigSum).divide(totalPossible).intValueExact() + 1;
            int position = tree.findKth(target); // 1-based type index
            int type = position - 1;

            BigInteger permutationsBefore = totalPossible
                    .multiply(BigInteger.valueOf(tree.rsq(position - 1)))
                    .divide(bigSum);
            totalPossible = totalPossible.multiply(BigInteger.valueOf(remaining[type])).divide(bigSum);

            output[j] = type;
            m = m.subtract(permutationsBefore);
            remaining[type]--;
            tree.update(position, -1);
            sum--;
        }
        return output;
    }
}
