/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem.permutadic;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms.*;

/**
 *
 * Permutadic or Permutational-number-system is the term I introduced for a number system based on permutations.
 * Definition: Permutational-number-system of size s and degree k, for some positive integers s and k
 * where 1 ≤ k ≤ s , is a correspondence between natural numbers (starting from 0) and
 * k-permutations, represented as sequence [Ck−1, Ck−2, Ck−3 . . . C1, C0][s] where Ci ∈ N.
 * Permutational-number-system of size s and degree k also referred to as Permutadic(s, k) for short,
 * is a mixed radix number system that has a unique representation for all natural numbers.
 * The number n corresponding to the permutadic string -
 * [Ck−1, Ck−2, Ck−3 . . . C1, C0][s]
 *
 * is expressed by the following equation -
 * n = Summation[Permutation(s-i,k-i) * Ck-i] for i= 1 to k
 * Where s−iPk−i is the place value for the ith digit/number from right
 *
 * @author Deepesh Patel
 */
public final class Permutadic {

    private final int degree;
    private final List<Integer> value;
    private final BigInteger decimalValue;

    private Permutadic(BigInteger decimalValue, List<Integer> value, int degree) {
        this.degree = degree;
        this.value = Collections.unmodifiableList(value);
        this.decimalValue = decimalValue;
    }

//    public static Permutadic of(long decimalValue, int s, int k) {
//        return of(BigInteger.valueOf(decimalValue),s-k);
//    }

    public static Permutadic of(long decimalValue, int degree) {
        return of(BigInteger.valueOf(decimalValue),degree);
    }

    public static Permutadic fromNthPermutation(int[] nthPerm, int degree) {
        List<Integer> perm = nthPermutationToPermutadic(nthPerm, degree);
        BigInteger decimal = toDecimal(perm, degree);
        return new Permutadic(decimal, perm, degree);
    }

    public int[] toNthPermutation(int countOfSelectItems) {
        return PermutadicAlgorithms.toNthPermutation(value,degree+countOfSelectItems ,countOfSelectItems);
    }

    public static Permutadic of(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = toPermutadic(decimalValue, degree);
        return new Permutadic(decimalValue, permutadicValues, degree);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for(int i=value.size()-1; i>0; i--) {
            s.append(value.get(i)).append(",");
        }
        s.append(value.get(0)).append("](").append(degree).append(")");
        return s.toString();
    }

    public int getDegree() {
        return degree;
    }

    public List<Integer> getValue() {
        return value;
    }

    public BigInteger decimalValue() {
        return decimalValue;
    }

    public String toMathExpression() {
        int s = degree;
        int k = 0;
        String expression = "";
        while(k < value.size()) {
            expression = value.get(k) + "("+ (s++) + "P" + (k++) +")" + " + " + expression;
        }
        return expression.substring(0, expression.length()-2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permutadic that = (Permutadic) o;
        return degree == that.degree && decimalValue.equals(that.decimalValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(degree, decimalValue);
    }
}