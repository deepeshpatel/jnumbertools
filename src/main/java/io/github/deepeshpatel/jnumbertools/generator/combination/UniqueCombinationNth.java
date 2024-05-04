/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil;
import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamCombination;

/**
 * Implements the iterable generating every n<sup>th</sup> unique combination of size k.
 * Combinations are generated in lex order of indices of input values,
 * considering value at each index as unique.
 * <p>
 * This concept is important because count of combinations can grow astronomically
 * large and to generate say, every 100 trillionth combination of 50 items out of 100 (100Choose50),
 * we do not like to wait for 100's of hours to generate all 1.008913445 X 10<sup>29</sup> combinations
 * sequentially and then selecting the required one.
 *
 * <pre>
 *     Code example -
 *     new UniqueCombinationNth&lt;&gt;(List.of("A","B","C","D"),2,2)
 *                  .forEach(System.out::println);
 *
 *     or
 *
 *     JNumberTools.of("A","B","C","D")
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
    private final BigInteger increment;
    private final BigInteger nCr;
    private final Calculator calculator;

    /**
     * @param input List of N items
     * @param r size of combinations from N items. r must be &lt;= N for generating unique combinations
     * @param increment next combination in lex order to be generated after previous combination
     *               starting from the 0th(first) combination.
     */
    public UniqueCombinationNth(Collection<T> input, int r, BigInteger increment, Calculator calculator) {
        super(input);
        this.r = r;
        this.increment = increment;
        checkParamCombination(seed.size(), r, "nth unique combination");
        CombinatoricsUtil.checkParamIncrement(increment, "Unique Combinations");
        this.calculator = calculator;
        this.nCr = calculator.nCr(seed.size(),r);
    }

    @Override
    public  Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        BigInteger rank = BigInteger.ZERO;
        final CombinadicAlgorithms algorithms = new CombinadicAlgorithms(calculator);

        int[] result;

        private Itr(){
            result =  IntStream.range(0, r).toArray();
        }

        @Override
        public boolean hasNext() {
            return rank.compareTo(nCr) < 0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            //TODO: For iterator no need to un-rank if we find the algo for next Kth combinadic
            //TODO: Assignment for Aditya
            result = algorithms.unRank(rank, nCr, seed.size(),r);
            rank = rank.add(increment);
            return indicesToValues(result, seed);
        }

    }
}