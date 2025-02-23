/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractMultisetCombination<T extends Comparable<T>> implements Iterable<Map<T, Integer>> {

    protected final Map.Entry<T, Integer>[] options;
    protected final int[] frequencies;
    protected final int r;

    public enum Order {
        LEX, REVERSE_LEX, INPUT
    }

    protected AbstractMultisetCombination(Map<T, Integer> options, int r, Order order) {
        checkParameters(options, r);
        this.options = orderOptions(options, order != null ? order : Order.LEX);
        this.frequencies = extractFrequencies(this.options);
        this.r = r;
    }

    private void checkParameters(Map<T, Integer> options, int r) {
        if (r < 0) throw new IllegalArgumentException("parameter r (no. of items in each combination) should be>=0 ");
        if (options == null) throw new IllegalArgumentException("Options map cannot be null");
        for (Map.Entry<T, Integer> entry : options.entrySet()) {
            Integer freq = entry.getValue();
            if (freq == null || freq <= 0) throw new IllegalArgumentException("All frequencies must be positive: found " + freq);
        }
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<T, Integer>[] orderOptions(Map<T, Integer> options, Order order) {
        Comparator<Map.Entry<T, Integer>> comparator;
        switch (order) {
            case LEX: comparator = Map.Entry.comparingByKey(); break;
            case REVERSE_LEX: comparator = Map.Entry.<T, Integer>comparingByKey().reversed(); break;
            case INPUT: comparator = (e1, e2) -> 0; break;
            default: throw new IllegalArgumentException("Unknown order: " + order);
        }
        return options.entrySet().stream()
                .sorted(comparator)
                .toArray(Map.Entry[]::new);
    }

    private int[] extractFrequencies(Map.Entry<T, Integer>[] entries) {
        int[] frequencies = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
            frequencies[i] = entries[i].getValue();
        }
        return frequencies;
    }

    public Stream<Map<T, Integer>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public abstract Iterator<Map<T, Integer>> iterator();
}