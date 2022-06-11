/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamCombination;

/**
 *
 * Utility for generating r-combinations of input = {1, 2 . . ., n}
 *
 * Generates r combinations from n=input.length items.
 * combinations are generated in Lexicographic order
 * of indices of items in a list. This class will not check for duplicate values and
 * treats all values differently based on the index
 *
 * Unique combinations of 4 items out of 6{1, 2, 3, 4, 5, 6} are -
 * <pre>
 * 1234 1245 1345 1456 2356
 * 1235 1246 1346 2345 2456
 * 1236 1256 1356 2346 3456
 * </pre>
 *
 * <pre>
 * Code example:
 *          new UniqueCombination&lt;&gt;(Arrays.asList("A","B","C"),2)
 *                  .forEach(System.out::println);
 * or
 *
 *         JNumberTools.combinationsOf("A","B","C")
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
public class UniqueCombination<T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * @param input List of N items
     * @param r number of combinations from N items. r must be &lt;= N for generating unique combinations
     */
    public UniqueCombination(Collection<T> input, int r) {
        super(input);
        this.r = r;
        checkParamCombination(seed.size(),r, "unique combinations");
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
            indices = nextCombination(indices, seed.size());
            return indicesToValues(old, seed);
        }

        private int[] nextCombination(int[]a, int n) {

            int[] next = Arrays.copyOf(a, a.length);
            int i=next.length-1;
            int maxSupportedValueAtIndexI = n-1;

            while( i>=0 && next[i] == maxSupportedValueAtIndexI) {
                i--;
                maxSupportedValueAtIndexI--;
            }
            if(i==-1) {
                return null;
            }

            next[i] = next[i] + 1;

            for(int j=i+1; j<next.length; j++) {
                next[j] = next[j-1]+1;
            }

            return next;
        }
    }
}

