package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.multiset.MultisetPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive.RepetitivePermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutationBuilder;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;


/**
 * Builder class to generating various types of permutations.
 *
 * <p>
 * Supports following permutations -
 * <ul>
 *     <li><strong>Unique permutation generation in lex order:</strong> </li>
 *     <li><strong>Unique permutation generation with single swap order</strong> as as per Heap's algorithm</li>
 *     <li><strong>Mth unique permutation generation in lex order</strong> </li>
 *     <li><strong>Repetitive permutation generation in lex order</strong> </li>
 *     <li><strong>Mth Repetitive permutation generation in lex order</strong> </li>
 *     <li><strong>Multiset permutation generation in lex order</strong> </li>
 *     <li><strong>Mth multiset permutation generation in lex order</strong> </li>
 *     <li><strong>k-permutation generation in lex order</strong> </li>
 *     <li><strong>Mth k-permutation generation in lex order</strong> </li>
 *     <li><strong>k-permutation generation in combination order</strong> </li>
 *     <li><strong>Mth k-permutation generation in combination order</strong> </li>
 * </ul>
 *
 * Example usage:
 * <pre>
 * <code>
 * Permutations perm = new Permutations();
 * List&lt;List&lt;Integer&gt;&gt; result = perm.nPr(3,2).lexOrder().stream().toList();
 * </code>
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 */
public final class Permutations {

    private final Calculator calculator;

    /**
     * Creates a new instance of {@code Permutations} with a default {@code Calculator}.
     */
    public Permutations() {
        this(new Calculator());
    }

    /**
     * Creates a new instance of {@code Permutations} with the specified {@code Calculator}.
     *
     * @param calculator The {@code Calculator} to be used by this instance.
     */
    public Permutations(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for unique permutations of a specified size.
     *
     * @param size The size of the permutations.
     * @return A builder for unique permutations.
     */
    public UniquePermutationBuilder<Integer> unique(int size) {
        return unique(IntStream.range(0, size).boxed().toList());
    }

    /**
     * Creates a builder for unique permutations of a specified list.
     *
     * @param list The list of elements to permute.
     * @param <T> The type of elements.
     * @return A builder for unique permutations.
     */
    public <T> UniquePermutationBuilder<T> unique(List<T> list) {
        return new UniquePermutationBuilder<>(calculator, list);
    }

    /**
     * Creates a builder for unique permutations of a specified array.
     *
     * Caution is required while using this method for single element as it may result in
     * accidental invocation of method unique(int size) for single length char array.
     * For example unique('a') will result in unique(97) because ASCII value of 'a' is 97
     *
     * @param array The array of elements to permute.
     * @param <T> The type of elements.
     * @return A builder for unique permutations.
     */
    @SafeVarargs
    public final <T> UniquePermutationBuilder<T> unique(T... array) {
        return unique(List.of(array));
    }

    /**
     * Creates a builder for permutations of r elements from a set of n elements.
     *
     * @param n The total number of elements.
     * @param k The number of elements to permute.
     * @return A builder for permutations.
     */
    public KPermutationBuilder<Integer> nPk(int n, int k) {
        return nPk(k, IntStream.range(0, n).boxed().toList());
    }

    /**
     * Creates a builder for permutations of r elements from a list of elements.
     *
     * @param k The number of elements to permute.
     * @param allElements The list of all elements.
     * @param <T> The type of elements.
     * @return A builder for permutations.
     */
    public <T> KPermutationBuilder<T> nPk(int k, List<T> allElements) {
        return new KPermutationBuilder<>(allElements, k, calculator);
    }

    /**
     * Creates a builder for permutations of r elements from a varargs list of elements.
     *
     * @param k The number of elements to permute.
     * @param allElements The varargs list of elements.
     * @param <T> The type of elements.
     * @return A builder for permutations.
     */
    @SafeVarargs
    public final <T> KPermutationBuilder<T> nPk(int k, T... allElements) {
        return nPk(k, asList(allElements));
    }

    /**
     * Creates a builder for multiset permutations of elements with specified frequencies.
     *
     * @param elements The list of elements.
     * @param frequencies The frequencies of elements.
     * @param <T> The type of elements.
     * @return A builder for multiset permutations.
     */
    public <T> MultisetPermutationBuilder<T> multiset(List<T> elements, int[] frequencies) {
        return new MultisetPermutationBuilder<>(elements, frequencies, calculator);
    }

    /**
     * Creates a builder for repetitive permutations of elements with specified width.
     *
     * @param width The width of the permutations.
     * @param elements The list of elements.
     * @param <T> The type of elements.
     * @return A builder for repetitive permutations.
     */
    public  <T> RepetitivePermutationBuilder<T> repetitive(int width, List<T> elements) {
        return new RepetitivePermutationBuilder<>(width, elements);
    }

    /**
     * Creates a builder for repetitive permutations of elements with specified width using varargs.
     *
     * @param width The width of the permutations.
     * @param elements The varargs list of elements.
     * @param <T> The type of elements.
     * @return A builder for repetitive permutations.
     */
    @SafeVarargs
    public final <T> RepetitivePermutationBuilder<T> repetitive(int width, T... elements) {
        return repetitive(width, List.of(elements));
    }

    /**
     * Creates a builder for repetitive permutations of integers with specified width and base.
     *
     * @param width The width of the permutations.
     * @param numberOfElements The max number symbols/elements int the permutations.
     * @return A builder for repetitive permutations.
     */
    public RepetitivePermutationBuilder<Integer> repetitive(int width, int numberOfElements) {
        if(numberOfElements <=0) {
            throw new IllegalArgumentException("Repetitive permutation generation required at least 1 element/symbol");
        }
        var symbols = IntStream.range(0, numberOfElements).boxed().toList();
        return repetitive(width, symbols);
    }
}
