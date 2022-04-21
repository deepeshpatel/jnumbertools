package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;

public class PermutationExamples {

    public static void main(String[] args) {

        //Generate every unique-permutations in lex order
        System.out.println("Unique permutation in lex order");
        JNumberTools.permutationsOf("A","B","C")
                .unique()
                .forEach(System.out::println);

        //Generate every 10th unique-permutations in lex order
        System.out.println("Nth unique permutation in lex order");
        JNumberTools.permutationsOf("A","B","C","D")
                .uniqueNth(10)
                .forEach(System.out::println);

        //Generate every K-Permutation in lex order
        System.out.println("K-Permutation in lex order");
        JNumberTools.permutationsOf("A","B","C")
                .k(2)
                .lexOrder()
                .forEach(System.out::println);

        //Generate every 3rd K-Permutation in lex order
        System.out.println("Nth K-Permutation lex order");
        JNumberTools.permutationsOf("A","B","C")
                .k(2)
                .lexOrderNth(3)
                .forEach(System.out::println);

        //Generate K-Permutations in combination order
        System.out.println("K-Permutation in combination order");
        JNumberTools.permutationsOf("A","B","C")
                .k(2)
                .combinationOrder()
                .forEach(System.out::println);

        //Generate every 3rd K-Permutations in combination order
        System.out.println("Nth K-Permutation in combination order");
        JNumberTools.permutationsOf("A","B","C")
                .k(2)
                .combinationOrderNth(3)
                .forEach(System.out::println);

        //Generate repetitive permutations of size 2 in lex order
        System.out.println("Repetitive permutation of given size in lex order");
        JNumberTools.permutationsOf("A","B","C")
                .repetitive(2)
                .forEach(System.out::println);

        //Generate every 3rd repetitive permutations of size 2 in lex order
        System.out.println("Nth Repetitive permutation of given size in lex order");
        JNumberTools.permutationsOf ("A","B","C")
                .repetitiveNth(2,3)
                .forEach(System.out::println);


        //Generate repetitive permutations with limited supply in lex order
        System.out.println("Repetitive permutation with limited supply in lex order");
        JNumberTools.permutationsOf ("A","B","C")
                .repetitiveWithSupply(1,2,2)
                .forEach(System.out::println);


        //Generate all subsets in lex order
        System.out.println("all subsets in lex order");
        JNumberTools.subsetsOf("A","B","C")
                .all()
                .forEach(System.out::println);

        //Generate all subsets in a given range in lex order
        System.out.println("subsets in given range in lex order");
        JNumberTools.subsetsOf("A","B","C")
                .inRange(2,3)
                .forEach(System.out::println);
    }
}
