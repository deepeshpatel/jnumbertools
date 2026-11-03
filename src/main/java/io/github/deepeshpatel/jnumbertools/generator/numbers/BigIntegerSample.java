/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
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
 */
public class BigIntegerSample implements Iterable<BigInteger> {

    private final BigInteger max;
    private final int sampleSize;
    private final Random random;

    /**
     * Constructs a BigIntegerSample instance.
     *
     * @param max        the upper bound (exclusive) of the range [0, max); must be positive
     * @param sampleSize the number of unique random values to generate; must be positive and ≤ max
     * @param random the random generator to use
     * @throws IllegalArgumentException if max is not positive, sampleSize is not positive, or sampleSize > max
     */
    public BigIntegerSample(BigInteger max, int sampleSize, Random random) {
        parameterValidationForChoiceAndSample(max, sampleSize, random);

        if (BigInteger.valueOf(sampleSize).compareTo(max) > 0) {
            throw new IllegalArgumentException("Sample size (" + sampleSize + ") cannot exceed max (" + max + ")");
        }
        this.max = max;
        this.sampleSize = sampleSize;
        this.random = random;
    }

    static void parameterValidationForChoiceAndSample(BigInteger max, int sampleSize, Random random) {
        if (max.signum() <= 0) {
            throw new IllegalArgumentException("Cannot choose or sample from an empty domain (count() is 0)");
        }
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        if(random == null) throw new NullPointerException("Random should be not null for sampling");
    }

    /**
     * Returns an iterator that generates the specified number of unique random BigInteger values lazily.
     *
     * @return an iterator over unique BigInteger values
     */
    /**
     * Returns a uniformly distributed value in [0, bound) for any positive bound.
     * <p>
     * Uses {@link Random#nextLong(long, long)} when the bound fits in a long, otherwise rejection
     * sampling: random bytes are masked to {@code bound.bitLength()} bits so that at least half of
     * all candidates are accepted, whatever the bit length.
     * </p>
     *
     * @param bound  exclusive upper bound, must be positive
     * @param random source of randomness
     * @return a value v with 0 ≤ v &lt; bound
     */
    static BigInteger randomBelow(BigInteger bound, Random random) {
        if (bound.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return BigInteger.valueOf(random.nextLong(0, bound.longValueExact()));
        }
        int bitLength = bound.bitLength();
        BigInteger mask = BigInteger.ONE.shiftLeft(bitLength).subtract(BigInteger.ONE);
        byte[] bytes = new byte[(bitLength + 7) / 8];
        BigInteger candidate;
        do {
            random.nextBytes(bytes);
            candidate = new BigInteger(1, bytes).and(mask);
        } while (candidate.compareTo(bound) >= 0);
        return candidate;
    }

    @Override
    public Iterator<BigInteger> iterator() {
        if (max.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
            return new FisherYatesIterator(max.intValueExact(), sampleSize, random);
        } else {
            return new RetryBasedIterator(max, sampleSize, random);
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

    public FisherYatesIterator(int max, int sampleSize, Random random) {
        this.sampleSize = sampleSize;
        this.numbers = new int[sampleSize];
        this.currentIndex = 0;

        // Sparse (virtual-array) Fisher-Yates: only positions that have been swapped are stored,
        // so memory is O(sampleSize) instead of O(max). It performs exactly the same
        // random.nextInt(max - i) calls as the classic dense version, so output for a given
        // seed is unchanged.
        Map<Integer, Integer> swapped = new HashMap<>(Math.max(16, sampleSize * 2));
        for (int i = 0; i < sampleSize; i++) {
            int j = i + random.nextInt(max - i);
            int valueAtJ = swapped.getOrDefault(j, j);
            int valueAtI = swapped.getOrDefault(i, i);
            swapped.put(j, valueAtI); // position i is never read again, so it needs no update
            numbers[i] = valueAtJ;
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
    public RetryBasedIterator(BigInteger max, int sampleSize, Random random) {
        this.max = max;
        this.sampleSize = sampleSize;
        this.random = random;
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
     * Uses rejection sampling to ensure uniform distribution.
     *
     * @param bound the upper bound (exclusive); must be positive
     * @return a random BigInteger in [0, bound)
     */
    private BigInteger randomBigInteger(BigInteger bound) {
        return BigIntegerSample.randomBelow(bound, random);
    }
}