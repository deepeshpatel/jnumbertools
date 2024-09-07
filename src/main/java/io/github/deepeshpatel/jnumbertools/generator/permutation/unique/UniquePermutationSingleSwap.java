package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Generates a sequence of permutations using Heap's algorithm.
 * (not to be confused with the heap data structure)
 * @param <T>
 */
public final class UniquePermutationSingleSwap<T> extends AbstractGenerator<T> implements Iterable<List<T>>{

    UniquePermutationSingleSwap(List<T> elements) {
        super(elements);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? newEmptyIterator() :
                new Itr();
    }


    private class Itr implements Iterator<List<T>>{

        int i=1;
        int[] indices = IntStream.range(0,elements.size()).toArray();
        int[] c = new int[indices.length];

        // Single swap permutations using Heap's algorithm.
        public void generate() {

            while (i < indices.length) {
                if (c[i] < i) {
                    if (i % 2 == 0) {
                        swap(indices, 0, i);
                    } else {
                        swap(indices, c[i], i);
                    }
                    c[i] += 1;
                    i = 1;
                    return;

                } else {
                    c[i] = 0;
                    i += 1;
                }
            }
            indices = null;
        }

        private static void swap(int[] a, int x, int y) {
            int temp = a[x];
            a[x] = a[y];
            a[y] = temp;
        }

        @Override
        public boolean hasNext() {
            return indices != null;
        }

        @Override
        public List<T> next() {
            var result = indicesToValues(indices);
            generate();
            return result;
        }
    }

}
