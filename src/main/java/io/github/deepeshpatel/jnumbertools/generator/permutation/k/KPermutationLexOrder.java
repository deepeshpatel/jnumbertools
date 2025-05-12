/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/**
 * Generates unique k-permutations in lexicographical order from a list of elements.
 *
 * <p>
 * Produces all ordered arrangements Pₙ,ₖ = n!/(n−k)! of size k from the input list,
 * where elements are treated as distinct by their position (e₀, e₁, ..., eₙ₋₁).
 * Permutations are ordered lexicographically based on element indices.
 *
 * <p>Example for elements [A, B, C] (distinct by position) and k=2:
 * <pre>
 * [A, B], [A, C], [B, A], [B, C], [C, A], [C, B]
 * </pre>
 *
 * <p>This is different from combination-order permutations where permutations are
 * grouped by their underlying combinations first.
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public final class KPermutationLexOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs a generator for k-permutations in lexicographical order.
     *
     * @param elements the list of elements to permute (must not be null)
     * @param k the size of each permutation (0 ≤ k ≤ elements.size())
     * @throws IllegalArgumentException if k is negative or exceeds elements size
     */
    KPermutationLexOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over k-permutations in lexicographical order.
     *
     * <p>The iterator behavior:
     * <ul>
     *   <li>Returns empty iterator if k=0</li>
     *   <li>Uses optimized full permutation iterator when k = n</li>
     *   <li>Otherwise uses incremental lexicographical generation</li>
     * </ul>
     *
     * <p>Note: This method relies on {@code indicesToValues}, defined in the parent class
     * {@code AbstractKPermutation}, to map index arrays to element lists.
     *
     * @return an iterator producing Pₙ,ₖ permutations in lexicographical order
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0) return Util.emptyListIterator();
        if (k == elements.size()) return new UniquePermutationLexElementIterator<>(elements.size(), this::indicesToValues);
        return new Itr();
    }

    /**
     * Iterator implementation that generates k-permutations incrementally
     * in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {
        private int[] indices;
        private final LinkedList<Integer> list;

        /**
         * Initializes iterator state with first permutation [0,1,...,k−1]
         * and remaining indices [k,...,n−1] in a linked list for efficient removal.
         */
        public Itr() {
            indices = IntStream.range(0, k).toArray();
            list = IntStream.range(k, elements.size()).boxed()
                    .collect(toCollection(LinkedList::new));
        }

        @Override
        public boolean hasNext() {
            return indices.length > 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available");
            }
            int[] old = indices;
            indices = kPermutationNextLex(indices, list, elements.size() - 1);
            return indicesToValues(old);
        }

        /**
         * Generates the next permutation in lexicographical order.
         *
         * <p>Algorithm:
         * <ol>
         *   <li>If remaining indices exist, appends next available index</li>
         *   <li>Otherwise finds rightmost incrementable position</li>
         *   <li>Updates it with next valid value and resets subsequent positions</li>
         * </ol>
         *
         * @param current current permutation indices
         * @param remaining available indices not in current permutation
         * @param maxAllowed maximum valid index (n−1)
         * @return next permutation indices or empty array if done
         */
        private int[] kPermutationNextLex(int[] current, LinkedList<Integer> remaining, int maxAllowed) {
            int[] a = Arrays.copyOf(current, current.length);

            if (!remaining.isEmpty()) {
                int next = remaining.removeFirst();
                a[a.length - 1] = next;
                return a;
            }

            int index = -1;
            for (int i = a.length - 1; i > 0; i--) {
                if (a[i - 1] < a[i]) {
                    index = i - 1;
                    break;
                }
            }

            if (index == -1) {
                return new int[0];
            }

            remaining.addAll(IntStream.rangeClosed(0, maxAllowed).boxed().toList());
            for (int i = 0; i < index; i++) {
                remaining.remove(Integer.valueOf(a[i]));
            }
            int valueForCurrentIndex = a[index] + 1;
            while (!remaining.contains(valueForCurrentIndex)) {
                valueForCurrentIndex++;
            }
            a[index] = valueForCurrentIndex;
            remaining.remove(Integer.valueOf(valueForCurrentIndex));

            for (int i = index + 1; i < a.length; i++) {
                a[i] = remaining.removeFirst();
            }
            return a;
        }
    }
}