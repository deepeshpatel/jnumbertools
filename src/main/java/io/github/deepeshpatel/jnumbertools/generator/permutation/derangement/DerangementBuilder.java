/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Builder for generating <strong>derangements</strong> (fixed-point-free
 * permutations) of a list of elements.
 *
 * <p>
 * A derangement is a permutation in which no element appears in its original
 * position. The total number of derangements of {@code n} distinct elements is
 * the subfactorial {@code D_n = !n}.
 * </p>
 *
 * <h2>Generation Strategies</h2>
 * <ul>
 *   <li><b>Lexicographical Order</b> — {@link #lexOrder()}: yields all
 *       {@code D_n} derangements, backed by the high-performance incremental
 *       {@link io.github.deepeshpatel.jnumbertools.numbersystem.Derangadic#next()
 *       Derangadic.next()} successor engine.</li>
 *   <li><b>Every m<sup>th</sup></b> — {@link #lexOrderMth(BigInteger, BigInteger)}:
 *       yields derangements at ranks {@code start, start+m, start+2m, …}.</li>
 *   <li><b>By explicit ranks</b> — {@link #byRanks(Iterable)}.</li>
 *   <li><b>Random sampling</b> — {@link #sample(int, java.util.Random)}
 *       (without replacement) and {@link #choice(int, java.util.Random)}
 *       (with replacement).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * Calculator calc = new Calculator();
 *
 * // All derangements of {A, B, C, D}
 * new Derangements(calc).of("A", "B", "C", "D")
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // Every 3rd derangement of {0..9} starting from rank 0
 * new Derangements(calc).of(10)
 *     .lexOrderMth(3, 0)
 *     .forEach(System.out::println);
 *
 * // 5 random derangements without replacement
 * new Derangements(calc).of("A", "B", "C", "D", "E")
 *     .sample(5)
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h2>Edge cases</h2>
 * <ul>
 *   <li>{@code n = 0}: {@link #count()} returns 1, lexOrder yields a single
 *       empty list.</li>
 *   <li>{@code n = 1}: {@link #count()} returns 0, lexOrder yields nothing
 *       (no derangement exists).</li>
 * </ul>
 *
 * <p>This builder is immutable. Configuration methods return new generator
 * instances.</p>
 *
 * @param <T> the element type
 * @author Deepesh Patel &amp; Aditya Patel
 * @see Derangement
 * @see DerangementByRanks
 * @see io.github.deepeshpatel.jnumbertools.numbersystem.Derangadic
 * @since 3.0.2
 */
public final class DerangementBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a builder for derangements of the given elements.
     *
     * @param calculator the calculator (memoizes subfactorial / restricted-derangement counts)
     * @param elements   the list of distinct elements to derange; {@code null}
     *                   is treated as the empty list
     */
    public DerangementBuilder(Calculator calculator, List<T> elements) {
        this.calculator = calculator;
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
    }

    /**
     * Generates all derangements in lexicographical order.
     *
     * @return a {@link Derangement} generator
     */
    @Override
    public Derangement<T> lexOrder() {
        return new Derangement<>(elements, calculator);
    }

    /**
     * Generates every m<sup>th</sup> derangement in lexicographical order,
     * starting at {@code start}.
     *
     * @param m     the step size (must be {@code > 0})
     * @param start the starting rank ({@code 0 ≤ start < D_n} when {@code D_n > 0})
     * @return a {@link DerangementByRanks} generator
     * @throws IllegalArgumentException if {@code m ≤ 0}, {@code start < 0}, or
     *         {@code start ≥ D_n} (with {@code D_n > 0})
     */
    @Override
    public DerangementByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, count());
        return new DerangementByRanks<>(elements, new EveryMthIterable(start, m, count()), calculator);
    }

    /**
     * Generates derangements at the specified lexicographical ranks.
     *
     * @param ranks the rank sequence (each {@code 0 ≤ rank < D_n})
     * @return a {@link DerangementByRanks} generator
     * @throws IllegalArgumentException if {@code ranks} is {@code null}; invalid
     *         individual ranks are reported during iteration
     */
    @Override
    public DerangementByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new DerangementByRanks<>(elements, ranks, calculator);
    }

    /**
     * Samples {@code sampleSize} derangements <em>without replacement</em>
     * using the given random generator.
     */
    @Override
    public DerangementByRanks<T> sample(int sampleSize, Random random) {
        return new DerangementByRanks<>(elements, new BigIntegerSample(count(), sampleSize, random), calculator);
    }

    /**
     * Samples {@code sampleSize} derangements <em>with replacement</em>
     * (duplicates allowed) using the given random generator.
     */
    @Override
    public DerangementByRanks<T> choice(int sampleSize, Random random) {
        return new DerangementByRanks<>(elements, new BigIntegerChoice(count(), sampleSize, random), calculator);
    }

    /**
     * @return the total number of derangements {@code D_n = !n} of the
     *         configured element list
     */
    @Override
    public BigInteger count() {
        return calculator.subFactorial(elements.size());
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public String toString() {
        return "DerangementBuilder{elements=" + elements + ", count=" + count() + '}';
    }
}

