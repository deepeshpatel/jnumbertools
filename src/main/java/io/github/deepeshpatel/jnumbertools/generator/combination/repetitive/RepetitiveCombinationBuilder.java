package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.List;

/**
 * A builder class for generating repetitive combinations of a specified size from a list of elements.
 * <p>
 * This class provides methods to generate all repetitive combinations in lexicographic order, retrieve the mth
 * lexicographic combination, and count the total number of possible combinations.
 *
 * @param <T> the type of elements in the combinations
 *
 * @author Deepesh Patel
 */
public final class RepetitiveCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;
    private long count = -1;

    /**
     * Constructs a {@code RepetitiveCombinationBuilder} with the specified elements, size, and calculator.
     *
     * @param elements   the list of elements to generate combinations from
     * @param size       the size of the combinations to generate
     * @param calculator the calculator to use for computing combinatorial values
     */
    public RepetitiveCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Generates all repetitive combinations in lexicographic order.
     *
     * @return a {@code RepetitiveCombination} instance for iterating over all combinations
     */
    public RepetitiveCombination<T> lexOrder() {
        return new RepetitiveCombination<>(elements, size);
    }

    /**
     * Retrieves the mth lexicographic repetitive combination, starting from a specified index.
     *
     * @param m     the mth combination to retrieve (0-based index)
     * @param start the starting index for generating combinations
     * @return a {@code RepetitiveCombinationMth} instance for generating the mth combination
     */
    public RepetitiveCombinationMth<T> lexOrderMth(long m, long start) {
        return new RepetitiveCombinationMth<>(elements, size, m, start, calculator);
    }

    /**
     * Retrieves the mth lexicographic repetitive combination, starting from a specified index.
     *
     * @param m     the mth combination to retrieve (0-based index)
     * @param start the starting index for generating combinations
     * @return a {@code RepetitiveCombinationMth} instance for generating the mth combination
     */
    public RepetitiveCombinationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new RepetitiveCombinationMth<>(elements, size, m.longValue(), start.longValue(), calculator);
    }

    /**
     * Returns the total number of possible repetitive combinations.
     * <p>
     * The result is cached for efficiency.
     *
     * @return the total number of combinations
     */
    public synchronized BigInteger count() {

        if (count == -1) {
            count = calculator.nCrRepetitive(elements.size(), size).longValue();
        }
        return BigInteger.valueOf(count);
    }
}
