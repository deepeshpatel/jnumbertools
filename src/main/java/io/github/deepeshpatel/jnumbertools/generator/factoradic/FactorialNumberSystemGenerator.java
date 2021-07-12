/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.factoradic;

import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implements the iterable producing sequence of factoradic from a given
 * starting point(fromInclusive) upto toExclusive.
 * @author Deeepsh Patel
 */
public class FactorialNumberSystemGenerator implements Iterable<Factoradic> {

    private final Factoradic start;
    private final BigInteger endExclusive;

    /**
     * @param fromInclusive first(inclusive) factoradic to be generated in a sequence
     * @param toExclusive  the upper bound (exclusive). of the factoradic sequence
     */
    public FactorialNumberSystemGenerator(long fromInclusive, long toExclusive) {
        this(BigInteger.valueOf(fromInclusive),BigInteger.valueOf(toExclusive));
    }

    /**
     * @param fromInclusive first(inclusive) factoradic to be generated in a sequence
     * @param toExclusive  the upper bound (exclusive). of the factoradic sequence
     */
    public FactorialNumberSystemGenerator(BigInteger fromInclusive, BigInteger toExclusive) {
        if(fromInclusive.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("fromInclusive must be >=0 for Factorial Number System");
        }

        if(toExclusive.compareTo(fromInclusive) <= 0) {
            throw new IllegalArgumentException("toExclusive must be > fromInclusive");
        }

        start = new Factoradic(fromInclusive);
        this.endExclusive = toExclusive;
    }


    public Stream<Factoradic> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<Factoradic> iterator() {
        return new Itr();
    }


    private class Itr implements Iterator<Factoradic> {

        Factoradic current;

        public Itr() {
            this.current = start;
        }

        @Override
        public boolean hasNext() {
            return current.decimalValue().compareTo(endExclusive) < 0;
        }

        @Override
        public Factoradic next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            Factoradic old = current;
            current = current.add(1);
            return old;
        }
    }
}