/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

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
    final BigInteger start;
    private final Calculator calculator;

    /**
     * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
     * Permutations are generated in lex order of indices of input values, considering value at each index as unique.
     * @param elements Input of size n from which permutations of size k will be generated
     * @param k size of permutations. k must be &lt;=n
     * @param increment position relative to first permutation which will be generated next in lexicographical order.
     *                  generating every 'increment' permutation in a sequence.
     *                  For example, if increment is set to 2, the output will generate 0<sup>th</sup>, 2<sup>nd</sup>,
     *                  4<sup>th</sup>.. permutations.
     *                  This is important because for large k, it is impractical to generate all possible k! permutations
     *                  and then increment to the desired position
     */
     KPermutationCombinationOrderMth(List<T> elements, int k, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements, k);
         AbstractGenerator.checkParamIncrement(increment, "mth K-Permutation");
        this.start = start;
        this.increment = increment;
        this.totalPermutations = calculator.nPr(elements.size(), k);
        this. permutationsPerList = calculator.factorial(k).longValue();
        this.calculator = calculator;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return ( k==0 || elements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        BigInteger currentIncrement = start;

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
            currentIncrement = currentIncrement.add(increment);

            List<T> next = combinationListNumber.equals(BigInteger.ZERO) ?
                    new Combinations(calculator).unique(k,elements).lexOrder().iterator().next() :
                    new Combinations(calculator).unique(k, elements).lexOrderMth(combinationListNumber, BigInteger.ZERO).build();

            return permutationIncrement == 0 ?
                    new Permutations(calculator).unique(next).lexOrder().iterator().next() :
                    new Permutations(calculator).unique(next).lexOrderMth(permutationIncrement,0).build();
        }
    }
}