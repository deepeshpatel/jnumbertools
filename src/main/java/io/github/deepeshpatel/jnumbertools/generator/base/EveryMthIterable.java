/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.Iterator;

/**
 * Iterable for generating every mᵗʰ rank in lexicographical order.
 *
 * Produces a sequence of ranks (BigInteger) starting from a given rank, incrementing by a step size,
 * up to an exclusive upper bound. Used by combinatorial generators (e.g., permutations, combinations)
 * to select every mᵗʰ element in lexicographical order. The iterator is not thread-safe; use separate
 * instances or synchronize access for concurrent use.
 *
 * Example usage:
 * ```
 * EveryMthIterable ranks = new EveryMthIterable(BigInteger.ZERO, BigInteger.valueOf(2), BigInteger.valueOf(6));
 * for (BigInteger rank : ranks) {
 *     System.out.println(rank); // Outputs: 0, 2, 4
 * }
 * ```
 *
 * @see io.github.deepeshpatel.jnumbertools.generator.base.Builder
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public class EveryMthIterable implements Iterable<BigInteger> {
    private final BigInteger start;
    private final BigInteger increment;
    private final BigInteger upperBound;

    /**
     * Constructs an iterable for generating every mᵗʰ rank.
     * <p>
     * Produces ranks starting from {@code start}, incrementing by {@code increment},
     * until reaching (but not including) {@code upperBound}.
     * </p>
     * <p>
     * This constructor does not perform parameter validation. Validation (m > 0, start ≥ 0, etc.)
     * is expected to be done by the caller (typically a {@link Builder} implementation) for fail-fast behavior.
     * Invalid parameters may lead to empty sequences, infinite loops, or unexpected behavior.
     * </p>
     *
     * @param start       the starting rank (should be ≥ 0)
     * @param increment   the step size between ranks (should be > 0)
     * @param upperBound  the exclusive upper limit for ranks (should be ≥ start)
     * @see EveryMthIterable#validateLexOrderMthParams(BigInteger, BigInteger)
     * @author Deepesh Patel
     */
    public EveryMthIterable(BigInteger start, BigInteger increment, BigInteger upperBound) {
        this.start = start;
        this.increment = increment;
        this.upperBound = upperBound;
    }

    /**
     * Returns an iterator for every mᵗʰ rank.
     *
     * Generates ranks from start to upperBound (exclusive), incrementing by increment.
     * The iterator is not thread-safe and should not be shared across threads.
     *
     * @return an Iterator<BigInteger> producing ranks
     */
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

    /**
     * Validates parameters commonly used in lexOrderMth(m, start) calls.
     * <p>
     * Builders should call this method before creating a generator
     * to ensure fail-fast validation.
     * </p>
     * @param m     increment/step size
     * @param start starting rank
     * @throws IllegalArgumentException if m is null or ≤ 0, or if start is null or < 0
     */
    public static void validateLexOrderMthParams(BigInteger m, BigInteger start) {
        if (m == null || m.signum() <= 0) {
            throw new IllegalArgumentException("Increment 'm' must be positive (m > 0)");
        }
        if (start == null || start.signum() < 0) {
            throw new IllegalArgumentException("Start position must be non-negative (start >= 0)");
        }
    }
}