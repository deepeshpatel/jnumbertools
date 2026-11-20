/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Ordered spliterator for the {@code DP} strategy.
 *
 * <p>Sequential traversal walks the tuples with {@link DpLexGenerator}, which checks the complete
 * compound-rule semantics. Pairwise-only products may split into rank ranges using exact completion
 * counts. Compound-rule products deliberately remain unsplit: the current rank-count tables do not
 * incorporate non-binary rules, so using them to partition the stream can produce invalid tuples and
 * omit valid ones. This preserves correctness for parallel stream consumers, at the cost of sequential
 * execution for this strategy when compound rules are present.</p>
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
        if (iterator != null || plan.isEmpty() || !model.multiRules.isEmpty()) return null;
        // Rank counts currently model pairwise relations only; compound rules must use the exact iterator.
        ranks = new DpRankSpliterator(model, plan, BigInteger.ZERO, plan.counts().total);
        return ranks.trySplit();
    }

    @Override
    public long estimateSize() {
        if (ranks != null) return ranks.estimateSize();
        if (iterator != null || !model.multiRules.isEmpty()) return Long.MAX_VALUE;
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
