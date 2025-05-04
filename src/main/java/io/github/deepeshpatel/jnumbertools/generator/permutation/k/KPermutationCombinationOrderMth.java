/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates every mᵗʰ unique k-permutation in combination order.
 * <p>
 * This class produces permutations of a subset of size {@code k} from an input list (e.g., [A, B, C]),
 * skipping to every mᵗʰ permutation based on the specified {@code increment}. Permutations are ordered
 * by combinations in lexicographical order, then by permutations within each combination. The rank of
 * each permutation is decomposed into:
 * <ul>
 *   <li><strong>Combination rank</strong>: rank ÷ k!, selecting the combination.</li>
 *   <li><strong>Permutation rank</strong>: rank mod k!, selecting the permutation within the combination.</li>
 * </ul>
 * For example, for [A, B, C], k=2, increment=2, start=0, it might generate [A, B], [A, C], [B, C], skipping
 * every other permutation (e.g., omitting [B, A], [C, A], [C, B]).
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationCombinationOrderMth<T> extends AbstractKPermutation<T> {

    final BigInteger totalPermutations;
    final long permutationsPerList;
    final BigInteger increment;
    final BigInteger start;
    private final Calculator calculator;

    /**
     * Constructs an instance for generating every mᵗʰ k-permutation in combination order.
     *
     * @param elements   the input list of elements (e.g., [A, B, C]) to permute
     * @param k          the size of each permutation; must be between 0 and elements.size()
     * @param increment  the step size between permutations (e.g., 2 for every 2nd); must be positive
     * @param start      the starting rank (0-based); must be non-negative
     * @param calculator utility for combinatorial calculations
     * @throws IllegalArgumentException if increment is non-positive or k/start bounds are invalid
     */
    KPermutationCombinationOrderMth(List<T> elements, int k, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements, k);
        if (increment.signum() <= 0) {
            throw new IllegalArgumentException("Increment value must be > 0 to generate every mth K-Permutation");
        }
        this.start = start;
        this.increment = increment;
        this.totalPermutations = calculator.nPr(elements.size(), k);
        this.permutationsPerList = calculator.factorial(k).longValue();
        this.calculator = calculator;
    }

    /**
     * Returns an iterator over every mᵗʰ k-permutation in combination order.
     * <p>
     * If k=0 or the input list is empty, returns an empty iterator. Otherwise, iterates through
     * permutations at the specified increment.
     * </p>
     *
     * @return an iterator over lists representing k-permutations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return (k == 0 || elements.isEmpty()) ? Util.emptyListIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {
        private BigInteger currentIncrement = start;

        /**
         * Constructs an iterator starting at the specified rank.
         */
        public Itr() {
        }

        /**
         * Checks if there are more permutations to generate.
         *
         * @return {@code true} if the current rank is less than the total permutations; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return currentIncrement.compareTo(totalPermutations) < 0;
        }

        /**
         * Returns the next mᵗʰ k-permutation.
         * <p>
         * Decomposes the current rank into a combination rank (quotient of rank ÷ k!) and permutation rank
         * (remainder), retrieves the corresponding combination, and applies the permutation rank within it.
         * Advances the rank by {@code increment} for the next iteration.
         * </p>
         *
         * @return the next permutation as a list of elements
         * @throws NoSuchElementException if no further permutations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available");
            }
            BigInteger[] divideAndRemainder = currentIncrement.divideAndRemainder(BigInteger.valueOf(permutationsPerList));
            BigInteger combinationListNumber = divideAndRemainder[0];
            long permutationIncrement = divideAndRemainder[1].longValue();
            currentIncrement = currentIncrement.add(increment);

            List<T> nextCombination = combinationListNumber.equals(BigInteger.ZERO)
                    ? new Combinations(calculator).unique(k, elements).lexOrder().iterator().next()
                    : new Combinations(calculator).unique(k, elements)
                    .lexOrderMth(combinationListNumber, combinationListNumber).iterator().next();

            return permutationIncrement == 0
                    ? new Permutations(calculator).unique(nextCombination).lexOrder().iterator().next()
                    : new Permutations(calculator).unique(nextCombination)
                    .lexOrderMth(permutationIncrement, permutationIncrement).iterator().next();
        }
    }
}