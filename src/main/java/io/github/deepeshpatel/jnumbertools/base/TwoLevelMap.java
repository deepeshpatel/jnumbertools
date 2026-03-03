/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple two-level map for memoization with thread-safe operations.
 * <p>
 * Memoization is an optimization technique that stores the results of expensive function calls
 * and returns the cached result when the same inputs occur again. This two-level map structure
 * is particularly useful for caching results that depend on two parameters, such as combinatorial
 * calculations where the result depends on both (n, k) pairs.
 * </p>
 * <p>
 * <strong>Performance Benefits:</strong>
 * <ul>
 *   <li>Avoids recomputation of expensive calculations</li>
 *   <li>Thread-safe using {@link ConcurrentHashMap}</li>
 *   <li>Memory-efficient with lazy initialization</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Use Case:</strong> Commonly used in combinatorial algorithms for caching
 * binomial coefficients C(n,k), factorial values, or permutation counts.
 * </p>
 *
 * @param <K1> the type of the first key (e.g., n in C(n,k))
 * @param <K2> the type of the second key (e.g., k in C(n,k))
 * @param <V>  the type of the value (e.g., C(n,k) result)
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
