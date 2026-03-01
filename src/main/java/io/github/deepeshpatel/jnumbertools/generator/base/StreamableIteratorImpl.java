package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A wrapper for an iterator to make it StreamableIterable.
 *
 * @param <E> the type of elements in the generated structures
 */
public record StreamableIteratorImpl<E>(Iterator<List<E>> iterator) implements StreamableIterable<E> {

    @Override
    public Stream<List<E>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}