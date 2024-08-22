package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;

class KPermutationExamples {

    public static void main(String[] args) {

        printKPermutationOfNumbersInLexOrder();
        printKPermutationOfElementsInLexOrder();

        printMthKPermutationOfNumbersInLexOrder();
        printMthKPermutationOfElementsInLexOrder();
        printMthKPermutationsOfVeryLargeListInLexOrder();

        printKPermutationOfNumbersInCombinationOrder();
        printKPermutationOfElementsInCombinationOrder();

        printMthKPermutationOfNumbersInCombinationOrder();
        printMthKPermutationOfElementsInCombinationOrder();

        printRankOfKPermutation();
        printKPermutationForGivenRank();
    }

    static void printKPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** 2-Permutation(selected 2 at a time) out of 3 items in lex order ***");
        JNumberTools.permutations().nPr(3,2)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printKPermutationOfElementsInLexOrder() {
        System.out.println("\n*** 2-Permutation(selected 2 at a time) out of 3 given elements in lex order ***");
        JNumberTools.permutations().nPr(2, "Red", "Green", "Blue")
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printMthKPermutationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 3rd 2-Permutation of 3 items in lex order ***");
        JNumberTools.permutations().nPr(3,2)
                .lexOrderMth(3)
                .forEach(System.out::println);
    }

    static void printMthKPermutationOfElementsInLexOrder() {
        System.out.println("\n*** Every 3rd 2-Permutation of 3 given elements in lex order ***");
        JNumberTools.permutations().nPr(2, "Red", "Green", "Blue")
                .lexOrderMth(3)
                .forEach(System.out::println);
    }

    static void printMthKPermutationsOfVeryLargeListInLexOrder() {

        System.out.println("\n*** Print every one hundred octillion-th(10^29 th) 20-Permutation of 40 elements(20P40) *** ");

        JNumberTools.permutations().nPr(40,20)
                .lexOrderMth(new BigInteger("100000000000000000000000000000"))
                .forEach(System.out::println);
    }

    static void printKPermutationOfNumbersInCombinationOrder() {

        System.out.println("\n***  Print 2-permutation of 4 items in combination order  ***");
        JNumberTools.permutations().nPr(4,2 )
                .combinationOrder()
                .forEach(System.out::println);
    }

    static void printKPermutationOfElementsInCombinationOrder() {

        System.out.println("\n***  Print 2-permutation of 4 given elements in combination order  ***");
        JNumberTools.permutations().nPr(2,"Red", "Green", "Blue","Yellow")
                .combinationOrder()
                .forEach(System.out::println);
    }

    static void printMthKPermutationOfNumbersInCombinationOrder() {

        System.out.println("\n***  Print every 3rd 2-permutation of 4 items in combination order  ***");
        JNumberTools.permutations().nPr(4,2)
                .combinationOrderMth(3)
                .forEach(System.out::println);
    }

    static void printMthKPermutationOfElementsInCombinationOrder() {

        System.out.println("\n***  Print every 3rd 2-permutation of 4 items in combination order  ***");
        JNumberTools.permutations().nPr(2,"Red", "Green", "Blue","Yellow")
                .combinationOrderMth(3)
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
