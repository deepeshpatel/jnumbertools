package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;

class KPermutationExamples {

    public static void main(String[] args) {

        printKPermutationOfNumbersInLexOrder();
        printKPermutationOfElementsInLexOrder();

        printNthKPermutationOfNumbersInLexOrder();
        printNthKPermutationOfElementsInLexOrder();
        printNthKPermutationsOfVeryLargeListInLexOrder();

        printKPermutationOfNumbersInCombinationOrder();
        printKPermutationOfElementsInCombinationOrder();

        printNthKPermutationOfNumbersInCombinationOrder();
        printNthKPermutationOfElementsInCombinationOrder();

        printRankOfKPermutation();
        printKPermutationForGivenRank();
    }

    static void printKPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** 2-Permutation(selected 2 at a time) of 3 items in lex order ***");
        JNumberTools.permutationsOf(3)
                .k(2)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printKPermutationOfElementsInLexOrder() {
        System.out.println("\n*** 2-Permutation(selected 2 at a time) of 3 given elements in lex order ***");
        JNumberTools.permutationsOf("Red", "Green", "Blue")
                .k(2)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printNthKPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 3rd 2-Permutation of 3 items in lex order ***");
        JNumberTools.permutationsOf(3)
                .k(2)
                .lexOrderNth(3)
                .forEach(System.out::println);
    }

    static void printNthKPermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 3rd 2-Permutation of 3 given elements in lex order ***");
        JNumberTools.permutationsOf("Red", "Green", "Blue")
                .k(2)
                .lexOrderNth(3)
                .forEach(System.out::println);
    }

    static void printNthKPermutationsOfVeryLargeListInLexOrder() {

        System.out.println("\n*** Print every one hundred octillion-th(10^29 th) 20-Permutation of 40 elements(20P40) *** ");

        JNumberTools.permutationsOf(40)
                .k(20)
                .lexOrderNth(new BigInteger("100000000000000000000000000000"))
                .forEach(System.out::println);
    }

    static void printKPermutationOfNumbersInCombinationOrder() {

        System.out.println("\n***  Print 2-permutation of 4 items in combination order  ***");
        JNumberTools.permutationsOf(4)
                .k(2)
                .combinationOrder()
                .forEach(System.out::println);
    }

    static void printKPermutationOfElementsInCombinationOrder() {

        System.out.println("\n***  Print 2-permutation of 4 given elements in combination order  ***");
        JNumberTools.permutationsOf("Red", "Green", "Blue","Yellow")
                .k(2)
                .combinationOrder()
                .forEach(System.out::println);
    }

    static void printNthKPermutationOfNumbersInCombinationOrder() {

        System.out.println("\n***  Print every 3rd 2-permutation of 4 items in combination order  ***");
        JNumberTools.permutationsOf(4)
                .k(2)
                .combinationOrderNth(3)
                .forEach(System.out::println);
    }

    static void printNthKPermutationOfElementsInCombinationOrder() {

        System.out.println("\n***  Print every 3rd 2-permutation of 4 items in combination order  ***");
        JNumberTools.permutationsOf("Red", "Green", "Blue","Yellow")
                .k(2)
                .combinationOrderNth(3)
                .forEach(System.out::println);
    }

    static void printRankOfKPermutation() {
        System.out.println("\n*** Print the rank of very large 20-Permutation of 40 items *** ");

        int[] permutation = new int[] {23, 34, 12, 11, 10, 28, 1, 8, 18, 3, 9, 30, 24, 0, 6, 20, 26, 27, 37, 35};

        BigInteger rank = JNumberTools
                .rankOf()
                .kPermutation(40, permutation);
        System.out.println("Rank of 20-Choose-40 is " + rank);
    }

    static void printKPermutationForGivenRank() {
        System.out.println("\n*** Print two hundred octillion-th 20-permutation of 40 items *** ");
        int[] permutation = JNumberTools
                .unRankingOf()
                .kPermutation(new BigInteger("200000000000000000000000000000"),40,20);

        System.out.println(Arrays.toString(permutation));
    }
}
