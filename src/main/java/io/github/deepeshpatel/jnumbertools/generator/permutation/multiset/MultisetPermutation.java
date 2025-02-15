/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermItrForElements;

import java.util.Iterator;
import java.util.List;

/**
 * An iterable that generates multiset (repetitive) permutations of a list of elements.
 * <p>
 * In this context, each element in the input list is associated with a frequency count that indicates
 * how many times that element can be repeated. Permutations are generated in lexicographical order based on
 * the indices of input values, with each value at a specific index (e.g. elements₀, elements₁, …, elementsₙ₋₁)
 * considered unique.
 * </p>
 * <p>
 * For example, if the input list is [A, B, C] and the frequency array is [2, 1, 3], then A can appear up to 2 times,
 * B up to 1 time, and C up to 3 times in the generated permutations.
 * </p>
 * <p>
 * <strong>Note:</strong> The current implementation "flattens" the frequency counts into an initial index array
 * using {@code initIndicesForMultisetPermutation(multisetFreqArray)}. For extremely large frequency values
 * (e.g. 10^99), this approach may not be efficient. Future enhancements should consider computing the next
 * permutation without fully flattening the frequency counts.
 * </p>
 * <p>
 * Instances of this class are intended to be created via a builder; therefore, the constructor is package‑private.
 * </p>
 *
 * @param <T> the type of elements in the input list
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class MultisetPermutation<T> extends AbstractGenerator<T> {

    private final int[] initialIndices;

    /**
     * Constructs a new {@code MultisetPermutation} instance for generating multiset permutations.
     * <p>
     * Permutations are generated in lexicographical order based on the indices of input values.
     * The {@code multisetFreqArray} parameter specifies the repetition count for each corresponding element in the input list.
     * For example, {@code multisetFreqArray[0]} holds the count for the 0ᵗʰ element, {@code multisetFreqArray[1]} for the 1ˢᵗ element,
     * and so on. Each count must be ≥ 1.
     * </p>
     *
     * @param elements the input list from which permutations are generated
     * @param multisetFreqArray an array of integers representing the repetition count for each element.
     *                          For example, {@code multisetFreqArray[0]} is the count for the 0ᵗʰ element, {@code multisetFreqArray[1]} for the 1ˢᵗ element, etc.
     * @throws IllegalArgumentException if the frequency array is null or its length does not match the size of the input list
     */
    MultisetPermutation(List<T> elements, int[] multisetFreqArray) {
        super(elements);
        checkParamMultisetFreqArray(elements.size(), multisetFreqArray, "permutation");
        /*
         * TODO: Enhancement advice – Develop an algorithm to calculate the next permutation
         * without flattening the frequency counts. The current implementation may not work efficiently
         * for very large frequency values (e.g., 10^99).
         */
        initialIndices = initIndicesForMultisetPermutation(multisetFreqArray);
    }

    /**
     * Returns an iterator over the multiset permutations.
     * <p>
     * The iterator converts permutations represented by index arrays into permutations of actual elements.
     * </p>
     *
     * @return an iterator for generating multiset permutations as lists of elements
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new UniquePermItrForElements<>(this::indicesToValues, initialIndices);
    }
}
