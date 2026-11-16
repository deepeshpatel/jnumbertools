/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Generator implementations for conditional Cartesian products.
 *
 * <p>This package-private class keeps traversal and sampling mechanics separate from the fluent builder.
 * The builder and its immutable {@link ConstraintModel} remain responsible for configuration and state.</p>
 */
final class ConditionalProductGenerators {
    private ConditionalProductGenerators() {}

    static StreamableIterable<Object> lexOrder(ConstraintModel model, LexOrderStrategy strategy) {
        return new LexOrderGenerator(model, strategy);
    }

    static StreamableIterable<Object> anyOrder(ConstraintModel model) {
        return new AnyOrderGenerator(model);
    }

    static StreamableIterable<Object> rejectionChoice(ConstraintModel model, int sampleSize, Random random) {
        return new RejectionChoiceGenerator(model, sampleSize, random);
    }

    static StreamableIterable<Object> rankBased(ConstraintModel model, Iterable<BigInteger> ranks) {
        return new RankBasedGenerator(model, ranks);
    }

    private static final class LexOrderGenerator extends AbstractGenerator<Object> {
        private final ConstraintModel model;
        private final LexOrderStrategy strategy;

        LexOrderGenerator(ConstraintModel model, LexOrderStrategy strategy) {
            super(Collections.emptyList());
            this.model = model;
            this.strategy = strategy;
        }

        @Override
        public Spliterator<List<Object>> spliterator() {
            if (strategy == LexOrderStrategy.PRUNED_BITSET || strategy == LexOrderStrategy.PRUNED_DFS) {
                return new PrunedLexSpliterator(model, strategy);
            }
            if (strategy == LexOrderStrategy.DP) {
                ConstraintPlan plan = model.plan();
                return new DpRankSpliterator(model, plan, BigInteger.ZERO, plan.counts.total);
            }
            return Spliterators.spliteratorUnknownSize(iterator(),
                    Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        @Override
        public Iterator<List<Object>> iterator() {
            return switch (strategy) {
                case PRUNED_BITSET -> new BitSetPrunedGenerator(model);
                case PRUNED_DFS -> {
                    ConstraintModel.DenseRows rows = model.denseRows();
                    yield rows == null ? new BitSetPrunedGenerator(model) : new DenseDfsGenerator(model, rows);
                }
                case DP -> new DpLexGenerator(model, model.plan());
                case ADAPTIVE -> throw new IllegalArgumentException("ADAPTIVE must be resolved by the builder");
            };
        }
    }

    /**
     * Uniform choice with replacement by rejection: draw a random point of the full product and keep it if it
     * satisfies every rule. Exact (each valid tuple is equally likely) and used only when at least a quarter
     * of the product is valid, so a draw is accepted after four attempts on average.
     */
    private static final class RejectionChoiceGenerator extends AbstractGenerator<Object> {
        private final ConstraintModel model;
        private final int sampleSize;
        private final Random random;

        RejectionChoiceGenerator(ConstraintModel model, int sampleSize, Random random) {
            super(Collections.emptyList());
            this.model = model;
            this.sampleSize = sampleSize;
            this.random = random;
        }

        @Override
        public Iterator<List<Object>> iterator() {
            ConstraintPlan p = model.plan();
            return new Iterator<>() {
                private int produced;

                @Override public boolean hasNext() { return produced < sampleSize; }

                @Override
                public List<Object> next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    produced++;
                    int[] val = new int[p.dimensions];
                    do {
                        for (int d = 0; d < val.length; d++) val[d] = random.nextInt(p.dimSize[d]);
                    } while (!p.isValid(val));
                    return model.tuple(val);
                }
            };
        }
    }

    /** Resolves each rank to a tuple through the counting tables. Ranks are consumed lazily. */
    private static final class RankBasedGenerator extends AbstractGenerator<Object> {
        private final ConstraintModel model;
        private final Iterable<BigInteger> ranks;

        RankBasedGenerator(ConstraintModel model, Iterable<BigInteger> ranks) {
            super(Collections.emptyList());
            this.model = model;
            this.ranks = ranks;
        }

        @Override
        public Iterator<List<Object>> iterator() {
            Iterator<BigInteger> rankIt = ranks.iterator();
            return new Iterator<>() {
                @Override public boolean hasNext() { return rankIt.hasNext(); }

                @Override
                public List<Object> next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    BigInteger rank = rankIt.next();
                    ConstraintPlan p = model.plan();
                    if (rank.signum() < 0 || rank.compareTo(p.counts.total) >= 0) {
                        throw new IllegalArgumentException(
                                "Rank " + rank + " is out of bounds [0, " + p.counts.total + ")");
                    }
                    return model.tuple(p.counts.unrank(rank));
                }
            };
        }
    }
}
