/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;

import java.util.*;

/**
 *
 * Utility for generating r-combinations of Seed = {1, 2, . . ., n}
 *
 * Generates r combinations from n=seed.length items.
 * combinations are generated in Lexicographic order
 * of indices of items in a list. This class will not check for duplicate values and
 * treats all values differently based on the index
 *
 * Repetitive combinations of 3 items out of 2{1, 2} are -
 * <pre>
 * 111 112 122 222
 * </pre>
 *
 * <pre>
 * Code example:
 *
 *         int[] supply = new int[]{2,1,1};
 *         new RepetitiveCombinationLimitedSupply&lt;&gt;(Arrays.asList("A","B","C"),3,supply)
 *                 .forEach(System.out::println);
 *
 *         or
 *
 *         //here "A" can be repeated 2 times , "B" and "C" can nt be repeated(occur only once)
 *         JNumberTools.combinationsOf("A","B","C")
 *                 .repetitiveWithSupply(3, supply)
 *                 .forEach(System.out::println);
 *
 * will generate following combinations of size 3 -
 * [A, A, B]
 * [A, A, C]
 * [A, B, C]
 * </pre>
 * @author Deepesh Patel
 */
public class RepetitiveCombinationLimitedSupply <T> extends AbstractGenerator<T> {

    private final int[] supply;
    private final int r;

    /**
     * @param seed List of N items
     * @param r number of items in every combination.
     * @param supply int array containing the number of times(count) element in seed can be repeated.
     *               supply[0] contains the count for 0th element in seed
     *               supply[1] contains the count for 1st element in seed, ...
     *               supply[n] contains the count of nth element in seed
     *               count of every element must be &gt;=1
     */
    public RepetitiveCombinationLimitedSupply(Collection<T> seed, int r, int[] supply) {
        super(seed);
        this.r = r;
        this.supply = supply;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return nullCase() ? Collections.emptyIterator() : new Itr();
    }

    private boolean nullCase() {
        return seed.isEmpty()
                || r<=0 || supply== null
                || supply.length !=seed.size();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices;

        private Itr(){
            indices = new int[r];
            indices[0]  = -1;
            indices = nextRepetitiveCombinationWithLimitedSupply(indices, supply);
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
            indices = nextRepetitiveCombinationWithLimitedSupply(indices, supply);
            return AbstractGenerator.indicesToValues(old, seed);
        }

        private int[] nextRepetitiveCombinationWithLimitedSupply(int[]a, int[] availableCount) {

            int[] next = new int[a.length];
            System.arraycopy(a,0,next,0, a.length);

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
                return nextRepetitiveCombinationWithLimitedSupply(next, availableCount);
            }
            return next;
        }
    }
}
