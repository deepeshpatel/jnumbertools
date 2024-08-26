/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil;

import java.util.*;

/**
 *
 * Utility for generating r-combinations of input = {1, 2 . . ., n}
 * <p>
 * Generates r combinations from n=input.length items.
 * combinations are generated in Lexicographic order
 * of indices of items in a list. This class will not check for duplicate values and
 * treats all values differently based on the index
 * <p></p>
 * Repetitive combinations of 3 items out of 2{1, 2} are -
 * <pre>
 * 111 112 122 222
 * </pre>
 * <pre>
 * Code example:
 *
 *         int[] multisetFreqArray = new int[]{2,1,1};
 *         new RepetitiveCombinationMultiset&lt;&gt;(List.of("A","B","C"),3,multisetFreqArray)
 *                 .forEach(System.out::println);
 *
 *         or
 *
 *         //here "A" can be repeated 2 times , "B" and "C" can nt be repeated(occur only once)
 *         JNumberTools.of("A","B","C")
 *                 ..repetitiveMultiset(3, multisetFreqArray)
 *                 .forEach(System.out::println);
 *
 * will generate following combinations of size 3 -
 * [A, A, B]
 * [A, A, C]
 * [A, B, C]
 * </pre>
 * @author Deepesh Patel
 */
public final class RepetitiveCombinationMultiset<T> extends AbstractGenerator<T> {

    private final int[] multisetFreqArray;
    private final int r;

    /**
     * @param input List of N items
     * @param r number of items in every combination.
     * @param multisetFreqArray int array containing the number of times(count) element in input can be repeated.
     *               multisetFreqArray[0] contains the count for 0th element in input
     *               multisetFreqArray[1] contains the count for 1st element in input, ...
     *               multisetFreqArray[n] contains the count of mth element in input
     *               count of every element must be &gt;=1
     */
    public RepetitiveCombinationMultiset(List<T> input, int r, int[] multisetFreqArray) {
        super(input);
        CombinatoricsUtil.checkParamCombination(seedElements.size(), r, "Repetitive Combination");
        CombinatoricsUtil.checkParamMultisetFreqArray(seedElements.size(), multisetFreqArray, "Repetitive Combination");

        this.r = r;
        this.multisetFreqArray = multisetFreqArray;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return ( r==0 || seedElements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices;

        private Itr(){
            indices = new int[r];
            indices[0]  = -1;
            indices = nextRepetitiveCombinationOfMultiset(indices, multisetFreqArray);
        }

        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextRepetitiveCombinationOfMultiset(indices, multisetFreqArray);
            return indicesToValues(old, seedElements);
        }

        private int[] nextRepetitiveCombinationOfMultiset(int[]a, int[] availableCount) {

            int[] next = Arrays.copyOf(a, a.length);
            int i=next.length-1;
            int maxSupportedValue = availableCount.length-1;

            while( i>=0 && next[i] == maxSupportedValue) {
                i--;
            }

            if(i==-1) {
                return new int[]{};
            }

            i = next[0] ==-1 ? 0 : i; //hack to indicate first combination
            int fillValue = next[i] + 1;
            int k=i;

            while(k<next.length && fillValue <availableCount.length) {
                int availableFillValueCount  = availableCount[fillValue];
                while (availableFillValueCount > 0 && k < next.length) {
                    next[k] = fillValue;
                    availableFillValueCount--;
                    k++;
                }

                fillValue = fillValue+1;
            }

            if(k<next.length){
                //TODO: This can be optimized by checking if
                // [1] the count of maxSupportedValue is reached and
                // [2] previousValue is maxSupportedValue-1;
                // example: in 56777, count of 7 reached the availableCount(3)
                // and previous number is 6
                return nextRepetitiveCombinationOfMultiset(next, availableCount);
            }
            return next;
        }
    }
}
