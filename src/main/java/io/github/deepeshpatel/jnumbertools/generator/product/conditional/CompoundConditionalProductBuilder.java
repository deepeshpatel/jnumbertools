/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * Builder for Cartesian products constrained by composed {@link Condition} expressions.
 *
 * <p>Build trigger expressions from atomic conditions using nested {@code and(...)} and
 * {@code or(...)} calls, then attach the expression with {@code when(condition)}:</p>
 *
 * <pre>{@code
 * var trigger = condition(browser).is("Safari")
 *         .and(condition(os).is("Windows").or(condition(os).is("Linux")));
 *
 * var products = JNumberTools.cartesianProduct()
 *         .compoundProductOf(os, browser, database)
 *         .when(trigger)
 *         .then(database).isNot("PostgreSQL");
 * }</pre>
 *
 * <p>Every completed rule must hold. Compound rules support exact {@link #count()} and
 * {@link #isEmpty()} queries and lazy generation through {@link #generate()}. Generation
 * does not promise lexicographic encounter order during {@link #generate()}. Lexicographic traversal,
 * rank-based access, choice and sampling are also available. Compound rules use the DP lexicographic strategy.</p>
 *
 * <p>Dimensions are identified by the same list instances supplied to
 * {@code compoundProductOf(...)} or {@link #and(List)}. Dimension values are
 * copied when the builder is created. Instances are immutable and thread-safe.</p>
 *
 * @author Deepesh Patel
 */
public final class CompoundConditionalProductBuilder {

    private final ConditionalProductBuilder delegate;

    /**
     * Creates a compound conditional product builder with one dimension.
     *
     * @param elements first dimension; copied
     */
    public CompoundConditionalProductBuilder(List<?> elements) {
        this(new ConditionalProductBuilder(elements));
    }

    /**
     * Creates a compound conditional product builder with two or more dimensions, equivalent to
     * calling {@link #CompoundConditionalProductBuilder(List)} with {@code first} and then adding
     * each remaining dimension through {@link #and(List)}. The dimensions retain the supplied order.
     *
     * @param first the first dimension; copied
     * @param second the second dimension; copied
     * @param additional any further dimensions, in product order; each copied
     * @throws NullPointerException if any dimension is null or contains a null element
     */
    public CompoundConditionalProductBuilder(List<?> first, List<?> second, List<?>... additional) {
        this(new ConditionalProductBuilder(first, second, additional));
    }

    private CompoundConditionalProductBuilder(ConditionalProductBuilder delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns a new builder with another dimension appended.
     *
     * @param elements the next dimension; copied
     * @return a new immutable builder
     */
    public CompoundConditionalProductBuilder and(List<?> elements) {
        return new CompoundConditionalProductBuilder(delegate.and(elements));
    }

    /**
     * Starts a compound rule from a composed {@link Condition}.
     *
     * @param condition the trigger condition; dimensions inside it are identified by the same list
     *                  instances supplied to {@code compoundProductOf(...)} or {@link #and(List)}
     * @return the consequence step of the rule
     * @throws NullPointerException if {@code condition} is null
     * @throws IllegalArgumentException if a dimension referenced inside {@code condition} is not one of
     *                                  this product's dimensions, or a value is not a value of its dimension
     */
    public Trigger when(Condition condition) {
        return new Trigger(delegate.when(condition));
    }

    /** Consequence of a compound trigger expression. */
    public final class Trigger {
        private final ConditionalProductBuilder.Trigger step;

        private Trigger(ConditionalProductBuilder.Trigger step) {
            this.step = step;
        }

        public ThenDimension then(List<?> dimension) {
            return new ThenDimension(step.then(dimension));
        }
    }

    /** Consequence of a compound trigger expression. */
    public final class ThenDimension {
        private final ConditionalProductBuilder.ThenDimension step;

        private ThenDimension(ConditionalProductBuilder.ThenDimension step) {
            this.step = step;
        }

        public CompoundConditionalProductBuilder is(Object value) {
            return new CompoundConditionalProductBuilder(step.is(value));
        }
        public CompoundConditionalProductBuilder isNot(Object value) {
            return new CompoundConditionalProductBuilder(step.isNot(value));
        }
        public CompoundConditionalProductBuilder isGreaterThan(Object value) {
            return new CompoundConditionalProductBuilder(step.isGreaterThan(value));
        }
        public CompoundConditionalProductBuilder isGreaterThanOrEqual(Object value) {
            return new CompoundConditionalProductBuilder(step.isGreaterThanOrEqual(value));
        }
        public CompoundConditionalProductBuilder isLessThan(Object value) {
            return new CompoundConditionalProductBuilder(step.isLessThan(value));
        }
        public CompoundConditionalProductBuilder isLessThanOrEqual(Object value) {
            return new CompoundConditionalProductBuilder(step.isLessThanOrEqual(value));
        }
        public CompoundConditionalProductBuilder isOneOf(Object... values) {
            return new CompoundConditionalProductBuilder(step.isOneOf(values));
        }
        public CompoundConditionalProductBuilder isOneOf(java.util.Collection<?> values) {
            return new CompoundConditionalProductBuilder(step.isOneOf(values));
        }
        public CompoundConditionalProductBuilder isNoneOf(Object... values) {
            return new CompoundConditionalProductBuilder(step.isNoneOf(values));
        }
        public CompoundConditionalProductBuilder isNoneOf(java.util.Collection<?> values) {
            return new CompoundConditionalProductBuilder(step.isNoneOf(values));
        }
        public CompoundConditionalProductBuilder isNotIn(Object... values) {
            return new CompoundConditionalProductBuilder(step.isNotIn(values));
        }
        public CompoundConditionalProductBuilder isNotIn(java.util.Collection<?> values) {
            return new CompoundConditionalProductBuilder(step.isNotIn(values));
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

    /** Returns valid tuples in lexicographic order. Compound rules use the DP strategy. */
    public StreamableIterable<Object> lexOrder() {
        return delegate.lexOrder();
    }

    /** Returns valid tuples in lexicographic order using the requested strategy. */
    public StreamableIterable<Object> lexOrder(LexOrderStrategy strategy) {
        return delegate.lexOrder(strategy);
    }

    /** Returns valid tuples in lexicographic order using the adaptive strategy policy. */
    public StreamableIterable<Object> lexOrder(LexOrderStrategy strategy, long estimatedLimit) {
        return delegate.lexOrder(strategy, estimatedLimit);
    }

    /** Returns every m-th valid tuple in lexicographic rank order. */
    public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
        return delegate.lexOrderMth(m, start);
    }

    /** Resolves the supplied valid-tuple ranks lazily. */
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        return delegate.byRanks(ranks);
    }

    /** Uniform random choice with replacement. */
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        return delegate.choice(sampleSize, random);
    }

    /** Uniform random sampling without replacement. */
    public StreamableIterable<Object> sample(int sampleSize, Random random) {
        return delegate.sample(sampleSize, random);
    }

    /** Returns whether this product contains no valid tuples. */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
