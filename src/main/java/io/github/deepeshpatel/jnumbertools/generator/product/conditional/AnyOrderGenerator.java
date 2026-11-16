/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Unordered sequential traversal of a conditional Cartesian product.
 *
 * <p>This first implementation reuses the prefix-pruned BitSet iterator. Its
 * spliterator deliberately does not report {@link Spliterator#ORDERED} and
 * does not split. A later implementation stage can introduce parallel
 * partitioning without changing the public traversal contract.</p>
 */
final class AnyOrderGenerator extends AbstractGenerator<Object> {
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
        return new Spliterator<>() {
            private final Iterator<List<Object>> iterator = AnyOrderGenerator.this.iterator();

            @Override
            public boolean tryAdvance(Consumer<? super List<Object>> action) {
                if (action == null) throw new NullPointerException("action");
                if (!iterator.hasNext()) return false;
                action.accept(iterator.next());
                return true;
            }

            @Override
            public Spliterator<List<Object>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return NONNULL | IMMUTABLE;
            }
        };
    }
}
