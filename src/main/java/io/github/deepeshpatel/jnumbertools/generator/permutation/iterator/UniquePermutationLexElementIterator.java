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
 * This iterator converts permutations represented by indices into permutations of the actual elements.
 * It is especially useful when you want to generate permutations without manually handling index arrays.
 * </p>
 *
 * @param <T> the type of elements to be permuted
 * @see UniquePermutationLexIndicesIterator
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermutationLexElementIterator<T> implements Iterator<List<T>> {

    private final IndicesToValueMapper<T> mapper;
    private final UniquePermutationLexIndicesIterator iterator;

    /**
     * Constructs a new {@code UniquePermutationLexElementIterator} instance with the specified number of elements and a mapper.
     *
     * @param size   the number of elements in the list for which permutations are to be generated
     * @param mapper the mapper that converts an array of indices to a list of corresponding element values
     */
    public UniquePermutationLexElementIterator(int size, IndicesToValueMapper<T> mapper) {
        this.mapper = mapper;
        this.iterator = new UniquePermutationLexIndicesIterator(size);
    }

    /**
     * Constructs a new {@code UniquePermutationLexElementIterator} instance with the specified mapper and initial state of indices.
     *
     * @param mapper                the mapper that converts an array of indices to a list of corresponding element values
     * @param initialStateOfIndices the initial state of indices to begin permutation generation
     */
    public UniquePermutationLexElementIterator(IndicesToValueMapper<T> mapper, int[] initialStateOfIndices) {
        this.mapper = mapper;
        this.iterator = new UniquePermutationLexIndicesIterator(initialStateOfIndices);
    }

    /**
     * Returns {@code true} if there are more permutations to generate.
     *
     * @return {@code true} if the iteration has more elements; {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next unique permutation of elements.
     *
     * @return the next permutation as a list of elements
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
         * Converts an array of indices to their corresponding values.
         *
         * @param indices the array of indices to convert
         * @return a list of values corresponding to the given indices
         */
        List<T> indicesToValues(int[] indices);
    }
}
