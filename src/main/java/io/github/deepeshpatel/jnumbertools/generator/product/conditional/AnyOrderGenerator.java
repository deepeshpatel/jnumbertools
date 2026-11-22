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
 * Traversal of a conditional Cartesian product with no encounter-order guarantee.
 *
 * <p>The traversal prioritizes efficient execution rather than lexicographic ordering. Its spliterator
 * can partition disjoint ranges below a fixed prefix, allowing a caller's parallel stream to expose
 * independent work. This is an execution opportunity, not a promise that traversal will run in parallel;
 * sequential iteration remains supported and does not invoke splitting.</p>
 *
 * <p>When a dimension offers only one candidate in a partition, splitting advances the prefix and
 * continues at the next dimension. A partition is split only while the candidate-tuple upper bound
 * below its prefix exceeds {@link #SPLIT_WORK_THRESHOLD}. The bound ignores pruning and is also
 * reported by {@code estimateSize()}, allowing the stream implementation to estimate the amount of
 * work available in the partition. Smaller partitions are left for sequential traversal.</p>
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

            // When at most one value remains at the current level, the only way to split is to descend: fix that
            // value into the prefix and continue at the next level, whose range starts out as all of its values.
            // The prefix array is replaced, never modified in place, so the spliterator returned below and this one
            // can share it. If the loop gives up (returns null) the state is still an equivalent description of the
            // same remaining tuples.
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

        /**
         * Candidate tuples below the current prefix in the current range, before any pruning (saturating).
         *
         * <p>An empty range, or an empty dimension below the prefix, means there is no tuple at all. That is
         * checked before multiplying: the product saturates at {@code Long.MAX_VALUE}, which would otherwise
         * hide an empty dimension that comes after the dimensions that overflow it.</p>
         */
        private long workBound() {
            if (to <= from) return 0;
            int dimensions = model.dimensions.size();
            for (int d = prefix.length + 1; d < dimensions; d++) {
                if (model.dimensions.get(d).isEmpty()) return 0;
            }
            long bound = to - from;
            for (int d = prefix.length + 1; d < dimensions; d++) {
                long size = model.dimensions.get(d).size();
                if (bound > Long.MAX_VALUE / size) return Long.MAX_VALUE;
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
