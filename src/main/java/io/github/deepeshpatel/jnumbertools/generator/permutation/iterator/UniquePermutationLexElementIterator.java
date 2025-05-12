/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.iterator;

import java.util.Iterator;
import java.util.List;

/**
 * An iterator for generating unique permutations of a list of elements.
 * <p>
 * This iterator generates all nₙ! unique permutations of nₙ elements in lexicographical order by
 * converting permutations of indices (produced by {@link UniquePermutationLexIndicesIterator}) into
 * permutations of the actual elements. It is especially useful for generating permutations without
 * manually handling index arrays.
 * </p>
 *
 * @param <T> the type of elements to be permuted
 * @see UniquePermutationLexIndicesIterator
 * @author Deepesh Patel
 */
public final class UniquePermutationLexElementIterator<T> implements Iterator<List<T>> {

    private final IndicesToValueMapper<T> mapper;
    private final UniquePermutationLexIndicesIterator iterator;

    /**
     * Constructs a new iterator for permutations of nₙ elements.
     * <p>
     * Initializes with the identity permutation of indices [0, 1, ..., nₙ−1], mapped to elements
     * using the provided mapper.
     * </p>
     *
     * @param size the number of elements to permute (nₙ); must be non-negative
     * @param mapper the mapper that converts an array of indices to a list of corresponding element values
     * @return a new UniquePermutationLexElementIterator instance
     * @throws IllegalArgumentException if size is negative
     */
    public UniquePermutationLexElementIterator(int size, IndicesToValueMapper<T> mapper) {
        this.mapper = mapper;
        this.iterator = new UniquePermutationLexIndicesIterator(size);
    }

    /**
     * Constructs a new iterator with a specified starting permutation.
     *
     * @param mapper the mapper that converts an array of indices to a list of corresponding element values
     * @param initialStateOfIndices the initial permutation of indices; must be a valid permutation of {0, 1, ..., nₙ−1}
     * @return a new UniquePermutationLexElementIterator instance
     * @throws IllegalArgumentException if initialStateOfIndices is invalid
     */
    public UniquePermutationLexElementIterator(IndicesToValueMapper<T> mapper, int[] initialStateOfIndices) {
        this.mapper = mapper;
        this.iterator = new UniquePermutationLexIndicesIterator(initialStateOfIndices);
    }

    /**
     * Checks if there are more permutations to generate.
     *
     * @return {@code true} if the iteration has more permutations; {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next unique permutation of elements.
     *
     * @return the next permutation as a list of elements
     * @throws java.util.NoSuchElementException if no more permutations are available
     */
    @Override
    public List<T> next() {
        return mapper.indicesToValues(iterator.next());
    }

    /**
     * A functional interface for mapping an array of indices to a list of values.
     *
     * @param <T> the type of elements to be mapped
     */
    public interface IndicesToValueMapper<T> {
        /**
         * Converts an array of indices to their corresponding element values for a permutation.
         *
         * @param indices the array of indices to convert
         * @return a list of elements corresponding to the given indices
         */
        List<T> indicesToValues(int[] indices);
    }
}