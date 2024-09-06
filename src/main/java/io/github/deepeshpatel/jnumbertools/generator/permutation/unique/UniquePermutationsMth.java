/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.FactoradicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamIncrement;

/**
 * Utility to generate permutations with unique values starting from
 * 0<sup>th</sup> permutation(input) and then generate every n<sup>th</sup> permutation in
 * lexicographical order of indices of input values where each value is considered unique.
 * Generating n<sup>th</sup> permutation is important because say, if we need to
 * generate next 100 trillionth permutation
 * of 100 items then it will take months to compute if we go sequentially and then increment
 * to the desired permutations, because the total # of permutations is astronomical (100!= 9.3326E X 10<sup>157</sup>)
 * <p>
 * This class will provide a mechanism to generate directly the next n<sup>th</sup>
 * lexicographical permutation.
 * <pre>
 *  Code example:
 *         new UniquePermutationsMth&lt;&gt;(List.of("A","B","C"),2)
 *                     .forEach(System.out::println);
 *         or
 *
 *         JNumberTools.permutationsOf("A","B","C")
 *                 .uniqueMth(2)
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
public final class UniquePermutationsMth<T> extends AbstractGenerator<T> {

    private final BigInteger start;
    private final BigInteger increment;
    BigInteger numOfPermutations;
    private final int[] initialIndices;
    private final Calculator calculator;

    UniquePermutationsMth(List<T> elements, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements);
        this.start = start;
        this.increment = increment;
        checkParamIncrement(increment, "unique permutations");
        this.calculator = calculator;
        initialIndices = IntStream.range(0, elements.size()).toArray();
    }

    /**
     * Use this method instead of iterator if you need only mth value and not 0th, mth, 2mth
     * creating iterator is expensive because hasNext() implementation requires extra calculations.
     */
    public List<T> build() {
        var indices = FactoradicAlgorithms.unRank(increment, initialIndices.length);
        return indicesToValues(indices);
    }

    @Override
    public Iterator<List<T>> iterator() {
        synchronized (this) {
            numOfPermutations = numOfPermutations == null ? calculator.factorial(elements.size()) : numOfPermutations;
        }
        return new KthItemIterator();
    }

    private class KthItemIterator implements  Iterator<List<T>> {

        BigInteger nextK = start;//BigInteger.ZERO;

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
            return indicesToValues(currentIndices);
        }

        public int[] nextPermutation(BigInteger kth, int numberOfItems) {
            return FactoradicAlgorithms.unRank(kth, numberOfItems);
        }
    }
}