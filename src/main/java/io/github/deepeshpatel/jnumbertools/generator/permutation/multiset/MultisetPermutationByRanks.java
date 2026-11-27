/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;

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
    private final long longTotal;   // total permutations when the long path applies, else -1

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
        this.longTotal = totalForLongArithmetic(totalPermutations);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    private class SequenceIterator implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;
        private final MultisetUnranker unranker = new MultisetUnranker(frequencies, totalLength, totalPermutations, longTotal);
        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
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
            return indicesToValues(unranker.unrank(rank));
        }
    }
}
