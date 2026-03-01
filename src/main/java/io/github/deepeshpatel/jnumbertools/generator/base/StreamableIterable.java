package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.List;
import java.util.stream.Stream;

/**
 * An iterable collection of combinatorial structures that supports streaming.
 *
 * @param <E> the type of elements in the generated structures
 */
public interface StreamableIterable<E> extends Iterable<List<E>> {
    /**
     * Returns a stream of the combinatorial structures.
     *
     * @return a stream of lists containing elements of type E
     */
    Stream<List<E>> stream();
}