package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class PermutationExamples {

    public static void main(String[] args) {

        printUniquePermutationsOfNumbersInLexOrder();
        printUniquePermutationsOfElementsInLexOrder();

        printMthUniquePermutationOfNumbersInLexOrder();
        printMthUniquePermutationOfElementsInLexOrder();

        printRepetitivePermutationOfNumbersInLexOrder();
        printRepetitivePermutationOfElementsInLexOrder();

        printMthRepetitivePermutationOfNumbersInLexOrder();
        printMthRepetitivePermutationOfElementsInLexOrder();

        printMultisetPermutationOfNumbersInLexOrder();
        printMultisetPermutationOfElementsInLexOrder();

        printMthMultisetPermutationOfNumbersInLexOrder();
        printMthMultisetPermutationOfElementsInLexOrder();

        printRankOfUniquePermutation();
        printUniquePermutationForGivenRank();

    }

    static void printUniquePermutationsOfNumbersInLexOrder() {
        System.out.println("\n*** Unique Permutations of size 3 in Lex order ***");
        JNumberTools.permutations().unique(3)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printUniquePermutationsOfElementsInLexOrder() {
        System.out.println("\n*** Unique Permutations of given 3 elements in lex order ***");
        JNumberTools.permutations().unique("Red", "Green", "Blue")
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printMthUniquePermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 10th unique permutation of size 4 in lex order ***");
        JNumberTools.permutations().unique(4)
                .lexOrderMth(10)
                .forEach(System.out::println);
    }

    static void printMthUniquePermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 10th unique permutation of given 4 elements in lex order ***");
        JNumberTools.permutations().unique("Red", "Green", "Blue","Yellow")
                .lexOrderMth(10)
                .forEach(System.out::println);
    }

    static void printRepetitivePermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Repetitive permutation of 2 items out of 3 in lex order ***");
        JNumberTools.permutations().repetitive(2,3)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printRepetitivePermutationOfElementsInLexOrder() {
        System.out.println("\n*** Repetitive permutation of 2 elements out of given 3 elements in lex order ***");
        JNumberTools.permutations().repetitive(2,"Red", "Green", "Blue")
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printMthRepetitivePermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 4th repetitive permutation of 2 items out of 3 in lex order ***");
        JNumberTools.permutations().repetitive(2, 3)
                .lexOrderMth(4)
                .forEach(System.out::println);
    }

    static void printMthRepetitivePermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 4th repetitive permutation of 2 elements out of given 3 elements in lex order ***");
        JNumberTools.permutations().repetitive(2,"Red", "Green", "Blue")
                .lexOrderMth(4)
                .forEach(System.out::println);
    }

    static void printMultisetPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        var elements = List.of(1,2,3);
        int[] frequencies = new int[] {1,2,2};
        JNumberTools.permutations().multiset(elements, frequencies)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printMultisetPermutationOfElementsInLexOrder() {
        System.out.println("\n*** Multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        System.out.println("count of Red=1, Green=2 and Blue=2");

        var elements = List.of("Red","Green","Blue");
        int[] frequencies = new int[] {1,2,2};
        JNumberTools.permutations().multiset(elements, frequencies)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printMthMultisetPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 5th multiset permutation of 3 items with count 1,2 & 2 in lex order ***");

        var elements = List.of(1,2,3);
        int[] frequencies = new int[] {1,2,2};

        JNumberTools.permutations().multiset(elements, frequencies)
                .lexOrderMth(5)
                .forEach(System.out::println);
    }

    static void printMthMultisetPermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 5th multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        System.out.println("count of Red=1, Green=2 and Blue=2");

        var elements = List.of("Red","Green","Blue");
        int[] frequencies = new int[] {1,2,2};

        JNumberTools.permutations().multiset(elements, frequencies)
                .lexOrderMth(5)
                .forEach(System.out::println);
    }

    static void printRankOfUniquePermutation() {
        System.out.println("\n*** Rank of given permutation *** ");
        int[] permutation  = new int[] {7,6,5,3,4,1,2,0};
        BigInteger rank = JNumberTools.rankOf().uniquePermutation(permutation);
        System.out.println("Rank of permutation " + Arrays.toString(permutation) + " is " + rank);
    }

    static void printUniquePermutationForGivenRank() {
        System.out.println("\n*** Permutation of size 20 for rank = five hundred quadrillion  *** ");
        int size = 20;
        BigInteger rank = new BigInteger("500000000000000000");
        int[] permutation = JNumberTools.unRankingOf().uniquePermutation(rank, size);
        System.out.printf("%s-th unique permutation of size %d is " + Arrays.toString(permutation), rank, size);
    }
}
