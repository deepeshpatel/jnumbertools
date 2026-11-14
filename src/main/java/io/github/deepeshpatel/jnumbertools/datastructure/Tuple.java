/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * An immutable list that wraps an array without copying it.
 * <p>
 * The Cartesian-product generators build one tuple per result, so the wrapper is kept as small as possible:
 * one object around the array, instead of the two that {@code Collections.unmodifiableList(Arrays.asList(a))}
 * creates. It behaves like any other {@link java.util.List}: {@code equals}, {@code hashCode} and
 * {@code toString} follow the {@code List} contract, so a tuple is equal to a {@code List.of(...)} with the
 * same elements. Every method that would change it throws {@link UnsupportedOperationException}.
 * </p>
 *
 * <p>This class is immutable provided the wrapped array is never modified afterwards (see {@link #wrap}).
 * It is {@link Serializable} when its elements are, like the lists it replaces.</p>
 *
 * @param <E> the element type
 * @author Deepesh Patel
 */
public final class Tuple<E> extends AbstractList<E> implements RandomAccess, Serializable {

    private static final long serialVersionUID = 1L;

    private final Object[] items;

    private Tuple(Object[] items) {
        this.items = items;
    }

    /**
     * Wraps the array as an immutable list <strong>without copying it</strong>.
     * <p>
     * The caller hands over ownership: the array must not be modified, or kept anywhere it could be
     * modified, after this call. That is what makes the wrapper cheap; copy the array first if you cannot
     * guarantee it.
     * </p>
     *
     * @param items the elements, in order; must not be null
     * @param <E>   the element type
     * @return an immutable list view of {@code items}
     * @throws NullPointerException if {@code items} is null
     */
    public static <E> Tuple<E> wrap(Object[] items) {
        if (items == null) throw new NullPointerException("items must not be null");
        return new Tuple<>(items);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) items[index];
    }

    @Override
    public int size() {
        return items.length;
    }

    /** Returns a copy of the elements; changing it does not change the tuple. */
    @Override
    public Object[] toArray() {
        return items.clone();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int next;

            @Override
            public boolean hasNext() {
                return next < items.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                if (next >= items.length) throw new NoSuchElementException();
                return (E) items[next++];
            }
        };
    }
}
