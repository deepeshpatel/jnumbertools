package io.github.deepeshpatel.jnumbertools.examples.fordevs.intermediate;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Demonstrates ranking (position) and unranking (get by position)
 * Essential for understanding combinatorial number systems
 */
public class RankAndUnrankExamples {
    public static void main(String[] args) {

        // RANKING: Find position of a permutation
        System.out.println("=== Ranking Examples ===");
        int[] permutation = {2, 0, 1};
        BigInteger rank = JNumberTools.rankOf()
                .uniquePermutation(permutation);
        System.out.printf("Permutation %s has rank %d%n",
                Arrays.toString(permutation), rank);

        // UNRANKING: Get permutation at a specific rank
        System.out.println("\n=== Unranking Examples ===");
        int size = 4;
        BigInteger targetRank = BigInteger.valueOf(10);
        int[] result = JNumberTools.unrankOf()
                .uniquePermutation(targetRank, size);
        System.out.printf("%d-th permutation of size %d is %s%n",
                targetRank, size, Arrays.toString(result));

        // Combination ranking
        System.out.println("\n=== Combination Ranking ===");
        int[] combination = {0, 2, 4, 6, 8};
        BigInteger combRank = JNumberTools.rankOf()
                .uniqueCombination(10, combination);
        System.out.printf("Combination %s from 10 elements has rank %d%n",
                Arrays.toString(combination), combRank);
    }
}
