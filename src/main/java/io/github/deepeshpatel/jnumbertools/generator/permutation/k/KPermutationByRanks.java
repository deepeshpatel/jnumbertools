/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.api.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Generates k-permutations at specified lexicographical ranks.
 * <p>
 * This class produces permutations of kₖ elements from a list of nₙ elements at specific ranks
 * (e.g., the 0ᵗʰ, 2ⁿᵈ permutations) provided by an iterable sequence of rank numbers. The total number
 * of k-permutations is Pₙ,ₖ = n!/(n−kₖ)!. It uses permutadic algorithms to map each rank to a permutation
 * of indices, which are then converted to element permutations. For example, for elements [A, B, C],
 * kₖ=2, and ranks [0, 2], it might generate [A, B], [A, C].
 * </p>
 * <p>
 * Ranks are 0-based in lexicographical order.
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public class KPermutationByRanks<T> extends AbstractKPermutation<T> {

    /**
     * The initial identity permutation indices [0, 1, ..., kₖ−1].
     */
    final int[] initialValue;

    /**
     * The total number of k-permutations, Pₙ,ₖ = n!/(n−kₖ)!.
     */
    final BigInteger nPk;

    /**
     * The iterable sequence of rank numbers to generate permutations.
     */
    final Iterable<BigInteger> ranks;

    /**
     * Constructs an instance for generating k-permutations at specified ranks.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder#byRanks(Iterable)}.
     * All parameter validation (null check, 0 ≤ k ≤ n) is handled by the builder.
     * Rank validation (non-negative, less than total) is deferred to iteration.
     * </p>
     *
     * @param elements the input list of elements to permute (assumed non-null)
     * @param k the size of each permutation (assumed 0 ≤ k ≤ elements.size())
     * @param ranks an iterable of 0-based rank numbers
     * @param calculator utility for combinatorial calculations (assumed non-null)
     */
    public KPermutationByRanks(List<T> elements, int k, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements, k);
        this.ranks = ranks;
        this.initialValue = IntStream.range(0, k).toArray();
        nPk = calculator.nPr(elements.size(), initialValue.length);
    }

    /**
     * Returns an iterator over k-permutations at the specified ranks.
     *
     * @return an iterator producing each permutation as a {@code List<T>}
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new Itr(ranks.iterator());
    }

    /**
     * Iterator for generating k-permutations at specified lexicographical ranks.
     */
    class Itr implements Iterator<List<T>> {

        private final Iterator<BigInteger> seqGenerator;

        /**
         * Constructs an iterator for the given sequence of ranks.
         *
         * @param seqGenerator the iterator providing rank numbers
         */
        public Itr(Iterator<BigInteger> seqGenerator) {
            this.seqGenerator = seqGenerator;
        }

        /**
         * Checks if there are more permutations to generate.
         *
         * @return {@code true} if the sequence has more ranks; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return seqGenerator.hasNext();
        }

        /**
         * Returns the next k-permutation for the current rank.
         * <p>
         * Uses permutadic un-ranking to convert the rank to a permutation of indices, then maps
         * the indices to the corresponding elements.
         * </p>
         *
         * @return the next permutation as a {@code List<T>}
         * @throws java.util.NoSuchElementException if no more ranks are available
         */
        @Override
        public List<T> next() {
            BigInteger rank = seqGenerator.next();

            if (rank.signum() < 0 || rank.compareTo(nPk) >= 0) {
                throw new IllegalArgumentException("Rank " + rank + " is out of bounds [0, " + nPk + ")");
            }

            int[] next = PermutadicAlgorithms.unRankWithoutBoundCheck(rank, elements.size(), k);
            return indicesToValues(next);
        }
    }
}