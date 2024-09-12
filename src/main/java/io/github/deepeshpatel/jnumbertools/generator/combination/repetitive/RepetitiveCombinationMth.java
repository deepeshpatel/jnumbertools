package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.MthElementGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * A generator class for computing the mth lexicographical combination with repetition, starting from a specified index.
 * <p>
 * This class can also generate subsequent combinations at intervals of a specified increment. The main use case is to
 * generate a specific combination directly without generating all previous combinations.
 *
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the combinations
 *
 * @author Deepesh Patel
 */
public class RepetitiveCombinationMth<T> extends AbstractGenerator<T> implements MthElementGenerator<T> {

    private final int r;
    private final long start;
    private final long increment;
    private final Calculator calculator;

    /**
     * Constructs a {@code RepetitiveCombinationMth} with the specified elements, combination size, increment, start index,
     * and calculator.
     *
     * @param elements   the list of elements to generate combinations from
     * @param r          the size of the combinations
     * @param increment  the increment for generating combinations at intervals
     * @param start      the starting index for generating combinations
     * @param calculator the calculator to use for computing combinatorial values
     */
    RepetitiveCombinationMth(List<T> elements, int r, long increment, long start, Calculator calculator) {
        super(elements);
        this.r = r;
        this.increment = increment;
        this.start = start;
        this.calculator = calculator;
    }

    /**
     * Generates the mth lexicographical combination with repetition, directly from the specified start index.
     * <p>
     * Use this method when you need only the mth combination rather than generating multiple combinations.
     *
     * @return the mth combination as a list of elements
     */
    public List<T> build() {
        int[] idx = mthCombinationWithRepetition(increment);
        return super.indicesToValues(idx);
    }

    /**
     * Returns an iterator over the repetitive combinations, starting from the specified start index.
     * <p>
     * The iterator generates combinations at intervals of the specified increment.
     *
     * @return an iterator over combinations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    /**
     * Computes the mth lexicographical combination with repetition.
     * <p>
     * This method converts the mth combination's index to an array of element indices.
     *
     * @param m the index of the combination to compute
     * @return an array of indices representing the mth combination
     */
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

    /**
     * An iterator implementation for iterating over combinations with repetition, starting from a specified index.
     */
    private class Itr implements Iterator<List<T>> {

        final long max;
        long current = start;

        /**
         * Constructs an iterator that computes combinations starting from the specified start index.
         */
        public Itr() {
            this.max = calculator.nCrRepetitive(elements.size(), r).longValue();
        }

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if there are more combinations, {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return current < max;
        }

        /**
         * Generates the next combination in the sequence.
         *
         * @return the next combination as a list of elements
         */
        @Override
        public List<T> next() {
            int[] idx = mthCombinationWithRepetition(current);
            var result = indicesToValues(idx);
            current += increment;
            return result;
        }
    }
}
