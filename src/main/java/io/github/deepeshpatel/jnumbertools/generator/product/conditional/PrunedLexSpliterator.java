/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Ordered spliterator for prefix-pruned generators.
 *
 * <p>Each split owns a contiguous range of values in the first dimension. The
 * ranges are disjoint and the returned split always represents the lower range,
 * so encounter order remains lexicographic. Iterators are created lazily and
 * independently for each range.</p>
 */
final class PrunedLexSpliterator implements Spliterator<List<Object>> {
    private static final int MIN_SPLIT_RANGE = 2;

    private final ConstraintModel model;
    private final LexOrderStrategy strategy;
    private final ConstraintModel.DenseRows denseRows;
    private int from;
    private final int to;
    private Iterator<List<Object>> iterator;

    PrunedLexSpliterator(ConstraintModel model, LexOrderStrategy strategy) {
        this(model, strategy, 0, model.dimensions.get(0).size());
    }

    private PrunedLexSpliterator(ConstraintModel model, LexOrderStrategy strategy, int from, int to) {
        this.model = model;
        this.strategy = strategy;
        this.from = from;
        this.to = to;
        this.denseRows = strategy == LexOrderStrategy.PRUNED_DFS ? model.denseRows() : null;
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<Object>> action) {
        if (action == null) throw new NullPointerException("action");
        if (iterator == null) iterator = newIterator();
        if (!iterator.hasNext()) return false;
        action.accept(iterator.next());
        return true;
    }

    @Override
    public Spliterator<List<Object>> trySplit() {
        // Once traversal has started, the iterator may have consumed part of this
        // range. Refusing a late split avoids overlap or omissions.
        if (iterator != null || to - from <= MIN_SPLIT_RANGE) return null;

        int midpoint = from + (to - from) / 2;
        PrunedLexSpliterator lower = new PrunedLexSpliterator(model, strategy, from, midpoint);
        from = midpoint;
        return lower;
    }

    @Override
    public long estimateSize() {
        // Constraints can remove an arbitrary number of tuples from every range.
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }

    private Iterator<List<Object>> newIterator() {
        if (strategy == LexOrderStrategy.PRUNED_DFS && denseRows != null) {
            return new DenseDfsGenerator(model, denseRows, from, to);
        }
        return new BitSetPrunedGenerator(model, from, to);
    }
}
