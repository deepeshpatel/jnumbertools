/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.MthElementGenerator;

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
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class SubsetGeneratorMth<T> extends AbstractGenerator<T> implements MthElementGenerator<T> {

    private final BigInteger increment;
    private final Calculator calculator;
    private final Combinations combinations;
    private BigInteger limit;
    private final BigInteger initialM;
    private final int from;
    private final int to;

    /**
     * Constructs a {@code SubsetGeneratorMth} to generate every mᵗʰ subset within the specified size range,
     * starting from the specified initial position.
     *
     * @param from       the minimum subset size (inclusive)
     * @param to         the maximum subset size (inclusive)
     * @param m          the increment for selecting subsets (i.e. every mᵗʰ subset will be generated)
     * @param start      the initial position for generating subsets
     * @param elements   the list of elements from which subsets are generated (each item is treated as unique)
     * @param calculator the calculator used for combinatorial calculations
     */
    SubsetGeneratorMth(int from, int to, BigInteger m, BigInteger start, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.increment = m;
        this.calculator = calculator;
        combinations = new Combinations(calculator);
        initialM = start.add(increment).add(totalSubsetsBeforeRange());
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
            return combinations.unique(size, r).lexOrder().iterator().next();
        }

        m = m.subtract(prev);
        return new Combinations(calculator).unique(size, r - 1)
                .lexOrderMth(m, m).iterator().next();
    }

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

        private BigInteger m = initialM.subtract(increment);

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
