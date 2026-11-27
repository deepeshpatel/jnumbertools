/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Namespace for the strongly typed lambda predicates and builders used by
 * {@link ConditionalProductBuilder#withLambda()}.
 *
 * <p>One dimension uses {@link Predicate}; two use {@link BiPredicate}; three through five use
 * {@link Predicate3} through {@link Predicate5}. Products with more than five dimensions use
 * {@link ArrayBased}, whose predicate receives an {@code Object[]}.</p>
 *
 * <p>{@code when(predicate)} applies to <em>all</em> dimensions registered at that point, so it is compiled
 * over the complete Cartesian product of those dimensions. To compile against only selected dimensions, use
 * {@code when(dim1, dim2, ...).matches(predicate)}: a scoped predicate receives only the selected values, in
 * the supplied order, and costs only as much as the product of the selected dimensions. Two through five
 * selected dimensions have typed predicates; any other scope uses {@code Predicate<Object[]>}.</p>
 *
 * <p>Typed and scoped rules can be mixed in any order. A scoped rule returns the same builder type it was
 * started from, so a {@code Typed3} stays a {@code Typed3} and can still take a typed {@code when(predicate)}
 * or {@code and(list)}. Rules are combined with AND. The one-million-probe limit applies to each rule's
 * selected scope.</p>
 *
 * <p>Every predicate must be deterministic and free of side effects. It is evaluated while the rule is being
 * added, not during counting or generation, so its result is fixed at that moment. Predicates that receive an
 * {@code Object[]} must not keep a reference to the array: it is reused between calls.</p>
 */
public final class LambdaConditionalProductBuilder {
    private LambdaConditionalProductBuilder() { }

    /** A predicate over three values. */
    @FunctionalInterface public interface Predicate3<A,B,C> { boolean test(A a, B b, C c); }
    /** A predicate over four values. */
    @FunctionalInterface public interface Predicate4<A,B,C,D> { boolean test(A a, B b, C c, D d); }
    /** A predicate over five values. */
    @FunctionalInterface public interface Predicate5<A,B,C,D,E> { boolean test(A a, B b, C c, D d, E e); }

    /** Checks that {@code delegate} holds exactly {@code expected} dimensions, as the typed builder named requires. */
    private static ConditionalProductBuilder requireDimensions(
            ConditionalProductBuilder delegate, int expected, String builder) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        if (delegate.dimensionCount() != expected) {
            throw new IllegalArgumentException(builder + " requires a product with exactly " + expected
                    + (expected == 1 ? " dimension" : " dimensions") + ", but it has " + delegate.dimensionCount());
        }
        return delegate;
    }

    /**
     * Shared rule methods. {@code S} is the concrete builder type, so a rule returns the type it started from.
     */
    private abstract static class Base<S extends Base<S>> {
        final ConditionalProductBuilder delegate;

        Base(ConditionalProductBuilder delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        }

        /** A builder of the same concrete type over {@code next}. */
        abstract S with(ConditionalProductBuilder next);

        final int[] allDimensions() {
            int[] indexes = new int[delegate.dimensionCount()];
            for (int i = 0; i < indexes.length; i++) indexes[i] = i;
            return indexes;
        }

        final int[] scopeIndexes(List<?>... scope) {
            Objects.requireNonNull(scope, "scope must not be null");
            if (scope.length < 2) {
                throw new IllegalArgumentException("A scoped lambda rule must name at least two dimensions");
            }
            int[] indexes = new int[scope.length];
            for (int i = 0; i < scope.length; i++) {
                Objects.requireNonNull(scope[i], "scope dimension must not be null");
                indexes[i] = delegate.indexOfDimension(scope[i]);
            }
            return indexes;
        }

        /**
         * Starts a scoped predicate over any number of dimensions; its arguments arrive as an {@code Object[]}.
         *
         * @param scope the dimensions the predicate depends on, as the list instances given to this builder
         * @return the scoped rule, completed with {@code matches(predicate)}
         */
        public Scoped<S> when(List<?>... scope) {
            return new Scoped<>(delegate, scopeIndexes(scope), this::with);
        }

        /** Starts a typed scoped predicate over two dimensions. */
        public <A,B> Scoped2<A,B,S> when(List<A> a, List<B> b) {
            return new Scoped2<>(delegate, scopeIndexes(a, b), this::with);
        }

        /** Starts a typed scoped predicate over three dimensions. */
        public <A,B,C> Scoped3<A,B,C,S> when(List<A> a, List<B> b, List<C> c) {
            return new Scoped3<>(delegate, scopeIndexes(a, b, c), this::with);
        }

        /** Starts a typed scoped predicate over four dimensions. */
        public <A,B,C,D> Scoped4<A,B,C,D,S> when(List<A> a, List<B> b, List<C> c, List<D> d) {
            return new Scoped4<>(delegate, scopeIndexes(a, b, c, d), this::with);
        }

        /** Starts a typed scoped predicate over five dimensions. */
        public <A,B,C,D,E> Scoped5<A,B,C,D,E,S> when(List<A> a, List<B> b, List<C> c, List<D> d, List<E> e) {
            return new Scoped5<>(delegate, scopeIndexes(a, b, c, d, e), this::with);
        }

        /** Returns the valid tuples, with no guarantee about their order. */
        public StreamableIterable<Object> anyOrder() { return delegate.anyOrder(); }

        /** Returns the exact number of valid tuples. */
        public BigInteger count() { return delegate.count(); }

        /** Returns valid tuples in lexicographic order. Compound rules use the DP strategy. */
        public StreamableIterable<Object> lexOrder() { return delegate.lexOrder(); }

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

        /** Returns whether no tuple is valid. */
        public boolean isEmpty() { return delegate.isEmpty(); }
    }

    /** Strongly typed builder for one dimension; {@code when} filters that dimension. */
    public static final class Typed1<A> extends Base<Typed1<A>> {
        /**
         * @param delegate a product with exactly one dimension
         * @throws IllegalArgumentException if {@code delegate} does not have exactly one dimension
         */
        public Typed1(ConditionalProductBuilder delegate) { super(requireDimensions(delegate, 1, "Typed1")); }
        @Override Typed1<A> with(ConditionalProductBuilder next) { return new Typed1<>(next); }

        /** Keeps only the values of the dimension that satisfy {@code predicate}. */
        @SuppressWarnings("unchecked")
        public Typed1<A> when(Predicate<? super A> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed1<>(delegate.withUnaryLambdaRule(value -> predicate.test((A) value)));
        }

        /** Appends a dimension. */
        public <B> Typed2<A,B> and(List<B> dimension) {
            return new Typed2<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for two dimensions. */
    public static final class Typed2<A,B> extends Base<Typed2<A,B>> {
        /**
         * @param delegate a product with exactly two dimensions
         * @throws IllegalArgumentException if {@code delegate} does not have exactly two dimensions
         */
        public Typed2(ConditionalProductBuilder delegate) { super(requireDimensions(delegate, 2, "Typed2")); }
        @Override Typed2<A,B> with(ConditionalProductBuilder next) { return new Typed2<>(next); }

        /** Adds a rule over both dimensions. */
        @SuppressWarnings("unchecked")
        public Typed2<A,B> when(BiPredicate<? super A,? super B> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed2<>(delegate.withLambdaRule(allDimensions(),
                    v -> predicate.test((A) v[0], (B) v[1])));
        }

        /** Appends a dimension. */
        public <C> Typed3<A,B,C> and(List<C> dimension) {
            return new Typed3<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for three dimensions. */
    public static final class Typed3<A,B,C> extends Base<Typed3<A,B,C>> {
        /**
         * @param delegate a product with exactly three dimensions
         * @throws IllegalArgumentException if {@code delegate} does not have exactly three dimensions
         */
        public Typed3(ConditionalProductBuilder delegate) { super(requireDimensions(delegate, 3, "Typed3")); }
        @Override Typed3<A,B,C> with(ConditionalProductBuilder next) { return new Typed3<>(next); }

        /** Adds a rule over all three dimensions. */
        @SuppressWarnings("unchecked")
        public Typed3<A,B,C> when(Predicate3<? super A,? super B,? super C> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed3<>(delegate.withLambdaRule(allDimensions(),
                    v -> predicate.test((A) v[0], (B) v[1], (C) v[2])));
        }

        /** Appends a dimension. */
        public <D> Typed4<A,B,C,D> and(List<D> dimension) {
            return new Typed4<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for four dimensions. */
    public static final class Typed4<A,B,C,D> extends Base<Typed4<A,B,C,D>> {
        /**
         * @param delegate a product with exactly four dimensions
         * @throws IllegalArgumentException if {@code delegate} does not have exactly four dimensions
         */
        public Typed4(ConditionalProductBuilder delegate) { super(requireDimensions(delegate, 4, "Typed4")); }
        @Override Typed4<A,B,C,D> with(ConditionalProductBuilder next) { return new Typed4<>(next); }

        /** Adds a rule over all four dimensions. */
        @SuppressWarnings("unchecked")
        public Typed4<A,B,C,D> when(Predicate4<? super A,? super B,? super C,? super D> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed4<>(delegate.withLambdaRule(allDimensions(),
                    v -> predicate.test((A) v[0], (B) v[1], (C) v[2], (D) v[3])));
        }

        /** Appends a dimension. */
        public <E> Typed5<A,B,C,D,E> and(List<E> dimension) {
            return new Typed5<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for five dimensions. */
    public static final class Typed5<A,B,C,D,E> extends Base<Typed5<A,B,C,D,E>> {
        /**
         * @param delegate a product with exactly five dimensions
         * @throws IllegalArgumentException if {@code delegate} does not have exactly five dimensions
         */
        public Typed5(ConditionalProductBuilder delegate) { super(requireDimensions(delegate, 5, "Typed5")); }
        @Override Typed5<A,B,C,D,E> with(ConditionalProductBuilder next) { return new Typed5<>(next); }

        /** Adds a rule over all five dimensions. */
        @SuppressWarnings("unchecked")
        public Typed5<A,B,C,D,E> when(Predicate5<? super A,? super B,? super C,? super D,? super E> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed5<>(delegate.withLambdaRule(allDimensions(),
                    v -> predicate.test((A) v[0], (B) v[1], (C) v[2], (D) v[3], (E) v[4])));
        }

        /** Appends a dimension; a product with more than five dimensions uses {@code Object[]} predicates. */
        public ArrayBased and(List<?> dimension) {
            return new ArrayBased(delegate.and(dimension));
        }
    }

    /** Scoped predicate over any number of selected dimensions; the values arrive as an {@code Object[]}. */
    public static final class Scoped<S> {
        private final ConditionalProductBuilder delegate;
        private final int[] indexes;
        private final Function<ConditionalProductBuilder, S> next;

        private Scoped(ConditionalProductBuilder delegate, int[] indexes, Function<ConditionalProductBuilder, S> next) {
            this.delegate = delegate;
            this.indexes = indexes;
            this.next = next;
        }

        /**
         * Adds the rule. The predicate must not keep a reference to its argument array, which is reused.
         *
         * @return the builder this rule was started from, with the rule added
         */
        public S matches(Predicate<Object[]> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return next.apply(delegate.withLambdaRule(indexes, predicate));
        }
    }

    /** Typed scoped predicate over exactly two selected dimensions. */
    public static final class Scoped2<A,B,S> {
        private final ConditionalProductBuilder delegate;
        private final int[] indexes;
        private final Function<ConditionalProductBuilder, S> next;

        private Scoped2(ConditionalProductBuilder delegate, int[] indexes, Function<ConditionalProductBuilder, S> next) {
            this.delegate = delegate;
            this.indexes = indexes;
            this.next = next;
        }

        /** Adds the rule and returns the builder it was started from. */
        @SuppressWarnings("unchecked")
        public S matches(BiPredicate<? super A, ? super B> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Predicate<Object[]> untyped = values -> predicate.test((A) values[0], (B) values[1]);
            return next.apply(delegate.withLambdaRule(indexes, untyped));
        }
    }

    /** Typed scoped predicate over exactly three selected dimensions. */
    public static final class Scoped3<A,B,C,S> {
        private final ConditionalProductBuilder delegate;
        private final int[] indexes;
        private final Function<ConditionalProductBuilder, S> next;

        private Scoped3(ConditionalProductBuilder delegate, int[] indexes, Function<ConditionalProductBuilder, S> next) {
            this.delegate = delegate;
            this.indexes = indexes;
            this.next = next;
        }

        /** Adds the rule and returns the builder it was started from. */
        @SuppressWarnings("unchecked")
        public S matches(Predicate3<? super A, ? super B, ? super C> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Predicate<Object[]> untyped = values -> predicate.test((A) values[0], (B) values[1], (C) values[2]);
            return next.apply(delegate.withLambdaRule(indexes, untyped));
        }
    }

    /** Typed scoped predicate over exactly four selected dimensions. */
    public static final class Scoped4<A,B,C,D,S> {
        private final ConditionalProductBuilder delegate;
        private final int[] indexes;
        private final Function<ConditionalProductBuilder, S> next;

        private Scoped4(ConditionalProductBuilder delegate, int[] indexes, Function<ConditionalProductBuilder, S> next) {
            this.delegate = delegate;
            this.indexes = indexes;
            this.next = next;
        }

        /** Adds the rule and returns the builder it was started from. */
        @SuppressWarnings("unchecked")
        public S matches(Predicate4<? super A, ? super B, ? super C, ? super D> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Predicate<Object[]> untyped = values ->
                    predicate.test((A) values[0], (B) values[1], (C) values[2], (D) values[3]);
            return next.apply(delegate.withLambdaRule(indexes, untyped));
        }
    }

    /** Typed scoped predicate over exactly five selected dimensions. */
    public static final class Scoped5<A,B,C,D,E,S> {
        private final ConditionalProductBuilder delegate;
        private final int[] indexes;
        private final Function<ConditionalProductBuilder, S> next;

        private Scoped5(ConditionalProductBuilder delegate, int[] indexes, Function<ConditionalProductBuilder, S> next) {
            this.delegate = delegate;
            this.indexes = indexes;
            this.next = next;
        }

        /** Adds the rule and returns the builder it was started from. */
        @SuppressWarnings("unchecked")
        public S matches(Predicate5<? super A, ? super B, ? super C, ? super D, ? super E> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Predicate<Object[]> untyped = values ->
                    predicate.test((A) values[0], (B) values[1], (C) values[2], (D) values[3], (E) values[4]);
            return next.apply(delegate.withLambdaRule(indexes, untyped));
        }
    }

    /**
     * Builder for products with more than five dimensions, whose predicates receive an {@code Object[]}.
     * A rule added with {@code when(predicate)} covers the dimensions registered at that point.
     */
    public static final class ArrayBased extends Base<ArrayBased> {
        /** @param delegate the product to build rules on */
        public ArrayBased(ConditionalProductBuilder delegate) { super(delegate); }
        @Override ArrayBased with(ConditionalProductBuilder next) { return new ArrayBased(next); }

        /** Appends a dimension. */
        public ArrayBased and(List<?> dimension) {
            return new ArrayBased(delegate.and(dimension));
        }

        /**
         * Adds a rule over all dimensions registered so far. The predicate must not keep a reference to its
         * argument array, which is reused.
         */
        public ArrayBased when(Predicate<Object[]> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new ArrayBased(delegate.withLambdaRule(allDimensions(), predicate));
        }
    }
}
