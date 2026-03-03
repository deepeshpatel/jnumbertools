/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A wrapper for an iterator to make it StreamableIterable, enabling Java Stream API integration.
 * <p>
 * This adapter class bridges traditional Iterator-based APIs with modern Stream processing,
 * allowing combinatorial generators to be used in functional programming paradigms.
 * It's particularly useful when you need to:
 * </p>
 * <ul>
 *   <li>Use Stream operations (filter, map, collect) on iterator-based generators</li>
 *   <li>Integrate with other Stream-based APIs</li>
 *   <li>Leverage parallel processing capabilities</li>
 *   <li>Use Stream terminal operations like forEach, count, reduce</li>
 * </ul>
 * <p>
 * <strong>Example:</strong>
 * <pre>{@code
 * Iterator<List<String>> iterator = someGenerator();
 * StreamableIteratorImpl<String> streamable = new StreamableIteratorImpl<>(iterator);
 * 
 * // Now you can use Stream operations
 * long count = streamable.stream().count();
 * List<String> first = streamable.stream().findFirst().orElse(List.of());
 * }</pre>
 * </p>
 *
 * @param <E> the type of elements in the generated structures
 * @author Deepesh Patel
 * @see StreamableIterable
 * @see java.util.stream.Stream
 */
public record StreamableIteratorImpl<E>(Iterator<List<E>> iterator) implements StreamableIterable<E> {

    /**
     * Returns a sequential Stream of elements from the wrapped iterator.
     * <p>
     * This method enables Stream API operations on the underlying iterator,
     * allowing functional programming paradigms such as filtering, mapping,
     * and collecting results.
     * </p>
     *
     * @return a sequential Stream over the elements from the iterator
     * @see java.util.stream.Stream
     */
    @Override
    public Stream<List<E>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}