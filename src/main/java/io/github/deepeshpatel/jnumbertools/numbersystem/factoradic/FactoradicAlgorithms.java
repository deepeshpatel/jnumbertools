package io.github.deepeshpatel.jnumbertools.numbersystem.factoradic;

import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class FactoradicAlgorithms {



    public static  int[] intToFactoradic(BigInteger k) {

        if(k.equals(BigInteger.ZERO)) {
            return new int[]{0};
        }

        List<Integer> factoradic = new ArrayList<>();
        long d = 1;

        while(!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = k.divideAndRemainder(BigInteger.valueOf(d));
            factoradic.add(divideAndRemainder[1].intValue());
            k = divideAndRemainder[0];
            d++;
        }

        return factoradic.stream().mapToInt(Integer::intValue).toArray();
    }

    public static  int[] intToFactoradicKnowSize(BigInteger k, int knownSize) {

        int[] factoradic = new int[knownSize];

        if(k.equals(BigInteger.ZERO)) {
            return factoradic;
        }

        long d = 1;
        int i=knownSize-1;

        while(!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = k.divideAndRemainder(BigInteger.valueOf(d));
            factoradic[i--] = divideAndRemainder[1].intValue();
            k = divideAndRemainder[0];
            d++;
        }

        return factoradic;
    }

    public static BigInteger factoradicToInt(int[] factoradic) {

        if(factoradic.length == 1) {
            return BigInteger.ZERO;
        }

        BigInteger result = BigInteger.ZERO;
        BigInteger placeValue = BigInteger.ONE;
        int multiplier = 1;

        for(int i=factoradic.length-2; i>=0; i--) {
            placeValue = placeValue.multiply(BigInteger.valueOf(multiplier++));
            BigInteger currentDigit = BigInteger.valueOf(factoradic[i]);
            result = result.add(placeValue.multiply(currentDigit));
        }

        return result;
    }

    public static int[] factoradicToNthPermutation(int[] factoradic){

        int[] output = IntStream.range(0, factoradic.length).toArray();

        for (int i = 0; i < output.length; i++) {
            int index = factoradic[i] + i;

            if(index != i) {
                int temp = output[index];
                if (index - i >= 0) {
                    System.arraycopy(output, i, output, i + 1, index - i);
                }
                output[i] = temp;
            }
        }
        return output;
    }

    public static int[] unRank(BigInteger rank, int size) {
        return factoradicToNthPermutation(intToFactoradicKnowSize(rank,size));
    }

    public static BigInteger rank(int[] nthPermutation) {
        List<Integer> factoradic = PermutadicAlgorithms.nthPermutationToPermutadic(nthPermutation, 0);
        return  PermutadicAlgorithms.toDecimal(factoradic,0);// factoradicToInt(factoradic);
    }


}
