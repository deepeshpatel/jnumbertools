/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates repetitive permutations of a specified width from a list of elements based on a sequence of ranks.
 * <p>
 * This class produces permutations of length {@code width} from the input list, allowing elements to repeat
 * (e.g., for [A, B], width=2: [A, A], [A, B], [B, A], [B, B]). Permutations are determined by ranks from
 * the provided {@code Iterable<BigInteger>}, mapped using base-n unranking (n = number of elements).
 * Total permutations are nᵂ where w is the width. Supports lexicographical order (all or mᵗʰ), sampling with/without replacement,
 * and custom rank sequences.
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public final class RepetitivePermutationOfRanks<T> extends AbstractGenerator<T> {

    private final int width;
    private final BigInteger totalPermutations;
    private final Iterable<BigInteger> ranks;
    private final Calculator calculator;

    /**
     * Constructs a generator for repetitive permutations based on a rank sequence.
     *
     * @param elements   the list of distinct elements to permute; must not be null or empty
     * @param width      the length of each permutation; must be non-negative
     * @param ranks      the sequence of ranks to generate permutations; each rank must be in [0, nᵂ)
     * @param calculator utility for computing powers and permutations
     * @throws IllegalArgumentException if width is negative, elements is null/empty, or ranks are invalid
     */
    public RepetitivePermutationOfRanks(List<T> elements, int width, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative: " + width);
        }

        this.width = width;
        this.ranks = ranks;
        this.calculator = calculator;
        this.totalPermutations = calculator.power(elements.size(), width);
    }

    /**
     * Returns an iterator over repetitive permutations specified by the rank sequence.
     *
     * @return an iterator over lists representing permutations; empty if width is 0
     */
    @Override
    public Iterator<List<T>> iterator() {
        return width == 0 ? Util.emptyListIterator() : new SequenceIterator();
    }

    private class SequenceIterator implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;

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
            if (rank.compareTo(totalPermutations) >= 0) {
                throw new IllegalArgumentException("Rank " + rank + " exceeds total permutations " + totalPermutations);
            }
            int[] indices = unrankRepetitivePermutation(rank, elements.size(), width);
            return indicesToValues(indices);
        }

        /**
         * Unranks a repetitive permutation using base-n conversion.
         * <p>
         * Converts the rank from base-10 to base-n (where n is the number of elements), mapping each
         * digit to an index in the elements list to form a permutation of length width.
         * </p>
         *
         * @param rank   the rank of the permutation (0 to nᵂ - 1)
         * @param n      the number of distinct elements
         * @param width  the length of the permutation
         * @return an array of indices representing the permutation
         */
        private int[] unrankRepetitivePermutation(BigInteger rank, int n, int width) {
            int[] indices = new int[width];
            BigInteger remaining = rank;
            for (int i = width - 1; i >= 0; i--) {
                BigInteger divisor = calculator.power(n, i);
                indices[width - 1 - i] = remaining.divide(divisor).intValue();
                remaining = remaining.mod(divisor);
            }
            return indices;
        }
    }
}