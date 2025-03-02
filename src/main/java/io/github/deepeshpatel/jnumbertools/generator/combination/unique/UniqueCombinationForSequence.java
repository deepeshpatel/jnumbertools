/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Generates an iterable sequence of unique combinations based on a provided sequence of ranks.
 * <p>
 * This class generates combinations of size {@code r} from the input elements, where each combination is determined
 * by a rank from the provided {@code Iterable<BigInteger>}. The ranks are mapped to combinations using combinadic
 * unranking, supporting various sampling strategies (e.g., without replacement, with replacement, lexicographical).
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class UniqueCombinationForSequence<T> extends AbstractGenerator<T> {

    private final int r;
    private final BigInteger nCr;
    private final CombinadicAlgorithms algorithms;
    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a new UniqueCombinationForSequence instance.
     *
     * @param elements   the list of N items from which combinations are generated
     * @param r          the size of each combination (r); must be ≤ N
     * @param ranks      the iterable providing the sequence of ranks
     * @param calculator the Calculator used for computing combination counts
     * @throws IllegalArgumentException if parameters are invalid
     */
    public UniqueCombinationForSequence(List<T> elements, int r, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.r = r;
        this.ranks = ranks;
        checkParamCombination(elements.size(), r, "unique combination sequence");
        this.nCr = calculator.nCr(elements.size(), r);
        this.algorithms = new CombinadicAlgorithms(calculator);
    }

    /**
     * Returns an iterator that generates unique combinations based on the provided rank sequence.
     *
     * @return an iterator over lists of elements representing combinations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    private class SequenceIterator implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;

        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
        }

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public List<T> next() {
            BigInteger m = rankIterator.next();
            int[] indices = algorithms.unRank(m, nCr, elements.size(), r);
            return indicesToValues(indices);
        }
    }
}