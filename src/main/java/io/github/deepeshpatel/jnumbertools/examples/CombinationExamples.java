package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;

public class CombinationExamples {
    public static void main(String[] args) {

        printUniqueCombinationsOfNumbers();
        printUniqueCombinationsOfElements();

        printNthUniqueCombinationOfNumbersInLexOrder();
        printNthUniqueCombinationOfElementsInLexOrder();

        printRepetitiveCombinationOfNumbersInLexOrder();
        printRepetitiveCombinationOfElementsInLexOrder();

        printRepetitiveCombinationOfMultiset();

        printRankOfUniqueCombination();
        printUniqueCombinationForGivenRank();


    }

    static void printUniqueCombinationsOfNumbers() {
        System.out.println("\n*** Unique combination of 2 items out of 4 in lex order ***");

        new JNumberTools().combinations().ofnCr(4,2)
                .unique()
                .forEach(System.out::println);
    }

    static void printUniqueCombinationsOfElements() {
        System.out.println("\n*** Unique combination of 2 elements out of given 4 elements in lex order ***");

        new JNumberTools().combinations().of(2,"Red", "Green", "Blue","Yellow")
                .unique()
                .forEach(System.out::println);
    }

    static void printNthUniqueCombinationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 3rd Unique combination of 2 items out of 4 in lex order ***");
        new JNumberTools().combinations().ofnCr(4,2)
                .uniqueNth(3)
                .forEach(System.out::println);
    }

    static void printNthUniqueCombinationOfElementsInLexOrder() {
        System.out.println("\n*** Every 3rd Unique combination of 2 elements out of given 4 elements in lex order ***");
        new JNumberTools().combinations().of(2,"Red", "Green", "Blue","Yellow")
                .uniqueNth(3)
                .forEach(System.out::println);
    }

    static void printRepetitiveCombinationOfNumbersInLexOrder() {
        System.out.println("\n*** Repetitive combination of 2 items out of 3 in lex order ***");
        new JNumberTools().combinations().ofnCr(3,2)
                .repetitive()
                .forEach(System.out::println);
    }

    static void printRepetitiveCombinationOfElementsInLexOrder() {
        System.out.println("\n*** Repetitive combination of 2 elements out of given 3 elements in lex order ***");
        new JNumberTools().combinations().of(2, "Red", "Green", "Blue")
                .repetitive()
                .forEach(System.out::println);
    }


    static void printRepetitiveCombinationOfMultiset() {
        System.out.println("\n*** Repetitive combination of 3 elements from multiset of size 4 in lex order ***");
        System.out.println("count of Red=3, Green=2, Blue=1 and Yellow=1");
        new JNumberTools().combinations().of(3,"Red", "Green", "Blue","Yellow")
                //3 red ,2 green, 1 blue, 1 yellow
                .repetitiveMultiset(new int[]{3,2,1,1})
                .forEach(System.out::println);
    }

    static void printRankOfUniqueCombination() {
        System.out.println("\n*** Rank of given combination  ***");
        int[] combination = new int[]{0,1,4,6,8,9,10,11,13,14,17};
        int n = 20;
        BigInteger rank = new JNumberTools().rankOf().uniqueCombination(20,combination);
        System.out.printf("Rank of " + Arrays.toString(combination) + ", selected from %d items is %d\n", n,rank);
    }

    static void printUniqueCombinationForGivenRank() {
        System.out.println("\n*** Combination for given rank and size ***");
        int n = 20;
        int r = 11;
        BigInteger rank = BigInteger.valueOf(41559);
        int[] combination = new JNumberTools().unRankingOf().uniqueCombination(rank,n, r);
        System.out.printf("%dth combination of %d items selected from %d items is " + Arrays.toString(combination),rank, r,n);
    }
}
