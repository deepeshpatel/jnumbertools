package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;

public class CombinationExamples {
    public static void main(String[] args) {

        //Generate every unique combination of size 2 in lex order
        System.out.println("Unique combination of given size in lex order");
        JNumberTools.combinationsOf("A","B","C","D")
                .unique(2)
                .forEach(System.out::println);

        //Generate every 2nd unique combination of size 2 in lex order
        System.out.println("Nth Unique combination of given size in lex order");
        JNumberTools.combinationsOf("A","B","C","D")
                .uniqueNth(2,2)
                .forEach(System.out::println);


        //Generate repetitive combination of size 2 in lex order
        System.out.println("Nth repetitive combination of given size in lex order");
        JNumberTools.combinationsOf("A","B","C","D")
                .repetitive(2)
                .forEach(System.out::println);


        //Generate repetitive combination with limited supply
        System.out.println("repetitive combination with limited supply");
        JNumberTools.combinationsOf("Red", "Green", "Blue","Yellow")
                //3 red ,2 green, 1 blue, 1 yellow
                .repetitiveWithSupply(3, new int[]{3,2,1,1})
                .forEach(System.out::println);


    }
}
