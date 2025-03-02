/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.numbers;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * An iterable that generates a fixed number of random BigInteger values within a specified range [0, max),
 * sampling with replacement.
 * <p>
 * This class generates exactly {@code sampleSize} random numbers from the range [0, max) lazily in the
 * {@code next()} method of the iterator. Each value is chosen independently, allowing duplicates.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class BigIntegerChoice implements Iterable<BigInteger> {

    private final BigInteger max;
    private final int sampleSize;

    /**
     * Constructs a BigIntegerChoice instance.
     *
     * @param max        the upper bound (exclusive) of the range [0, max); must be positive
     * @param sampleSize the number of random values to generate; must be positive
     * @throws IllegalArgumentException if max is not positive or sampleSize is not positive
     */
    public BigIntegerChoice(BigInteger max, int sampleSize) {
        if (max.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Max must be positive");
        }
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        this.max = max;
        this.sampleSize = sampleSize;
    }

    /**
     * Returns an iterator that generates the specified number of random BigInteger values with replacement.
     *
     * @return an iterator over random BigInteger values
     */
    @Override
    public Iterator<BigInteger> iterator() {
        return new BigIntegerChoiceIterator(max, sampleSize);
    }
}

/**
 * Iterator that generates a fixed number of random BigInteger values with replacement on-demand.
 */
class BigIntegerChoiceIterator implements Iterator<BigInteger> {

    private final BigInteger max;
    private final int sampleSize;
    private final Random random;
    private int generatedCount;

    /**
     * Constructs a BigIntegerChoiceIterator with minimal initialization.
     *
     * @param max        the upper bound (exclusive) of the range [0, max)
     * @param sampleSize the number of values to generate
     */
    public BigIntegerChoiceIterator(BigInteger max, int sampleSize) {
        this.max = max;
        this.sampleSize = sampleSize;
        this.random = new Random();
        this.generatedCount = 0;
    }

    /**
     * Checks if there are more values to generate.
     *
     * @return {@code true} if fewer than sampleSize values have been generated; {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return generatedCount < sampleSize;
    }

    /**
     * Generates the next random BigInteger value.
     *
     * @return the next BigInteger in the sequence
     * @throws NoSuchElementException if no more values are available
     */
    @Override
    public BigInteger next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more values available");
        }

        BigInteger value = randomBigInteger(max);
        generatedCount++;
        return value;
    }

    /**
     * Generates a random BigInteger in [0, bound).
     *
     * @param bound the upper bound (exclusive)
     * @return a random BigInteger in [0, bound)
     */
    private BigInteger randomBigInteger(BigInteger bound) {
        if (bound.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return BigInteger.valueOf(random.nextLong(0, bound.longValueExact()));
        }
        return BigInteger.valueOf(random.nextLong()).abs().mod(bound);
    }
}