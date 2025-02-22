/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermItrForElements;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/**
 * An iterable that generates unique permutations of size {@code k} from a given list of elements
 * in lexicographical order.
 * <p>
 * This class generates permutations by treating each element at a specific index (e.g. elements₀, elements₁, …)
 * in the input list as distinct and then forming all possible orderings of a subset of size {@code k}.
 * For example, for the input list [1, 2, 3] and {@code k = 2}, the unique permutations are:
 * <pre>
 * [1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]
 * </pre>
 * <p>
 * Instances of this class are intended to be created via a builder; therefore, it does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the input list
 * @since 1.0.3
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationLexOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs a new {@code KPermutationLexOrder} instance.
     *
     * @param elements the list of elements from which unique permutations of size {@code k} will be generated
     * @param k the size of the permutations; must be ≤ {@code elements.size()}
     */
    KPermutationLexOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over unique permutations of size {@code k}.
     * <p>
     * If {@code k == 0}, an empty iterator is returned.
     * If {@code k} equals the size of the input list, a faster iterator is used which directly generates the
     * permutations of the full list. Otherwise, a custom iterator is used that generates permutations in
     * lexicographical order.
     * </p>
     *
     * @return an iterator for generating permutations of size {@code k}
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0) return Util.emptyListIterator();
        if (k == elements.size()) return new UniquePermItrForElements<>(elements.size(), this::indicesToValues);
        return new Itr();
    }

    /**
     * Inner iterator class that generates permutations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current permutation of indices.
         */
        int[] indices;

        /**
         * A list of remaining indices available for constructing the next permutation.
         */
        final LinkedList<Integer> list;

        /**
         * Constructs a new {@code Itr} instance to generate permutations.
         * <p>
         * The initial permutation is the identity permutation on the first {@code k} indices.
         * The {@code list} holds the remaining indices from {@code k} up to {@code elements.size() - 1}.
         * </p>
         */
        public Itr() {
            indices = IntStream.range(0, k).toArray();
            list = IntStream.range(k, elements.size()).boxed()
                    .collect(toCollection(LinkedList::new));
        }

        /**
         * Returns {@code true} if there are more permutations to generate.
         *
         * @return {@code true} if a subsequent permutation exists; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length > 0;
        }

        /**
         * Returns the next permutation in lexicographical order.
         *
         * @return the next permutation as a {@code List<T>}
         * @throws NoSuchElementException if there are no more permutations
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = kPermutationNextLex(indices, list, elements.size() - 1);
            return indicesToValues(old);
        }

        /**
         * Computes the next permutation of indices in lexicographical order.
         *
         * @param current the current permutation of indices
         * @param remaining a list of remaining indices available for use
         * @param maxAllowed the maximum allowed index (typically {@code elements.size() - 1})
         * @return the next permutation as an array of indices, or an empty array if the last permutation has been reached
         */
        private int[] kPermutationNextLex(int[] current, LinkedList<Integer> remaining, int maxAllowed) {
            int[] a = Arrays.copyOf(current, current.length);

            // If there are remaining indices, assign the first remaining index to the last position.
            if (!remaining.isEmpty()) {
                int next = remaining.removeFirst();
                a[a.length - 1] = next;
                return a;
            }

            // Find the rightmost index that can be incremented.
            int index = -1;
            for (int i = a.length - 1; i > 0; i--) {
                if (a[i - 1] < a[i]) {
                    index = i - 1;
                    break;
                }
            }

            // If no such index exists, we've reached the last permutation.
            if (index == -1) {
                return new int[0];
            }

            // Reinitialize remaining with all indices from 0 to maxAllowed.
            remaining.addAll(IntStream.rangeClosed(0, maxAllowed).boxed().toList());

            // Remove the indices that are already in use up to the current index.
            for (int i = 0; i < index; i++) {
                remaining.remove(Integer.valueOf(a[i]));
            }
            int valueForCurrentIndex = a[index] + 1;
            while (!remaining.contains(valueForCurrentIndex)) {
                valueForCurrentIndex++;
            }
            a[index] = valueForCurrentIndex;
            remaining.remove(Integer.valueOf(valueForCurrentIndex));

            // Rebuild the remainder of the permutation using the smallest available indices.
            for (int i = index + 1; i < a.length; i++) {
                a[i] = remaining.removeFirst();
            }
            return a;
        }
    }
}
