/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An abstract base class for generators that produce collections of elements.
 * <p>
 * This class provides common functionality for generators that produce combinations,
 * permutations, and other collections of elements. Concrete generator implementations
 * should extend this class.
 * </p>
 *
 * <p>
 * Features provided by this class include:
 * </p>
 * <ul>
 *     <li><strong>Element Retrieval:</strong> Converts indices to their corresponding values.</li>
 *     <li><strong>Stream Support:</strong> Provides a stream view of the generated collections.</li>
 *     <li><strong>Empty Iterator:</strong> Supplies an iterator for an empty collection (mimicking the null-set (φ)).</li>
 * </ul>
 *
 * @param <E> the type of elements generated by this class.
 * @author Deepesh Patel
 * @version 3.0.1
 */
public abstract class AbstractGenerator<E> implements Iterable<List<E>> {

    /**
     * The list of elements used by this generator.
     * <p>
     * This list is initialized during construction. If {@code null} is provided,
     * it is replaced with an empty list.
     * </p>
     */
    protected final List<E> elements;

    /**
     * Constructs a new {@code AbstractGenerator} with the specified list of elements.
     *
     * @param elements the list of elements to use in the generator; if {@code null},
     *                 an empty list is used.
     */
    protected AbstractGenerator(List<E> elements) {
        this.elements = (elements != null) ? elements : Collections.emptyList();
    }

    /**
     * Converts an array of indices to their corresponding values from the element list.
     *
     * @param indices the array of indices to convert.
     * @return a list of values corresponding to the provided indices.
     */
    protected final List<E> indicesToValues(int[] indices) {
        var output = new ArrayList<E>(indices.length);
        for (int index : indices) {
            output.add(elements.get(index));
        }
        return output;
    }

    /**
     * Provides a sequential {@link Stream} with the generated collections as its source.
     *
     * @return a {@link Stream} of lists representing the generated collections.
     */
    public Stream<List<E>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }


    /**
     * Initializes an array of indices for generating multiset permutations based on given frequencies.
     *
     * @param frequencies an array of frequencies for the multiset permutation.
     * @return an array of indices corresponding to the provided frequencies.
     */
    public static int[] initIndicesForMultisetPermutation(int... frequencies) {
        return IntStream.range(0, frequencies.length)
                .flatMap(i -> IntStream.generate(() -> i).limit(frequencies[i]))
                .toArray();
    }

    /**
     * Validates the frequencies array for generating a multiset.
     *
     * @param inputSize   the size of the input list.
     * @param frequencies the frequencies array.
     * @param message     a descriptive message for the error context.
     * @throws IllegalArgumentException if the frequencies array is {@code null} or its length
     *                                  does not match the input size.
     */
    protected static void checkParamMultisetFreqArray(int inputSize, int[] frequencies, String message) {
        if (frequencies == null) {
            throw new IllegalArgumentException("Frequencies must be non-null to generate multiset " + message);
        }

        if (frequencies.length != inputSize) {
            throw new IllegalArgumentException("Length of frequencies should be equal to input length to generate "
                    + message + " of multiset");
        }
    }

    /**
     * Validates the increment value for generating every nth element.
     *
     * @param increment the increment value.
     * @param message   a descriptive message for the error context.
     * @throws IllegalArgumentException if the increment value is less than or equal to zero.
     */
    protected static void checkParamIncrement(BigInteger increment, String message) {
        if (increment.signum() <= 0) {
            throw new IllegalArgumentException("Increment value must be > 0 to generate every nth " + message);
        }
    }

    /**
     * Validates the parameters for generating combinations.
     *
     * @param inputSize the size of the input list.
     * @param r         the size of the combination.
     * @param message   a descriptive message for the error context.
     * @throws IllegalArgumentException if {@code r} is greater than {@code inputSize} or less than zero.
     */
    protected static void checkParamCombination(int inputSize, int r, String message) {
        if (r > inputSize) {
            throw new IllegalArgumentException("r should be <= input length to generate " + message);
        }
        if (r < 0) {
            throw new IllegalArgumentException("r should be >= 0 to generate " + message);
        }
    }
}
