package io.github.deepeshpatel.jnumbertools.numbersystem;

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
}
