/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Generates an iterable sequence of repetitive combinations based on a provided sequence of ranks.
 * <p>
 * This class generates combinations of size {@code r} from the input elements, where elements may repeat
 * within each combination (e.g., [A, A, B]). The combinations are determined by ranks from the provided
 * {@code Iterable<BigInteger>}, mapped to combinations using a lexicographical unranking algorithm.
 * Supports various sampling strategies (e.g., without replacement, with replacement, lexicographical).
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class RepetitiveCombinationForSequence<T> extends AbstractGenerator<T> {

    private final int r;
    private final Calculator calculator;
    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a new RepetitiveCombinationForSequence instance.
     *
     * @param elements   the list of N items from which combinations are generated
     * @param r          the size of each combination (r); must be non-negative
     * @param ranks      the iterable providing the sequence of ranks
     * @param calculator the Calculator used for computing combination counts
     * @throws IllegalArgumentException if r is negative
     */
    public RepetitiveCombinationForSequence(List<T> elements, int r, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.r = r;
        this.ranks = ranks;
        this.calculator = calculator;
        if (r < 0) {
            throw new IllegalArgumentException("Combination size (r) must be non-negative");
        }
    }

    /**
     * Returns an iterator that generates repetitive combinations based on the provided rank sequence.
     *
     * @return an iterator over lists of elements representing repetitive combinations
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
            int[] indices = mthCombinationWithRepetition(m.longValueExact());
            return indicesToValues(indices);
        }

        private int[] mthCombinationWithRepetition(long m) {
            int n = elements.size();
            int[] result = new int[r];
            BigInteger remainingM = BigInteger.valueOf(m);
            int currentValue = 0;

            for (int i = 0; i < r; i++) {
                for (int j = currentValue; j < n; j++) {
                    BigInteger combinationsWithCurrentElement = calculator.nCrRepetitive(n - j, r - i - 1);
                    if (remainingM.compareTo(combinationsWithCurrentElement) < 0) {
                        result[i] = j;
                        currentValue = j;
                        break;
                    }
                    remainingM = remainingM.subtract(combinationsWithCurrentElement);
                }
            }
            return result;
        }
    }
}