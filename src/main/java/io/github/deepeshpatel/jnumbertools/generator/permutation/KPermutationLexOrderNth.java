/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil;
import io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamKPermutation;
import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.newEmptyIterator;

/**
 * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
 * Permutations are generated in lex order of combinations of indices of input values, considering value at each index as unique.
 * <pre>
 * Code example -
 *
 *  new KPermutationNth&lt;&gt;(List.of("A","B","C"),2,2) //size =2 and increment to every 2nd permutation
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
public class KPermutationLexOrderNth<T> extends AbstractGenerator<T> {

    final int k;
    final int[] initialValue;
    final BigInteger nPk;
    final BigInteger increment;

    /**
     * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
     * Permutations are generated in lex order of indices of input values, considering value at each index as unique.
     * @param input Input of size n from which permutations of size k will be generated
     * @param k size of permutations. k must be &lt;=n
     * @param increment position relative to first permutation which will be generated next in lexicographical order.
     *                  incrementing to every increment-1 permutation in a sequence.
     *                  For example, if increment is set to 2, the output will generate 0<sup>th</sup>, 2<sup>nd</sup>,
     *                  4<sup>th</sup>.. permutations.
     *                  This is important because for large k, it is impractical to generate all possible k! permutations
     *                  and then increment to the desired position
     */

    public KPermutationLexOrderNth(Collection<T> input, int k, BigInteger increment) {
        super(input);

        CombinatoricsUtil.checkParamIncrement(increment, "K-Permutation");
        checkParamKPermutation(seed.size(), k,"K-Permutation");

        this.k = k;
        this.increment = increment;
        this.initialValue = IntStream.range(0,k).toArray();
        this.nPk = MathUtil.nPrBig(seed.size(),initialValue.length);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return k==0 ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] next = initialValue;
        BigInteger  n = BigInteger.ZERO;

        public Itr() {}

        @Override
        public boolean hasNext() {
            return n.compareTo(nPk) < 0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            next = nextNthKPermutation(n, initialValue.length,seed.size() );
            n =  n.add(increment);
            return  indicesToValues(next, seed);
        }

        private int[] nextNthKPermutation(BigInteger n, int degree, int size) {
            return Permutadic.of(n,size-degree).toNthPermutation(degree);
            //return PermutadicAlgorithms.unRankWithoutBoundCheck(n,size,degree);
        }
    }
}