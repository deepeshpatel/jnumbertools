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

/**
 * An iterable that generates multiset permutations from a {@code LinkedHashMap} of elements to their frequencies.
 * <p>
 * This class generates permutations in lexicographical order based on the insertion order of elements
 * in the provided {@code LinkedHashMap}, where each element is repeated according to its frequency count
 * (e.g., {A=2, B=1} → [A,A,B], [A,B,A], [B,A,A]). The total number of unique permutations is given by
 * the multinomial coefficient n! / (m₁!·m₂!·…), where n is the sum of frequencies and mᵢ are the frequencies
 * of distinct elements. Extends {@link AbstractMultisetPermutation} to leverage its sorting and multiset
 * management capabilities, ensuring consistent ordering via {@code Comparable}.
 * </p>
 * <p>
 * <strong>Note:</strong> The current implementation flattens frequency counts into an initial index array using
 * {@code initIndicesForMultisetPermutation}. For extremely large frequency values (e.g., 10⁹⁹), this may be inefficient.
 * </p>
 * <p>
 * Instances are intended to be created via a builder; thus, the constructor is package-private.
 * </p>
 *
 * @param <T> the type of elements, must implement {@code Comparable} for lexicographical ordering
 * @author Deepesh Patel
 */
public final class MultisetPermutation<T> extends AbstractMultisetPermutation<T> {

    private final int[] initialIndices;

    /**
     * Constructs a new {@code MultisetPermutation} instance for generating multiset permutations.
     *
     * @param multiset a {@code LinkedHashMap} mapping elements to their frequency counts; must not be null or empty,
     *                 with insertion order determining lexicographical order
     * @param calculator utility for computing permutations and factorials
     * @throws IllegalArgumentException if multiset is null, empty, contains negative frequencies, or sum is zero
     */
    MultisetPermutation(LinkedHashMap<T, Integer> multiset, Calculator calculator) {
        super(multiset, calculator);
        //Note: using freq vector is more practical for combination where the return type is multi-beg.
        //for permutations, flattening once at the beginning is simpler, faster in practice,
        // and not meaningfully worse in memory unless frequencies are astronomically large
        // (in which case the whole permutation is impossible to enumerate anyway).
        //It's possible (some papers/algorithms do it), but the complexity explodes, and each next()
        // becomes O(total items) time anyway (because you have to output the full list)
        //so I am choosing the simple flattening to
        this.initialIndices = createFlattenedIndices(frequencies);
    }

    /**
     * Returns an iterator over all distinct permutations of the multiset in lexicographical order.
     * <p>
     * The total number of permutations is given by the multinomial coefficient:
     * n! / (f₁! × f₂! × ... × fₖ!), where n is the sum of all frequencies.
     * </p>
     * <p>
     * Example: For multiset {A=2, B=1}, the iterator produces:
     * [A,A,B], [A,B,A], [B,A,A]
     * </p>
     *
     * @return an iterator over multiset permutations in lexicographical order;
     *         returns an empty iterator if the multiset is empty
     * @throws IllegalStateException if the multiset was not properly initialized
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new UniquePermutationLexElementIterator<>(this::indicesToValues, initialIndices);
    }
}