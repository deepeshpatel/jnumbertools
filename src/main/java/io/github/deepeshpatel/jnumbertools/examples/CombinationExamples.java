package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;

public class CombinationExamples {
    public static void main(String[] args) {

        System.out.println("Unique combination of given size in lex order");
        JNumberTools.combinationsOf("A","B","C","D")
                .unique(2)
                .forEach(System.out::println);

        System.out.println("Nth Unique combination of given size in lex order");
        JNumberTools.combinationsOf("A","B","C","D")
                .uniqueNth(2,2)
                .forEach(System.out::println);


        System.out.println("Nth repetitive combination of given size in lex order");
        JNumberTools.combinationsOf("A","B","C","D")
                .repetitive(2)
                .forEach(System.out::println);


        System.out.println("repetitive combination of multiset");
        JNumberTools.combinationsOf("Red", "Green", "Blue","Yellow")
                //3 red ,2 green, 1 blue, 1 yellow
                .repetitiveMultiset(3, new int[]{3,2,1,1})
                .forEach(System.out::println);


    }
}
