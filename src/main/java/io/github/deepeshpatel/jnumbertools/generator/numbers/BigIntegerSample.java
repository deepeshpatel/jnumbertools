/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.numbers;

import java.math.BigInteger;
import java.util.*;

/**
 * An iterable that generates a fixed number of unique random BigInteger values within a specified range [0, max),
 * sampling without replacement.
 * <p>
 * This class generates exactly {@code sampleSize} unique random numbers from the range [0, max) lazily in the
 * {@code next()} method. For small ranges (max ≤ Integer.MAX_VALUE), it uses a Fisher-Yates shuffle on an array for
 * efficiency. For larger ranges, it uses a retry-based approach with a set of used values.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class BigIntegerSample implements Iterable<BigInteger> {

    private final BigInteger max;
    private final int sampleSize;

    /**
     * Constructs a BigIntegerSample instance.
     *
     * @param max        the upper bound (exclusive) of the range [0, max); must be positive
     * @param sampleSize the number of unique random values to generate; must be positive and ≤ max
     * @throws IllegalArgumentException if max is not positive, sampleSize is not positive, or sampleSize > max
     */
    public BigIntegerSample(BigInteger max, int sampleSize) {
        if (max.signum() <= 0) {
            throw new IllegalArgumentException("Max must be positive");
        }
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        if (BigInteger.valueOf(sampleSize).compareTo(max) > 0) {
            throw new IllegalArgumentException("Sample size (" + sampleSize + ") cannot exceed max (" + max + ")");
        }
        this.max = max;
        this.sampleSize = sampleSize;
    }

    /**
     * Returns an iterator that generates the specified number of unique random BigInteger values lazily.
     *
     * @return an iterator over unique BigInteger values
     */
    @Override
    public Iterator<BigInteger> iterator() {
        if (max.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
            return new FisherYatesIterator(max.intValueExact(), sampleSize);
        } else {
            return new RetryBasedIterator(max, sampleSize);
        }
    }
}

/**
 * Iterator that uses Fisher-Yates shuffle for small ranges (max ≤ Integer.MAX_VALUE).
 */
class FisherYatesIterator implements Iterator<BigInteger> {
    private final int[] numbers;
    private int currentIndex;
    private final int sampleSize;

    public FisherYatesIterator(int max, int sampleSize) {
        this.sampleSize = sampleSize;
        Random  random = new Random();
        this.numbers = new int[sampleSize];
        this.currentIndex = 0;

        // Initialize with unique values from [0, max)
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < sampleSize; i++) {
            int value;
            do {
                value = random.nextInt(max);
            } while (!used.add(value));
            numbers[i] = value;
        }
    }

    @Override
    public boolean hasNext() {
        return currentIndex < sampleSize;
    }

    @Override
    public BigInteger next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more unique values available");
        }
        return BigInteger.valueOf(numbers[currentIndex++]);
    }
}

/**
 * Iterator that uses a retry-based approach with a set for large ranges (max > Integer.MAX_VALUE).
 */
class RetryBasedIterator implements Iterator<BigInteger> {

    private final BigInteger max;
    private final int sampleSize;
    private final Random random;
    private final Set<BigInteger> usedValues;
    private int generatedCount;

    /**
     * Constructs a RetryBasedIterator with minimal initialization.
     *
     * @param max        the upper bound (exclusive)
     * @param sampleSize the number of unique values to generate
     */
    public RetryBasedIterator(BigInteger max, int sampleSize) {
        this.max = max;
        this.sampleSize = sampleSize;
        this.random = new Random();
        this.usedValues = new HashSet<>();
        this.generatedCount = 0;
    }

    @Override
    public boolean hasNext() {
        return generatedCount < sampleSize;
    }

    @Override
    public BigInteger next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more unique values available");
        }

        BigInteger remaining = max.subtract(BigInteger.valueOf(usedValues.size()));
        if (remaining.signum() <= 0) {
            throw new NoSuchElementException("Cannot generate more unique values; range exhausted");
        }

        BigInteger candidate;
        do {
            candidate = randomBigInteger(max);
        } while (!usedValues.add(candidate));

        generatedCount++;
        return candidate;
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


    public static void main(String[] args) {
        // Small range (uses Fisher-Yates)
        BigIntegerSample smallSampler = new BigIntegerSample(BigInteger.valueOf(10), 4);
        Iterator<BigInteger> smallIterator = smallSampler.iterator();
        System.out.println("Small range:");
        while (smallIterator.hasNext()) {
            System.out.println(smallIterator.next());
        }

        // Large range (uses retry-based)
        BigIntegerSample largeSampler = new BigIntegerSample(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE), 4);
        Iterator<BigInteger> largeIterator = largeSampler.iterator();
        System.out.println("\nLarge range:");
        while (largeIterator.hasNext()) {
            System.out.println(largeIterator.next());
        }
    }
}