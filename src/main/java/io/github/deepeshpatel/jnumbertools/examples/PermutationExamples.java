package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;

public class PermutationExamples {

    public static void main(String[] args) {

        printUniquePermutationsOfNumbersInLexOrder();
        printUniquePermutationsOfElementsInLexOrder();

        printNthUniquePermutationOfNumbersInLexOrder();
        printNthUniquePermutationOfElementsInLexOrder();

        printRepetitivePermutationOfNumbersInLexOrder();
        printRepetitivePermutationOfElementsInLexOrder();

        printNthRepetitivePermutationOfNumbersInLexOrder();
        printNthRepetitivePermutationOfElementsInLexOrder();

        printMultisetPermutationOfNumbersInLexOrder();
        printMultisetPermutationOfElementsInLexOrder();

        printNthMultisetPermutationOfNumbersInLexOrder();
        printNthMultisetPermutationOfElementsInLexOrder();

        printRankOfUniquePermutation();
        printUniquePermutationForGivenRank();

    }

    static void printUniquePermutationsOfNumbersInLexOrder() {
        System.out.println("\n*** Unique Permutations of size 3 in Lex order ***");
        new JNumberTools().permutations().of(3)
                .unique()
                .forEach(System.out::println);
    }

    static void printUniquePermutationsOfElementsInLexOrder() {
        System.out.println("\n*** Unique Permutations of given 3 elements in lex order ***");
        new JNumberTools().permutations().of("Red", "Green", "Blue")
                .unique()
                .forEach(System.out::println);
    }

    static void printNthUniquePermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 10th unique permutation of size 4 in lex order ***");
        new JNumberTools().permutations().of(4)
                .uniqueNth(10)
                .forEach(System.out::println);
    }

    static void printNthUniquePermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 10th unique permutation of given 4 elements in lex order ***");
        new JNumberTools().permutations().of("Red", "Green", "Blue","Yellow")
                .uniqueNth(10)
                .forEach(System.out::println);
    }

    static void printRepetitivePermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Repetitive permutation of 2 items out of 3 in lex order ***");
        new JNumberTools().permutations().of(3)
                .repetitive(2)
                .forEach(System.out::println);
    }

    static void printRepetitivePermutationOfElementsInLexOrder() {
        System.out.println("\n*** Repetitive permutation of 2 elements out of given 3 elements in lex order ***");
        new JNumberTools().permutations().of("Red", "Green", "Blue")
                .repetitive(2)
                .forEach(System.out::println);
    }

    static void printNthRepetitivePermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 4th repetitive permutation of 2 items out of 3 in lex order ***");
        new JNumberTools().permutations().of(3)
                .repetitiveNth(2,4)
                .forEach(System.out::println);
    }

    static void printNthRepetitivePermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 4th repetitive permutation of 2 elements out of given 3 elements in lex order ***");
        new JNumberTools().permutations().of("Red", "Green", "Blue")
                .repetitiveNth(2,4)
                .forEach(System.out::println);
    }

    static void printMultisetPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        new JNumberTools().permutations().of(3)
                .multiset(1,2,2)
                .forEach(System.out::println);
    }

    static void printMultisetPermutationOfElementsInLexOrder() {
        System.out.println("\n*** Multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        System.out.println("count of Red=1, Green=2 and Blue=2");
        new JNumberTools().permutations().of("Red","Green","Blue")
                .multiset(1,2,2)
                .forEach(System.out::println);
    }

    static void printNthMultisetPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 5th multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        new JNumberTools().permutations().of(3)
                .multisetNth(5,1,2,2)
                .forEach(System.out::println);
    }

    static void printNthMultisetPermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 5th multiset permutation of 3 items with count 1,2 & 2 in lex order ***");
        System.out.println("count of Red=1, Green=2 and Blue=2");
        new JNumberTools().permutations().of("Red","Green","Blue")
                .multisetNth(5,1,2,2)
                .forEach(System.out::println);
    }

    static void printRankOfUniquePermutation() {
        System.out.println("\n*** Rank of given permutation *** ");
        int[] permutation  = new int[] {7,6,5,3,4,1,2,0};
        BigInteger rank = new JNumberTools().rankOf().uniquePermutation(permutation);
        System.out.println("Rank of permutation " + Arrays.toString(permutation) + " is " + rank);
    }

    static void printUniquePermutationForGivenRank() {
        System.out.println("\n*** Permutation of size 20 for rank = five hundred quadrillion  *** ");
        int size = 20;
        BigInteger rank = new BigInteger("500000000000000000");
        int[] permutation = new JNumberTools().unRankingOf().uniquePermutation(rank, size);
        System.out.printf("%s-th unique permutation of size %d is " + Arrays.toString(permutation), rank, size);
    }
}
