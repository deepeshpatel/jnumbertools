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
 * Ordered DP spliterator over a half-open range of valid-tuple lexicographic ranks.
 *
 * <p>Each split owns a disjoint contiguous rank interval. Exact completion counts make the midpoint
 * independent of tuple values. A range unranks only its first tuple and then steps through the following
 * tuples incrementally (the same stepping as {@link DpLexGenerator}), so the cost of unranking is paid once
 * per range, not once per tuple. The lower interval is returned and this spliterator retains the upper
 * interval, which preserves encounter order for ordered parallel streams.</p>
 */
final class DpRankSpliterator implements Spliterator<List<Object>> {
    private static final BigInteger MIN_SPLIT_SIZE = BigInteger.valueOf(2);

    private final ConstraintModel model;
    private final ConstraintPlan plan;
    private BigInteger nextRank;
    private final BigInteger fence;
    private Iterator<List<Object>> iterator;   // positioned at nextRank; null until needed or after a split

    DpRankSpliterator(ConstraintModel model, ConstraintPlan plan,
                      BigInteger origin, BigInteger fence) {
        this.model = model;
        this.plan = plan;
        this.nextRank = origin;
        this.fence = fence;
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<Object>> action) {
        if (action == null) throw new NullPointerException("action");
        if (nextRank.compareTo(fence) >= 0) return false;

        if (iterator == null) {
            iterator = DpLexGenerator.startingAt(model, plan, plan.counts().unrank(nextRank));
        }
        nextRank = nextRank.add(BigInteger.ONE);
        action.accept(iterator.next());
        return true;
    }

    @Override
    public Spliterator<List<Object>> trySplit() {
        BigInteger remaining = fence.subtract(nextRank);
        if (remaining.compareTo(MIN_SPLIT_SIZE) <= 0) return null;

        BigInteger midpoint = nextRank.add(remaining.shiftRight(1));
        DpRankSpliterator lower = new DpRankSpliterator(model, plan, nextRank, midpoint);
        nextRank = midpoint;
        iterator = null;   // the retained range starts at the midpoint: unrank again when it is next used
        return lower;
    }

    @Override
    public long estimateSize() {
        BigInteger remaining = fence.subtract(nextRank);
        return remaining.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                ? Long.MAX_VALUE : remaining.longValue();
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }
}
