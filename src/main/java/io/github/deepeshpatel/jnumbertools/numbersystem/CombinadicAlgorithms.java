package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Provides algorithms for converting between Combinadic and decimal representations,
 * and for computing ranks and un-ranks of combinations in Combinadic number system.
 * <p>
 * Combinadic algorithms are used for operations related to the Combinadic representation,
 * including converting combinations to Combinadic format, ranking, and finding the next
 * Combinadic value.
 *
 * @author Deepesh Patel
 */
public class CombinadicAlgorithms {

    private final Calculator calculator;

    /**
     * Constructs an instance of {@link CombinadicAlgorithms} with the specified calculator.
     *
     * @param calculator the calculator used for combinatorial calculations.
     */
    public CombinadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Computes the rank of a given combination in Combinadic representation.
     *
     * @param n the total number of elements.
     * @param mthCombination the combination whose rank is to be computed.
     * @return the rank of the given combination in Combinadic representation.
     */
    public BigInteger rank(int n, int[] mthCombination) {
        int[] combinadic = combinationToCombinadic(n, mthCombination);
        BigInteger x = combinadicToDecimal(combinadic);
        BigInteger nCr = calculator.nCr(n, mthCombination.length);
        return nCr.subtract(x).subtract(BigInteger.ONE);
    }

    /**
     * Computes the combination from a given rank in Combinadic representation.
     *
     * @param rank the rank of the combination.
     * @param nCr the total number of combinations.
     * @param n the total number of elements.
     * @param r the size of the combination.
     * @return the combination corresponding to the given rank.
     */
    public int[] unRank(BigInteger rank, BigInteger nCr, int n, int r) {
        BigInteger x = nCr.subtract(rank).subtract(BigInteger.ONE);
        int[] a = decimalToCombinadic(x, r);
        return combinadicToCombination(a, n);
    }

    /**
     * Converts a combination to its Combinadic representation.
     *
     * @param n the total number of elements.
     * @param mthCombination the combination to be converted.
     * @return an array representing the Combinadic values of the combination.
     */
    public static int[] combinationToCombinadic(int n, int[] mthCombination) {
        int[] combinadic = new int[mthCombination.length];
        for (int i = 0; i < combinadic.length; i++) {
            combinadic[i] = n - 1 - mthCombination[i];
        }
        return combinadic;
    }

    /**
     * Converts a Combinadic representation to a combination.
     *
     * @param combinadic the Combinadic representation to be converted.
     * @param n the total number of elements.
     * @return an array representing the combination.
     */
    public static int[] combinadicToCombination(int[] combinadic, int n) {
        int[] a = Arrays.copyOf(combinadic, combinadic.length);
        for (int i = 0; i < a.length; i++) {
            a[i] = n - 1 - a[i];
        }
        return a;
    }

    /**
     * Converts a Combinadic representation to its decimal equivalent.
     *
     * @param combinadic the Combinadic representation to be converted.
     * @return the decimal value of the Combinadic representation.
     */
    public BigInteger combinadicToDecimal(int[] combinadic) {
        BigInteger decimalValue = BigInteger.ZERO;
        int r = combinadic.length;
        for (int j : combinadic) {
            decimalValue = decimalValue.add(calculator.nCr(j, r));
            r--;
        }
        return decimalValue;
    }

    /**
     * Converts a decimal value to its Combinadic representation.
     *
     * @param value the decimal value to be converted.
     * @param degree the degree of the Combinadic representation.
     * @return an array representing the Combinadic values of the decimal number.
     */
    public int[] decimalToCombinadic(BigInteger value, int degree) {
        int[] combinadic = new int[degree];
        int r = degree;
        BigInteger max = value;
        for (int i = 0; r > 0; i++, r--) {
            int n = r;
            BigInteger nCr = calculator.nCr(n, r);
            BigInteger result = nCr;
            while (nCr.compareTo(max) <= 0) {
                result = nCr;
                nCr = calculator.nCr(++n, r);
            }
            combinadic[i] = n - 1;
            max = max.subtract(result);
        }
        return combinadic;
    }

    /**
     * Computes the next Combinadic value in the sequence.
     *
     * @param combinadic the current Combinadic value.
     * @return an array representing the next Combinadic value.
     */
    public static int[] nextCombinadic(List<Integer> combinadic) {
        int[] result = combinadic.stream().mapToInt(Integer::intValue).toArray();
        int k = 0;
        for (int i = result.length - 1; i > 0; i--) {
            result[i] = result[i] + 1;
            if (result[i] < result[i - 1]) {
                return result;
            }
            result[i] = k++;
        }
        result[0]++;
        return result;
    }

    //TODO: Add algorithm for nextMthCombinadic(int[] combinadic) without converting to decimal.
}
