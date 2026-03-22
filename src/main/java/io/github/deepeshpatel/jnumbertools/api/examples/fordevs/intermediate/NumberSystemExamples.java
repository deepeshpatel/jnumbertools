package io.github.deepeshpatel.jnumbertools.api.examples.fordevs.intermediate;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;
import io.github.deepeshpatel.jnumbertools.core.internal.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.core.internal.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.core.internal.numbersystem.Permutadic;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Demonstrates combinatorial number systems: Factoradic, Combinadic, and Permutadic
 * Shows how these systems enable efficient ranking/unranking operations
 */
public class NumberSystemExamples {

    public static void main(String[] args) {
        System.out.println("=== Combinatorial Number Systems ===\n");

        factoradicExamples();
        combinadicExamples();
        permutadicExamples();
        practicalApplications();
    }

    private static void factoradicExamples() {
        System.out.println("1. FACTORADIC (Factorial Number System)");
        System.out.println("   Used for ranking permutations\n");

        // Convert decimal to factoradic
        System.out.println("   Decimal 10 in factoradic:");
        Factoradic fac = JNumberTools.numberSystem().factoradic(10);
        System.out.println("   " + fac);  // [1,2,0,0]

        // Get permutation at rank 10 for 4 elements
        System.out.println("\n   Permutation at rank 10 for 4 elements:");
        int[] perm = JNumberTools.unrankOf().uniquePermutation(BigInteger.valueOf(10), 4);
        System.out.println("   " + Arrays.toString(perm));

        // Show the mathematical relationship
        System.out.println("\n   Mathematical representation:");
        System.out.println("   10 = 1×3! + 2×2! + 0×1! + 0×0!");
        System.out.println("       = [1,2,0,0] in factoradic\n");
    }

    private static void combinadicExamples() {
        System.out.println("2. COMBINADIC (Combinatorial Number System)");
        System.out.println("   Used for ranking combinations\n");

        // Convert decimal to combinadic for combinations of size 3
        System.out.println("   Decimal 7 in combinadic (degree 3):");
        Combinadic comb = JNumberTools.numberSystem().combinadic(7, 3);
        System.out.println("   " + comb);  // [4,2,1]

        // Get combination at rank 7 for C(10,3)
        System.out.println("\n   Combination at rank 7 for C(10,3):");
        int[] combination = JNumberTools.unrankOf()
                .uniqueCombination(BigInteger.valueOf(7), 10, 3);
        System.out.println("   " + Arrays.toString(combination));

        // Show the mathematical relationship
        System.out.println("\n   Mathematical representation:");
        System.out.println("   7 = C(4,3) + C(2,2) + C(1,1)");
        System.out.println("     = [4,2,1] in combinadic\n");
    }

    private static void permutadicExamples() {
        System.out.println("3. PERMUTADIC (k-Permutation Number System)");
        System.out.println("   Used for ranking k-permutations\n");

        // Convert decimal to permutadic for k=3 from set of size 8
        System.out.println("   Decimal 42 in permutadic (degree 3):");
        Permutadic perm = JNumberTools.numberSystem().permutadic(42, 3);
        System.out.println("   " + perm);

        // Get k-permutation at rank 42 for P(8,3)
        System.out.println("\n   k-permutation at rank 42 for P(8,3):");
        int[] kPerm = JNumberTools.unrankOf()
                .kPermutation(BigInteger.valueOf(42), 8, 3);
        System.out.println("   " + Arrays.toString(kPerm));

        // Show mathematical expression
        System.out.println("\n   Mathematical expression:");
        System.out.println("   " + perm.toMathExpression());
        System.out.println();
    }

    private static void practicalApplications() {
        System.out.println("4. PRACTICAL APPLICATIONS\n");

        // Efficient lottery ticket lookup
        System.out.println("   Lottery ticket lookup using combinadic:");
        BigInteger lotteryRank = new BigInteger("1234567");
        int[] lotteryTicket = JNumberTools.unrankOf()
                .uniqueCombination(lotteryRank, 49, 6);
        System.out.println("   Ticket at rank " + lotteryRank + ": " +
                Arrays.toString(lotteryTicket));

        // Efficient permutation lookup
        System.out.println("\n   Dictionary word lookup using factoradic:");
        BigInteger wordRank = new BigInteger("1000000");
        int[] wordPerm = JNumberTools.unrankOf()
                .uniquePermutation(wordRank, 10);
        System.out.println("   Permutation at rank " + wordRank + ": " +
                Arrays.toString(wordPerm));

        // Converting between number systems
        System.out.println("\n   Number system conversions:");
        BigInteger number = new BigInteger("999999");
        System.out.println("   Decimal: " + number);
        System.out.println("   Factoradic: " + JNumberTools.numberSystem().factoradic(number));
        System.out.println("   Combinadic (degree 4): " +
                JNumberTools.numberSystem().combinadic(number, 4));
        System.out.println("   Permutadic (degree 3): " +
                JNumberTools.numberSystem().permutadic(number, 3));
    }
}
