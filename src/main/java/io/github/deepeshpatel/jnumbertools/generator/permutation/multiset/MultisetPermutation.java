/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator.initIndicesForMultisetPermutation;

/**
 * An iterable that generates multiset permutations from a {@code LinkedHashMap} of elements to their frequencies.
 * <p>
 * This class generates permutations in lexicographical order based on the insertion order of elements
 * in the provided {@code LinkedHashMap}, where each element is repeated according to its frequency count
 * (e.g., {A=2, B=1} → [A,A,B], [A,B,A], [B,A,A]). Extends {@link AbstractMultisetPermutation} to leverage
 * its sorting and multiset management capabilities, ensuring consistent ordering via {@code Comparable}.
 * </p>
 * <p>
 * <strong>Note:</strong> The current implementation flattens frequency counts into an initial index array using
 * {@code initIndicesForMultisetPermutation}. For extremely large frequency values (e.g., 10^99), this may be inefficient.
 * Future enhancements should compute the next permutation without full flattening.
 * </p>
 * <p>
 * Instances are intended to be created via a builder; thus, the constructor is package-private.
 * </p>
 *
 * @param <T> the type of elements, must implement {@code Comparable} for lexicographical ordering
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class MultisetPermutation<T> extends AbstractMultisetPermutation<T> {

    private final int[] initialIndices;

    /**
     * Constructs a new {@code MultisetPermutation} instance for generating multiset permutations.
     *
     * @param multiset   the {@code LinkedHashMap} mapping elements to their frequency counts; must not be null or empty
     * @param calculator utility for computing permutations and factorials
     * @throws IllegalArgumentException if multiset is null/empty, frequencies are negative, or sum is zero
     */
    MultisetPermutation(LinkedHashMap<T, Integer> multiset, Calculator calculator) {
        super(multiset, calculator);
        /*
         * TODO: Enhancement advice – Develop an algorithm to calculate the next permutation
         * without flattening the frequency counts. The current implementation may not work efficiently
         * for very large frequency values (e.g., 10^99).
         */
        this.initialIndices = initIndicesForMultisetPermutation(frequencies);
    }

        /**
         * Returns an iterator over the multiset permutations.
         * <p>
         * The iterator uses {@code UniquePermutationLexElementIterator} to generate permutations in lexicographical
         * order based on the sorted indices, converting them to lists of actual elements.
         * </p>
         *
         * @return an iterator for generating multiset permutations as lists of elements
         */
    @Override
    public Iterator<List<T>> iterator() {
        return new UniquePermutationLexElementIterator<>(this::indicesToValues, initialIndices);
    }
}