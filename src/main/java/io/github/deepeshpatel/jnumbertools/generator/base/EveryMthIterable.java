/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.Iterator;

// EveryMthIterable (simple implementation for lexicographical sequence)
public class EveryMthIterable implements Iterable<BigInteger> {
    private final BigInteger start;
    private final BigInteger increment;
    private final BigInteger upperBound;

    public EveryMthIterable(BigInteger start, BigInteger increment, BigInteger upperBound) {
        if (increment.signum() < 0) {
            throw new IllegalArgumentException("increment value should be > 0");
        }
        this.start = start;
        this.increment = increment;
        this.upperBound = upperBound;
    }

    @Override
    public Iterator<BigInteger> iterator() {
        return new Iterator<>() {
            private BigInteger current = start;

            @Override
            public boolean hasNext() {
                return current.compareTo(upperBound) < 0;
            }

            @Override
            public BigInteger next() {
                BigInteger result = current;
                current = current.add(increment);
                return result;
            }
        };
    }
}