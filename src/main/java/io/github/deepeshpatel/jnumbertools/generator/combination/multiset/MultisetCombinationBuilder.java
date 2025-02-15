/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.List;

/**
 * A builder class for generating combinations from a multiset, where elements may be repeated.
 * <p>
 * This class facilitates the construction of a {@link MultisetCombination} instance using the provided list of elements,
 * corresponding frequencies, and the desired combination size.
 * </p>
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class MultisetCombinationBuilder<T> {

    private final List<T> elements;
    private final int[] frequencies;
    private final int size;

    /**
     * Constructs a new {@code MultisetCombinationBuilder} with the specified elements, frequencies, and combination size.
     *
     * @param elements    the list of elements from which combinations will be generated
     * @param frequencies an array of integers representing the frequency of each element in the multiset
     * @param size        the size of each combination to be generated
     */
    public MultisetCombinationBuilder(List<T> elements, int[] frequencies, int size) {
        this.elements = elements;
        this.frequencies = frequencies;
        this.size = size;
    }

    /**
     * Builds a {@link MultisetCombination} that generates combinations in lexicographical order.
     *
     * @return a {@code MultisetCombination} instance capable of generating combinations based on the specified elements,
     * frequencies, and size
     */
    public MultisetCombination<T> lexOrder() {
        //TODO: change the signature and name.
        return new MultisetCombination<>(elements, size, frequencies);
    }

    /**
     * Retrieves every mᵗʰ lexicographic multiset combination, starting from a specified index.
     * <p>
     * This method returns a {@code MultisetCombinationMth} instance that generates combinations at regular intervals,
     * determined by the specified {@code increment} and beginning at the provided {@code start} index.
     * </p>
     *
     * @param increment the interval for selecting combinations (e.g., an {@code increment} of 2 generates every 2nd combination)
     * @param start     the starting index (0-based) from which to begin generating combinations
     * @return a {@code MultisetCombinationMth} instance for generating every mᵗʰ combination in lexicographical order
     */
    public MultisetCombinationMth<T> lexOrderMth(long increment, long start) {
        //List<T> elements, int r, long start, long increment, int[] frequencies
        return new MultisetCombinationMth<>(elements, size, start, increment, frequencies);
    }
}
