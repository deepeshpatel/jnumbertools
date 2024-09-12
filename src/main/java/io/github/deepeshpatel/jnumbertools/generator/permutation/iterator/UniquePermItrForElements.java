/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.iterator;

import java.util.Iterator;
import java.util.List;

/**
 * An iterator for generating unique permutations of a list of elements.
 *
 * <p>
 * This iterator converts permutations represented by indices into permutations of actual elements.
 * It is useful for generating permutations of elements without having to manually handle the indices.
 * </p>
 *
 * @param <T> The type of elements to be permuted.
 * @see UniquePermItrForIndices
 */
public final class UniquePermItrForElements<T> implements Iterator<List<T>> {

    private final List<T> elements;
    private final IndicesToValueMapper<T> mapper;
    private final UniquePermItrForIndices iterator;

    /**
     * Constructs a new {@code UniquePermItrForElements} with the specified elements and mapper.
     *
     * @param elements The list of elements to generate permutations of.
     * @param mapper The mapper that converts indices to values.
     */
    public UniquePermItrForElements(List<T> elements, IndicesToValueMapper<T> mapper) {
        this.elements = elements;
        this.mapper = mapper;
        iterator = new UniquePermItrForIndices(elements.size());
    }

    /**
     * Constructs a new {@code UniquePermItrForElements} with the specified elements, mapper, and initial state of indices.
     *
     * @param elements The list of elements to generate permutations of.
     * @param mapper The mapper that converts indices to values.
     * @param initialStateOfIndices The initial state of indices for the iterator.
     */
    public UniquePermItrForElements(List<T> elements, IndicesToValueMapper<T> mapper, int[] initialStateOfIndices) {
        this.elements = elements;
        this.mapper = mapper;
        iterator = new UniquePermItrForIndices(initialStateOfIndices);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iteration has more elements; {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next permutation of elements.
     *
     * @return The next permutation as a list of elements.
     */
    @Override
    public List<T> next() {
        return mapper.indicesToValues(iterator.next());
    }

    /**
     * A functional interface for mapping indices to a list of values.
     *
     * @param <T> The type of elements to be mapped.
     */
    public interface IndicesToValueMapper<T> {
        /**
         * Converts an array of indices to their corresponding values.
         *
         * @param indices The array of indices to convert.
         * @return A list of values corresponding to the given indices.
         */
        List<T> indicesToValues(int[] indices);
    }
}
