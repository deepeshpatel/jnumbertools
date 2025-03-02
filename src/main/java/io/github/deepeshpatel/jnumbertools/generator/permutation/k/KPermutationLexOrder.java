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
 * Generates unique permutations of size {@code k} from a list of elements in lexicographical order.
 * <p>
 * This class produces all permutations of a subset of size {@code k} from the input list, treating each
 * element as distinct by its position (e.g., elements₀, elements₁, ...). Permutations are generated in
 * lexicographical order based on the indices of the selected subset. For example, for [1, 2, 3] and k=2,
 * it generates:
 * <pre>
 * [1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]
 * </pre>
 * Instances are typically created via a builder.
 * </p>
 *
 * @param <T> the type of elements in the input list
 * @author Deepesh Patel
 * @version 3.0.1
 * @since 1.0.3
 */
public final class KPermutationLexOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs an instance for generating k-permutations in lexicographical order.
     *
     * @param elements the list of elements to permute
     * @param k        the size of each permutation; must be between 0 and elements.size()
     * @throws IllegalArgumentException if k is negative or exceeds the input list size
     */
    KPermutationLexOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over unique k-permutations in lexicographical order.
     * <p>
     * If k=0, returns an empty iterator. If k equals the list size, uses a direct permutation iterator
     * for efficiency. Otherwise, employs a custom iterator to generate permutations incrementally.
     * </p>
     *
     * @return an iterator over lists representing k-permutations
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0) return Util.emptyListIterator();
        if (k == elements.size()) return new UniquePermutationLexElementIterator<>(elements.size(), this::indicesToValues);
        return new Itr();
    }

    /**
     * Inner iterator for generating k-permutations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current permutation of indices.
         */
        private int[] indices;

        /**
         * A list of remaining indices available for constructing the next permutation.
         */
        private final LinkedList<Integer> list;

        /**
         * Constructs an iterator starting with the first k indices.
         * <p>
         * Initializes with the identity permutation [0, 1, ..., k-1] and populates the remaining
         * indices from k to elements.size()-1 in a {@code LinkedList} for dynamic selection.
         * </p>
         */
        public Itr() {
            indices = IntStream.range(0, k).toArray();
            list = IntStream.range(k, elements.size()).boxed()
                    .collect(toCollection(LinkedList::new));
        }

        /**
         * Checks if there are more permutations to generate.
         *
         * @return {@code true} if additional permutations exist; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length > 0;
        }

        /**
         * Returns the next k-permutation in lexicographical order.
         *
         * @return the next permutation as a list of elements
         * @throws NoSuchElementException if no more permutations are available
         */
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
         * Computes the next k-permutation of indices in lexicographical order.
         * <p>
         * If remaining indices exist, appends the next available index. Otherwise, finds the rightmost
         * index that can be incremented, updates it with the next available value, and rebuilds the
         * remaining positions with the smallest available indices.
         * </p>
         *
         * @param current    the current permutation of indices
         * @param remaining  a list of remaining indices available for use
         * @param maxAllowed the maximum allowed index (typically elements.size() - 1)
         * @return the next permutation as an array of indices, or an empty array if the last permutation has been reached
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