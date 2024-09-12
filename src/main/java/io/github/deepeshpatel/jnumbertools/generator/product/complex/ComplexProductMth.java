/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.complex;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a complex product of multiple sets with the ability to generate every m<sup>th</sup> combination.
 * <p>
 * This class generates combinations by iterating through the Cartesian product of the lists provided,
 * but only at every m<sup>th</sup> position starting from the specified position.
 * It implements the {@link Iterable} interface to support iteration and provides a stream for functional-style operations.
 * This example creates a complex product of combinations and prints every 2<sup>nd</sup> combination.
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the combinations
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class ComplexProductMth<T> implements Iterable<List<T>> {

    private final BigInteger m;
    private final BigInteger start;
    private final List<Builder<T>> builders;
    private BigInteger maxCount = BigInteger.ZERO;


    /**
     * Constructs a ComplexProductMth with the specified parameters.
     *
     * @param m the interval for selecting combinations (every m<sup>th</sup> combination)
     * @param start the starting position for combinations
     * @param builders a list of builders used to generate combinations
     */
    ComplexProductMth(BigInteger m, BigInteger start, List<Builder<T>> builders) {
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

    private synchronized BigInteger maxCount() {
        if (!BigInteger.ZERO.equals(maxCount)) {
            return maxCount;
        }
        maxCount = BigInteger.ONE;
        for (Builder<T> builder : builders) {
            maxCount = maxCount.multiply(builder.count());
        }
        return maxCount;
    }

    //TODO: Priority:low,   Type:New API Idea
    // Add ComplexProductMth-Quick along with  ComplexProductMth-lexOrder
    // algorithm will be same but we can ignore collections.reverse
    private List<T> getMth(BigInteger m) {
        List<T> output = new ArrayList<>();
        BigInteger remaining = m;
        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<T> e = builders.get(i);
            BigInteger[] division = remaining.divideAndRemainder(e.count());
            remaining = division[0];
            List<T> values = e.lexOrderMth( division[1], BigInteger.ZERO).build();
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

    @Override
    public String toString() {
        return "ComplexProductMth{" +
               "m=" + m +
               ", start=" + start +
               ", maxCount=" + maxCount +
               '}';
    }

    private class Itr implements Iterator<List<T>> {

        private BigInteger position = start;
        private final BigInteger memoizedMaxCount;

        public Itr() {
            memoizedMaxCount = maxCount();
        }

        @Override
        public boolean hasNext() {
            return position.compareTo(memoizedMaxCount) < 0;
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
