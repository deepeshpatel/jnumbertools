/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.numbersystem.Derangadic;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates all {@code D_n} derangements (fixed-point-free permutations) of a
 * list of elements in lexicographical order.
 * <p>
 * Each yielded {@code List<T>} is a derangement: the value at position {@code i}
 * is never the element originally at position {@code i} in the input list.
 * </p>
 * <p>
 * Backed by {@link Derangadic.Walker}, which makes each successor effectively
 * O(log n) — see the package-level documentation. Instances should be created
 * via {@link DerangementBuilder#lexOrder()} or
 * {@code new Derangements().of(...).lexOrder()}.
 * </p>
 *
 * <h3>Edge cases</h3>
 * <ul>
 *   <li>If the input list is empty (n = 0): yields a single empty list. There
 *       is exactly one derangement of the empty set (the empty arrangement),
 *       and {@code D_0 = 1}.</li>
 *   <li>If the input list has exactly one element (n = 1): yields nothing.
 *       A single element cannot be deranged ({@code D_1 = 0}).</li>
 * </ul>
 *
 * @param <T> the element type
 * @author Deepesh Patel &amp; Aditya Patel
 * @see Derangadic
 * @see DerangementBuilder
 * @since 3.0.2
 */
public final class Derangement<T> extends AbstractGenerator<T> {

    private final Calculator calculator;

    /**
     * Constructs the generator.
     * <p><strong>Internal use only.</strong> Library users should obtain
     * instances through {@link DerangementBuilder#lexOrder()}.</p>
     */
    Derangement(List<T> elements, Calculator calculator) {
        super(elements);
        this.calculator = calculator;
    }

    @Override
    public Iterator<List<T>> iterator() {
        int n = elements.size();
        if (n == 0) return Util.emptyListIterator();
        if (n == 1) return Collections.emptyIterator();
        return new WalkerIterator(n);
    }

    private final class WalkerIterator implements Iterator<List<T>> {

        private final Derangadic.Walker walker;
        private boolean exhausted = false;

        WalkerIterator(int n) {
            this.walker = Derangadic.walker(n, calculator);
        }

        @Override
        public boolean hasNext() {
            return !exhausted;
        }

        @Override
        public List<T> next() {
            if (exhausted) throw new NoSuchElementException();
            // indicesToValues() copies values out of the live array, so the
            // subsequent advance() is safe.
            List<T> result = indicesToValues(walker.current());
            if (!walker.advance()) exhausted = true;
            return result;
        }
    }
}

