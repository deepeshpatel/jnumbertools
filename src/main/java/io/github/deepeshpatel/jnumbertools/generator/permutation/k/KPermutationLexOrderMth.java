/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Generates an iterable of every {@code n}<sup>th</sup> unique permutation of size {@code k}.
 * <p>
 * The permutations are generated in lexicographical order of combinations of indices of input values,
 * with each value at a specific index considered unique. This class allows for efficient generation
 * of permutations by skipping directly to every {@code n}<sup>th</sup> permutation rather than
 * generating all possible permutations.
 * <p>
 *  Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the input list
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class KPermutationLexOrderMth<T> extends AbstractKPermutation<T> {

    final int[] initialValue;
    final BigInteger nPk;
    final BigInteger increment;
    final BigInteger start;

    /**
     * Constructs a new {@code KPermutationLexOrderMth} instance.
     * <p>
     * This constructor sets up an iterable that generates every {@code n}<sup>th</sup> unique permutation
     * of size {@code k}, starting from the specified {@code start} permutation and incrementing by the
     * specified {@code increment} value.
     *
     * @param elements the list of elements from which permutations of size {@code k} will be generated
     * @param k the size of the permutations; must be {@code &lt;= elements.size()}
     * @param increment the increment value specifying the position relative to the first permutation
     *                  which will be generated next in lexicographical order. For example, if increment
     *                  is set to 2, the output will generate 0<sup>th</sup>, 2<sup>nd</sup>, 4<sup>th</sup>, etc.
     *                  permutations. This allows for efficient skipping of permutations, which is important
     *                  for large {@code k} values where generating all permutations would be impractical.
     * @param start the starting permutation index to begin generating permutations from
     * @param calculator a {@link Calculator} instance for computing permutations
     * @throws IllegalArgumentException if {@code increment} is less than or equal to zero
     */
    KPermutationLexOrderMth(List<T> elements, int k, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements, k);
        AbstractGenerator.checkParamIncrement(increment, "K-Permutation");

        this.start = start;
        this.increment = increment;
        this.initialValue = IntStream.range(0, k).toArray();
        this.nPk = calculator.nPr(elements.size(), initialValue.length);
    }

    /**
     * Returns an iterator over elements of type {@code List&lt;T&gt;}.
     * <p>
     * If {@code k == 0}, an empty iterator is returned. Otherwise, an iterator that generates every
     * {@code n}<sup>th</sup> permutation in lexicographical order is returned.
     *
     * @return an iterator for generating every {@code n}<sup>th</sup> permutation of size {@code k}
     */
    @Override
    public Iterator<List<T>> iterator() {
        return k == 0 ? newEmptyIterator() : new Itr();
    }

    /**
     * Inner class implementing the {@code Iterator&lt;List&lt;T&gt;&gt;} interface
     * to generate every {@code n}<sup>th</sup> permutation in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        int[] next = initialValue;
        BigInteger n = start;

        /**
         * Constructs a new {@code Itr} instance to start generating permutations.
         */
        public Itr() {}

        /**
         * Returns {@code true} if there are more permutations to generate.
         *
         * @return {@code true} if there are more permutations; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return n.compareTo(nPk) < 0;
        }

        /**
         * Returns the next permutation in the iteration.
         *
         * @return the next permutation as a {@code List&lt;T&gt;}
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            next = nextMthKPermutation(n, initialValue.length, elements.size());
            n = n.add(increment);
            return indicesToValues(next);
        }

        /**
         * Computes the {@code rank}-th permutation of size {@code degree} from {@code size} elements.
         *
         * @param rank the rank of the permutation to compute
         * @param degree the size of the permutation
         * @param size the total number of elements
         * @return the {@code rank}-th permutation as an array of indices
         */
        private int[] nextMthKPermutation(BigInteger rank, int degree, int size) {
            return PermutadicAlgorithms.unRankWithoutBoundCheck(rank, size, degree);
        }
    }
}
