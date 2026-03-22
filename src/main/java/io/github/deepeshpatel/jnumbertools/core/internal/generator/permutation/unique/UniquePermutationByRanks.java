/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.core.external.Calculator;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.core.internal.numbersystem.FactoradicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates unique permutations of a list of elements based on a sequence of ranks.
 * <p>
 * This class produces all n! permutations of the input list, treating each element as distinct by its
 * position (e.g., elements₀, elements₁, ...). Permutations are determined by ranks from the provided
 * {@code Iterable<BigInteger>}, mapped using factoradic unranking for lexicographical order or custom
 * iterators for other strategies (e.g., Heap's single-swap). For example, for [A, B, C], ranks [0, 1, 2]
 * yield [A, B, C], [A, C, B], [B, A, C]. Supports lexicographical order (all or mᵗʰ), single-swap,
 * and sampling with/without replacement.
 * </p>
 * <p>
 * <strong>This class is intended for internal use via {@link UniquePermutationBuilder}.</strong>
 * All parameter validation (non-negative ranks, ranks < n!, etc.) is performed in the builder methods
 * ({@code lexOrderMth}, {@code byRanks}, {@code choice}, {@code sample}) to ensure fail-fast behavior.
 * Direct instantiation of this class bypasses validation and is not part of the public API.
 * </p>
 * <p>
 * Ranks are 0-based in lexicographical order.
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 * @see UniquePermutationBuilder
 */
public final class UniquePermutationByRanks<T> extends AbstractGenerator<T> {

    private final BigInteger totalPermutations;
    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a generator for unique permutations based on a rank sequence.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link UniquePermutationBuilder#byRanks(Iterable)}.
     * All parameter validation (null checks) is handled by the builder.
     * Rank validation (non-negative, less than n!) is deferred to iteration.
     * </p>
     *
     * @param elements   the list of elements to permute (assumed non-null)
     * @param ranks      the sequence of ranks to generate permutations
     * @param calculator utility for computing factorials and permutations
     */
    public UniquePermutationByRanks(List<T> elements, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.ranks = ranks;
        this.totalPermutations = calculator.factorial(elements.size());
    }

    /**
     * Returns an iterator over permutations specified by the rank sequence.
     * @return an iterator over lists representing permutations; empty if the input list is empty
     */
    @Override
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? emptyListIterator() : new SequenceIterator();
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
            if (rank.signum() < 0) {
                throw new IllegalArgumentException("Rank " + rank + " cannot be negative. Valid range is [0, " + totalPermutations + ")");
            }
            if (rank.compareTo(totalPermutations) >= 0) {
                throw new IllegalArgumentException("Rank " + rank + " exceeds total permutations " + totalPermutations);
            }
            int[] indices = FactoradicAlgorithms.unRank(rank, elements.size());
            return indicesToValues(indices);
        }
    }
}
