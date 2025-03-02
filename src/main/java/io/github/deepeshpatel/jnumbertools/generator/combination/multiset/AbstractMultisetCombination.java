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
 * This class provides common functionality for multiset combination generators, including ordering
 * options (LEX, REVERSE_LEX, INSERTION) and frequency extraction from a provided map. Subclasses
 * implement specific iteration strategies.
 * </p>
 *
 * @param <T> the type of elements in the combinations; must be comparable for ordering
 * @author Deepesh Patel
 * @version 3.0.1
 */
public abstract class AbstractMultisetCombination<T> implements Iterable<Map<T, Integer>> {

    protected final Map.Entry<T, Integer>[] options;
    protected final int[] frequencies;
    protected final int r;

    /**
     * Constructs a new AbstractMultisetCombination instance.
     *
     * @param options the map of distinct elements and their frequencies defining the multiset
     * @param r       the size of each combination (r); must be non-negative
     * @throws IllegalArgumentException if r is negative, options is null, or frequencies are invalid
     */
    protected AbstractMultisetCombination(LinkedHashMap<T, Integer> options, int r) {
        checkParameters(options, r);
        this.options = orderOptions(options);
        this.frequencies = extractFrequencies(this.options);
        this.r = r;
    }

    private void checkParameters(Map<T, Integer> options, int r) {
        if (r < 0) throw new IllegalArgumentException("Parameter r (number of items in each combination) must be >= 0");
        if (options == null) throw new IllegalArgumentException("Options map cannot be null");
        for (Map.Entry<T, Integer> entry : options.entrySet()) {
            Integer freq = entry.getValue();
            if (freq == null || freq <= 0) throw new IllegalArgumentException("All frequencies must be positive: found " + freq);
        }
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<T, Integer>[] orderOptions(Map<T, Integer> options) {
        return options.entrySet().stream()
                .toArray(Map.Entry[]::new);
    }

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
     *
     * @return an iterator of immutable frequency maps
     */
    @Override
    public abstract Iterator<Map<T, Integer>> iterator();
}