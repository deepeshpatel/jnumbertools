package io.github.deepeshpatel.jnumbertools.numbersystem.permutadic;

import io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PermutadicAlgorithms {

    public static List<Integer> toPermutadic(long decimalValue, int degree){
        return toPermutadic(BigInteger.valueOf(decimalValue),degree);
    }

    public static List<Integer> toPermutadic(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = new ArrayList<>();
        ++degree;

        do  {
            BigInteger deg = BigInteger.valueOf(degree);
            BigInteger[] divideAndRemainder =  decimalValue.divideAndRemainder(deg);
            permutadicValues.add(divideAndRemainder[1].intValue());
            decimalValue = divideAndRemainder[0];
            degree++;
        }while((decimalValue.compareTo(BigInteger.ZERO) > 0));
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
        List<Integer> allValues = IntStream.range(0, s).boxed().collect(Collectors.toList());

        for(int i=a.length-1,j=0; i>=0; i--,j++) {
            int index;
            if(i> permutadic.size()-1) {
                index = 0;
            } else {
                index = permutadic.get(i);
            }
            a[j] = allValues.remove(index);
        }
        return a;
    }

    public static List<Integer> nthPermutationToPermutadic(int[] nthPermutation, int degree) {

        List<Integer> a = new ArrayList<>();
        int size = degree + nthPermutation.length;

        List<Integer> allValues = IntStream.range(0, size).boxed().collect(Collectors.toList());
        allValues.removeAll(Arrays.stream(nthPermutation).boxed().collect(Collectors.toList()));

        for(int i=nthPermutation.length-1; i>=0; i--) {
            int index = Collections.binarySearch(allValues,nthPermutation[i]);
            index = -(index+1);

            allValues.add(index,nthPermutation[i]);
            a.add(index);
        }
        return a;
    }

    //assumption: nth_kPermutation is valid permutation
    public static BigInteger rank(int[] nth_kPermutation, int size) {
        int degree = size-nth_kPermutation.length;
        List<Integer> perm2 = nthPermutationToPermutadic(nth_kPermutation,degree);
        return  toDecimal(perm2,degree);
    }

    public static int[] unRank(BigInteger rank, int size, int k) {
        BigInteger maxPermutationCount = MathUtil.nPrBig(size,k);
        if(maxPermutationCount.compareTo(rank) <=0 ) {
            String message = String.format("Out of range. Can't decode %d to nth permutation as it is >= Permutation(%d,%d).", rank,size,k);
            throw new ArithmeticException(message);
        }
        List<Integer> permutadic = toPermutadic(rank, size - k);
        return toNthPermutation(permutadic, size,k);
    }
}
