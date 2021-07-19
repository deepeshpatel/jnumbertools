/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCrBig;

/**
 * Implements the iterable generating every n<sup>th</sup> unique combination of size k.
 * Combinations are generated in lex order of indices of input values,
 * considering value at each indices as unique.
 *
 * This concept is important because count of combinations can grow astronomically
 * large and to generate say, every 100 trillionth combination of 50 items out of 100 (100Choose50),
 * we do not like to wait for 100's of hours to generate all 1.008913445 X 10<sup>29</sup> combinations
 * sequentially and then selecting the required one.
 *
 * <pre>
 *     Code example -
 *     new UniqueCombinationNth&lt;&gt;(Arrays.asList("A","B","C","D"),2,2)
 *                  .forEach(System.out::println);
 *
 *     or
 *
 *     JNumberTools.combinationsOf("A","B","C","D")
 *                 .uniqueNth(2,1)
 *                 .forEach(System.out::println);
 *
 * will generate following(0th, 2nd and 4th) unique combinations of all 6 possible combinations:
 * [A, B]
 * [A, D]
 * [B, D]
 * </pre>
 *
 * @author Deepesh Patel
 */
public class UniqueCombinationNth<T> extends AbstractGenerator<T> {

    private final int r;
    private final BigInteger k;

    /**
     * @param seed List of N items
     * @param r size of combinations from N items. r must be &lt;= N for generating unique combinations
     * @param skipTo next combination in lex order to be generated after previous combination
     *               starting from the 0th(first) combination.
     */
    public UniqueCombinationNth(Collection<T> seed, int r, long skipTo) {
        this(seed,r,BigInteger.valueOf(skipTo));
    }

    public UniqueCombinationNth(Collection<T> seed, int r, BigInteger skipTo) {
        super(seed);
        this.r = r;
        this.k = skipTo;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return (r==0 || r>seed.size()) ? Collections.emptyIterator() : new Itr();
    }


    private class Itr implements Iterator<List<T>> {

        BigInteger currentK;
        int[] result;

        private Itr(){
            result =  IntStream.range(0, r).toArray();
            currentK = k;
        }

        @Override
        public boolean hasNext() {
            return result.length!=0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = result;
            result = kthUniqueCombination(seed.size(),r,currentK);
            currentK  = currentK.add(k);
            return AbstractGenerator.indicesToValues(old, seed);
        }

        private int[] kthUniqueCombination(int n, int r, BigInteger k) {
            BigInteger x = nCrBig(n, r).subtract(BigInteger.ONE).subtract(k);
            if (x.compareTo(BigInteger.ZERO) < 0) {
                return new int[]{};
            }

            Combinadic combinadic = new Combinadic(x,r);
            int[] a = combinadic.value();
            for(int i=0; i<a.length; i++) {
                a[i] = n-1-a[i];
            }
            return a;
        }
    }
}