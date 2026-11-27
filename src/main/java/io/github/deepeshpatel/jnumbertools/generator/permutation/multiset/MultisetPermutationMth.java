/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.*;

/**
 * Generates every mᵗʰ multiset permutation in lexicographical order, starting from a specified rank.
 * <p>
 * This class produces permutations of a multiset with specified multiplicities, returning each permutation as a list of elements,
 * in lexicographical order, selecting ranks {@code start}, {@code start + increment},
 * {@code start + 2 * increment}, and so on, while the rank remains below the total count.
 * </p>
 * <p>
 * <strong>Total available permutations:</strong>
 * {@code N = n!/(n₁!·n₂!·...·nₖ!)}, where nᵢ are the multiplicities.
 * The number emitted is {@code 0} when {@code start >= N}; otherwise it is
 * {@code floor((N - 1 - start) / increment) + 1}.
 * </p>
 * <p>
 * <strong>Performance:</strong>
 * <ul>
 *   <li>Time Complexity: O(L × T) per permutation in the worst case, excluding the cost of
 *       {@link BigInteger} arithmetic, where L is the total number of elements and T is the
 *       number of distinct element types</li>
 *   <li>Space Complexity: O(L + T), for the output/index arrays and frequency counts</li>
 *   <li>Memory Efficient: Generates permutations on-demand</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Example:</strong> For multiset {A:2, B:1, C:1} with m=2, start=0:
 * Generates 0th, 2nd, 4th... permutations instead of all 12 permutations.
 * </p>
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 * @see <a href="https://en.wikipedia.org/wiki/Multinomial_coefficient">Wikipedia: Multinomial Coefficient</a>
 */
public final class MultisetPermutationMth<T> extends AbstractMultisetPermutation<T> {

    private final BigInteger possiblePermutations;
    private final long longTotal;   // total permutations when the long path applies, else -1
    private final BigInteger start;
    private final BigInteger increment;

    /**
     * Constructs a generator for every mᵗʰ multiset permutation.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.permutation.multiset.MultisetPermutationBuilder#lexOrderMth(BigInteger, BigInteger)}.
     * All parameter validation (null checks, increment > 0, start ≥ 0, start < total when count > 0)
     * is handled by the builder.
     * </p>
     *
     * @param multiset   the multiset with element multiplicities (assumes zero frequencies filtered, map non-null)
     * @param increment  the step size between permutations (assumed > 0)
     * @param start      the starting rank (assumed ≥ 0)
     * @param calculator the calculator for combinatorial computations (assumed non-null)
     */
    MultisetPermutationMth(LinkedHashMap<T, Integer> multiset, BigInteger increment, BigInteger start, Calculator calculator) {
        super(multiset, calculator);
        if (start.signum() < 0) {
            throw new IllegalArgumentException("Start must be non-negative: " + start);
        }
        this.possiblePermutations = getTotalPermutations();
        this.longTotal = totalForLongArithmetic(possiblePermutations);
        this.start = start;
        this.increment = increment;
    }

    @Override
    public Iterator<List<T>> iterator() {
        // Handle empty map case: 0! = 1 -> returns [[]]
        if (elements.isEmpty()) {
            return Util.emptyListIterator();
        }
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {
        private BigInteger currentN = start;
        private final MultisetUnranker unranker = new MultisetUnranker(frequencies, totalLength, possiblePermutations, longTotal);
        @Override
        public boolean hasNext() {
            return currentN.compareTo(possiblePermutations) < 0;
        }
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available");
            }
            int[] indices = unranker.unrank(currentN);
            currentN = currentN.add(increment);
            return indicesToValues(indices);
        }
    }
}
