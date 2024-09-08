/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
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
 * Generates subsets of a set within a specified size range, but only produces every {@code m}-th subset
 * starting from a specified initial position. This class implements {@link MthElementGenerator} to provide
 * the {@code m}-th element efficiently.
 * <p>
 * This generator uses a more efficient approach to directly access the {@code m}-th subset without generating
 * all preceding subsets, which can be useful when dealing with large sets and ranges.
 * </p>
 *
 * <pre>
 *     Code example:
 *        new SubsetGeneratorMth&lt;&gt;(0, 2, 5, 0, List.of("A", "B", "C"), new Calculator())
 *            .forEach(System.out::println);
 *
 *   or
 *
 *        JNumberTools.of("A", "B", "C")
 *             .inRange(0, 2)
 *             .lexOrderMth(5, 0)
 *             .forEach(System.out::println);
 *
 * will generate every 5th subset within the size range 0 to 2, starting from the 0th subset.
 * </pre>
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
     * Constructs a {@link SubsetGeneratorMth} to generate every {@code m}-th subset within the specified size range,
     * starting from the specified initial position.
     *
     * @param from the minimum subset size (inclusive).
     * @param to the maximum subset size (inclusive).
     * @param m the increment to every m-th subset to be generated.
     * @param start the initial position for generating subsets.
     * @param elements the list of elements from which subsets are generated. Each item in the collection is treated as unique.
     * @param calculator the calculator used for combinatorial calculations.
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
     * Returns the {@code m}-th subset as a list of elements.
     * <p>
     * Use this method instead of the iterator if only a specific {@code m}-th value is needed rather than a sequence.
     * Creating an iterator is expensive because the {@code hasNext()} implementation requires expensive calculations.
     * </p>
     *
     * @return the {@code m}-th subset as a list of elements.
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
        return new Combinations(calculator).unique(size, r - 1).lexOrderMth(m, BigInteger.ZERO).build();
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
