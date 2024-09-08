package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides methods for generating combinations of elements.
 * This class includes functionality to compute combinations with and without repetition,
 * as well as combinations of distinct objects.
 *
 * <p>
 * The class supports:
 * <ul>
 *     <li><strong>Combinations without Repetition:</strong> The number of ways to choose a subset of elements from a larger set where each element is used exactly once.</li>
 *     <li><strong>Combinations with Repetition:</strong> The number of ways to choose a subset of elements from a larger set where elements can be repeated.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>
 * Combinations&lt;Integer&gt; comb = new Combinations&lt;&gt;();
 * List&lt;List&lt;Integer&gt;&gt; result = comb.getCombinationsWithoutRepetition(Arrays.asList(1, 2, 3), 2);
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for more detailed examples and usage scenarios.
 */

public final class Combinations {

    private final Calculator calculator;

    /**
     * Constructs a new {@code Combinations} instance with a default {@code Calculator}.
     */
    public Combinations() {
        this(new Calculator());
    }

    /**
     * Constructs a new {@code Combinations} instance with the specified {@code Calculator}.
     *
     * @param calculator The {@code Calculator} to use for generating combinations.
     */
    public Combinations(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for unique combinations of a specified size from a range of elements.
     *
     * @param n The range of elements.
     * @param r The size of combinations.
     * @return A builder for unique combinations.
     */
    public UniqueCombinationBuilder<Integer> unique(int n, int r) {
        var elements = IntStream.range(0,n).boxed().toList();
        return unique(r, elements);
    }

    /**
     * Creates a builder for unique combinations of a specified size from a varargs list of elements.
     *
     * @param size The size of combinations.
     * @param elements The varargs list of elements.
     * @param <T> The type of elements.
     * @return A builder for unique combinations.
     */
    @SafeVarargs
    public final <T> UniqueCombinationBuilder<T> unique(int size, T... elements) {
        return unique(size, List.of(elements));
    }

    /**
     * Creates a builder for unique combinations of a specified size from a list of elements.
     *
     * @param size The size of combinations.
     * @param elements The list of elements.
     * @param <T> The type of elements.
     * @return A builder for unique combinations.
     */
    public <T> UniqueCombinationBuilder<T> unique(int size, List<T> elements) {
        return new UniqueCombinationBuilder<>(elements, size, calculator);
    }

    /**
     * Creates a builder for repetitive combinations of a specified size from a range of elements.
     *
     * @param n The range of elements.
     * @param r The size of combinations.
     * @return A builder for repetitive combinations.
     * @throws IllegalArgumentException if r<0
     */
    public RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        if(r < 0 ) {
            throw new IllegalArgumentException("r should be >=0");
        }
        var elements = IntStream.range(0,n).boxed().toList();
        return repetitive(r, elements);
    }

    /**
     * Creates a builder for repetitive combinations of a specified size from a varargs list of elements.
     *
     * @param size The size of combinations.
     * @param elements The varargs list of elements.
     * @param <T> The type of elements.
     * @return A builder for repetitive combinations.
     */
    @SafeVarargs
    public final <T> RepetitiveCombinationBuilder<T> repetitive(int size, T... elements) {
        return  repetitive(size, List.of(elements));
    }

    /**
     * Creates a builder for repetitive combinations of a specified size from a list of elements.
     *
     * @param size The size of combinations.
     * @param elements The list of elements.
     * @param <T> The type of elements.
     * @return A builder for repetitive combinations.
     */
    public  <T> RepetitiveCombinationBuilder<T> repetitive(int size, List<T> elements) {
        return new RepetitiveCombinationBuilder<>(elements, size, calculator);
    }

    /**
     * Creates a builder for multiset combinations of elements with specified frequencies and size.
     *
     * @param elements The list of elements.
     * @param frequencies The frequencies of elements.
     * @param size The size of combinations.
     * @param <T> The type of elements.
     * @return A builder for multiset combinations.
     */
    public <T> MultisetCombinationBuilder<T> multiset(List<T> elements, int[] frequencies, int size) {
        if(frequencies == null) {
            throw new IllegalArgumentException("frequencies must be not null");
        }
        for(int f: frequencies) {
            if(f<0) {
                throw new IllegalArgumentException("frequencies must be >=0");
            }
        }
        return new MultisetCombinationBuilder<>(elements, frequencies, size);
    }
}
