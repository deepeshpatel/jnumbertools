package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

/**
 * Provides methods for calculating the rank of permutations and combinations.
 * This class uses combinadic and permutadic algorithms to determine the rank
 * of a given permutation or combination.
 *
 * <p>
 * The class supports:
 * <ul>
 *     <li><strong>K-Permutation Rank:</strong> Calculates the rank of a k-permutation.</li>
 *     <li><strong>Unique Permutation Rank:</strong> Calculates the rank of a unique permutation.</li>
 *     <li><strong>Repeated Permutation Rank:</strong> Calculates the rank of a repeated permutation.</li>
 *     <li><strong>Unique Combination Rank:</strong> Calculates the rank of a unique combination.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>
 * RankOf rankOf = new RankOf();
 * BigInteger kPermRank = rankOf.kPermutation(5, 2, 1, 0);
 * BigInteger uniquePermRank = rankOf.uniquePermutation(3, 2, 1, 0);
 * BigInteger repeatedPermRank = rankOf.repeatedPermutation(4, 2, 1, 0);
 * BigInteger uniqueCombRank = rankOf.uniqueCombination(5, 2, 1, 0);
 * </pre>
 */
public final class RankOf {

    private final Calculator calculator;

    /**
     * Constructs a new {@code RankOf} instance with a default {@code Calculator}.
     */
    public RankOf() {
        this(new Calculator());
    }

    /**
     * Constructs a new {@code RankOf} instance with the specified {@code Calculator}.
     *
     * @param calculator The {@code Calculator} to use for ranking calculations.
     */
    public RankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Calculates the rank of a k-permutation.
     *
     * @param size The size of the permutation.
     * @param permutation The permutation for which to calculate the rank.
     * @return The rank of the k-permutation.
     */
    public BigInteger kPermutation(int size, int... permutation) {
        return PermutadicAlgorithms.rank(size, permutation);
    }

    /**
     * Calculates the rank of a unique permutation.
     *
     * @param permutation The permutation for which to calculate the rank.
     * @return The rank of the unique permutation.
     */
    public BigInteger uniquePermutation(int... permutation) {
        return PermutadicAlgorithms.rank(permutation.length, permutation);
    }

    /**
     * Calculates the rank of a repeated permutation.
     *
     * @param base The base used for the permutation.
     * @param permutation The permutation for which to calculate the rank.
     * @return The rank of the repeated permutation.
     */
    public BigInteger repeatedPermutation(int base, Integer... permutation) {

        BigInteger result = BigInteger.ZERO;
        long power = 1;
        for(int i = permutation.length - 1; i >= 0; i--) {
            long placeValue = permutation[i] * power;
            result = result.add(BigInteger.valueOf(placeValue));
            power *= base;
        }
        return result;
    }

    /**
     * Calculates the rank of a unique combination.
     *
     * @param n The total number of distinct objects.
     * @param combination The combination for which to calculate the rank.
     * @return The rank of the unique combination.
     */
    public BigInteger uniqueCombination(int n, int... combination) {
        return new CombinadicAlgorithms(calculator).rank(n, combination);
    }

//not working for large values
//    public BigInteger repeatedCombination(int[] combination, int n, int r) {
//
//        BigInteger rank = BigInteger.ZERO;
//        int currentValue = 0;
//
//        for (int i = 0; i < r; i++) {
//            int element = combination[i];
//
//            for (int j = currentValue; j < element; j++) {
//                rank = rank.add(calculator.nCrRepetitive(n - j, r - i - 1));
//            }
//
//            currentValue = element;
//        }
//
//        return rank;
//    }


}
