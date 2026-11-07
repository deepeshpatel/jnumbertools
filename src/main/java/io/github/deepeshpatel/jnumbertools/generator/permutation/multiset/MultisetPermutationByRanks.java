/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

import java.math.BigInteger;
import java.util.*;

/**
 * Generates multiset permutations at specific lexicographical rank positions.
 * <p>
 * Given a multiset with element frequencies (f₁, f₂, ..., fₖ), this class efficiently computes
 * permutations at specified ranks without generating all preceding permutations.
 * The total number of permutations is n!/(f₁! × f₂! × ... × fₖ!) where n = ∑fᵢ.
 * </p>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // Generate permutations at ranks 0, 2 for multiset [A, A, B]
 * LinkedHashMap<String, Integer> multiset = new LinkedHashMap<>();
 * multiset.put("A", 2);
 * multiset.put("B", 1);
 *
 * Calculator calculator = new Calculator();
 * new MultisetPermutationBuilder<>(multiset, calculator)
 *     .byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(2)))
 *     .forEach(System.out::println);
 *
 * // Output:
 * // [A, A, B] (rank 0)
 * // [A, B, A] (rank 2)
 * }</pre>
 *
 * <p>
 * Ranks are 0-based in lexicographical order.
 * </p>
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 * @see MultisetPermutation
 * @see MultisetPermutationBuilder
 */
public final class MultisetPermutationByRanks<T> extends AbstractMultisetPermutation<T> {

    private final Iterable<BigInteger> ranks;
    private final BigInteger totalPermutations;

    /**
     * Constructs a multiset permutation generator for specific ranks.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.permutation.multiset.MultisetPermutationBuilder#byRanks(Iterable)}.
     * All parameter validation (null checks, frequency filtering) is handled by the builder.
     * Rank validation (non-negative, less than total) is deferred to iteration.
     * </p>
     *
     * @param multiset   the multiset with element frequencies (assumes zero frequencies filtered, map non-null)
     * @param ranks      the sequence of 0-based rank positions to generate
     * @param calculator the calculator for combinatorial operations (assumed non-null)
     */
    public MultisetPermutationByRanks(LinkedHashMap<T, Integer> multiset, Iterable<BigInteger> ranks, Calculator calculator) {
        super(multiset, calculator);
        this.ranks = ranks;
        this.totalPermutations = getTotalPermutations();
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    private class SequenceIterator implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;

        /** Remaining count of every element type; rebuilt to the full multiset after each rank. */
        private final int[] remaining = Arrays.copyOf(frequencies, frequencies.length);

        /** Fenwick tree over {@link #remaining}, so the type chosen at a position is found in O(log types). */
        private final FenwickTree remainingTree = new FenwickTree(Math.max(1, frequencies.length));

        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i] > 0) {
                    remainingTree.update(i + 1, frequencies[i]);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available in rank sequence");
            }
            BigInteger rank = rankIterator.next();
            if (rank.signum() < 0 || rank.compareTo(totalPermutations) >= 0) {
                throw new IllegalArgumentException("Rank " + rank + " is out of bounds [0, " + totalPermutations + ")");
            }
            return indicesToValues(indicesForRank(rank));
        }

        /**
         * Unranks position by position. With S items left and N = their number of distinct arrangements, the
         * arrangements that start with type i number N * c_i / S exactly, so the arrangements starting with a type
         * of index below i number N * P_i / S where P_i is the sum of the counts below i. The type for the
         * current rank m is therefore the one whose cumulative count first exceeds floor(m * S / N), found with a
         * Fenwick search instead of scanning every type.
         */
        private int[] indicesForRank(BigInteger m) {
            int[] output = new int[totalLength];
            long sum = totalLength;
            BigInteger totalPossible = totalPermutations;

            for (int j = 0; j < output.length; j++) {
                BigInteger bigSum = BigInteger.valueOf(sum);
                int target = m.multiply(bigSum).divide(totalPossible).intValueExact() + 1;
                int position = remainingTree.findKth(target); // 1-based type index
                int type = position - 1;

                BigInteger permutationsBefore = totalPossible
                        .multiply(BigInteger.valueOf(remainingTree.rsq(position - 1)))
                        .divide(bigSum);
                totalPossible = totalPossible.multiply(BigInteger.valueOf(remaining[type])).divide(bigSum);

                output[j] = type;
                m = m.subtract(permutationsBefore);
                remaining[type]--;
                remainingTree.update(position, -1);
                sum--;
            }

            // Put the consumed items back so the next rank starts from the full multiset.
            for (int index : output) {
                remaining[index]++;
                remainingTree.update(index + 1, 1);
            }
            return output;
        }
    }
}
