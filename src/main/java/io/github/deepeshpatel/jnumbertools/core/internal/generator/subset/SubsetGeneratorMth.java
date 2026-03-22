/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.subset;

import io.github.deepeshpatel.jnumbertools.core.external.Calculator;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.unique.UniqueCombinationBuilder;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates subsets of a set within a specified size range, producing only every mᵗʰ subset
 * (starting from a specified initial position) in lexicographical order.
 * <p>
 * This generator uses an efficient approach to directly access the mᵗʰ subset without generating
 * all preceding subsets, which can be beneficial when handling large sets and ranges.
 * </p>
 * <p>
 * <strong>Note:</strong> This class is intended to be constructed via a builder and does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the subsets
 * @author Deepesh Patel
 */
public final class SubsetGeneratorMth<T> extends AbstractGenerator<T> {

    private final BigInteger increment;
    private final Calculator calculator;
    private BigInteger limit;
    private final BigInteger initialM;
    private final int from;
    private final int to;

    /**
     * Constructs a {@code SubsetGeneratorMth} to generate every mᵗʰ subset within the specified size range,
     * starting from the specified initial position.
     * <p>
     * <strong>This constructor is intended for internal use only.</strong> Parameter validation (range,
     * non-negative values, m > 0, start < total, etc.) is performed in the {@link SubsetBuilder} methods
     * ({@code all()}, {@code inRange(int, int)}, {@code lexOrderMth(...)}) to ensure fail-fast behavior
     * at the builder level, before creating the generator instance.
     * </p>
     * <p>
     * Direct calls to this constructor bypass validation and are not part of the public API.
     * Always create instances via {@link SubsetBuilder} for correct behavior and safety.
     * </p>
     *
     * @param from       minimum subset size (inclusive); must be ≥ 0
     * @param to         maximum subset size (inclusive); must be ≥ from
     * @param m          step size for every mᵗʰ subset; must be > 0
     * @param start      starting rank (0-based); must be >= 0 and < total subsets in range
     * @param elements   list of elements from which subsets are generated (assumed unique)
     * @param calculator calculator for combinatorial operations
     */
    SubsetGeneratorMth(int from, int to, BigInteger m, BigInteger start, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.increment = m;
        this.calculator = calculator;
        initialM = start.add(totalSubsetsBeforeRange());
    }

    /**
     * Returns the mᵗʰ subset as a list of elements.
     * <p>
     * Use this method instead of the iterator if only a specific mᵗʰ subset is needed, rather than a full sequence.
     * This is useful because creating an iterator may involve expensive computations in {@code hasNext()}.
     * </p>
     *
     * @return the mᵗʰ subset as a list of elements
     */
    public List<T> build() {
        var mthIndices = mth(initialM);
        return indicesToValues(mthIndices.stream().mapToInt(Integer::intValue).toArray());
    }

    /**
     * Computes the indices of the mᵗʰ subset in lexicographical order.
     *
     * @param m the position of the subset to compute
     * @return the list of indices representing the mᵗʰ subset
     */
    private List<Integer> mth(BigInteger m) {
        int r = 0;
        int size = elements.size();
        BigInteger sum = BigInteger.ZERO;
        BigInteger prev = BigInteger.ZERO;

        for (; r < size && sum.compareTo(m) < 0; r++) {
            prev = sum;
            sum = sum.add(calculator.nCr(size, r));
        }

        if (sum.equals(m)) {
            return UniqueCombinationBuilder.newInstance(size, r, calculator).lexOrder().iterator().next();
        }

        m = m.subtract(prev);

        return UniqueCombinationBuilder.newInstance(size, r-1, calculator)
                .lexOrderMth(m, m).iterator().next();
    }

    /**
     * Calculates the total number of subsets before the specified range.
     *
     * @return the total number of subsets with sizes from 0 to from−1
     */
    private BigInteger totalSubsetsBeforeRange() {
        return calculator.totalSubsetsInRange(0, from - 1, elements.size());
    }

    @Override
    public synchronized Iterator<List<T>> iterator() {
        if (limit == null) {
            limit = calculator.totalSubsetsInRange(0, to, elements.size());
        }
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private BigInteger m = initialM;  // ← removed .subtract(increment)

        @Override
        public boolean hasNext() {
            return m.compareTo(limit) < 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            List<Integer> result = mth(m);
            m = m.add(increment);
            return indicesToValues(result.stream().mapToInt(Integer::intValue).toArray());
        }
    }
}
