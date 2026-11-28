/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Shared lexicographic depth-first control flow for prefix-pruned conditional-product generators.
 * Subclasses provide the next candidate value allowed by the already selected prefix.
 */
abstract class PrunedLexGenerator implements Iterator<List<Object>> {
    protected final ConstraintModel model;
    protected final int dimensions;
    protected final int[] val;
    /**
     * Completion lookahead (feasibility tier). Pairwise pruning cannot see a prefix that is consistent so far but has
     * no valid completion (a loop, or a rule between distant dimensions that no value combination can satisfy), and
     * enumerates the free dimensions below it before failing. Checking every candidate costs more than it saves when
     * few prefixes are dead, so lookahead starts off for every constraint graph (loop or acyclic alike). It is switched on
     * as soon as the number of descents exceeds {@code WASTE_FLOOR + WASTE_RATIO * emitted}, that is 1,024 plus four
     * times the tuples emitted so far; the rule looks only at observed work, never at the graph's shape. Building the
     * feasibility plan is then worth its cost, and an empty plan ends the traversal at once. Once on, lookahead stays on
     * for the rest of that traversal and does not change which tuples are returned or their order.
     */
    private ConstraintPlan lookaheadPlan;
    private long descents;
    private long emitted;
    private static final long WASTE_FLOOR = 1024;
    private static final long WASTE_RATIO = 4;
    private final int[] nextCandidate;
    private final int[] candidateLimit;
    private int depth;
    private final int baseDepth;
    private boolean ready;
    private boolean exhausted;
    private final Traversal traversal;

    PrunedLexGenerator(ConstraintModel model) {
        this(model, new int[0], 0, model.dimensions.get(0).size());
    }

    PrunedLexGenerator(ConstraintModel model, int firstDimensionFrom, int firstDimensionTo) {
        this(model, new int[0], firstDimensionFrom, firstDimensionTo);
    }

    PrunedLexGenerator(ConstraintModel model, int[] prefix, int candidateFrom, int candidateTo) {
        this.model = model;
        this.dimensions = model.dimensions.size();
        if (prefix.length >= dimensions) {
            throw new IllegalArgumentException("A traversal prefix must leave at least one dimension");
        }
        this.val = new int[dimensions];
        this.nextCandidate = new int[dimensions];
        this.candidateLimit = new int[dimensions];
        for (int d = 0; d < dimensions; d++) {
            int size = model.dimensions.get(d).size();
            candidateLimit[d] = size;
            if (size == 0) exhausted = true;
        }
        for (int d = 0; d < prefix.length; d++) {
            if (prefix[d] < 0 || prefix[d] >= candidateLimit[d]) {
                throw new IllegalArgumentException("Invalid traversal prefix");
            }
            val[d] = prefix[d];
        }
        this.baseDepth = prefix.length;
        this.depth = baseDepth;
        int size = candidateLimit[baseDepth];
        if (candidateFrom < 0 || candidateTo < candidateFrom || candidateTo > size) {
            throw new IllegalArgumentException("Invalid candidate range");
        }
        nextCandidate[baseDepth] = candidateFrom;
        candidateLimit[baseDepth] = candidateTo;
        if (candidateFrom == candidateTo) exhausted = true;
        this.traversal = model.multiRules.isEmpty() ? new PairwiseTraversal() : new MultiTriggerTraversal();
    }

    /** Whether completion lookahead is active (for tests). */
    final boolean lookaheadEnabled() {
        return lookaheadPlan != null;
    }

    @Override
    public final boolean hasNext() {
        if (!ready && !exhausted) {
            ready = advance();
            if (!ready) exhausted = true;
        }
        return ready;
    }

    @Override
    public final List<Object> next() {
        if (!hasNext()) throw new NoSuchElementException();
        ready = false;
        return model.tuple(val);
    }

    private boolean advance() {
        return traversal.advance();
    }

    /**
     * Traversal strategies are selected once when the iterator is constructed. The pairwise
     * candidate loop therefore contains no multi-trigger mode test or multi-trigger rule evaluation.
     */
    private abstract static class Traversal {
        abstract boolean advance();
    }

    /** Hot path for ordinary pairwise conditional products. */
    private final class PairwiseTraversal extends Traversal {
        @Override
        boolean advance() {
            while (depth >= 0) {
                int candidate = nextValidCandidate(depth, nextCandidate[depth]);
                if (candidate >= candidateLimit[depth]) candidate = -1;
                if (candidate < 0) {
                    if (depth == baseDepth) return false;
                    depth--;
                    nextCandidate[depth] = val[depth] + 1;
                    continue;
                }

                val[depth] = candidate;
                nextCandidate[depth] = candidate + 1;
                if (depth == dimensions - 1) {
                    emitted++;
                    return true;
                }
                if (lookaheadPlan == null && ++descents > WASTE_FLOOR + WASTE_RATIO * emitted) {
                    lookaheadPlan = model.plan();
                    if (lookaheadPlan.isEmpty()) return false;
                }

                // The feasibility tier rejects a prefix with no valid completion before its suffix is explored.
                if (lookaheadPlan != null && !lookaheadPlan.hasCompletion(depth + 1, val)) {
                    continue;
                }

                depth++;
                nextCandidate[depth] = 0;
            }
            return false;
        }
    }

    /** multi-trigger traversal checks each multi-trigger implication as soon as its scope is assigned. */
    private final class MultiTriggerTraversal extends Traversal {
        @Override
        boolean advance() {
            while (depth >= 0) {
                int candidate = nextValidCandidate(depth, nextCandidate[depth]);
                if (candidate >= candidateLimit[depth]) candidate = -1;
                if (candidate < 0) {
                    if (depth == baseDepth) return false;
                    depth--;
                    nextCandidate[depth] = val[depth] + 1;
                    continue;
                }

                val[depth] = candidate;
                nextCandidate[depth] = candidate + 1;
                if (!model.acceptsPrefix(depth, val)) continue;
                if (depth == dimensions - 1) {
                    emitted++;
                    return true;
                }
                if (lookaheadPlan == null && ++descents > WASTE_FLOOR + WASTE_RATIO * emitted) {
                    lookaheadPlan = model.plan();
                    if (lookaheadPlan.isEmpty()) return false;
                }

                // The feasibility tier rejects a prefix with no valid completion before its suffix is explored.
                if (lookaheadPlan != null && !lookaheadPlan.hasCompletion(depth + 1, val)) {
                    continue;
                }

                depth++;
                nextCandidate[depth] = 0;
            }
            return false;
        }
    }

    /** Returns the first allowed candidate at or after {@code from}, or -1 if none exists. */
    protected abstract int nextValidCandidate(int dimension, int from);
}
