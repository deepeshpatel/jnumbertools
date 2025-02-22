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
 * An iterable that generates every mᵗʰ unique permutation of size k.
 * <p>
 * This class generates unique permutations of a subset of size {@code k} from an input list (e.g. elements₀, elements₁, …, elementsₙ₋₁)
 * at regular intervals specified by the {@code increment} parameter. The overall rank of the permutation to be generated is
 * decomposed into two parts:
 * <ol>
 *   <li>
 *     <strong>Combination rank:</strong> obtained by dividing the overall rank by k! (the number of permutations per combination).
 *     This rank is used to select the specific combination (in lexicographical order) from the list.
 *   </li>
 *   <li>
 *     <strong>Permutation rank:</strong> the remainder from the division, which determines the specific permutation
 *     within the chosen combination.
 *   </li>
 * </ol>
 * For example, if the {@code increment} is set to 2, the iterator will generate the 0ᵗʰ, 2ⁿᵈ, 4ᵗʰ, … permutations.
 * <br>
 * Instances of this class are intended to be created via a builder and therefore do not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the permutation
 *
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
     * Constructs a new KPermutationCombinationOrderMth instance that generates every mᵗʰ unique permutation of size k.
     * <p>
     * Permutations are generated in lexicographical order based on combinations of indices of the input values,
     * treating each element as unique by its index.
     * </p>
     *
     * @param elements  the input list of n elements (e.g. elements₀, elements₁, …, elementsₙ₋₁) from which permutations of size k are generated
     * @param k         the size of the permutations; k must be ≤ n
     * @param increment the step size relative to the first permutation; for example, if increment is 2, the iterator generates the 0ᵗʰ, 2ⁿᵈ, 4ᵗʰ, … permutations
     *                  in lexicographical order. This parameter is critical when k is large, as generating all k! permutations is impractical.
     * @param start     the starting rank (position) from which permutation generation begins
     * @param calculator a Calculator used for combinatorial computations (e.g. computing nPr and factorial values)
     * @throws IllegalArgumentException if the increment is not valid (checked via {@link AbstractGenerator#checkParamIncrement})
     */
    KPermutationCombinationOrderMth(List<T> elements, int k, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements, k);
        AbstractGenerator.checkParamIncrement(increment, "mth K-Permutation");
        this.start = start;
        this.increment = increment;
        this.totalPermutations = calculator.nPr(elements.size(), k);
        this.permutationsPerList = calculator.factorial(k).longValue();
        this.calculator = calculator;
    }

    /**
     * Returns an iterator over unique k‑permutations generated at intervals specified by the increment.
     *
     * @return an {@code Iterator} over lists representing the unique k‑permutations, or an empty iterator if k is 0 or the input is empty
     */
    @Override
    public Iterator<List<T>> iterator() {
        return (k == 0 || elements.isEmpty()) ? Util.emptyListIterator() : new Itr();
    }

    /**
     * Iterator implementation for generating every mᵗʰ unique permutation of size k.
     */
    private class Itr implements Iterator<List<T>> {

        BigInteger currentIncrement = start;

        public Itr() {
        }

        /**
         * Checks if there are more permutations to generate.
         *
         * @return {@code true} if the current rank is less than the total number of permutations; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return currentIncrement.compareTo(totalPermutations) < 0;
        }

        /**
         * Returns the next unique permutation.
         * <p>
         * The overall rank ({@code currentIncrement}) is divided by the number of permutations per combination (k!):
         * <ul>
         *   <li>The quotient determines the combination rank, used to select the specific combination (via lexicographical order).</li>
         *   <li>The remainder determines the permutation rank within that combination.</li>
         * </ul>
         * After computing the next permutation, the current rank is incremented by {@code increment}.
         * </p>
         *
         * @return the next permutation as a list of elements
         * @throws NoSuchElementException if there are no further permutations available
         */
        @Override
        public List<T> next() {
            BigInteger[] divideAndRemainder = currentIncrement.divideAndRemainder(BigInteger.valueOf(permutationsPerList));
            BigInteger combinationListNumber = divideAndRemainder[0];
            long permutationIncrement = divideAndRemainder[1].longValue();
            currentIncrement = currentIncrement.add(increment);

            List<T> nextCombination = combinationListNumber.equals(BigInteger.ZERO)
                    ? new Combinations(calculator).unique(k, elements).lexOrder().iterator().next()
                    : new Combinations(calculator).unique(k, elements).lexOrderMth(combinationListNumber, BigInteger.ZERO).build();

            return permutationIncrement == 0
                    ? new Permutations(calculator).unique(nextCombination).lexOrder().iterator().next()
                    : new Permutations(calculator).unique(nextCombination).lexOrderMth(permutationIncrement, 0).build();
        }
    }
}
