/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.MthElementGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Implements an iterable that generates every nᵗʰ unique combination of size k.
 * <p>
 * Combinations are generated in lexicographic order based on the indices of the input values
 * (e.g. considering elements₀, elements₁, …, elementsₙ₋₁ as unique). This approach is crucial
 * when the total number of combinations is astronomically large. For example, to generate every
 * 100 trillionᵗʰ combination of 50 items out of 100 (i.e. 100Choose50), it is impractical to generate
 * all 1.008913445 × 10²⁹ combinations sequentially and then select the required one.
 * </p>
 * <p>
 * Instances of this class are intended to be created via a builder; hence, the constructor is package‑private.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniqueCombinationMth<T> extends AbstractGenerator<T> implements MthElementGenerator<T> {

    private final int r;
    private final BigInteger increment;
    private final BigInteger nCr;
    final CombinadicAlgorithms algorithms;
    private final BigInteger start;

    /**
     * Constructs a new UniqueCombinationMth instance with the specified parameters.
     *
     * @param elements  the list of N items (e.g. elements₀, elements₁, …, elementsₙ₋₁) from which combinations are generated
     * @param r         the size of each combination (r); r must be ≤ N for generating unique combinations
     * @param start     the starting rank (index) from which to generate combinations (e.g. 0ᵗʰ combination)
     * @param increment the step (increment) by which subsequent combinations are selected in lexicographic order
     * @param calculator the Calculator used for computing combination counts
     */
    UniqueCombinationMth(List<T> elements, int r, BigInteger start, BigInteger increment, Calculator calculator) {
        super(elements);
        this.r = r;
        this.start = start; // TODO: add check for start
        this.increment = increment;
        checkParamCombination(elements.size(), r, "nth unique combination");

        this.nCr = calculator.nCr(elements.size(), r);
        this.algorithms = new CombinadicAlgorithms(calculator);
    }

    /**
     * Returns the mᵗʰ unique combination (using combinadic unranking) without iterating over previous combinations.
     * <p>
     * Use this method if you need only the mᵗʰ combination rather than generating a series of combinations
     * (e.g. 0ᵗʰ, mᵗʰ, 2mᵗʰ, etc.), as creating an iterator is computationally expensive due to its costly
     * {@code hasNext()} implementation.
     * </p>
     *
     * @return the mᵗʰ combination as a list of elements
     */
    public List<T> build() {
        var result = algorithms.unRank(increment, nCr, elements.size(), r);
        return indicesToValues(result);
    }

    /**
     * Returns an iterator over unique combinations starting from the specified start rank.
     * <p>
     * The iterator generates combinations at intervals defined by {@code increment}.
     * </p>
     *
     * @return an iterator over lists of elements representing unique combinations
     */
    @Override
    public Iterator<List<T>> iterator() {
        AbstractGenerator.checkParamIncrement(increment, "Unique Combinations");
        return new Itr();
    }

    /**
     * Iterator implementation for generating unique combinations using combinadic unranking.
     */
    private class Itr implements Iterator<List<T>> {

        BigInteger rank = start;
        int[] result;

        private Itr() {
            result = IntStream.range(0, r).toArray();
        }

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if the current rank is less than the total number of combinations; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return rank.compareTo(nCr) < 0;
        }

        /**
         * Generates the next unique combination in the sequence.
         *
         * @return the next combination as a list of elements
         * @throws NoSuchElementException if no further combinations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            // TODO (priority low): For the iterator, consider optimizing to compute the next combination directly
            // without using a full unranking operation.
            // TODO (priority medium): Explore replacing combinadic unranking with an optimized method from the experiments package.
            result = algorithms.unRank(rank, nCr, elements.size(), r);
            rank = rank.add(increment);
            return indicesToValues(result);
        }
    }
}
