/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable map from elements to counts, stored as two parallel arrays without copying them.
 * <p>
 * Multiset combinations return one such map per result. A {@code LinkedHashMap} wrapped in
 * {@code Collections.unmodifiableMap} costs a hash table, one entry object per element and the wrapper; this
 * class holds just the key array and an {@code int} count array. Lookups scan the keys, which is fast for the
 * small number of distinct elements a result holds.
 * </p>
 * <p>
 * It behaves like any other {@link Map}: iteration follows the array order (the order of the options the
 * multiset was created with), and {@code equals}, {@code hashCode} and {@code toString} follow the {@code Map}
 * contract, so it is equal to a {@code LinkedHashMap} with the same mappings. Every method that would change it
 * throws {@link UnsupportedOperationException}. It is {@link Serializable} when its keys are.
 * </p>
 *
 * @param <K> the element type
 * @author Deepesh Patel
 */
public final class FrequencyMap<K> extends AbstractMap<K, Integer> {

    private final Object[] keys;
    private final int[] counts;

    private FrequencyMap(Object[] keys, int[] counts) {
        this.keys = keys;
        this.counts = counts;
    }

    /**
     * Wraps the arrays as an immutable map <strong>without copying them</strong>.
     * <p>
     * The caller hands over ownership: neither array may be modified, or kept anywhere it could be modified,
     * after this call. Keys must be distinct and non-null.
     * </p>
     *
     * @param keys   the keys, in iteration order; must not be null
     * @param counts {@code counts[i]} is the value of {@code keys[i]}; must have the same length as {@code keys}
     * @param <K>    the key type
     * @return an immutable map view of the two arrays
     * @throws NullPointerException     if either array is null
     * @throws IllegalArgumentException if the arrays differ in length
     */
    public static <K> FrequencyMap<K> wrap(Object[] keys, int[] counts) {
        Objects.requireNonNull(keys, "keys must not be null");
        Objects.requireNonNull(counts, "counts must not be null");
        if (keys.length != counts.length) {
            throw new IllegalArgumentException("keys and counts differ in length: " + keys.length + " vs " + counts.length);
        }
        return new FrequencyMap<>(keys, counts);
    }

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public boolean isEmpty() {
        return keys.length == 0;
    }

    private int indexOf(Object key) {
        if (key == null) return -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) return i;
        }
        return -1;
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf(key) >= 0;
    }

    @Override
    public Integer get(Object key) {
        int i = indexOf(key);
        return i < 0 ? null : counts[i];
    }

    @Override
    public Set<Entry<K, Integer>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public int size() {
                return keys.length;
            }

            @Override
            public Iterator<Entry<K, Integer>> iterator() {
                return new Iterator<>() {
                    private int next;

                    @Override
                    public boolean hasNext() {
                        return next < keys.length;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public Entry<K, Integer> next() {
                        if (next >= keys.length) throw new NoSuchElementException();
                        int i = next++;
                        return new SimpleImmutableEntry<>((K) keys[i], counts[i]);
                    }
                };
            }
        };
    }
}
