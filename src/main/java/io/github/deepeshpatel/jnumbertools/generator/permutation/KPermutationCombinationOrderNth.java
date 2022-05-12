/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombination;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNth;
import io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.newEmptyIterator;

/**
 * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
 * Permutations are generated in lex order of combinations of indices of input values, considering value at each index as unique.
 * <pre>
 * Code example -
 *
 *  new KPermutationNth&lt;&gt;(Arrays.asList("A","B","C"),2,2) //size =2 and increment to every 2nd permutation
 *      .forEach(System.out::println);
 *
 *  or
 *
 *  JNumberTools.permutationsOf("A","B","C")
 *      .kNth(2,2) //size =2 and increment to every 2nd permutation starting from 0
 *      .forEach(System.out::println);
 *
 * will generate -
 * [A, B]
 * [A, C]
 * [B, C]
 * </pre>
 * @author Deepesh Patel
 */
public class KPermutationCombinationOrderNth<T> extends AbstractGenerator<T> {

    final int k;
    final BigInteger totalPermutations;
    final long permutationsPerList;
    final BigInteger increment;

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
    public KPermutationCombinationOrderNth(Collection<T> input, int k, BigInteger increment) {
        super(input);
        CombinatoricsUtil.checkParamIncrement(increment, "nth K-Permutation");
        CombinatoricsUtil.checkParamKPermutation(seed.size(),k,"nth K-Permutation");
        this.increment = increment;
        this.k = k;
        this.totalPermutations = MathUtil.nPrBig(seed.size(), k);
        this. permutationsPerList = MathUtil.factorial(k).longValue();
    }

    @Override
    public Iterator<List<T>> iterator() {
        return ( k==0 || seed.isEmpty()) ? newEmptyIterator() : new Itr();
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
            BigInteger permutationsPerListBig = BigInteger.valueOf(permutationsPerList);
            BigInteger combinationListNumber = currentIncrement.divide(permutationsPerListBig);
            long permutationIncrement = currentIncrement.mod(permutationsPerListBig).longValue();
            currentIncrement  = currentIncrement.add(increment);

            List<T> next;
            if(combinationListNumber.equals(BigInteger.ZERO)) {
                next  = new UniqueCombination<>(seed, k).iterator().next();
            } else {
                Iterator<List<T>> combinationIterator = new UniqueCombinationNth<>(seed, k, combinationListNumber).iterator();
                combinationIterator.next();
                next = combinationIterator.next();
            }

            Iterator<List<T>> permutationIterator;
            if(permutationIncrement == 0) {
                permutationIterator = new UniquePermutation<>(next).iterator();
            } else {
                permutationIterator = new UniquePermutationsNth<>(next, BigInteger.valueOf(permutationIncrement)).iterator();
                permutationIterator.next();
            }

            return permutationIterator.next();
        }
    }
}