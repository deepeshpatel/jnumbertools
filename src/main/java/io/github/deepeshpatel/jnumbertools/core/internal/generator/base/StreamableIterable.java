/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.base;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * An iterable collection of combinatorial structures that supports streaming.
 *
 * @param <E> the type of elements in the generated structures
 * @author Deepesh Patel
 */
public interface StreamableIterable<E> extends Iterable<List<E>> {
    /**
     * Returns a stream of the combinatorial structures.
     *
     * @return a stream of lists containing elements of type E
     */
    Stream<List<E>> stream();

    /**
     * Returns an iterator over a collection that contains a single element: an empty list.
     * <p>
     * This iterator is designed to mimic the empty-set(∅)by returning a collection with
     * a size of one (a single empty list), which is necessary for correct count assertions.
     * </p>
     *
     * @param <E> the type of elements contained in the empty list.
     * @return an iterator over a collection with a single empty list.
     */
    default <E> Iterator<List<E>> emptyListIterator() {
        return emptyIterator();
    }

    public static <E> Iterator<List<E>> emptyIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty list (size is 1),
        // which is required to mimic the empty-set(∅)for correct count assertions.
        return Collections.singletonList(Collections.<E>emptyList()).iterator();
    }
}
