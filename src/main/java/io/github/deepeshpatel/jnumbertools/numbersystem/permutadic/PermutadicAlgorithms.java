package io.github.deepeshpatel.jnumbertools.numbersystem.permutadic;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPrBig;

public class PermutadicAlgorithms {

    public  static void checkBounds(BigInteger decimal, int size, int degree) {
        BigInteger maxSupported = nPrBig(size, degree);
        if(decimal.compareTo(maxSupported) >= 0) {
            String message = String.format("Out of range. Can't decode %d to nth permutation as it is >= Permutation(%d,%d).", decimal,size,degree);
            throw new ArithmeticException(message);
        }
    }

    public static int[] decimalToPermutadic(BigInteger decimalValue, int size, int degree) {

        if(degree> size) {
            throw new IllegalArgumentException(" degree " + degree + " should be <= size " + size);
        }

        int[] a = IntStream.range(0,degree).toArray();

        long j=size-degree+1L;
        for(int i=a.length-1; i>0; i--, j++) {
            BigInteger bigJ = BigInteger.valueOf(j);
            a[i] = decimalValue.mod(bigJ).intValue();
            decimalValue = decimalValue.divide(bigJ);
        }

        a[0] = decimalValue.intValue();
        return a;
    }

    public static BigInteger permutadicToDecimal(int[] value, int size){

        BigInteger result = BigInteger.valueOf(value[value.length-1]);
        BigInteger startingMultiplier = BigInteger.valueOf(size - value.length+1);
        BigInteger multiplier = startingMultiplier;
        for(int i=value.length-2; i>=0; i--) {

            BigInteger bigValueI = BigInteger.valueOf(value[i]);
            result = result.add(bigValueI.multiply(multiplier));
            startingMultiplier = startingMultiplier.add(BigInteger.ONE);
            multiplier = multiplier.multiply(startingMultiplier);
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

    public static BigInteger rank(int[] nthKPermutation, int size) {
        int[] perm2 = nthPermutationToPermutadic(nthKPermutation,size);
        return  permutadicToDecimal(perm2,size);
    }

    public static int[] unRankWithoutBoundCheck(BigInteger rank, int size, int degree) {
        int [] permutadic = decimalToPermutadic(rank,size,degree);
        return permutadicToNthPermutation(permutadic, size);
    }

    public static int[] unRankingWithBoundCheck(BigInteger rank, int size, int degree) {
        checkBounds(rank,size,degree);
        return unRankWithoutBoundCheck(rank,size,degree);
    }
}
