/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract base class for generating multiset combinations as frequency maps.
 * <p>
 * A multiset combination selects r items from a multiset of n distinct elements with specified
 * multiplicities (e.g., {A:2, B:1}), where each element can be selected up to its multiplicity,
 * and order does not matter. The total number of combinations is determined by the multichoose
 * formula, constrained by element multiplicities. This class provides common functionality,
 * including ordering options (LEX, REVERSE_LEX, INSERTION) and multiplicity extraction.
 * Subclasses implement specific iteration strategies, producing immutable frequency maps
 * (e.g., {A:1, B:2}).
 * </p>
 *
 * @param <T> the type of elements in the combinations; must be comparable for ordering
 * @author Deepesh Patel
 */
public abstract class AbstractMultisetCombination<T> implements Iterable<Map<T, Integer>> {

    protected final Map.Entry<T, Integer>[] options;
    protected final int[] frequencies;
    protected final int r;

    /**
     * Constructs a base for multiset combination generators.
     *
     * @param map the map of n distinct elements to their multiplicities (must not be null)
     * @param r   the size of each combination (r ≥ 0)
     * @throws IllegalArgumentException if r < 0, map is null, or any multiplicity ≤ 0
     */
    protected AbstractMultisetCombination(LinkedHashMap<T, Integer> map, int r) {
        checkParameters(map, r);
        this.options = orderOptions(map);
        this.frequencies = extractFrequencies(this.options);
        this.r = r;
    }

    /**
     * Validates the input parameters for multiset combinations.
     *
     * @param map the map of elements to their multiplicities
     * @param r   the combination size
     * @throws IllegalArgumentException if r < 0, map is null, or any multiplicity ≤ 0
     */
    private void checkParameters(Map<T, Integer> map, int r) {
        if (r < 0) throw new IllegalArgumentException("Parameter r (number of items in each combination) must be >= 0");
        if (map == null) throw new IllegalArgumentException("Options map cannot be null");
        for (Map.Entry<T, Integer> entry : map.entrySet()) {
            Integer freq = entry.getValue();
            if (freq == null || freq <= 0) throw new IllegalArgumentException("All frequencies must be positive: found " + freq);
        }
    }

    /**
     * Converts the input map to an array of entries.
     *
     * @param map the map of elements to their multiplicities
     * @return an array of map entries
     */
    @SuppressWarnings("unchecked")
    private Map.Entry<T, Integer>[] orderOptions(Map<T, Integer> map) {
        return map.entrySet().stream()
                .toArray(Map.Entry[]::new);
    }

    /**
     * Extracts multiplicities from the options array.
     *
     * @param entries the array of map entries
     * @return an array of multiplicities
     */
    private int[] extractFrequencies(Map.Entry<T, Integer>[] entries) {
        int[] frequencies = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
            frequencies[i] = entries[i].getValue();
        }
        return frequencies;
    }

    /**
     * Returns a stream of multiset combinations.
     *
     * @return a stream of immutable frequency maps representing combinations
     */
    public Stream<Map<T, Integer>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Returns an iterator over multiset combinations.
     * <p>
     * Subclasses implement specific iteration strategies (e.g., lexicographical order).
     * </p>
     *
     * @return an iterator of immutable frequency maps
     */
    @Override
    public abstract Iterator<Map<T, Integer>> iterator();
}