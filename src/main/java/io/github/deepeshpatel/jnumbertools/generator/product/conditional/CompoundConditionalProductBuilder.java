/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * Builder for Cartesian products constrained by compound trigger expressions.
 *
 * <p>Use {@code when(...)} to start a trigger, {@code and(...)} to add a condition to the
 * current conjunction, and {@code or(...)} to start an alternative conjunction. AND binds
 * more tightly than OR. For example:</p>
 *
 * <pre>{@code
 * var products = JNumberTools.cartesianProduct()
 *         .compoundProductOf(os, browser, database)
 *         .when(browser).is("Safari").and(os).is("Windows")
 *         .or(browser).is("Chrome").and(os).is("Linux")
 *         .then(database).isNot("PostgreSQL")
 *         .generate();
 * }</pre>
 *
 * <p>Every completed rule must hold. Compound rules support exact {@link #count()} and
 * {@link #isEmpty()} queries and lazy generation through {@link #generate()}. Generation
 * does not promise lexicographic encounter order. Rank-based traversal and sampling
 * operations are intentionally not exposed by this builder.</p>
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
     * Starts a compound rule.
     *
     * @param dimension a dimension supplied to this builder
     * @return the first trigger condition
     */
    public WhenDimension when(List<?> dimension) {
        return new WhenDimension(delegate.when(dimension));
    }

    /**
     * Starts a compound rule from a composed {@link Condition}, built with nested {@code and(...)}/
     * {@code or(...)} calls rather than the flat {@code .when(dim)...and(dim2)...or(dim3)...} chain.
     * See {@link Condition} for why this allows nesting the flat chain cannot express, such as
     * {@code A and (B or C)}.
     *
     * @param condition the trigger condition; dimensions inside it are identified by the same list
     *                   instances supplied to {@code compoundProductOf(...)} or {@link #and(List)}
     * @return the consequence step of the rule
     * @throws NullPointerException     if {@code condition} is null
     * @throws IllegalArgumentException if a dimension referenced inside {@code condition} is not one of
     *                                  this product's dimensions, or a value is not a value of its dimension
     */
    public Trigger when(Condition condition) {
        return new Trigger(delegate.when(condition));
    }

    /** First condition of a trigger expression. */
    public final class WhenDimension {
        private final ConditionalProductBuilder.WhenDimension step;

        private WhenDimension(ConditionalProductBuilder.WhenDimension step) {
            this.step = step;
        }

        public Trigger is(Object value) { return new Trigger(step.is(value)); }
        public Trigger isNot(Object value) { return new Trigger(step.isNot(value)); }
        public Trigger isOneOf(Object... values) { return new Trigger(step.isOneOf(values)); }
        public Trigger isOneOf(Collection<?> values) { return new Trigger(step.isOneOf(values)); }
        public Trigger isNoneOf(Object... values) { return new Trigger(step.isNoneOf(values)); }
        public Trigger isNoneOf(Collection<?> values) { return new Trigger(step.isNoneOf(values)); }
        public Trigger isNotIn(Object... values) { return new Trigger(step.isNotIn(values)); }
        public Trigger isNotIn(Collection<?> values) { return new Trigger(step.isNotIn(values)); }
    }

    /** A completed condition that can be extended with AND/OR or followed by a consequence. */
    public final class Trigger {
        private final ConditionalProductBuilder.Trigger step;

        private Trigger(ConditionalProductBuilder.Trigger step) {
            this.step = step;
        }

        public WhenDimension and(List<?> dimension) {
            return new WhenDimension(step.and(dimension));
        }

        public WhenDimension or(List<?> dimension) {
            return new WhenDimension(step.or(dimension));
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
        public CompoundConditionalProductBuilder isOneOf(Object... values) {
            return new CompoundConditionalProductBuilder(step.isOneOf(values));
        }
        public CompoundConditionalProductBuilder isOneOf(Collection<?> values) {
            return new CompoundConditionalProductBuilder(step.isOneOf(values));
        }
        public CompoundConditionalProductBuilder isNoneOf(Object... values) {
            return new CompoundConditionalProductBuilder(step.isNoneOf(values));
        }
        public CompoundConditionalProductBuilder isNoneOf(Collection<?> values) {
            return new CompoundConditionalProductBuilder(step.isNoneOf(values));
        }
        public CompoundConditionalProductBuilder isNotIn(Object... values) {
            return new CompoundConditionalProductBuilder(step.isNotIn(values));
        }
        public CompoundConditionalProductBuilder isNotIn(Collection<?> values) {
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

    /** Returns whether this product contains no valid tuples. */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
