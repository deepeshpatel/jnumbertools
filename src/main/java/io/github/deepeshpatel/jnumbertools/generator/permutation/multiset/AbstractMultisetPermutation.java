/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.api.Calculator;
import io.github.deepeshpatel.jnumbertools.api.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract base class for generating multiset permutations with sorting capability.
 * <p>
 * This class provides a foundation for permutation generators that operate on a multiset defined
 * by a map of elements to their frequencies (e.g., {A=2, B=1}). The total number of unique permutations
 * is given by the multinomial coefficient n! / ∏ (fᵢ!), where n = ∑fᵢ is the sum of frequencies and fᵢ are
 * the frequencies of distinct elements. It ensures elements are comparable for consistent
 * lexicographical ordering and manages the multiset structure. Subclasses must implement the
 * {@code iterator()} method to generate permutations as lists, where position matters.
 * </p>
 *
 * @param <T> the type of elements, must implement {@code Comparable} for sorting
 * @author Deepesh Patel
 */
public abstract class AbstractMultisetPermutation<T> implements StreamableIterable<T> {

    protected final List<T> elements;
    protected final int[] frequencies;
    protected final int totalLength;
    protected final Calculator calculator;

    /**
     * Constructs an {@code AbstractMultisetPermutation} instance with a sorted multiset.
     * <p>
     * <strong>For internal/library use only.</strong>
     * Application code must use the {@link Permutations} builder instead.
     * </p>
     * <p>
     * This constructor assumes that the input has already been validated by the builder:
     * <ul>
     *   <li>{@code multiset} is not null</li>
     *   <li>All frequencies are non-negative</li>
     *   <li>The map uses insertion order for lexicographical ordering</li>
     * </ul>
     * Zero frequencies are permitted (element is ignored in generation but preserved for order).
     * An empty map is allowed and results in a single empty permutation.
     * </p>
     *
     * @param multiset   a {@link LinkedHashMap} defining the multiset; keys determine lexicographical order,
     *                   values are non-negative frequencies (zero allowed)
     * @param calculator utility for computing factorials and multinomial coefficients
     */
    protected AbstractMultisetPermutation(LinkedHashMap<T, Integer> multiset, Calculator calculator) {

        int size = multiset.size();
        frequencies = new int[size];
        elements = new ArrayList<>(size);
        createElementsAndFrequencies(multiset);

        int sum = 0;
        for (int i = 0; i < elements.size(); i++) {
            T element = elements.get(i);
            int freq = multiset.get(element);
            frequencies[i] = freq;
            sum += freq;
        }
        this.totalLength = sum;
        this.calculator = calculator;
    }

    /**
     * Populates the elements list and frequencies array from the provided multiset.
     *
     * @param multiset the map of elements to their frequencies
     */
    private void createElementsAndFrequencies(LinkedHashMap<T, Integer> multiset) {
        int index = 0;
        for (var entry : multiset.entrySet()) {
            elements.add(entry.getKey());
            frequencies[index++] = entry.getValue();
        }
    }

    /**
     * Provides a sequential stream of the multiset permutations.
     *
     * @return a {@link Stream} of permutations as lists of elements
     */
    public Stream<List<T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Returns the total number of permutations as a {@code BigInteger}.
     * <p>
     * Computes n! / (f₁!·f₂!·…·fₖ!), where n is the sum of frequencies and fᵢ are individual frequencies.
     * </p>
     *
     * @return the total number of permutations
     */
    protected BigInteger getTotalPermutations() {
        return calculator.multinomial(frequencies);
    }

    /**
     * Converts an array of indices to a permutation list using the sorted elements.
     *
     * @param indices the array of indices corresponding to positions in the elements list
     * @return the permutation as a {@code List<T>}
     */
    protected List<T> indicesToValues(int[] indices) {
        List<T> result = new ArrayList<>(indices.length);
        for (int index : indices) {
            result.add(elements.get(index));
        }
        return List.copyOf(result);
    }

    /**
     * Returns an iterator over the multiset permutations.
     *
     * @return an iterator producing each permutation as a {@code List<T>}
     */
    @Override
    public abstract Iterator<List<T>> iterator();

    /**
     * Creates a flattened array of indices representing the multiset permutation.
     * Each element index is repeated according to its frequency.
     *
     * @param frequencies array of frequencies for each distinct element
     * @return flattened int[] of indices (length = sum of frequencies)
     */
    protected int[] createFlattenedIndices(int... frequencies) {
        return IntStream.range(0, frequencies.length)
                .flatMap(i -> IntStream.generate(() -> i).limit(frequencies[i]))
                .toArray();
    }
}
