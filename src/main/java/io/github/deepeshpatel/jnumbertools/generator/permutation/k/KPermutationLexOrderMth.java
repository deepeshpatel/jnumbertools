/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Generates an iterable of every nᵗʰ unique permutation of size {@code k}.
 * <p>
 * The permutations are generated in lexicographical order based on combinations of indices from the input values,
 * with each value at a specific index (e.g. elements₀, elements₁, …, elementsₙ₋₁) treated as unique. This class
 * allows for efficient generation of permutations by skipping directly to every nᵗʰ permutation rather than
 * generating all possible permutations.
 * </p>
 * <p>
 * Instances of this class are intended to be created via a builder and therefore do not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the input list
 * @since 1.0.3
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationLexOrderMth<T> extends AbstractKPermutation<T> {

    final int[] initialValue;
    final BigInteger nPk;
    final BigInteger increment;
    final BigInteger start;

    /**
     * Constructs a new {@code KPermutationLexOrderMth} instance.
     * <p>
     * This constructor sets up an iterable that generates every nᵗʰ unique permutation of size {@code k},
     * starting from the specified {@code start} permutation and incrementing by the specified {@code increment}.
     * </p>
     *
     * @param elements   the list of elements from which permutations of size {@code k} will be generated
     *                   (e.g. elements₀, elements₁, …, elementsₙ₋₁)
     * @param k          the size of the permutations; must be ≤ {@code elements.size()}
     * @param increment  the increment value specifying the position relative to the first permutation
     *                   which will be generated next in lexicographical order. For example, if increment
     *                   is set to 2, the output will generate the 0ᵗʰ, 2ⁿᵈ, 4ᵗʰ, etc., permutations.
     *                   This allows for efficient skipping of permutations, which is important for large {@code k} values.
     * @param start      the starting permutation index from which permutation generation begins
     * @param calculator a {@link Calculator} instance for computing permutations
     * @throws IllegalArgumentException if {@code increment} is less than or equal to zero
     */
    KPermutationLexOrderMth(List<T> elements, int k, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements, k);
        AbstractGenerator.checkParamIncrement(increment, "K-Permutation");
        this.start = start;
        this.increment = increment;
        this.initialValue = IntStream.range(0, k).toArray();
        this.nPk = calculator.nPr(elements.size(), initialValue.length);
    }

    /**
     * Returns an iterator over every nᵗʰ unique permutation of size {@code k}.
     * <p>
     * If {@code k == 0}, an empty iterator is returned. Otherwise, the returned iterator generates
     * permutations by computing the nᵗʰ permutation (using a combinadic unranking algorithm) at each step.
     * </p>
     *
     * @return an iterator for generating every nᵗʰ permutation of size {@code k}
     */
    @Override
    public Iterator<List<T>> iterator() {
        return k == 0 ? Util.emptyListIterator() : new Itr();
    }

    /**
     * Inner class implementing the {@code Iterator<List<T>>} interface to generate every nᵗʰ permutation.
     */
    private class Itr implements Iterator<List<T>> {

        int[] next = initialValue;
        BigInteger n = start;

        /**
         * Constructs a new {@code Itr} instance for permutation generation.
         */
        public Itr() {}

        /**
         * Returns {@code true} if there are more permutations to generate.
         *
         * @return {@code true} if the current rank is less than the total number of permutations; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return n.compareTo(nPk) < 0;
        }

        /**
         * Returns the next nᵗʰ permutation in the iteration.
         * <p>
         * The overall rank {@code n} is used to compute the corresponding permutation by applying a combinadic
         * unranking algorithm. After generating the permutation, the rank is incremented by the specified {@code increment}.
         * </p>
         *
         * @return the next permutation as a {@code List<T>}
         * @throws NoSuchElementException if no further permutations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            next = nextMthKPermutation(n, initialValue.length, elements.size());
            n = n.add(increment);
            return indicesToValues(next);
        }

        /**
         * Computes the permutation corresponding to the given {@code rank} (i.e., the rankᵗʰ permutation)
         * for permutations of a given {@code degree} from {@code size} elements.
         *
         * @param rank   the rank of the permutation to compute
         * @param degree the size of the permutation
         * @param size   the total number of elements in the input list
         * @return the rankᵗʰ permutation as an array of indices
         */
        private int[] nextMthKPermutation(BigInteger rank, int degree, int size) {
            return PermutadicAlgorithms.unRankWithoutBoundCheck(rank, size, degree);
        }
    }
}
