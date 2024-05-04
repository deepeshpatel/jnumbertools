package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PermutadicAlgorithms {

    private final Calculator calculator;

    public PermutadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    public static List<Integer> toPermutadic(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = new ArrayList<>();
        ++degree;

        do {
            BigInteger deg = BigInteger.valueOf(degree);
            BigInteger[] divideAndRemainder =  decimalValue.divideAndRemainder(deg);
            permutadicValues.add(divideAndRemainder[1].intValue());
            decimalValue = divideAndRemainder[0];
            degree++;
        } while((decimalValue.compareTo(BigInteger.ZERO) > 0));
        return permutadicValues;
    }

    public static BigInteger toDecimal(List<Integer> permutadicValues, int degree) {
        BigInteger sum = BigInteger.ZERO;
        BigInteger placeValue = BigInteger.ONE;

        for(Integer i : permutadicValues) {
            sum  = sum.add(placeValue.multiply(BigInteger.valueOf(i)));
            placeValue = placeValue.multiply(BigInteger.valueOf(++degree));
        }
        return sum;
    }

    public static int[] toNthPermutation(List<Integer> permutadic, int s, int k) {

        int[] a = new int[k];
        List<Integer> mutableList = IntStream.range(0, s).boxed().collect(Collectors.toList());

        for(int i=a.length-1,j=0; i>=0; i--,j++) {
            int index = i >= permutadic.size() ? 0: permutadic.get(i);
            a[j] = mutableList.remove(index);
        }
        return a;
    }

    public static List<Integer> nthPermutationToPermutadic(int[] nthPermutation, int degree) {

        List<Integer> a = new ArrayList<>();
        int size = degree + nthPermutation.length;

        List<Integer> mutableList = IntStream.range(0, size).boxed().collect(Collectors.toList());
        mutableList.removeAll(Arrays.stream(nthPermutation).boxed().toList());

        for(int i=nthPermutation.length-1; i>=0; i--) {
            int index = Collections.binarySearch(mutableList,nthPermutation[i]);
            index = -(index+1);

            mutableList.add(index,nthPermutation[i]);
            a.add(index);
        }
        return a;
    }

    //assumption: nth_kPermutation is valid permutation
    public static BigInteger rank(int size, int... nth_kPermutation) {
        int degree = size-nth_kPermutation.length;
        List<Integer> perm2 = nthPermutationToPermutadic(nth_kPermutation,degree);
        return  toDecimal(perm2,degree);
    }

    public static int[] unRankWithoutBoundCheck(BigInteger rank, int size, int k) {
        List<Integer> permutadic = toPermutadic(rank, size - k);
        return toNthPermutation(permutadic, size,k);
    }

    public int[] unRankWithBoundCheck(BigInteger rank, int size, int k) {
        BigInteger maxPermutationCount = calculator.nPr(size,k);
        if(maxPermutationCount.compareTo(rank) <=0 ) {
            String message = "Out of range. Can't decode %d to nth permutation as it is >= Permutation(%d,%d).";
            message = String.format(message, rank,size,k);
            throw new ArithmeticException(message);
        }

        return unRankWithoutBoundCheck(rank, size, k);
    }
}
