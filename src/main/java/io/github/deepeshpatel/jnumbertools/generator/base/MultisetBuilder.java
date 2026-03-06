/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.Map;

/**
 * Interface for builders that generate multiset combinations (frequency maps).
 * <p>
 * Similar to {@link Builder}, but specialized for multiset combinations where each element is a
 * {@link Map <T,Integer>} representing frequencies of elements in the combination.
 * </p>
 * <p>
 * All methods return generators producing {@link Map <T,Integer>} (immutable frequency maps).
 * </p>
 * <p>
 * Implementations are expected to be immutable and thread-safe. Configuration methods should return new instances.
 * </p>
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 * @see Builder
 */
public interface MultisetBuilder<T> {

    /**
     * Generates all multiset combinations in lexicographical order.
     *
     * @return a generator producing all multiset combinations as frequency maps
     */
    Iterable<Map<T, Integer>> lexOrder();

    /**
     * Convenience overload for {@link #lexOrderMth(BigInteger, BigInteger)} using long values.
     */
    default Iterable<Map<T, Integer>> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ multiset combination in lexicographical order, starting from a given rank.
     *
     * @param m     the increment (m > 0)
     * @param start the starting rank (start >= 0)
     * @return a generator producing every mᵗʰ combination
     * @throws IllegalArgumentException if m <= 0 or start < 0
     */
    Iterable<Map<T, Integer>> lexOrderMth(BigInteger m, BigInteger start);

    /**
     * Generates multiset combinations at the specified ranks.
     * <p>
     * Ranks are 0-based in lexicographical order.
     * </p>
     *
     * @param ranks the sequence of ranks (each must be >= 0 and < total)
     * @return a generator producing combinations at the given ranks
     */
    Iterable<Map<T, Integer>> byRanks(Iterable<BigInteger> ranks);

    /**
     * Generates a random sample of multiset combinations with replacement.
     *
     * @param sampleSize number of combinations to generate (must be > 0)
     * @return a generator producing random combinations (duplicates allowed)
     * @throws IllegalArgumentException if sampleSize <= 0
     */
    Iterable<Map<T, Integer>> choice(int sampleSize);

    /**
     * Generates a random sample of unique multiset combinations without replacement.
     *
     * @param sampleSize number of combinations to generate (must be > 0 and <= total)
     * @return a generator producing unique random combinations
     * @throws IllegalArgumentException if sampleSize <= 0 or exceeds total
     */
    Iterable<Map<T, Integer>> sample(int sampleSize);

    /**
     * Returns the total number of possible multiset combinations.
     *
     * @return the multichoose value constrained by multiplicities
     */
    BigInteger count();
}