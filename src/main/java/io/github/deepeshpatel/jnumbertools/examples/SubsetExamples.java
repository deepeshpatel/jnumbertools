package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

public class SubsetExamples {

    public static void main(String[] args) {

        printAllSubsetsOfNumbersInLexOrder();
        printAllSubsetsOfElementsInLexOrder();

        printSubsetsOfNumbersInGivenRangeInLexOrder();
        printSubsetsOfElementsInGivenRangeInLexOrder();

    }
    static void printAllSubsetsOfNumbersInLexOrder() {
        System.out.println("\n*** All subsets up to size 3 in lex order ***");
        JNumberTools.subsets().of(3)
                .all().lexOrder()
                .forEach(System.out::println);
    }

    static void printAllSubsetsOfElementsInLexOrder() {
        System.out.println("\n*** All subsets of elements up to size 3 in lex order ***");
        JNumberTools.subsets().of("Red","Green","Blue")
                .all().lexOrder()
                .forEach(System.out::println);
    }

    static void printSubsetsOfNumbersInGivenRangeInLexOrder() {
        System.out.println("\n*** All subsets of size 2 to 3 of 3 items in lex order ***");
        JNumberTools.subsets().of(3)
                .inRange(2,3).lexOrder()
                .forEach(System.out::println);
    }

    static void printSubsetsOfElementsInGivenRangeInLexOrder() {
        System.out.println("\n*** All subsets of size 2 to 3 of 3 given elements in lex order ***");
        JNumberTools.subsets().of("Red","Green","Blue")
                .inRange(2,3).lexOrder()
                .forEach(System.out::println);
    }

}
