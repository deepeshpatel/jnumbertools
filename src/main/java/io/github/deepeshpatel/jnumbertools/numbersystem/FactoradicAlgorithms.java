package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides various algorithms for working with Factoradic numeral system.
 * This includes conversion between Factoradic representation and permutations,
 * as well as utility functions for ranking and unranking permutations.
 * <p>
 * The Factoradic numeral system is used to represent permutations of a set.
 *
 * @author Deepesh Patel
 */
public class FactoradicAlgorithms {

    private FactoradicAlgorithms() {
    }

    /**
     * Converts a positive integer to its Factoradic representation.
     *
     * @param k the positive integer to be converted.
     * @return an array representing the Factoradic representation of the given integer.
     */
    public static int[] intToFactoradic(BigInteger k) {

        if (k.equals(BigInteger.ZERO)) {
            return new int[]{0};
        }

        List<Integer> factoradic = new ArrayList<>();
        long d = 1;

        while (!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = k.divideAndRemainder(BigInteger.valueOf(d));
            factoradic.add(divideAndRemainder[1].intValue());
            k = divideAndRemainder[0];
            d++;
        }

        return factoradic.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Converts a positive integer to its Factoradic representation with a known size.
     *
     * @param k the positive integer to be converted.
     * @param knownSize the size of the Factoradic representation array.
     * @return an array representing the Factoradic representation of the given integer.
     */
    public static int[] intToFactoradicKnownSize(BigInteger k, int knownSize) {

        int[] factoradic = new int[knownSize];

        if (k.equals(BigInteger.ZERO)) {
            return factoradic;
        }

        long d = 1;
        int i = knownSize - 1;

        while (!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = k.divideAndRemainder(BigInteger.valueOf(d));
            factoradic[i--] = divideAndRemainder[1].intValue();
            k = divideAndRemainder[0];
            d++;
        }

        return factoradic;
    }

    /**
     * Converts a Factoradic representation to the corresponding permutation.
     *
     * @param factoradic the Factoradic representation to be converted.
     * @return an array representing the permutation corresponding to the given Factoradic representation.
     */
    public static int[] factoradicToMthPermutation(int[] factoradic) {

        int[] output = IntStream.range(0, factoradic.length).toArray();

        for (int i = 0; i < output.length; i++) {
            int index = factoradic[i] + i;

            if (index != i) {
                int temp = output[index];
                if (index - i >= 0) {
                    System.arraycopy(output, i, output, i + 1, index - i);
                }
                output[i] = temp;
            }
        }
        return output;
    }

    /**
     * Converts a rank to the corresponding permutation of a given size.
     *
     * @param rank the rank of the permutation.
     * @param size the size of the permutation.
     * @return an array representing the permutation corresponding to the given rank.
     */
    public static int[] unRank(BigInteger rank, int size) {
        return factoradicToMthPermutation(intToFactoradicKnownSize(rank, size));
    }
}
