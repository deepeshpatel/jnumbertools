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
    private final int[] nextCandidate;
    private final int[] candidateLimit;
    private int depth;
    private boolean ready;
    private boolean exhausted;

    PrunedLexGenerator(ConstraintModel model) {
        this(model, 0, model.dimensions.get(0).size());
    }

    PrunedLexGenerator(ConstraintModel model, int firstDimensionFrom, int firstDimensionTo) {
        this.model = model;
        this.dimensions = model.dimensions.size();
        this.val = new int[dimensions];
        this.nextCandidate = new int[dimensions];
        this.candidateLimit = new int[dimensions];
        for (int d = 0; d < dimensions; d++) {
            int size = model.dimensions.get(d).size();
            candidateLimit[d] = size;
            if (size == 0) exhausted = true;
        }
        if (firstDimensionFrom < 0 || firstDimensionTo < firstDimensionFrom
                || firstDimensionTo > candidateLimit[0]) {
            throw new IllegalArgumentException("Invalid first-dimension range");
        }
        nextCandidate[0] = firstDimensionFrom;
        candidateLimit[0] = firstDimensionTo;
        if (firstDimensionFrom == firstDimensionTo) exhausted = true;
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
        while (depth >= 0) {
            int candidate = nextValidCandidate(depth, nextCandidate[depth]);
            if (candidate >= candidateLimit[depth]) candidate = -1;
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

    /** Returns the first allowed candidate at or after {@code from}, or -1 if none exists. */
    protected abstract int nextValidCandidate(int dimension, int from);
}
