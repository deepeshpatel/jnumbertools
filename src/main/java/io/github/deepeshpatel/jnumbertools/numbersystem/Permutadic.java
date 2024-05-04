/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms.*;

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
 * is expressed by the following equation -
 * n = Summation[Permutation(s-i,k-i) * Ck-i] for i= 1 to k
 * Where s−iPk−i is the place value for the ith digit/number from right
 *
 * @author Deepesh Patel
 */
public final class Permutadic implements Serializable {

    public final int degree;
    public final BigInteger decimalValue;
    public final transient List<Integer> permutadicValues;

    private Permutadic(BigInteger decimalValue, List<Integer> permutadicValues, int degree) {
        this.degree = degree;
        this.decimalValue = decimalValue;
        this.permutadicValues = List.copyOf(permutadicValues);
    }

    public static Permutadic of(long decimalValue, int degree) {
        return of(BigInteger.valueOf(decimalValue),degree);
    }

    public static Permutadic of(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = toPermutadic(decimalValue, degree);
        return new Permutadic(decimalValue, permutadicValues, degree);
    }

    public static Permutadic fromNthPermutation ( int[] nthPerm, int degree){
        List<Integer> perm = nthPermutationToPermutadic(nthPerm, degree);
        BigInteger decimal = toDecimal(perm, degree);
        return new Permutadic(decimal, perm, degree);
    }

    public int[] toNthPermutation ( int countOfSelectItems){
        return PermutadicAlgorithms.toNthPermutation(permutadicValues, degree + countOfSelectItems, countOfSelectItems);
    }

    @Override
    public String toString () {
        StringBuilder s = new StringBuilder("[");
        for (int i = permutadicValues.size() - 1; i > 0; i--) {
            s.append(permutadicValues.get(i)).append(",");
        }
        s.append(permutadicValues.get(0)).append("](").append(degree).append(")");
        return s.toString();
    }

    public String toMathExpression () {

        StringBuilder expression;

        int k = permutadicValues.size()-1;
        int s = degree + k;
        expression = new StringBuilder(permutadicValues.get(k) + "(" + (nPrString(s--, k--)) + ")");
        for(; k >=0; s--, k--) {
            expression.append(" + ")
                    .append(permutadicValues.get(k))
                    .append("(")
                    .append(nPrString(s, k))
                    .append(")");
        }
        return expression.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permutadic that = (Permutadic) o;
        return degree == that.degree && decimalValue.equals(that.decimalValue);
    }

    @Override
    public int hashCode () {
        return Objects.hash(degree, decimalValue);
    }

    public static String nPrString(int n, int r) {
        return  superOrSubscript("" + n, Permutadic::superscript) + "P" + superOrSubscript("" + r,  Permutadic::subscript);
    }

    private static String superOrSubscript(String s, Function<Character, Character> superOrSubscriptConverter){
        StringBuilder sb = new StringBuilder(s.length());
        for (char value : s.toCharArray()) {
            sb.append(superOrSubscriptConverter.apply(value));
        }
        return sb.toString();
    }

    private static char superscript(char c) {
        return switch (c) {
            case '1' -> '¹';
            case '2' -> '²';
            case '3' -> '³';
            default -> (char) (8304+c-48);
        };
    }

    private static char subscript(char c) {
        return (char) (8320+c-48);
    }
}