/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.complex;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a complex product of multiple sets with the ability to generate every m<sup>th</sup> combination.
 * <p>
 * This class generates combinations by iterating through the Cartesian product of the lists provided,
 * but only at every m<sup>th</sup> position starting from the specified position.
 * It implements the {@link Iterable} interface to support iteration and provides a stream for functional-style operations.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * ComplexProductMth&lt;String&gt; complexProductMth = new ComplexProductMth&lt;&gt;(2, 0, List.of(
 *     new Combinations(calculator).unique(2, List.of("A", "B", "C")),
 *     new Combinations(calculator).repetitive(2, List.of("D", "E"))
 * ));
 * complexProductMth.stream().forEach(System.out::println);
 * </pre>
 * This example creates a complex product of combinations and prints every 2<sup>nd</sup> combination.
 *
 * @param <T> the type of elements in the combinations
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class ComplexProductMth<T> implements Iterable<List<T>> {

    private final long m;
    private final long start;
    private final List<Builder<T>> builders;
    private long maxCount;

    /**
     * Constructs a ComplexProductMth with the specified parameters.
     *
     * @param m the interval for selecting combinations (every m<sup>th</sup> combination)
     * @param start the starting position for combinations
     * @param builders a list of builders used to generate combinations
     */
    public ComplexProductMth(long m, long start, List<Builder<T>> builders) {
        this.m = m;
        this.start = start;
        this.builders = builders;
    }

    /**
     * Returns a stream of the every m<sup>th</sup> combinations.
     *
     * @return a stream of combinations
     */
    public Stream<List<T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Builds and returns the m<sup>th</sup> combination.
     *
     * @return the m<sup>th</sup> combination
     */
    public List<T> build() {
        return getMth(m);
    }

    private synchronized long maxCount() {
        if (maxCount != 0) {
            return maxCount;
        }
        maxCount = 1;
        for (Builder<T> builder : builders) {
            maxCount *= builder.count();
        }
        return maxCount;
    }

    private List<T> getMth(long m) {
        List<T> output = new ArrayList<>();
        long remaining = m;
        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<T> e = builders.get(i);
            long index = remaining % e.count();
            remaining = remaining / e.count();
            List<T> values = e.lexOrderMth(index, 0).build();
            Collections.reverse(values);
            output.addAll(values);
        }
        Collections.reverse(output);
        return output;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private long position = start;
        private final long memoizedMaxCount;

        public Itr() {
            memoizedMaxCount = maxCount();
        }

        @Override
        public boolean hasNext() {
            return position < memoizedMaxCount;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            List<T> result = getMth(position);
            position += m;
            return result;
        }
    }
}
