/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Builder for Cartesian products constrained by a raw joint predicate over several dimensions, for logic
 * that a {@link Condition}'s equality/membership leaves cannot express — arithmetic or relational logic
 * spanning two or more dimensions at once.
 *
 * <pre>{@code
 * var products = JNumberTools.cartesianProduct()
 *         .withLambda(cores, memoryGb, budget)
 *         .when(cores, memoryGb).matches((c, m) -> c * m <= 256)
 *         .andWhen(memoryGb, budget).matches((m, b) -> m * 4 <= b);
 * }</pre>
 *
 * <h2>How this differs from {@link CompoundConditionalProductBuilder}</h2>
 * <p>Each {@code when(...).matches(...)} call is compiled once, when it is added: every combination of
 * the named dimensions' values is enumerated and the predicate is evaluated against it, bounded the same
 * way a compound rule's scope is bounded (see {@code ConditionalProductBuilder}'s class Javadoc). The
 * result is compiled into the same multi-trigger rule representation {@link Condition} resolves into, so
 * generation, {@link #count()} and {@link #isEmpty()} never call the predicate again — they read the
 * precomputed table, exactly as they would for a compound rule. The predicate itself may express anything
 * a {@code Condition} cannot (arbitrary Java code over the joint values), at the cost of being opaque to
 * static inspection: unlike a {@code Condition}, a lambda cannot be decomposed or displayed by a future
 * diagnostic/explain tool.</p>
 *
 * <p>Every completed rule must hold; rules are combined with AND, like {@code compoundProductOf(...)}'s.
 * Compound rules support exact {@link #count()} and {@link #isEmpty()} queries and lazy generation through
 * {@link #generate()}; generation does not promise lexicographic encounter order, and rank-based traversal
 * and sampling are intentionally not exposed, matching {@link CompoundConditionalProductBuilder}.</p>
 *
 * <p>Dimensions are identified by the same list instances supplied to {@code withLambda(...)} or
 * {@link #and(List)}. Dimension values are copied when the builder is created. Instances are immutable
 * and thread-safe.</p>
 *
 * @author Deepesh Patel
 */
public final class LambdaConditionalProductBuilder {

    private final ConditionalProductBuilder delegate;

    /**
     * Creates a lambda-constrained product builder with one dimension.
     *
     * @param elements first dimension; copied
     */
    public LambdaConditionalProductBuilder(List<?> elements) {
        this(new ConditionalProductBuilder(elements));
    }

    /**
     * Creates a lambda-constrained product builder with two or more dimensions, equivalent to calling
     * {@link #LambdaConditionalProductBuilder(List)} with {@code first} and then adding each remaining
     * dimension through {@link #and(List)}. The dimensions retain the supplied order.
     *
     * @param first the first dimension; copied
     * @param second the second dimension; copied
     * @param additional any further dimensions, in product order; each copied
     * @throws NullPointerException if any dimension is null or contains a null element
     */
    public LambdaConditionalProductBuilder(List<?> first, List<?> second, List<?>... additional) {
        this(new ConditionalProductBuilder(first, second, additional));
    }

    private LambdaConditionalProductBuilder(ConditionalProductBuilder delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns a new builder with another dimension appended.
     *
     * @param elements the next dimension; copied
     * @return a new immutable builder
     */
    public LambdaConditionalProductBuilder and(List<?> elements) {
        return new LambdaConditionalProductBuilder(delegate.and(elements));
    }

    /**
     * Starts a rule whose predicate will receive one argument per dimension here, in this order — the
     * uncapped form, for any number of dimensions.
     *
     * <p>A rule must name at least two dimensions: one antecedent (the {@code when} side) and one target
     * (the last dimension in {@code scope}). A single-dimension {@link Condition} can be used as
     * the trigger for a compound-product rule on a separate target dimension; that constrains the target,
     * not the trigger dimension itself. To filter values from one dimension, pre-filter its input list.
     * Single-dimension scopes are rejected because
     * {@code ConstraintPlan} does not run rules through its node enumeration for a single-dimension node,
     * so such a rule would be enforced during traversal but ignored by {@code count()} and rank-based
     * operations.</p>
     *
     * @param scope the dimensions the predicate jointly depends on, in argument order; each the same
     *              list instance supplied to {@code withLambda(...)} or {@link #and(List)}
     * @return the next step, which takes the predicate itself
     * @throws NullPointerException if {@code scope} or any element is null
     * @throws IllegalArgumentException if {@code scope} has fewer than two dimensions, a dimension is not
     *                                  one of this product's, or the same dimension appears twice
     */
    public Scope when(List<?>... scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        if (scope.length < 2) {
            throw new IllegalArgumentException("A lambda rule must name at least two dimensions "
                    + "(one antecedent and one target); to filter a single dimension, pre-filter its "
                    + "input list before building the product");
        }
        int[] indexes = resolve(scope);
        return new Scope(indexes);
    }

    /**
     * Alias of {@link #when(List[])}, for fluent declaration of another rule after the first.
     */
    public Scope andWhen(List<?>... scope) {
        return when(scope);
    }

    /**
     * Starts a rule over exactly two dimensions, with a typed {@link BiPredicate} rather than the
     * uncapped {@code Object[]} form.
     *
     * @param first the first dimension the predicate depends on
     * @param second the second dimension the predicate depends on
     * @return the next step, typed to {@code first} and {@code second}'s element types
     * @throws NullPointerException if either dimension is null
     * @throws IllegalArgumentException if either dimension is not one of this product's, or the two
     *                                  dimensions are the same
     */
    public <A, B> Scope2<A, B> when(List<A> first, List<B> second) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        int[] indexes = resolve(new List<?>[] {first, second});
        return new Scope2<>(indexes);
    }

    /** Alias of {@link #when(List, List)}, for fluent declaration of another rule after the first. */
    public <A, B> Scope2<A, B> andWhen(List<A> first, List<B> second) {
        return when(first, second);
    }

    private int[] resolve(List<?>[] scope) {
        if (scope.length == 0) {
            throw new IllegalArgumentException("A lambda rule must depend on at least one dimension");
        }
        int[] indexes = new int[scope.length];
        for (int i = 0; i < scope.length; i++) {
            Objects.requireNonNull(scope[i], "a scope dimension must not be null");
            indexes[i] = delegate.indexOfDimension(scope[i]);
        }
        return indexes;
    }

    /** The uncapped next step after {@link #when(List[])}: supply the predicate itself. */
    public final class Scope {
        private final int[] indexes;

        private Scope(int[] indexes) {
            this.indexes = indexes;
        }

        /**
         * Adds this rule. The predicate receives one argument per scope dimension, in the order
         * {@link #when(List[])} was given them, and is evaluated once per combination of the scope's
         * values during compilation of this call, never during generation.
         *
         * @param predicate the joint predicate; must be deterministic and side-effect-free
         * @return a new builder with this rule added
         * @throws NullPointerException if {@code predicate} is null
         * @throws IllegalArgumentException if compiling this rule would need to evaluate the predicate
         *                                  more times than the configured limit allows
         */
        public LambdaConditionalProductBuilder matches(Predicate<Object[]> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new LambdaConditionalProductBuilder(delegate.withLambdaRule(indexes, predicate));
        }
    }

    /** The typed next step after {@link #when(List, List)}: supply a {@link BiPredicate}. */
    public final class Scope2<A, B> {
        private final int[] indexes;

        private Scope2(int[] indexes) {
            this.indexes = indexes;
        }

        /**
         * Adds this rule, typed to the two dimensions named in {@link #when(List, List)}.
         *
         * @param predicate the joint predicate; must be deterministic and side-effect-free
         * @return a new builder with this rule added
         * @throws NullPointerException if {@code predicate} is null
         * @throws IllegalArgumentException if compiling this rule would need to evaluate the predicate
         *                                  more times than the configured limit allows
         */
        @SuppressWarnings("unchecked")
        public LambdaConditionalProductBuilder matches(BiPredicate<? super A, ? super B> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Predicate<Object[]> untyped = args -> predicate.test((A) args[0], (B) args[1]);
            return new LambdaConditionalProductBuilder(delegate.withLambdaRule(indexes, untyped));
        }
    }

    /**
     * Lazily generates every valid tuple exactly once. Encounter order is unspecified.
     *
     * @return a re-iterable generator of valid tuples
     */
    public StreamableIterable<Object> generate() {
        return delegate.anyOrder();
    }

    /** Returns the exact number of valid tuples without generating them. */
    public BigInteger count() {
        return delegate.count();
    }

    /** Returns whether this product contains no valid tuples. */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
