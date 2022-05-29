package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;

public class KPermutationExamples {

    public static void main(String[] args) {
        printListOfKPermutations();
        printLargeListOfKPermutations();
        printTheRankOfKPermutation();
        printTheNthKPermutation();
    }

    public static void printListOfKPermutations() {

        System.out.println("\n***  Print every 3rd 2-permutation of 4 elements ***");
        JNumberTools.permutationsOf("A","B","C","D")
                .k(2)
                .lexOrderNth(3)
                .stream()
                .forEach(System.out::println);
    }

    public static void printLargeListOfKPermutations() {

        System.out.println("\n*** Print every 10^29th 20-Permutation of 40 elements(20P40) *** ");

        JNumberTools.permutationsOfSize(40)
                .k(20)
                .lexOrderNth(new BigInteger("100000000000000000000000000000"))
                .stream().forEach(System.out::println);
    }

    public static void printTheRankOfKPermutation() {
        System.out.println("\n*** Print the rank of very large K-Permutation *** ");

        int[] permutation = new int[] {23, 34, 12, 11, 10, 28, 1, 8, 18, 3, 9, 30, 24, 0, 6, 20, 26, 27, 37, 35};

        BigInteger rank = JNumberTools
                .rankOf()
                .kPermutation(40, permutation);
        System.out.println("Rank of 20-Choose-40 is " + rank);
    }

    public static void printTheNthKPermutation () {
        System.out.println("\n*** Print the k-permutation for given rank *** ");
        int[] permutation = JNumberTools
                .unRankingOf()
                .kPermutation(new BigInteger("200000000000000000000000000000"),40,20);

        System.out.println(Arrays.toString(permutation));
    }
}
