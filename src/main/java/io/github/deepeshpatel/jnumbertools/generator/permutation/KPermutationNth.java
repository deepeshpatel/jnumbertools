/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNth;
import io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
 * Permutations are generated in lex order of indices of input values, considering value at each indices as unique.
 * <pre>
 * Code example -
 *
 *  new KPermutationNth&lt;&gt;(Arrays.asList("A","B","C"),2,2) //size =2 and skipTo every 2nd permutation
 *      .forEach(System.out::println);
 *
 *  or
 *
 *  JNumberTools.permutationsOf("A","B","C")
 *      .kNth(2,2) //size =2 and skipTo every 2nd permutation starting from 0
 *      .forEach(System.out::println);
 *
 * will generate -
 * [A, B]
 * [A, C]
 * [B, C]
 * </pre>
 * @author Deepesh Patel
 */
public class KPermutationNth<T> extends AbstractGenerator<T> {

    final int k;
    final long skip;

    /**
     * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
     * Permutations are generated in lex order of indices of input values, considering value at each indices as unique.
     * @param seed Input of size n from which permutations of size k will be generated
     * @param k size of permutations. k must be &lt;=n
     * @param skipTo position relative to first permutation which will be generated next in lexicographical order.
     *                  skipping every skipTo-1 permutation in a sequence.
     *                  For example, if skipTo is set to 2, the output will generate 0<sup>th</sup>, 2<sup>nd</sup>,
     *                  4<sup>th</sup>.. permutations.
     *                  This is important because for large k, it is impractical to generate all possible k! permutations
     *                  and then skip to the desired position
     */
    public KPermutationNth(Collection<T> seed, int k, long skipTo) {
        super(seed);

        if(k<0 || k>seed.size()) {
            throw new IllegalArgumentException("k must be >= 0 and < length of input");
        }
        if(skipTo<=0) {
            throw new IllegalArgumentException("skipTo must be > 0");
        }
        this.k = k;
        this.skip = skipTo;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return k==0 ? emptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int currentSkip;
        final long totalPermutations;
        final long permutationsPerList;
        final BigInteger permutationsInList;
        BigInteger cumulativeCount;
        int combinationListNumber;

        public Itr() {
            totalPermutations = MathUtil.nPr(seed.size(), k);
            long totalNoOfLists = MathUtil.nCr(seed.size(),k);
            permutationsPerList = totalPermutations/totalNoOfLists;
            permutationsInList = BigInteger.valueOf(permutationsPerList);
            cumulativeCount = permutationsInList.add(BigInteger.ZERO);
            combinationListNumber = 0;
        }

        @Override
        public boolean hasNext() {
            return currentSkip<totalPermutations;
        }

        @Override
        public List<T> next() {
            BigInteger currentSkipAsBig = BigInteger.valueOf(currentSkip);

            while (cumulativeCount.compareTo(currentSkipAsBig) <= 0) {
                combinationListNumber++;
                cumulativeCount = cumulativeCount.add(permutationsInList);
            }
            long skipValue = currentSkip % permutationsPerList;
            currentSkip +=skip;

            Iterator<List<T>> combinationIterator = new UniqueCombinationNth<>(seed, k, combinationListNumber).iterator();
            combinationIterator.next(); //0th
            List<T> next = combinationIterator.next();

            Iterator<List<T>> permutationIterator = new UniquePermutationsNth<>(next, skipValue).iterator();
            permutationIterator.next(); //0th
            return permutationIterator.next();
        }
    }
}