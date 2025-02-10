package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An abstract base class for generators that produce collections of elements.
 * <p>
 * This class provides common functionality for generators that produce combinations,
 * permutations, and other collections of elements.
 *
 * <p>
 * The class supports:
 * <ul>
 *     <li><strong>Element Retrieval:</strong> Converts indices to their corresponding values.</li>
 *     <li><strong>Stream Support:</strong> Provides a stream view of the generated collections.</li>
 *     <li><strong>Empty Iterator:</strong> Provides an iterator for the empty collection.</li>
 * </ul>
 *
 * @param <E> The type of elements generated by this class.
 *
 * @author Deepesh Patel
 */
public abstract class AbstractGenerator<E> implements Iterable<List<E>> {

    /**
     * The list of elements used by this generator.
     * This list is immutable and initialized in the constructor. If {@code null} is provided, it is replaced with an empty list.
     */
    protected final List<E> elements;

    /**
     * Constructs a new {@code AbstractGenerator} with the specified list of elements.
     *
     * @param elements The list of elements to use in the generator. If {@code null}, an empty list is used.
     */
    protected AbstractGenerator(List<E> elements) {
        this.elements = (elements != null) ? elements : Collections.emptyList();
    }

    /**
     * Converts an array of indices to their corresponding values in the element list.
     *
     * @param indices The array of indices to convert.
     * @return A list of values corresponding to the given indices.
     */
    protected List<E> indicesToValues(int[] indices) {
        var output = new ArrayList<E>(indices.length);
        for (int index : indices) {
            output.add(elements.get(index));
        }
        return output;
    }

    /**
     * Provides a stream view of the generated collections.
     *
     * @return A {@link Stream} of lists representing the generated collections.
     */
    public Stream<List<E>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Creates an iterator that yields an empty collection.
     * @param <E> The type of elements to be generated.
     * @return An iterator for an empty collection.
     */
    protected static <E> Iterator<List<E>> newEmptyIterator() {
        var list = new ArrayList<List<E>>();
        list.add(Collections.emptyList());
        return list.iterator();
    }

    /**
     * Initializes an array of indices for generating permutations with given frequencies.
     *
     * @param frequencies An array of frequencies for the multiset permutation.
     * @return An array of indices corresponding to the frequencies.
     */
    protected static int[] initIndicesForMultisetPermutation(int... frequencies) {
        return IntStream.range(0, frequencies.length)
                .flatMap(i -> IntStream.generate(() -> i).limit(frequencies[i]))
                .toArray();
    }

    /**
     * Checks the validity of the frequencies array for generating a multiset.
     *
     * @param inputSize The size of the input list.
     * @param frequencies The frequencies array.
     * @param message A descriptive message for the error.
     * @throws IllegalArgumentException If the frequencies array is null or its length does not match the input size.
     */
    protected static void checkParamMultisetFreqArray(int inputSize, int[] frequencies, String message) {
        if (frequencies == null) {
            throw new IllegalArgumentException("frequencies must be non-null to generate multiset " + message);
        }

        if (frequencies.length != inputSize) {
            throw new IllegalArgumentException("Length of frequencies should be equal to input length to generate " + message + " of multiset");
        }
    }

    /**
     * Checks the validity of the increment value for generating every nth element.
     *
     * @param increment The increment value.
     * @param message A descriptive message for the error.
     * @throws IllegalArgumentException If the increment value is less than or equal to zero.
     */
    protected static void checkParamIncrement(BigInteger increment, String message) {
        if (increment.signum() <= 0) {
            throw new IllegalArgumentException("Increment value must be > 0 to generate every nth " + message);
        }
    }

    /**
     * Checks the validity of the combination parameters.
     *
     * @param inputSize The size of the input list.
     * @param r The size of the combination.
     * @param message A descriptive message for the error.
     * @throws IllegalArgumentException If {@code r} is greater than {@code inputSize} or less than zero.
     */
    protected static void checkParamCombination(int inputSize, int r, String message){
        if (r > inputSize) {
            throw new IllegalArgumentException("r should be <= input length to generate " + message);
        }
        if (r < 0) {
            throw new IllegalArgumentException("r should be >= 0 to generate " + message);
        }
    }
}
