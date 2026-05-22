/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.Derangadic;

import java.math.BigInteger;
import java.util.*;

/**
 * Generates all {@code D_n} derangements (fixed-point-free permutations) of a
 * list of elements in lexicographical order.
 * <p>
 * Each yielded {@code List<T>} is a derangement: the value at position {@code i}
 * is never the element originally at position {@code i} in the input list.
 * </p>
 * <p>
 * Backed by {@link Derangadic#next()}, which uses the incremental Derangadic
 * successor engine. Instances should be created via {@link DerangementBuilder#lexOrder()} or
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
    private final BigInteger maxCount;

    /**
     * Constructs the generator.
     * <p><strong>Internal use only.</strong> Library users should obtain
     * instances through {@link DerangementBuilder#lexOrder()}.</p>
     */
    Derangement(List<T> elements, Calculator calculator) {
        super(elements);
        this.calculator = calculator;
        this.maxCount = calculator.subFactorial(elements.size());
    }

    @Override
    public Iterator<List<T>> iterator() {
        int n = elements.size();
        if (n == 0) return Util.emptyListIterator();
        if (n == 1) return Collections.emptyIterator();
        return new DerangadicIterator(n);
    }

    private final class DerangadicIterator implements Iterator<List<T>> {

        private final Derangadic current;

        DerangadicIterator(int n) {
            this.current = new Derangadic(n, BigInteger.ZERO, calculator);
        }

        @Override
        public boolean hasNext() {
            return current.rank().compareTo(maxCount) < 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            List<T> derangement= indicesToValues(current.derangement().clone());
            current.nextWithoutBoundCheck();
            return derangement;
        }
    }
}

