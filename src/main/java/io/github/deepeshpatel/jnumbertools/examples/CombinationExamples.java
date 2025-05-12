package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class CombinationExamples {
    public static void main(String[] args) {

        printUniqueCombinationsOfNumbers();
        printUniqueCombinationsOfElements();

        printMthUniqueCombinationOfNumbersInLexOrder();
        printMthUniqueCombinationOfElementsInLexOrder();

        printRepetitiveCombinationOfNumbersInLexOrder();
        printRepetitiveCombinationOfElementsInLexOrder();

        printCombinationOfMultiset();

        printRankOfUniqueCombination();

        printUniqueCombinationForGivenRank();

    }

    static void printUniqueCombinationsOfNumbers() {
        System.out.println("\n*** Unique combination of 2 items out of 4 in lex order ***");

        JNumberTools.combinations().unique(4,2)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printUniqueCombinationsOfElements() {
        System.out.println("\n*** Unique combination of 2 elements out of given 4 elements in lex order ***");

        JNumberTools.combinations().unique(2,"Red", "Green", "Blue","Yellow")
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printMthUniqueCombinationOfNumbersInLexOrder() {
        System.out.println("\n*** Every 3rd Unique combination of 2 items out of 4 in lex order ***");
        JNumberTools.combinations().unique(4,2)
                .lexOrderMth(3, 0)
                .forEach(System.out::println);
    }

    static void printMthUniqueCombinationOfElementsInLexOrder() {
        System.out.println("\n*** Every 3rd Unique combination of 2 elements out of given 4 elements in lex order ***");
        JNumberTools.combinations().unique(2,"Red", "Green", "Blue","Yellow")
                .lexOrderMth(3, 0)
                .forEach(System.out::println);
    }

    static void printRepetitiveCombinationOfNumbersInLexOrder() {
        System.out.println("\n*** Repetitive combination of 2 items out of 3 in lex order ***");
        JNumberTools.combinations().repetitive(3,2)
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printRepetitiveCombinationOfElementsInLexOrder() {
        System.out.println("\n*** Repetitive combination of 2 elements out of given 3 elements in lex order ***");
        JNumberTools.combinations().repetitive(2, "Red", "Green", "Blue")
                .lexOrder()
                .forEach(System.out::println);
    }


    static void printCombinationOfMultiset() {
        System.out.println("\n*** Multiset combination of 3 colors out of  ***");
        System.out.println("3 Red, 2 Green,  1 Blue,and 1 Yellow");

        //var options = Map.of("Red", 3, "Green", 2, "Blue", 1, "Yellow", 1);
        var options = new LinkedHashMap<String, Integer>();
        options.put("Red", 3);
        options.put("Green", 2);
        options.put("Blue", 1);
        options.put("Yellow", 1);

        JNumberTools.combinations().multiset(options, 3)
                //3 red ,2 green, 1 blue, 1 yellow
                .lexOrder()
                .forEach(System.out::println);
    }

    static void printRankOfUniqueCombination() {
        System.out.println("\n*** Rank of given combination  ***");
        int[] combination = {0,1,4,6,8,9,10,11,13,14,17};
        int n = 20;
        BigInteger rank = JNumberTools.rankOf().uniqueCombination(20,combination);
        System.out.printf("Rank of " + Arrays.toString(combination) + ", selected from %d items is %d\n", n,rank);
    }

    static void printUniqueCombinationForGivenRank() {
        System.out.println("\n*** Combination for given rank and size ***");
        int n = 20;
        int r = 11;
        BigInteger rank = BigInteger.valueOf(41559);
        int[] combination = JNumberTools.unrankOf().uniqueCombination(rank,n, r);
        System.out.printf("%dth combination of %d items selected from %d items is " + Arrays.toString(combination),rank, r,n);
    }
}
