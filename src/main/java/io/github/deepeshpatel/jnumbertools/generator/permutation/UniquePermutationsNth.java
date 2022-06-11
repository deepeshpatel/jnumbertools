/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.factoradic.FactoradicAlgorithms;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamIncrement;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.factorial;

/**
 * Utility to generate permutations with unique values starting from
 * 0<sup>th</sup> permutation(input) and then generate every n<sup>th</sup> permutation in
 * lexicographical order of indices of input values where each value is considered unique.
 * Generating n<sup>th</sup> permutation is important because say, if we need to
 * generate next 100 trillionth permutation
 * of 100 items then it will take months to compute if we go sequentially and then increment
 * to the desired permutations, because the total # of permutations is astronomical (100!= 9.3326E X 10<sup>157</sup>)
 *
 * This class will provide a mechanism to generate directly the next n<sup>th</sup>
 * lexicographical permutation.
 * <pre>
 *  Code example:
 *         new UniquePermutationsNth&lt;&gt;(Arrays.asList("A","B","C"),2)
 *                     .forEach(System.out::println);
 *         or
 *
 *         JNumberTools.permutationsOf("A","B","C")
 *                 .uniqueNth(2)
 *                 .forEach(System.out::println);
 *
 *  will generate following (0th, 2nd and 4th) unique permutations of A,B and C)-
 *
 * [A, B, C]
 * [B, A, C]
 * [C, A, B]
 * </pre>
 *
 * @author Deepesh Patel
 */
public class UniquePermutationsNth<T> extends AbstractGenerator<T> {

    private final BigInteger increment;
    final BigInteger numOfPermutations;
    private final int[] initialIndices;

    public UniquePermutationsNth(Collection<T> input, BigInteger increment) {
        super(input);
        this.increment = increment;
        checkParamIncrement(increment, "unique permutations");
        numOfPermutations = factorial(seed.size());
        initialIndices = IntStream.range(0, seed.size()).toArray();
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new KthItemIterator();
    }

    private class KthItemIterator implements  Iterator<List<T>> {

        BigInteger nextK = BigInteger.ZERO;

        public KthItemIterator() {
        }

        @Override
        public boolean hasNext() {
            return nextK.compareTo(numOfPermutations) < 0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            int[] currentIndices = nextPermutation(nextK, initialIndices.length);
            nextK = nextK.add(increment);
            return indicesToValues(currentIndices, seed);
        }

        public int[] nextPermutation(BigInteger kth, int numberOfItems) {
            return FactoradicAlgorithms.unRank(kth, numberOfItems);
        }
    }
}