/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamCombination;

/**
 *
 * Utility for generating r-combinations of input = {1, 2 . . ., n}
 * <p>
 * Generates r combinations from n=input.length items.
 * combinations are generated in Lexicographic order
 * of indices of items in a list. This class will not check for duplicate values and
 * treats all values differently based on the index
 * <p>
 * Unique combinations of 4 items out of 6{1, 2, 3, 4, 5, 6} are -
 * <pre>
 * 1234 1245 1345 1456 2356
 * 1235 1246 1346 2345 2456
 * 1236 1256 1356 2346 3456
 * </pre>
 *
 * <pre>
 * Code example:
 *          new UniqueCombination&lt;&gt;(List.of("A","B","C"),2)
 *                  .forEach(System.out::println);
 * or
 *
 *         JNumberTools.of("A","B","C")
 *                 .unique(2)
 *                 .forEach(System.out::println);
 *
 * will generate following (all unique combinations of size 2 of A and B)
 * [A, B]
 * [A, C]
 * [B, C]
 * </pre>
 *
 * @author Deepesh Patel
 */
public final class UniqueCombination<T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * @param elements List of N items
     * @param r number of combinations from N items. r must be &lt;= N for generating unique combinations
     */
    UniqueCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
        checkParamCombination(elements.size(),r, "unique combinations");
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices;

        private Itr(){
            indices = IntStream.range(0, r).toArray();
        }

        @Override
        public boolean hasNext() {
            return indices != null;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextCombination(indices, elements.size());
            return indicesToValues(old);
        }

        private int[] nextCombination(int[]currentKCombination, int n) {

            if(currentKCombination.length == 0 || currentKCombination[0] == n-currentKCombination.length) {
                return null;
            }
            int[] next = Arrays.copyOf(currentKCombination, currentKCombination.length);
            int i=next.length-1;
            int maxSupportedValueAtIndexI = n-1;

            while( i>=0 && next[i] == maxSupportedValueAtIndexI) {
                i--;
                maxSupportedValueAtIndexI--;
            }
            next[i] = next[i] + 1;

            for(int j=i+1; j<next.length; j++) {
                next[j] = next[j-1]+1;
            }
            return next;
        }
    }
}

