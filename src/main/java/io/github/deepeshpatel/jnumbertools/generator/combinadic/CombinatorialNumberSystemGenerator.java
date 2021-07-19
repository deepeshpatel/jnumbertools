/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combinadic;

import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implements the iterable producing sequence of combinadics of given degree,
 * between start(inclusive) and end(inclusive) values
 *
 * <pre>
 *     Code example -
 *     new CombinatorialNumberSystemGenerator(5, 5001,5003)
 *          .forEach(System.out::println);
 *
 *     or
 *
 *     JNumberTools.combinadic(5,5001,5003)
 *          .forEach(System.out::println);
 *
 * will generate following(combinadics for 5001, 5002 and 5003) -
 * [16, 12, 10, 6, 3]
 * [16, 12, 10, 6, 4]
 * [16, 12, 10, 6, 5]
 * </pre>
 * @author Deepesh Patel
 */
public class CombinatorialNumberSystemGenerator implements Iterable<Combinadic> {

    private final Combinadic first;
    private final BigInteger end;

    /**
     * @param degree Degree of combinadic to be generated
     * @param startInclusive starting value(inclusive) of the combinadic sequence
     * @param endExclusive upper bound (exclusive) of the combinadic sequence
     */
    public CombinatorialNumberSystemGenerator(int degree, long startInclusive, long endExclusive) {
        this(degree, BigInteger.valueOf(startInclusive), BigInteger.valueOf(endExclusive));
    }

    public CombinatorialNumberSystemGenerator(int degree, BigInteger startInclusive, BigInteger endExclusive) {
        first = new Combinadic(startInclusive, degree);
        this.end = endExclusive;
    }

    public Stream<Combinadic> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<Combinadic> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<Combinadic> {

        Combinadic current;
        BigInteger start;

        private Itr(){
            current = first;
            start = first.decimalValue();
        }

        @Override
        public boolean hasNext() {
            return start.compareTo(end) < 0;
        }

        @Override
        public Combinadic next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            Combinadic old = current;
            current = current.nextCombinadic();
            start = start.add(BigInteger.ONE);
            return old;
        }
    }
}