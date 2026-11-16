/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

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
        public Iterator<List<Object>> iterator() {
            return strategy == LexOrderStrategy.PRUNED_BITSET
                    ? new PrunedLexItr(model)
                    : new LexItr(model, model.plan());
        }
    }

    /**
     * Lexicographic depth-first traversal without the counting plan. At each dimension, candidates are
     * filtered against all earlier dimensions connected to it by a rule. A prefix that has no completion
     * may still be explored until the conflicting later dimension is reached; unlike the DP iterator, this
     * iterator does not perform future-completion lookahead.
     */
    private static final class PrunedLexItr implements Iterator<List<Object>> {
        private final ConstraintModel model;
        private final int dimensions;
        private final int[] val;
        private final int[] nextCandidate;
        private final int[][] belowDims;
        private final BitSet[][][] belowRows;
        private int depth;
        private boolean ready;
        private boolean exhausted;

        PrunedLexItr(ConstraintModel model) {
            this.model = model;
            this.dimensions = model.dimensions.size();
            this.val = new int[dimensions];
            this.nextCandidate = new int[dimensions];
            ConstraintModel.Rows rows = model.rows();
            belowDims = rows.belowDims;
            belowRows = rows.belowRows;
            for (List<?> dimension : model.dimensions) {
                if (dimension.isEmpty()) {
                    exhausted = true;
                    break;
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (!ready && !exhausted) {
                ready = advance();
                if (!ready) exhausted = true;
            }
            return ready;
        }

        @Override
        public List<Object> next() {
            if (!hasNext()) throw new NoSuchElementException();
            ready = false;
            return model.tuple(val);
        }

        private boolean advance() {
            while (depth >= 0) {
                int candidate = nextValidCandidate(depth, nextCandidate[depth]);
                if (candidate < 0) {
                    if (depth == 0) return false;
                    depth--;
                    nextCandidate[depth] = val[depth] + 1;
                    continue;
                }

                val[depth] = candidate;
                nextCandidate[depth] = candidate + 1;
                if (depth == dimensions - 1) return true;

                depth++;
                nextCandidate[depth] = 0;
            }
            return false;
        }

        private int nextValidCandidate(int dimension, int from) {
            int size = model.dimensions.get(dimension).size();
            if (from >= size) return -1;

            int[] earlier = belowDims[dimension];
            if (earlier.length == 0) return from;

            BitSet[][] rows = belowRows[dimension];
            BitSet firstAllowed = rows[0][val[earlier[0]]];
            candidateLoop:
            for (int candidate = firstAllowed.nextSetBit(from);
                 candidate >= 0 && candidate < size;
                 candidate = firstAllowed.nextSetBit(candidate + 1)) {
                for (int i = 1; i < earlier.length; i++) {
                    if (!rows[i][val[earlier[i]]].get(candidate)) continue candidateLoop;
                }
                return candidate;
            }
            return -1;
        }
    }

    /** Walks the valid tuples in lexicographic order; branches without any valid completion are skipped. */
    private static final class LexItr implements Iterator<List<Object>> {
        private final ConstraintPlan p;
        private final ConstraintModel model;
        private final int[] val;
        private boolean started;
        private boolean ready;
        private boolean exhausted;

        LexItr(ConstraintModel model, ConstraintPlan p) {
            this.model = model;
            this.p = p;
            this.val = new int[p.dimensions];
            this.exhausted = p.counts.total.signum() == 0;
        }

        @Override
        public boolean hasNext() {
            if (!ready && !exhausted) {
                if (advance()) {
                    ready = true;
                } else {
                    exhausted = true;
                }
            }
            return ready;
        }

        @Override
        public List<Object> next() {
            if (!hasNext()) throw new NoSuchElementException();
            ready = false;
            return model.tuple(val);
        }

        private boolean advance() {
            int k;
            int from;
            if (!started) {
                started = true;
                k = 0;
                from = 0;
            } else {
                k = p.dimensions - 1;
                from = val[k] + 1;
            }
            while (true) {
                int idx = p.firstValid(k, from, val);
                if (idx < 0) {
                    k--;
                    if (k < 0) return false;
                    from = val[k] + 1;
                } else {
                    val[k] = idx;
                    k++;
                    if (k == p.dimensions) return true;
                    from = 0;
                }
            }
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
