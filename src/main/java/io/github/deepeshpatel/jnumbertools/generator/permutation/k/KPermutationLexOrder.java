/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermItrForElements;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/**
 * Implements an iterable that generates unique permutations of size {@code k}
 * from a list of elements in lexicographical order.
 * <p>
 * The permutations are generated by considering each value at a specific index
 * in the input list as unique, and then creating all possible permutations of size {@code k}.
 * For example, all permutations of size 2 for the input list [1, 2, 3] would be:
 * <pre>
 * [1, 2], [2, 1], [1, 3], [3, 1], [2, 3], and [3, 2]
 * </pre>
 * <p>
 *     Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the input list
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class KPermutationLexOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs a new {@code KPermutationLexOrder} instance.
     *
     * @param elements the list of elements from which unique permutations of size {@code k} will be generated
     * @param k the size of the permutations; must be {@code &lt;= elements.size()}
     */
    KPermutationLexOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over elements of type {@code List&lt;T&gt;}.
     * <p>
     * If {@code k == 0}, an empty iterator is returned. If {@code k == elements.size()},
     * a faster iterator implementation is used. Otherwise, a standard iterator
     * is used to generate permutations in lexicographical order.
     *
     * @return an iterator for generating permutations of size {@code k}
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0) return newEmptyIterator();
        if (k == elements.size()) return new UniquePermItrForElements<>(elements.size(), this::indicesToValues);
        return new Itr();
    }

    /**
     * Inner class implementing the {@code Iterator&lt;List&lt;T&gt;&gt;} interface
     * to generate permutations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        int[] indices;
        final LinkedList<Integer> list;

        /**
         * Constructs a new {@code Itr} instance to start generating permutations.
         */
        public Itr() {
            indices = IntStream.range(0, k).toArray();
            list = IntStream.range(k, elements.size()).boxed()
                    .collect(toCollection(LinkedList::new));
        }

        /**
         * Returns {@code true} if the iteration has more permutations.
         *
         * @return {@code true} if the iteration has more permutations; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length > 0;
        }

        /**
         * Returns the next permutation in the iteration.
         *
         * @return the next permutation as a {@code List&lt;T&gt;}
         * @throws NoSuchElementException if the iteration has no more elements
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
         * Generates the next permutation in lexicographical order.
         *
         * @param current the current permutation indices
         * @param remaining the list of remaining indices
         * @param maxAllowed the maximum allowed index
         * @return the next permutation indices
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
                // exceeds last permutation
                return new int[0];
            }

            remaining.addAll(IntStream.range(0, maxAllowed + 1).boxed().toList());

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
