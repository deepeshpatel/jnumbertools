/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Unordered, lazily partitionable traversal of a conditional Cartesian product.
 *
 * <p>Splits represent disjoint ranges below a fixed prefix. When a dimension
 * offers only one candidate in a partition, splitting advances the prefix and
 * continues at the next dimension. This allows parallel streams to expose work
 * even when the first dimension is small.</p>
 *
 * <p>A partition is split only while the work it may contain, bounded by the number of candidate tuples below its
 * prefix, exceeds {@link #SPLIT_WORK_THRESHOLD}. Smaller partitions are traversed sequentially, so a parallel
 * pipeline never creates tasks that cost more than the tuples they produce. The bound ignores pruning, so it is
 * an upper bound: it is also what {@code estimateSize()} reports, which lets the JDK choose its own task size.</p>
 */
final class AnyOrderGenerator extends AbstractGenerator<Object> {
    /** A partition is not split further when it holds at most this many candidate tuples. */
    static final long SPLIT_WORK_THRESHOLD = 1024;

    private final ConstraintModel model;

    AnyOrderGenerator(ConstraintModel model) {
        super(List.of());
        this.model = model;
    }

    @Override
    public Iterator<List<Object>> iterator() {
        return new BitSetPrunedGenerator(model);
    }

    @Override
    public Spliterator<List<Object>> spliterator() {
        return new AnyOrderSpliterator(model, new int[0], 0, model.dimensions.get(0).size());
    }

    private static final class AnyOrderSpliterator implements Spliterator<List<Object>> {
        private final ConstraintModel model;
        private int[] prefix;
        private int from;
        private int to;
        private Iterator<List<Object>> iterator;

        private AnyOrderSpliterator(ConstraintModel model, int[] prefix, int from, int to) {
            this.model = model;
            this.prefix = prefix;
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<Object>> action) {
            if (action == null) throw new NullPointerException("action");
            if (iterator == null) iterator = new BitSetPrunedGenerator(model, prefix, from, to);
            if (!iterator.hasNext()) return false;
            action.accept(iterator.next());
            return true;
        }

        @Override
        public Spliterator<List<Object>> trySplit() {
            // Do not split after traversal starts: the iterator owns the remaining subtree.
            if (iterator != null || workBound() <= SPLIT_WORK_THRESHOLD) return null;

            int dimensions = model.dimensions.size();
            while (to - from <= 1 && prefix.length < dimensions - 1) {
                if (from == to) return null;

                int dimension = prefix.length;
                int candidate = BitSetPrunedGenerator.nextValidCandidate(model, prefix, dimension, from);
                if (candidate < 0 || candidate >= to) return null;

                prefix = Arrays.copyOf(prefix, prefix.length + 1);
                prefix[prefix.length - 1] = candidate;
                from = 0;
                to = model.dimensions.get(prefix.length).size();
            }

            if (to - from <= 1 || workBound() <= SPLIT_WORK_THRESHOLD) return null;

            int midpoint = from + (to - from) / 2;
            AnyOrderSpliterator lower = new AnyOrderSpliterator(model, prefix, from, midpoint);
            from = midpoint;
            return lower;
        }

        /** Candidate tuples below the current prefix in the current range, before any pruning (saturating). */
        private long workBound() {
            long bound = to - from;
            for (int d = prefix.length + 1; d < model.dimensions.size(); d++) {
                long size = model.dimensions.get(d).size();
                if (size != 0 && bound > Long.MAX_VALUE / size) return Long.MAX_VALUE;
                bound *= size;
            }
            return bound;
        }

        @Override
        public long estimateSize() {
            // An upper bound: constraints can eliminate an arbitrary number of tuples from each partition.
            return workBound();
        }

        @Override
        public int characteristics() {
            return NONNULL | IMMUTABLE;
        }
    }
}
