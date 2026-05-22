/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.derangement.DerangementBuilder;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Factory for generating <strong>derangements</strong> — permutations in which
 * no element ends up in its original position.
 *
 * <p>
 * A derangement of {@code n} distinct elements exists in {@code D_n = !n}
 * arrangements (the subfactorial). This class is the user-facing entry point
 * to the derangement generator, mirroring the role of {@link Permutations} and
 * {@link Combinations} for their respective combinatorial families.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>
 * Derangements der = new Derangements();
 *
 * // All derangements of [0, 1, 2, 3] in lexicographical order
 * der.of(4)
 *    .lexOrder()
 *    .forEach(System.out::println);
 *
 * // All derangements of {A, B, C, D}
 * der.of("A", "B", "C", "D")
 *    .lexOrder()
 *    .forEach(System.out::println);
 *
 * // Every 3rd derangement starting from rank 1
 * der.of(5)
 *    .lexOrderMth(3, 1)
 *    .forEach(System.out::println);
 *
 * // 5 random derangements without replacement
 * der.of("A", "B", "C", "D", "E")
 *    .sample(5)
 *    .forEach(System.out::println);
 * </pre>
 *
 * <h2>Performance</h2>
 * <p>
 * {@link DerangementBuilder#lexOrder()} is backed by the incremental
 * {@link io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.Derangadic#next()
 * Derangadic.next()} successor engine.
 * </p>
 *
 * <p>This class is immutable and thread-safe. All factory methods return new
 * builder instances.</p>
 *
 * @see AllExamples
 * @see Permutations
 * @see Combinations
 * @see io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.Derangadic
 * @author Deepesh Patel &amp; Aditya Patel
 * @since 3.0.2
 */
public final class Derangements {

    private final Calculator calculator;

    /** Constructs a {@code Derangements} factory with a fresh {@link Calculator}. */
    public Derangements() {
        this(new Calculator());
    }

    /**
     * Constructs a {@code Derangements} factory with the supplied
     * {@link Calculator}. Reuse a single {@code Calculator} across the
     * different facade entry points to share memoization.
     */
    public Derangements(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for derangements of {@code {0, 1, …, n-1}}.
     *
     * @param n the number of elements ({@code n ≥ 0})
     * @return a {@link DerangementBuilder} over the integer range
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public DerangementBuilder<Integer> of(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be ≥ 0 for derangement generation");
        }
        return of(IntStream.range(0, n).boxed().toList());
    }

    /**
     * Creates a builder for derangements of the given list.
     *
     * @param list the list of distinct elements (each treated as unique by position)
     * @param <T>  the element type
     * @return a {@link DerangementBuilder} over {@code list}
     * @throws NullPointerException if {@code list} is {@code null}
     */
    public <T> DerangementBuilder<T> of(List<T> list) {
        Util.validateInput(list);
        return new DerangementBuilder<>(calculator, list);
    }

    /**
     * Creates a builder for derangements of the given varargs.
     *
     * @param array the elements to derange
     * @param <T>   the element type
     * @return a {@link DerangementBuilder} over {@code array}
     */
    @SafeVarargs
    public final <T> DerangementBuilder<T> of(T... array) {
        return of(List.of(array));
    }
}

