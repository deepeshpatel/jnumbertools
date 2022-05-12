/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem.factoradic;

import java.math.BigInteger;

/**
 * Object of this class encapsulates the Factoradic representation of a
 * positive integer value
 * @author Deepesh Patel
 */
public final class Factoradic {

    private final int[] values;
    private final long intValue;

    /**
     * @param positiveInt non -ve integer to be converted to Factoradic
     */
    public Factoradic(long positiveInt) {
        values =  FactoradicAlgorithms.intToFactoradic(BigInteger.valueOf(positiveInt));//factoradicOf(positiveInt);
        intValue = positiveInt;
    }

    /**
     * @return List containing Factoradic place values this Factoradic
     *  values are stored in order of indices of the list that is
     *  0th index contains 0! place value. toString method of this class
     *  prints the values in reverse order for correct reading interpretation.
     */
    public int[] getValues() {
        return values;
    }

    /**
     * @return Decimal number equivalent of this Factoradic
     */
    public long decimalValue() {
        return intValue;
    }

    @Override
    public String toString() {
        //print in reverse for readability.
        StringBuilder sb = new StringBuilder("[");
        for(int i=values.length-1; i>0; i--) {
            sb.append(values[i]);
            sb.append(", ");
        }

        sb.append(values[0]);
        sb.append("]");
        return sb.toString();
    }
}