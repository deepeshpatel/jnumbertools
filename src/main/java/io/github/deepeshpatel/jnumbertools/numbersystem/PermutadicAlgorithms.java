/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/**
 * Provides algorithms for operations related to the Permutadic number system, which is a mixed radix system based on permutations.
 * <p>
 * The class includes methods to convert between decimal values and Permutadic representations, compute m-th permutations,
 * and rank or un-rank permutations.
 *
 * <p>
 * Example usages:
 * <ul>
 * <li>Convert a decimal value to a Permutadic representation: {@link #toPermutadic(BigInteger, int)}</li>
 * <li>Convert a Permutadic representation to a decimal value: {@link #toDecimal(List, int)}</li>
 * <li>Compute the m-th permutation from a Permutadic representation: {@link #toMthPermutation(List, int, int)}</li>
 * <li>Convert an m-th permutation to Permutadic representation: {@link #mthPermutationToPermutadic(int[], int)}</li>
 * <li>Rank a permutation: {@link #rank(int, int...)}</li>
 * <li>Un-rank a permutation with or without bound checks: {@link #unRankWithoutBoundCheck(BigInteger, int, int)}, {@link #unRankWithBoundCheck(BigInteger, int, int)}</li>
 * </ul>
 *
 * @author Deepesh Patel
 */
public final class PermutadicAlgorithms {

    private final Calculator calculator;

    /**
     * Constructs a new PermutadicAlgorithms instance with the given Calculator.
     *
     * @param calculator the Calculator used for computing permutations.
     */
    public PermutadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Converts a decimal value to its Permutadic representation.
     *
     * @param decimalValue the decimal value to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a list of integers representing the Permutadic value.
     */
    public static List<Integer> toPermutadic(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = new ArrayList<>();
        ++degree;

        do {
            BigInteger deg = BigInteger.valueOf(degree);
            BigInteger[] divideAndRemainder = decimalValue.divideAndRemainder(deg);
            permutadicValues.add(divideAndRemainder[1].intValue());
            decimalValue = divideAndRemainder[0];
            degree++;
        } while (decimalValue.signum() > 0);

        return List.copyOf(permutadicValues);
    }

    /**
     * Converts a Permutadic representation to its decimal value.
     *
     * @param permutadicValues the Permutadic representation to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return the decimal value equivalent of the Permutadic representation.
     */
    public static BigInteger toDecimal(List<Integer> permutadicValues, int degree) {
        BigInteger sum = BigInteger.ZERO;
        BigInteger placeValue = BigInteger.ONE;

        for (Integer i : permutadicValues) {
            sum = sum.add(placeValue.multiply(BigInteger.valueOf(i)));
            placeValue = placeValue.multiply(BigInteger.valueOf(++degree));
        }

        return sum;
    }

    /**
     * Computes the m-th permutation from the given Permutadic representation.
     *
     * @param permutadic the Permutadic representation to be converted.
     * @param s the size of the set from which the permutation is selected.
     * @param k the number of items to be selected for the permutation.
     * @return an array representing the m-th permutation.
     */
    public static int[] toMthPermutation(List<Integer> permutadic, int s, int k) {
        int[] a = new int[k];
        LinkedList<Integer> mutableList = IntStream.range(0, s)
                .boxed()
                .collect(toCollection(LinkedList::new));

        for (int i = a.length - 1, j = 0; i >= 0; i--, j++) {
            int index = i >= permutadic.size() ? 0 : permutadic.get(i);
            a[j] = mutableList.remove(index);
        }

        return a;
    }

    /**
     * Converts an m-th permutation to its Permutadic representation.
     *
     * @param mthPermutation the m-th permutation to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a list of integers representing the Permutadic value.
     */
    public static List<Integer> mthPermutationToPermutadic(int[] mthPermutation, int degree) {
        List<Integer> result = new ArrayList<>();
        int size = degree + mthPermutation.length;

        Set<Integer> mthPermutationSet = Arrays.stream(mthPermutation).boxed().collect(Collectors.toSet());

        List<Integer> mutableList = IntStream.range(0, size)
                .filter(i -> !mthPermutationSet.contains(i))
                .boxed()
                .collect(Collectors.toCollection(LinkedList::new));

        for (int i = mthPermutation.length - 1; i >= 0; i--) {
            int index = Collections.binarySearch(mutableList, mthPermutation[i]);
            index = -(index + 1);

            mutableList.add(index, mthPermutation[i]);
            result.add(index);
        }

        return List.copyOf(result);
    }

    /**
     * Computes the rank of a permutation in the Permutadic system.
     *
     * @param size the size of the set from which the permutation is selected.
     * @param mth_kPermutation the m-th permutation to be ranked.
     * @return the rank of the given permutation.
     */
    public static BigInteger rank(int size, int... mth_kPermutation) {
        int degree = size - mth_kPermutation.length;
        List<Integer> perm2 = mthPermutationToPermutadic(mth_kPermutation, degree);
        return toDecimal(perm2, degree);
    }

    /**
     * Un-ranks a permutation without bound checking.
     *
     * @param rank the rank of the permutation to be un-ranked.
     * @param size the size of the set from which the permutation is selected.
     * @param k the number of items to be selected for the permutation.
     * @return an array representing the un-ranked permutation.
     */
    public static int[] unRankWithoutBoundCheck(BigInteger rank, int size, int k) {
        List<Integer> permutadic = toPermutadic(rank, size - k);
        return toMthPermutation(permutadic, size, k);
    }

    /**
     * Un-ranks a permutation with bound checking.
     *
     * @param rank the rank of the permutation to be un-ranked.
     * @param size the size of the set from which the permutation is selected.
     * @param k the number of items to be selected for the permutation.
     * @return an array representing the un-ranked permutation.
     * @throws ArithmeticException if the rank is out of the valid range.
     */
    public int[] unRankWithBoundCheck(BigInteger rank, int size, int k) {
        BigInteger maxPermutationCount = calculator.nPr(size, k);
        if (maxPermutationCount.compareTo(rank) <= 0) {
            String message = "Out of range. Can't decode %d to m-th permutation as it is >= Permutation(%d,%d).";
            message = String.format(message, rank, size, k);
            throw new ArithmeticException(message);
        }

        return unRankWithoutBoundCheck(rank, size, k);
    }
}
