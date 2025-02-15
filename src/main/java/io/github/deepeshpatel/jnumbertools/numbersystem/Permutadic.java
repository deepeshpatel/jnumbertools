/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms.*;

/**
 * Represents a Permutadic number, which is a mixed radix number system based on permutations.
 * <p>
 * The Permutadic number system, denoted as Permutadic(s, k), where s is the size and k is the degree,
 * represents natural numbers as k-permutations with a unique representation for all natural numbers.
 * The number n corresponding to the permutadic string [Ck−1, Ck−2, Ck−3 . . . C1, C0][s] is expressed by
 * the equation: n = Summation[Permutation(s-i,k-i) * Ck-i] for i= 1 to k
 *
 * <p>
 * Example:
 * Given a degree k and size s, the number system allows conversion between natural numbers and their
 * Permutadic representation. For instance, the number 7 in a Permutadic system with size 5 and degree 3
 * can be represented and manipulated using this class.
 *
 * @author Deepesh Patel
 */
public final class Permutadic implements Serializable {

    /**
     * The degree of this Permutadic representation.
     */
    public final int degree;

    /**
     * The decimal value equivalent of this Permutadic representation.
     */
    public final BigInteger decimalValue;

    /**
     * The list of integers representing the Permutadic value.
     */
    public final transient List<Integer> permutadicValues;

    private Permutadic(BigInteger decimalValue, List<Integer> permutadicValues, int degree) {
        this.degree = degree;
        this.decimalValue = decimalValue;
        this.permutadicValues = List.copyOf(permutadicValues);
    }

    /**
     * Creates a Permutadic instance from a long decimal value and a degree.
     *
     * @param decimalValue the decimal value to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a Permutadic instance representing the given decimal value and degree.
     */
    public static Permutadic of(long decimalValue, int degree) {
        return of(BigInteger.valueOf(decimalValue), degree);
    }

    /**
     * Creates a Permutadic instance from a BigInteger decimal value and a degree.
     *
     * @param decimalValue the decimal value to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a Permutadic instance representing the given decimal value and degree.
     */
    public static Permutadic of(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = toPermutadic(decimalValue, degree);
        return new Permutadic(decimalValue, permutadicValues, degree);
    }

    /**
     * Creates a Permutadic instance from an m-th permutation and a degree.
     *
     * @param mthPerm the m-th permutation to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a Permutadic instance representing the given permutation and degree.
     */
    public static Permutadic fromMthPermutation(int[] mthPerm, int degree) {
        List<Integer> perm = mthPermutationToPermutadic(mthPerm, degree);
        BigInteger decimal = toDecimal(perm, degree);
        return new Permutadic(decimal, perm, degree);
    }

    /**
     * Converts this Permutadic instance to an m-th permutation with a given count of selected items.
     *
     * @param countOfSelectItems the number of selected items.
     * @return an array representing the m-th permutation.
     */
    public int[] toMthPermutation(int countOfSelectItems) {
        return PermutadicAlgorithms.toMthPermutation(permutadicValues, degree + countOfSelectItems, countOfSelectItems);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (int i = permutadicValues.size() - 1; i > 0; i--) {
            s.append(permutadicValues.get(i)).append(",");
        }
        s.append(permutadicValues.get(0)).append("](").append(degree).append(")");
        return s.toString();
    }

    /**
     * Returns the mathematical expression representing this Permutadic instance.
     *
     * @return a string representing the mathematical expression of this Permutadic instance.
     */
    public String toMathExpression() {
        StringBuilder expression = new StringBuilder();
        int k = permutadicValues.size() - 1;
        int s = degree + k;
        expression.append(permutadicValues.get(k)).append("(").append(nPrString(s--, k--)).append(")");

        for (; k >= 0; s--, k--) {
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
    public int hashCode() {
        return Objects.hash(degree, decimalValue);
    }

    /**
     * Formats the given values as a permutation notation string.
     *
     * @param n the total number of items.
     * @param r the number of items to be selected.
     * @return a string representing the permutation notation.
     */
    public static String nPrString(int n, int r) {
        return "%sP%s".formatted(superOrSubscript("" + n, Permutadic::superscript), superOrSubscript("" + r, Permutadic::subscript));
    }

    private static String superOrSubscript(String s, Function<Character, Character> superOrSubscriptConverter) {
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
            default -> (char) (8304 + c - 48); //⁴,⁵,⁶,⁷,⁸,⁹
        };
    }

    private static char subscript(char c) {
        return (char) (8320 + c - 48);
    }
}
