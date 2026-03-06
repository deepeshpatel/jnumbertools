/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
class TwoLevelMap<K1, K2, V> extends ConcurrentHashMap<K1, Map<K2, V>> {

    public V get(K1 key1, K2 key2) {
        var map = get(key1);
        return map == null ? null : map.get(key2);
    }

    public V put(K1 key1, K2 key2, V value) {
        computeIfAbsent(key1, (e) -> new ConcurrentHashMap<>()).put(key2, value);
        return value;
    }
}
