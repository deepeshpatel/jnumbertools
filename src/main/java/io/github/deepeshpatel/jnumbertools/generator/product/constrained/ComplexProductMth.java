/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a complex product of multiple sets with the ability to generate every mᵗʰ combination.
 * <p>
 * This class generates combinations by iterating through the Cartesian product of the lists provided,
 * but only at every mᵗʰ position starting from the specified position. It implements the {@code Iterable}
 * interface to support iteration and provides a stream for functional-style operations.
 * </p>
 * <p>
 * Example: This class can be used (via its builder) to create a complex product of combinations and print
 * every 2ⁿᵈ combination.
 * </p>
 * <p>
 * <strong>Note:</strong> This class is intended to be constructed via a builder and does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class ComplexProductMth<T> implements Iterable<List<T>> {

    private final BigInteger m;
    private final BigInteger start;
    private final List<Builder<T>> builders;
    private final BigInteger maxCount;

    /**
     * Constructs a {@code ComplexProductMth} with the specified parameters.
     *
     * @param m        the interval for selecting combinations (every mᵗʰ combination)
     * @param start    the starting position for combinations
     * @param builders a list of builders used to generate combinations
     */
    ComplexProductMth(BigInteger m, BigInteger start, List<Builder<T>> builders) {
        this.m = m;
        this.start = start;
        this.builders = builders;
        this.maxCount = maxCount();
    }

    /**
     * Returns a stream of every mᵗʰ combination.
     *
     * @return a stream of combinations
     */
    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Builds and returns the mᵗʰ combination.
     * <p>
     * Use this method if you need only a specific mᵗʰ combination rather than iterating through the entire sequence.
     * </p>
     *
     * @return the mᵗʰ combination as a list of elements
     */
    public List<T> build() {
        return getMth(m);
    }

    private BigInteger maxCount() {
        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    private List<T> getMth(final BigInteger m) {
        Deque<T> output = new ArrayDeque<>();
        BigInteger remaining = m;

        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<T> e = builders.get(i);
            BigInteger[] division = remaining.divideAndRemainder(e.count());
            remaining = division[0];

            List<T> values = e.lexOrderMth(division[1], division[1]).iterator().next();//.build();

            // Add elements to the front in reverse order
            for (int j = values.size() - 1; j >= 0; j--) {
                output.addFirst(values.get(j));
            }
        }

        return new ArrayList<>(output);  // Convert deque to list
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    @Override
    public String toString() {
        return String.format("ComplexProductMth{m=%s, start=%s, maxCount=%s, buildersCount=%d}",
                m, start, maxCount, builders.size());
    }

    private class Itr implements Iterator<List<T>> {

        private BigInteger position = start;

        @Override
        public boolean hasNext() {
            return position.compareTo(maxCount) < 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            List<T> result = getMth(position);
            position = position.add(m);
            return result;
        }
    }
}
