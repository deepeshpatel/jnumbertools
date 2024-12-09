package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for generating unique combinations of elements.
 * <p>
 * This builder provides methods to generate combinations in lexicographical order and to retrieve a specific
 * m-th combination. It uses a {@link Calculator} to compute combination counts and ranks.
 *
 * @param <T> the type of elements in the combinations.
 *
 * @author Deepesh Patel
 */
public final class UniqueCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a new UniqueCombinationBuilder with the given elements, size, and calculator.
     *
     * @param elements the list of elements from which combinations are to be generated.
     * @param size the size of each combination.
     * @param calculator the Calculator used for computing combinations.
     */
    public UniqueCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Creates a new {@link UniqueCombination} instance that generates combinations in lexicographical order.
     *
     * @return a UniqueCombination instance.
     */
    public UniqueCombination<T> lexOrder() {
        return new UniqueCombination<>(elements, size);
    }

    /**
     * Creates a new {@link UniqueCombinationMth} instance to retrieve the m-th combination in lexicographical order.
     *
     * @param m the index of the combination to retrieve.
     * @param start the starting index for the search.
     * @return a UniqueCombinationMth instance.
     */
    public UniqueCombinationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a new {@link UniqueCombinationMth} instance to retrieve the m-th combination in lexicographical order.
     *
     * @param m the index of the combination to retrieve.
     * @param start the starting index for the search.
     * @return a UniqueCombinationMth instance.
     */
    public UniqueCombinationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new UniqueCombinationMth<>(elements, size, start, m, calculator);
    }

    /**
     * Returns the total number of unique combinations of the given size.
     * <p>
     * This method computes the count using the combination formula and caches the result for subsequent calls.
     *
     * @return the total number of unique combinations.
     */
    public BigInteger count() {
        //no need to cache the count as nCr values are cached in calculator
        return calculator.nCr(elements.size(), size);
    }

    @Override
    public String toString() {
        return "UniqueCombinationBuilder{" +
               "elements=" + elements +
               ", size=" + size +
               ", count=" + count() +
               '}';
    }
}
