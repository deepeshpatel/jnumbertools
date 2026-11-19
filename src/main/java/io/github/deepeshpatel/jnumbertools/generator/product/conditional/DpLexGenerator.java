/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lexicographic iterator that uses boolean feasibility lookahead to skip
 * prefixes that cannot lead to a valid tuple.
 *
 * <p>The iterator preserves the dimension order and candidate order of the
 * prefix-pruned generators. It uses the feasibility tier of the constraint
 * plan to test whether a candidate can be completed. This tier uses
 * precomputed feasibility bit sets and does not require the exact completion
 * counts maintained by the separate, lazily initialized counting tier.</p>
 */
final class DpLexGenerator implements Iterator<List<Object>> {
    private final ConstraintPlan plan;
    private final ConstraintModel model;
    private final int[] values;
    private boolean started;
    private boolean ready;
    private boolean exhausted;

    DpLexGenerator(ConstraintModel model, ConstraintPlan plan) {
        this.model = model;
        this.plan = plan;
        this.values = new int[plan.dimensions];
        this.exhausted = plan.isEmpty();
    }

    /** An iterator whose first tuple is {@code first}; later tuples follow in lexicographic order. */
    static DpLexGenerator startingAt(ConstraintModel model, ConstraintPlan plan, int[] first) {
        DpLexGenerator generator = new DpLexGenerator(model, plan);
        System.arraycopy(first, 0, generator.values, 0, first.length);
        generator.started = true;
        generator.ready = true;
        generator.exhausted = false;
        return generator;
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
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        ready = false;
        return model.tuple(values);
    }

    private boolean advance() {
        int dimension;
        int from;
        if (!started) {
            started = true;
            dimension = 0;
            from = 0;
        } else {
            dimension = plan.dimensions - 1;
            from = values[dimension] + 1;
        }

        while (true) {
            int candidate = plan.firstValid(dimension, from, values);
            if (candidate < 0) {
                dimension--;
                if (dimension < 0) {
                    return false;
                }
                from = values[dimension] + 1;
            } else {
                values[dimension] = candidate;
                dimension++;
                if (dimension == plan.dimensions) {
                    return true;
                }
                from = 0;
            }
        }
    }
}
