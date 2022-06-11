package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;

public class SubsetExamples {

    public static void main(String[] args) {

        printAllSubsetsOfNumbersInLexOrder();
        printAllSubsetsOfElementsInLexOrder();

        printSubsetsOfNumbersInGivenRangeInLexOrder();
        printSubsetsOfElementsInGivenRangeInLexOrder();

    }
    static void printAllSubsetsOfNumbersInLexOrder() {
        System.out.println("\n*** All subsets upto size 3 in lex order ***");
        JNumberTools.subsetsOf(3)
                .all()
                .forEach(System.out::println);
    }

    static void printAllSubsetsOfElementsInLexOrder() {
        System.out.println("\n*** All subsets of elements upto size 3 in lex order ***");
        JNumberTools.subsetsOf("Red","Green","Blue")
                .all()
                .forEach(System.out::println);
    }

    static void printSubsetsOfNumbersInGivenRangeInLexOrder() {
        System.out.println("\n*** All subsets of size 2 to 3 of 3 items in lex order ***");
        JNumberTools.subsetsOf(3)
                .inRange(2,3)
                .forEach(System.out::println);
    }

    static void printSubsetsOfElementsInGivenRangeInLexOrder() {
        System.out.println("\n*** All subsets of size 2 to 3 of 3 given elements in lex order ***");
        JNumberTools.subsetsOf("Red","Green","Blue")
                .inRange(2,3)
                .forEach(System.out::println);
    }

}
