/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Object of this class encapsulates the Factoradic representation of a
 * positive integer value
 *
 * @author Deepesh Patel
 */
public final class Factoradic implements Serializable {

    public transient final List<Integer> factoradicValues;
    public final BigInteger decimalValue;

    private Factoradic(List<Integer> factoradicValues, BigInteger decimalValue) {
        this.factoradicValues = factoradicValues;
        this.decimalValue = decimalValue;
    }

    public static Factoradic of(BigInteger positiveInt) {
        int[] intValues = FactoradicAlgorithms.intToFactoradic(positiveInt);
        var values = IntStream.of(intValues).boxed().toList();
        return new Factoradic(values, positiveInt);
    }

    public static Factoradic of(long positiveInt) {
        return Factoradic.of(BigInteger.valueOf(positiveInt));
    }

    @Override
    public String toString() {
        //print in reverse for readability.
        StringBuilder sb = new StringBuilder("[");
        for (int i = factoradicValues.size() - 1; i > 0; i--) {
            sb.append(factoradicValues.get(i));
            sb.append(", ");
        }

        sb.append(factoradicValues.get(0));
        sb.append("]");
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