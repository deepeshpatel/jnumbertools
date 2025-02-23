package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.*;

public class Util {
    /**
     * Returns an iterator over a collection that contains a single element: an empty list.
     * <p>
     * This iterator is designed to mimic the null-set (φ) by returning a collection with
     * a size of one (a single empty list), which is necessary for correct count assertions.
     * </p>
     *
     * @param <E> the type of elements contained in the empty list.
     * @return an iterator over a collection with a single empty list.
     */
    public static <E> Iterator<List<E>> emptyListIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty list (size is 1),
        // which is required to mimic the null-set (φ) for correct count assertions.
        return Collections.singletonList(Collections.<E>emptyList()).iterator();
    }

    public static <K,V> Iterator<Map<K,V>> emptyMapIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty map (size is 1),
        // which is required to mimic the null-set (φ) for correct count assertions.
        return Collections.singletonList(Collections.<K,V>emptyMap()).iterator();
    }
}
