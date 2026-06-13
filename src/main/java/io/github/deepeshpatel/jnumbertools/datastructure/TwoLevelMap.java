/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * A thread-safe two-level map for memoization of two-key values.
 * <p>
 * This map stores values indexed by a primary key (K1) and secondary key (K2).
 * It extends ConcurrentHashMap and uses nested ConcurrentHashMaps for thread safety.
 * </p>
 * <p>
 * Primarily used internally by {@link Calculator} for caching combinatorial values
 * like binomial coefficients C(n, k) where n is the primary key and k is the secondary key.
 * </p>
 *
 * @param <K1> the type of the first-level key
 * @param <K2> the type of the second-level key
 * @param <V> the type of the stored value
 * @author Deepesh Patel
 * @see <a href="https://en.wikipedia.org/wiki/Memoization">Wikipedia: Memoization</a>
 */
public class TwoLevelMap<K1, K2, V> extends ConcurrentHashMap<K1, Map<K2, V>> {

    /**
     * Gets a value from the two-level map.
     *
     * @param key1 the first-level key
     * @param key2 the second-level key
     * @return the value, or null if not found
     */
    public V get(K1 key1, K2 key2) {
        var map = get(key1);
        return map == null ? null : map.get(key2);
    }

    /**
     * Puts a value into the two-level map.
     *
     * @param key1 the first-level key
     * @param key2 the second-level key
     * @param value the value to store
     * @return the previous value associated with key2, or null if there was no mapping
     */
    @SuppressWarnings("all")
    public V put(K1 key1, K2 key2, V value) {
        return computeIfAbsent(key1, k -> new ConcurrentHashMap<>()).put(key2, value);
    }

    /**
     * Atomically computes and stores a value if the key pair is not already present.
     * Thread-safe: ConcurrentHashMap.computeIfAbsent ensures the mapping function
     * is invoked at most once, even under high concurrency.
     *
     * @param key1 the first-level key
     * @param key2 the second-level key
     * @param mappingFunction the function to compute the value
     * @return the value (either pre-existing or newly computed)
     */
    @SuppressWarnings("all")
    public V computeIfAbsent(K1 key1, K2 key2, BiFunction<? super K1, ? super K2, ? extends V> mappingFunction) {
        return computeIfAbsent(key1, k1 -> new ConcurrentHashMap<>())
                .computeIfAbsent(key2, k2 -> mappingFunction.apply(key1, k2));
    }
}