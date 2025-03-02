/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.LinkedHashMap;

/**
 * Builder for generating multiset combinations with various sampling strategies and ordering options.
 * <p>
 * This builder generates combinations from a multiset defined by a {@link LinkedHashMap} of elements and their
 * frequencies. It supports generating all combinations, random sampling with or without replacement,
 * lexicographical sequences, or custom rank sequences. The order of elements in the combinations will be lex order
 * of keys in the {@link LinkedHashMap} parameter.
 * </p>
 *
 * @param <T> the type of elements in the combinations; must implement {@link Comparable}
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class MultisetCombinationBuilder<T> {

    private final LinkedHashMap<T, Integer> options;
    private final int size;


    /**
     * Constructs a new builder for multiset combinations
     *
     * @param options    a {@link LinkedHashMap} containing elements as keys and their frequencies as values
     * @param size       the size of each combination (r); must be non-negative
     * @throws IllegalArgumentException if {@code options} is null, empty, contains non-positive frequencies, or if {@code size} is negative
     */
    public MultisetCombinationBuilder(LinkedHashMap<T, Integer> options, int size) {
        this.options = options;
        this.size = size;
    }

    /**
     * Creates an instance that generates all multiset combinations
     * The order of combinations depends on the keys of {@link LinkedHashMap} provided during construction:
     *
     * @return a new {@link MultisetCombination} instance for iterating over all combinations
     */
    public MultisetCombination<T> lexOrder() {
        return new MultisetCombination<>(options, size);
    }

    /**
     * Creates an instance that samples multiset combinations randomly without replacement.
     *
     * @param sampleSize the number of combinations to generate; must be positive and ≤ total combinations
     * @return a new {@link MultisetCombinationForSequence} instance for random sampling
     * @throws IllegalArgumentException if {@code sampleSize} is not positive or exceeds total combinations
     */
    public MultisetCombinationForSequence<T> sample(int sampleSize) {
        BigInteger total = Calculator.multisetCombinationsCount(size, options.values().stream().mapToInt(Integer::intValue).toArray());
        if (sampleSize <= 0 || BigInteger.valueOf(sampleSize).compareTo(total) > 0) {
            throw new IllegalArgumentException("Sample size must be positive and not exceed total combinations");
        }
        return new MultisetCombinationForSequence<>(options, size, new BigIntegerSample(total, sampleSize));
    }

    /**
     * Creates an instance that samples multiset combinations randomly with replacement.
     *
     * @param sampleSize the number of combinations to generate; must be positive
     * @return a new {@link MultisetCombinationForSequence} instance for random sampling with replacement
     * @throws IllegalArgumentException if {@code sampleSize} is not positive
     */
    public MultisetCombinationForSequence<T> choice(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        BigInteger total = Calculator.multisetCombinationsCount(size, options.values().stream().mapToInt(Integer::intValue).toArray());
        return new MultisetCombinationForSequence<>(options, size, new BigIntegerChoice(total, sampleSize));
    }

    /**
     * Creates an instance that generates every mᵗʰ multiset combination based on the specified order, using long values.
     * <p>
     * The order of combinations depends on the keys of the {@link LinkedHashMap} provided during construction:
     * The {@code m} parameter specifies the increment between combination ranks, and {@code start} defines the starting rank (0-based).
     * </p>
     *
     * @param m     the increment between combination ranks; must be positive
     * @param start the starting rank (inclusive); must be non-negative
     * @return a new {@link MultisetCombinationForSequence} instance for generating every mᵗʰ combination
     * @throws IllegalArgumentException if {@code m} is not positive or {@code start} is negative
     */
    public MultisetCombinationForSequence<T> lexOrderMth(long m, long start) {
        if (m <= 0) {
            throw new IllegalArgumentException("Increment 'm' must be positive");
        }
        if (start < 0) {
            throw new IllegalArgumentException("Start rank must be non-negative");
        }
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates an instance that generates every mᵗʰ multiset combination based on the specified order.
     * <p>
     * The order of combinations depends on the keys of {@link LinkedHashMap} provided during construction:
     * The {@code m} parameter specifies the increment between combination ranks, and {@code start} defines the starting rank (0-based).
     * </p>
     *
     * @param m     the increment between combination ranks; must be positive
     * @param start the starting rank (inclusive); must be non-negative
     * @return a new {@link MultisetCombinationForSequence} instance for generating every mᵗʰ combination
     * @throws IllegalArgumentException if {@code m} is not positive or {@code start} is negative
     */
    public MultisetCombinationForSequence<T> lexOrderMth(BigInteger m, BigInteger start) {
        if (m.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Increment 'm' must be positive");
        }
        if (start.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Start rank must be non-negative");
        }
        BigInteger total = Calculator.multisetCombinationsCount(size, options.values().stream().mapToInt(Integer::intValue).toArray());
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, total);
        return new MultisetCombinationForSequence<>(options, size, mthIterable);
    }

    /**
     * Creates an instance that generates multiset combinations based on a custom sequence of ranks.
     *
     * @param ranks an iterable providing the sequence of ranks; each rank must be in [0, total combinations)
     * @return a new {@link MultisetCombinationForSequence} instance for custom rank-based generation
     * @throws IllegalArgumentException if {@code ranks} is null
     */
    public MultisetCombinationForSequence<T> withSequence(Iterable<BigInteger> ranks) {
        if (ranks == null) {
            throw new IllegalArgumentException("Ranks sequence cannot be null");
        }
        return new MultisetCombinationForSequence<>(options, size, ranks);
    }
}