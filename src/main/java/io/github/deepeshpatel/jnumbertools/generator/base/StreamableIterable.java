/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

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
}