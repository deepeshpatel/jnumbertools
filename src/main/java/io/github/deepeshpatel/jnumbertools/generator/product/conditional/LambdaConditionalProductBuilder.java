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
 * Namespace for the strongly typed lambda predicates and builders used by
 * {@link io.github.deepeshpatel.jnumbertools.base.CartesianProduct#withLambda(List)}.
 *
 * <p>One dimension uses {@link Predicate}; two use {@link BiPredicate}; three through ten use
 * {@link Predicate3} through {@link Predicate10}. More than ten dimensions use
 * {@link ArrayBased}, whose predicate receives an {@code Object[]}.</p>
 *
 * <p>Each {@code when(...)} call adds one condition over all registered dimensions. Repeated calls
 * are combined with AND. Use Java's {@code &&}, {@code ||}, and {@code !} inside a predicate for
 * arbitrary Boolean expressions. Predicates are evaluated during rule compilation, subject to the
 * existing one-million-probe limit, not during later counting or generation.</p>
 */
public final class LambdaConditionalProductBuilder {
    private LambdaConditionalProductBuilder() { }

    @FunctionalInterface public interface Predicate3<A,B,C> { boolean test(A a, B b, C c); }
    @FunctionalInterface public interface Predicate4<A,B,C,D> { boolean test(A a, B b, C c, D d); }
    @FunctionalInterface public interface Predicate5<A,B,C,D,E> { boolean test(A a, B b, C c, D d, E e); }
    @FunctionalInterface public interface Predicate6<A,B,C,D,E,F> { boolean test(A a, B b, C c, D d, E e, F f); }
    @FunctionalInterface public interface Predicate7<A,B,C,D,E,F,G> { boolean test(A a, B b, C c, D d, E e, F f, G g); }
    @FunctionalInterface public interface Predicate8<A,B,C,D,E,F,G,H> { boolean test(A a, B b, C c, D d, E e, F f, G g, H h); }
    @FunctionalInterface public interface Predicate9<A,B,C,D,E,F,G,H,I> { boolean test(A a, B b, C c, D d, E e, F f, G g, H h, I i); }
    @FunctionalInterface public interface Predicate10<A,B,C,D,E,F,G,H,I,J> { boolean test(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j); }

    private abstract static class Base {
        final ConditionalProductBuilder delegate;
        Base(ConditionalProductBuilder delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        }
        final int[] allDimensions() {
            int[] indexes = new int[delegate.dimensionCount()];
            for (int i=0;i<indexes.length;i++) indexes[i]=i;
            return indexes;
        }
        public StreamableIterable<Object> generate() { return delegate.anyOrder(); }
        public BigInteger count() { return delegate.count(); }
        public boolean isEmpty() { return delegate.isEmpty(); }
    }

    /** Strongly typed builder for one dimension; {@code when} filters that dimension. */
    public static final class Typed1<A> extends Base {
        public Typed1(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed1<A> when(Predicate<? super A> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed1<>(delegate.withUnaryLambdaRule(value -> predicate.test((A)value)));
        }
        public <B> Typed2<A,B> and(List<B> dimension) {
            return new Typed2<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 2 dimensions. */
    public static final class Typed2<A,B> extends Base {
        public Typed2(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed2<A,B> when(BiPredicate<? super A,? super B> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed2<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1])));
        }
        public <C> Typed3<A,B,C> and(List<C> dimension) {
            return new Typed3<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 3 dimensions. */
    public static final class Typed3<A,B,C> extends Base {
        public Typed3(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed3<A,B,C> when(Predicate3<? super A,? super B,? super C> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed3<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2])));
        }
        public <D> Typed4<A,B,C,D> and(List<D> dimension) {
            return new Typed4<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 4 dimensions. */
    public static final class Typed4<A,B,C,D> extends Base {
        public Typed4(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed4<A,B,C,D> when(Predicate4<? super A,? super B,? super C,? super D> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed4<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3])));
        }
        public <E> Typed5<A,B,C,D,E> and(List<E> dimension) {
            return new Typed5<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 5 dimensions. */
    public static final class Typed5<A,B,C,D,E> extends Base {
        public Typed5(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed5<A,B,C,D,E> when(Predicate5<? super A,? super B,? super C,? super D,? super E> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed5<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3],(E)v[4])));
        }
        public <F> Typed6<A,B,C,D,E,F> and(List<F> dimension) {
            return new Typed6<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 6 dimensions. */
    public static final class Typed6<A,B,C,D,E,F> extends Base {
        public Typed6(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed6<A,B,C,D,E,F> when(Predicate6<? super A,? super B,? super C,? super D,? super E,? super F> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed6<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3],(E)v[4],(F)v[5])));
        }
        public <G> Typed7<A,B,C,D,E,F,G> and(List<G> dimension) {
            return new Typed7<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 7 dimensions. */
    public static final class Typed7<A,B,C,D,E,F,G> extends Base {
        public Typed7(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed7<A,B,C,D,E,F,G> when(Predicate7<? super A,? super B,? super C,? super D,? super E,? super F,? super G> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed7<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3],(E)v[4],(F)v[5],(G)v[6])));
        }
        public <H> Typed8<A,B,C,D,E,F,G,H> and(List<H> dimension) {
            return new Typed8<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 8 dimensions. */
    public static final class Typed8<A,B,C,D,E,F,G,H> extends Base {
        public Typed8(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed8<A,B,C,D,E,F,G,H> when(Predicate8<? super A,? super B,? super C,? super D,? super E,? super F,? super G,? super H> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed8<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3],(E)v[4],(F)v[5],(G)v[6],(H)v[7])));
        }
        public <I> Typed9<A,B,C,D,E,F,G,H,I> and(List<I> dimension) {
            return new Typed9<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 9 dimensions. */
    public static final class Typed9<A,B,C,D,E,F,G,H,I> extends Base {
        public Typed9(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed9<A,B,C,D,E,F,G,H,I> when(Predicate9<? super A,? super B,? super C,? super D,? super E,? super F,? super G,? super H,? super I> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed9<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3],(E)v[4],(F)v[5],(G)v[6],(H)v[7],(I)v[8])));
        }
        public <J> Typed10<A,B,C,D,E,F,G,H,I,J> and(List<J> dimension) {
            return new Typed10<>(delegate.and(dimension));
        }
    }

    /** Strongly typed builder for 10 dimensions. */
    public static final class Typed10<A,B,C,D,E,F,G,H,I,J> extends Base {
        public Typed10(ConditionalProductBuilder delegate) { super(delegate); }
        @SuppressWarnings("unchecked")
        public Typed10<A,B,C,D,E,F,G,H,I,J> when(Predicate10<? super A,? super B,? super C,? super D,? super E,? super F,? super G,? super H,? super I,? super J> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new Typed10<>(delegate.withLambdaRule(allDimensions(), v -> predicate.test((A)v[0],(B)v[1],(C)v[2],(D)v[3],(E)v[4],(F)v[5],(G)v[6],(H)v[7],(I)v[8],(J)v[9])));
        }
        public ArrayBased and(List<?> dimension) {
            return new ArrayBased(delegate.and(dimension));
        }
    }

    /** Array-based fallback for products with more than ten dimensions. */
    public static final class ArrayBased extends Base {
        public ArrayBased(ConditionalProductBuilder delegate) { super(delegate); }
        public ArrayBased and(List<?> dimension) {
            return new ArrayBased(delegate.and(dimension));
        }
        public ArrayBased when(Predicate<Object[]> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            return new ArrayBased(delegate.withLambdaRule(allDimensions(), predicate));
        }
    }
}
