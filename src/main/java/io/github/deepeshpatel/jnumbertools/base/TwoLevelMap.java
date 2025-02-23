package io.github.deepeshpatel.jnumbertools.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple two-level map for memoization.
 *
 * @param <K1> the type of the first key.
 * @param <K2> the type of the second key.
 * @param <V>  the type of the value.
 */
public class TwoLevelMap<K1, K2, V> extends ConcurrentHashMap<K1, Map<K2, V>> {

    public V get(K1 key1, K2 key2) {
        var map = get(key1);
        return map == null ? null : map.get(key2);
    }

    public V put(K1 key1, K2 key2, V value) {
        computeIfAbsent(key1, (e) -> new ConcurrentHashMap<>()).put(key2, value);
        return value;
    }
}
