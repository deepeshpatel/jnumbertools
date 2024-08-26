/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombination;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationMth;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutation;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutationsMth;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
 * Permutations are generated in lex order of combinations of indices of input values, considering value at each index as unique.
 * <pre>
 * Code example -
 *
 *  new KPermutationMth&lt;&gt;(List.of("A","B","C"),2,2) //size =2 and increment to every 2nd permutation
 *      .forEach(System.out::println);
 *
 *  or
 *
 *  JNumberTools.permutationsOf("A","B","C")
 *      .kMth(2,2) //size =2 and increment to every 2nd permutation starting from 0
 *      .forEach(System.out::println);
 *
 * will generate -
 * [A, B]
 * [A, C]
 * [B, C]
 * </pre>
 * @author Deepesh Patel
 */
public final class KPermutationCombinationOrderMth<T> extends AbstractKPermutation<T> {

    final BigInteger totalPermutations;
    final long permutationsPerList;
    final BigInteger increment;
    private final Calculator calculator;

    /**
     * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
     * Permutations are generated in lex order of indices of input values, considering value at each index as unique.
     * @param input Input of size n from which permutations of size k will be generated
     * @param k size of permutations. k must be &lt;=n
     * @param increment position relative to first permutation which will be generated next in lexicographical order.
     *                  generating every 'increment' permutation in a sequence.
     *                  For example, if increment is set to 2, the output will generate 0<sup>th</sup>, 2<sup>nd</sup>,
     *                  4<sup>th</sup>.. permutations.
     *                  This is important because for large k, it is impractical to generate all possible k! permutations
     *                  and then increment to the desired position
     */
    public KPermutationCombinationOrderMth(List<T> input, int k, BigInteger increment, Calculator calculator) {
        super(input, k);
        CombinatoricsUtil.checkParamIncrement(increment, "mth K-Permutation");
        this.increment = increment;
        this.totalPermutations = calculator.nPr(seedElements.size(), k);
        this. permutationsPerList = calculator.factorial(k).longValue();
        this.calculator = calculator;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return ( k==0 || seedElements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        BigInteger currentIncrement = BigInteger.ZERO;

        public Itr() {
        }

        @Override
        public boolean hasNext() {
            return currentIncrement.compareTo(totalPermutations) <  0;
        }

        @Override
        public List<T> next() {
            BigInteger[] divideAndRemainder = currentIncrement.divideAndRemainder(BigInteger.valueOf(permutationsPerList));
            BigInteger combinationListNumber = divideAndRemainder[0];
            long permutationIncrement = divideAndRemainder[1].longValue();
            currentIncrement  = currentIncrement.add(increment);

            List<T> next;
            if(combinationListNumber.equals(BigInteger.ZERO)) {
                next  = new UniqueCombination<>(seedElements, k).iterator().next();
            } else {
                Iterator<List<T>> combinationIterator = new UniqueCombinationMth<>(seedElements, k, combinationListNumber, calculator).iterator();
                combinationIterator.next();
                next = combinationIterator.next();
            }

            Iterator<List<T>> permutationIterator;
            if(permutationIncrement == 0) {
                permutationIterator = new UniquePermutation<>(next).iterator();
            } else {
                BigInteger numOfPer = calculator.factorial(next.size());
                permutationIterator = new UniquePermutationsMth<>(next, BigInteger.valueOf(permutationIncrement), numOfPer).iterator();
                permutationIterator.next();
            }

            return permutationIterator.next();
        }
    }
}