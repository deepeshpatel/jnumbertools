/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Object of this class encapsulates the Factoradic representation of a
 * positive integer value
 * @author Deepesh Patel
 */
public final class Factoradic implements Comparable<Factoradic>{
    private final List<Integer> values;
    private final BigInteger intValue;

    /**
     * @param positiveNumber non -ve integer to be converted to Factoradic
     */
    public Factoradic(long positiveNumber) {
        this(BigInteger.valueOf(positiveNumber));
    }

    /**
     * @param positiveInt non -ve integer to be converted to Factoradic
     */
    public Factoradic(BigInteger positiveInt) {
        values = factoradicOf(positiveInt);
        intValue = positiveInt;
    }

    private Factoradic(List<Integer> values, BigInteger intValue) {
        this.values = values;
        this.intValue = intValue;
    }

    /**
     * @return List containing Factoradic place values this Factoradic
     *  values are stored in order of indices of the list that is
     *  0th index contains 0! place value. toString method of this class
     *  prints the values in reverse order for correct reading interpretation.
     */
    public List<Integer> getValues() {
        return new ArrayList<>(values);
    }

    /**
     * @return Decimal number equivalent of this Factoradic
     */
    public BigInteger decimalValue() {
        return intValue;
    }

    /**
     * @param positiveNumber number to be added
     * @return Factoradic representing addition of this and positiveNumber
     */
    public Factoradic add(long positiveNumber) {
        return new Factoradic(add(values,positiveNumber) , intValue.add(BigInteger.valueOf(positiveNumber)));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for(int i=values.size()-1; i>0; i--) {
            sb.append(values.get(i));
            sb.append(", ");
        }

        sb.append(values.get(0));
        sb.append("]");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factoradic that = (Factoradic) o;
        return this.values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    private  List<Integer> factoradicOf(BigInteger k) {

        if(k.compareTo(BigInteger.ZERO)==0){
            return Collections.singletonList(0);
        }
        List<Integer> factoradic = new ArrayList<>();
        BigInteger d = BigInteger.ONE;

        while(k.compareTo(BigInteger.ZERO)!=0){
            int kModD = k.mod(d).intValue();
            factoradic.add(kModD);
            k = k.divide(d);
            d = d.add(BigInteger.ONE);
        }
        return factoradic;
    }

    private List<Integer> add(List<Integer> factoradic, long decimalValueToAdd) {
        int radix = 1;
        List<Integer> result = new ArrayList<>(factoradic);

        for(int i=1; i<result.size(); i++) {
            radix++;
            long value = result.get(i)+decimalValueToAdd;
            if(value < radix){
                result.set(i,(int)value);
                return result;
            }

            result.set(i,(int)(value%radix));
            decimalValueToAdd = value/radix;
        }

        while(decimalValueToAdd > 0){
            radix++;
            result.add((int) (decimalValueToAdd%radix));
            decimalValueToAdd = decimalValueToAdd/radix;
        }
        return result;
    }

    @Override
    public int compareTo(Factoradic o) {
        return this.intValue.compareTo(o.intValue);
    }
}