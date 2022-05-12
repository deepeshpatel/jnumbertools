package io.github.deepeshpatel.jnumbertools.numbersystem.permutadic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class PermutadicAlgorithms {

    public  static void checkBounds(long decimal, int size, int degree) {
        long maxSupported = nPr(size, degree);
        if(decimal >= maxSupported) {
            String message = String.format("Out of range. Can't decode %d to nth permutation as it is >= Permutation(%d,%d).", decimal,size,degree);
            throw new ArithmeticException(message);
        }
    }

    public static int[] decimalToPermutadic(long decimalValue, int size, int degree) {

        if(degree> size) {
            throw new IllegalArgumentException(" degree " + degree + " should be <= size " + size);
        }

        int[] a = IntStream.range(0,degree).toArray();

        long j=size-degree+1L;
        for(int i=a.length-1; i>0; i--, j++) {
            a[i] = (int) (decimalValue % j);
            decimalValue =  decimalValue / j;
        }
        a[0] = (int) decimalValue;
        return a;
    }

    public static long permutadicToDecimal(int[] value, int size){
        long result = value[value.length-1];
        long startingMultiplier = size - value.length+1;
        long multiplier = startingMultiplier;
        for(int i=value.length-2; i>=0; i--) {
            result = result + (value[i] * multiplier);
            startingMultiplier++;
            multiplier = multiplier * startingMultiplier;
        }
        return result;
    }

    public static int[] permutadicToNthPermutation(int[] permutadic, int size) {

        int[] a = new int[permutadic.length];
        List<Integer> allValues = IntStream.range(0, size).boxed().collect(Collectors.toList());

        for(int i=0; i<a.length; i++) {
            a[i] = allValues.remove(permutadic[i]);
        }
        return a;
    }

    public static int[] nthPermutationToPermutadic(int[] nthPermutation, int size) {

        int[] a = new int[nthPermutation.length];

        List<Integer> allValues = IntStream.range(0, size).boxed().collect(Collectors.toList());
        allValues.removeAll(Arrays.stream(nthPermutation).boxed().collect(Collectors.toList()));

        for(int i=a.length-1; i>=0; i--) {
            int index = Collections.binarySearch(allValues,nthPermutation[i]);
            index = -(index+1);

            allValues.add(index,nthPermutation[i]);
            a[i] = index;
        }
        return a;
    }

    public static long rank(int[] nthKPermutation, int size) {
        int[] perm2 = nthPermutationToPermutadic(nthKPermutation,size);
        return  permutadicToDecimal(perm2,size);
    }

    public static int[] unRankWithoutBoundCheck(long rank, int size, int degree) {
        int [] permutadic = decimalToPermutadic(rank,size,degree);
        return permutadicToNthPermutation(permutadic, size);
    }

    public static int[] unRankingWithBoundCheck(long rank, int size, int degree) {
        checkBounds(rank,size,degree);
        return unRankWithoutBoundCheck(rank,size,degree);
    }
}
