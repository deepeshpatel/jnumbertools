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
 * Builder for generating multiset combinations with various sampling and ordering strategies.
 * <p>
 * This builder constructs {@link MultisetCombination} or {@link MultisetCombinationOfRanks} instances
 * to generate combinations of size r from a multiset of n distinct elements with specified multiplicities
 * (e.g., {A:2, B:1}). Combinations are output as frequency maps (e.g., {A:1, B:2}), where order does not
 * matter. The total number of combinations is given by the multichoose formula, constrained by
 * multiplicities. Supports lexicographical order (based on {@link LinkedHashMap} keys), random sampling
 * with/without replacement, every mᵗʰ combination, and custom rank sequences.
 * </p>
 *
 * @param <T> the type of elements in the combinations; must implement {@link Comparable}
 * @author Deepesh Patel
 */
public class MultisetCombinationBuilder<T> {

    private final LinkedHashMap<T, Integer> options;
    private final int r;

    /**
     * Constructs a builder for multiset combinations.
     *
     * @param options a map of n distinct elements to their multiplicities (must not be null or empty)
     * @param r       the size of each combination (r ≥ 0)
     * @throws IllegalArgumentException if options is null, empty, contains non-positive multiplicities,
     *         or if r < 0
     */
    public MultisetCombinationBuilder(LinkedHashMap<T, Integer> options, int r) {
        this.options = options;
        this.r = r;
    }

    /**
     * Generates all multiset combinations in lexicographical order.
     * <p>
     * The order is determined by the keys of the input {@link LinkedHashMap}, producing all
     * possible combinations (totaling the multichoose value, constrained by multiplicities).
     * </p>
     *
     * @return a {@link MultisetCombination} for iterating all combinations
     */
    public MultisetCombination<T> lexOrder() {
        return new MultisetCombination<>(options, r);
    }

    /**
     * Generates a random sample of multiset combinations without replacement.
     * <p>
     * Uses {@link BigIntegerSample} to select distinct combinations, up to the total number
     * of combinations (multichoose value, constrained by multiplicities).
     * </p>
     *
     * @param sampleSize the number of combinations to sample (1 ≤ sampleSize ≤ total combinations)
     * @return a {@link MultisetCombinationOfRanks} for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or exceeds total combinations
     */
    public MultisetCombinationOfRanks<T> sample(int sampleSize) {
        BigInteger total = Calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
        if (sampleSize <= 0 || BigInteger.valueOf(sampleSize).compareTo(total) > 0) {
            throw new IllegalArgumentException("Sample size must be positive and not exceed total combinations");
        }
        return new MultisetCombinationOfRanks<>(options, r, new BigIntegerSample(total, sampleSize));
    }

    /**
     * Generates a random sample of multiset combinations with replacement.
     * <p>
     * Uses {@link BigIntegerChoice} to select combinations, allowing duplicates, based on the
     * total number of combinations (multichoose value, constrained by multiplicities).
     * </p>
     *
     * @param sampleSize the number of combinations to sample (sampleSize ≥ 1)
     * @return a {@link MultisetCombinationOfRanks} for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0
     */
    public MultisetCombinationOfRanks<T> choice(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        BigInteger total = Calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
        return new MultisetCombinationOfRanks<>(options, r, new BigIntegerChoice(total, sampleSize));
    }

    /**
     * Generates every mᵗʰ multiset combination in lexicographical order, starting from a given rank.
     * <p>
     * Produces ranks: start, start+m, start+2m, ..., up to the total number of combinations
     * (multichoose value, constrained by multiplicities). The order is determined by the keys
     * of the input {@link LinkedHashMap}.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < total combinations)
     * @return a {@link MultisetCombinationOfRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0
     */
    public MultisetCombinationOfRanks<T> lexOrderMth(long m, long start) {
        if (m <= 0) {
            throw new IllegalArgumentException("Increment 'm' must be positive");
        }
        if (start < 0) {
            throw new IllegalArgumentException("Start rank must be non-negative");
        }
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ multiset combination in lexicographical order, starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce ranks: start, start+m, start+2m, ..., up to the
     * total number of combinations (multichoose value, constrained by multiplicities). The order
     * is determined by the keys of the input {@link LinkedHashMap}.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < total combinations)
     * @return a {@link MultisetCombinationOfRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0
     */
    public MultisetCombinationOfRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        if (m.signum() <= 0) {
            throw new IllegalArgumentException("Increment 'm' must be positive");
        }
        if (start.signum() < 0) {
            throw new IllegalArgumentException("Start rank must be non-negative");
        }
        BigInteger total = Calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, total);
        return new MultisetCombinationOfRanks<>(options, r, mthIterable);
    }

    /**
     * Generates multiset combinations based on a custom sequence of ranks.
     * <p>
     * Each rank in the sequence must be in [0, total combinations), where the total is the
     * multichoose value constrained by multiplicities.
     * </p>
     *
     * @param ranks the iterable of ranks (each in [0, total combinations))
     * @return a {@link MultisetCombinationOfRanks} for the custom sequence
     * @throws IllegalArgumentException if ranks is null
     */
    public MultisetCombinationOfRanks<T> withSequence(Iterable<BigInteger> ranks) {
        if (ranks == null) {
            throw new IllegalArgumentException("Ranks sequence cannot be null");
        }
        return new MultisetCombinationOfRanks<>(options, r, ranks);
    }
}