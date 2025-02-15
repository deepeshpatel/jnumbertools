/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Encapsulates the Factoradic representation of a positive integer.
 * <p>
 * The Factoradic numeral system expresses an integer as a sum of factorials weighted by digits.
 * In this system, the digit at position <em>i</em> has a weight of <em>i!</em>. This class provides methods
 * to convert a positive integer into its Factoradic representation and to retrieve the original decimal value.
 * </p>
 * <p>
 * The {@code factoradicValues} list stores the digits in order from the least significant digit (index 0)
 * to the most significant digit.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class Factoradic implements Serializable {

    /**
     * The Factoradic representation of the integer, where each element is a digit in the Factoradic numeral system.
     * <p>
     * The list is ordered from the least significant digit (index 0) to the most significant.
     * </p>
     */
    public transient final List<Integer> factoradicValues;

    /**
     * The decimal equivalent of this Factoradic representation.
     */
    public final BigInteger decimalValue;

    private Factoradic(List<Integer> factoradicValues, BigInteger decimalValue) {
        this.factoradicValues = Collections.unmodifiableList(factoradicValues);
        this.decimalValue = decimalValue;
    }

    /**
     * Creates a {@link Factoradic} instance from a positive integer.
     *
     * @param positiveInt the positive integer to be converted to Factoradic representation.
     * @return a {@link Factoradic} instance representing the given positive integer.
     */
    public static Factoradic of(BigInteger positiveInt) {
        int[] intValues = FactoradicAlgorithms.intToFactoradic(positiveInt);
        var values = IntStream.of(intValues).boxed().toList();
        return new Factoradic(values, positiveInt);
    }

    /**
     * Creates a {@link Factoradic} instance from a positive long value.
     *
     * @param positiveInt the positive long value to be converted to Factoradic representation.
     * @return a {@link Factoradic} instance representing the given positive long value.
     */
    public static Factoradic of(long positiveInt) {
        return Factoradic.of(BigInteger.valueOf(positiveInt));
    }

    @Override
    public String toString() {
        // Print the Factoradic digits in reverse order for readability.
        StringBuilder sb = new StringBuilder("[");
        for (int i = factoradicValues.size() - 1; i > 0; i--) {
            sb.append(factoradicValues.get(i)).append(", ");
        }
        sb.append(factoradicValues.get(0)).append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factoradic that = (Factoradic) o;
        return decimalValue.equals(that.decimalValue);
    }

    @Override
    public int hashCode() {
        return decimalValue.hashCode();
    }
}
