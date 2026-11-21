/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Ordered spliterator for the {@code DP} strategy.
 *
 * <p>Sequential traversal walks the tuples with {@link DpLexGenerator}, which needs only the feasibility
 * tier and does not build exact completion counts. Before traversal starts, either {@link #estimateSize()}
 * or {@link #trySplit()} may build the exact counts. Splitting then hands traversal over to
 * {@link DpRankSpliterator}, which divides the valid-tuple rank interval into contiguous ranges.</p>
 */
final class DpLexSpliterator implements Spliterator<List<Object>> {
    private final ConstraintModel model;
    private final ConstraintPlan plan;
    private Iterator<List<Object>> iterator;    // sequential walk, once traversal has started
    private Spliterator<List<Object>> ranks;    // rank-range mode, once this spliterator has been split

    DpLexSpliterator(ConstraintModel model, ConstraintPlan plan) {
        this.model = model;
        this.plan = plan;
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<Object>> action) {
        if (action == null) throw new NullPointerException("action");
        if (ranks != null) return ranks.tryAdvance(action);
        if (iterator == null) iterator = new DpLexGenerator(model, plan);
        if (!iterator.hasNext()) return false;
        action.accept(iterator.next());
        return true;
    }

    @Override
    public Spliterator<List<Object>> trySplit() {
        if (ranks != null) return ranks.trySplit();
        if (iterator != null || plan.isEmpty()) return null;   // the iterator owns what remains
        ranks = new DpRankSpliterator(model, plan, BigInteger.ZERO, plan.counts().total);
        return ranks.trySplit();
    }

    @Override
    public long estimateSize() {
        if (ranks != null) return ranks.estimateSize();
        if (iterator != null) return Long.MAX_VALUE;
        // A pre-traversal size estimate needs the exact count to report the remaining tuple count.
        if (plan.isEmpty()) return 0;
        BigInteger total = plan.counts().total;
        return total.bitLength() > 62 ? Long.MAX_VALUE : total.longValue();
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }
}
