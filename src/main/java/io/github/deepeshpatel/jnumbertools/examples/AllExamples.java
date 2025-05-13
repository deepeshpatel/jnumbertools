package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

public class AllExamples {
    public static void main(String[] args) {


        //TODO:JNumberTools.permutations().unique("A","B","C").singleSwap().forEach(System.out::println);
        //TODO:JNumberTools.permutations().unique("A","B","C").parallel().forEach(System.out::println);

        LinkedHashMap<String, Integer> options;
        //all unique permutations in lex order
        JNumberTools.permutations().unique("A","B","C").lexOrder().stream().toList();

        //every mth unique permutation
        JNumberTools.permutations().unique("A","B","C").lexOrderMth(3,0).stream().toList();

        //all repetitive permutations
        JNumberTools.permutations().repetitive(3,"A","B","C").lexOrder().stream().toList();

        //every mth repetitive permutation
        JNumberTools.permutations().repetitive(3,"A","B","C").lexOrderMth(3, 0).stream().toList();

        //all k-permutation in lex order
        JNumberTools.permutations().nPk(2,"A","B","C").lexOrder().stream().toList();

        //every mth k-permutation in lex order
        JNumberTools.permutations().nPk(2,"A","B","C").lexOrderMth(2, 0).stream().toList();

        //all k-permutation in combination order
        JNumberTools.permutations().nPk(2,"A","B","C").combinationOrder().stream().toList();

        //every mth k-permutation in lex order
        JNumberTools.permutations().nPk(2,"A","B","C").combinationOrderMth(3, 0).stream().toList();

        //all multiset permutations in lex order
        options = new LinkedHashMap<>(Map.of("A", 1, "B", 2, "C", 1));
        JNumberTools.permutations().multiset(options).lexOrder().stream().toList();

        //every mth multiset permutation
        JNumberTools.permutations().multiset(options).lexOrderMth(3, 0).stream().toList();

        //all unique combination in lex order
        JNumberTools.combinations().unique(2,"A","B","C").lexOrder().stream().toList();

        //every mth unique combination
        JNumberTools.combinations().unique(2,"A","B","C").lexOrderMth(3,0).stream().toList();

        //all repetitive combinations in lex order
        JNumberTools.combinations().repetitive(2,"A","B","C").lexOrder().stream().toList();

        //every mth repetitive combinations in lex order //TODO: work in progress
        JNumberTools.combinations().repetitive(2,"A","B","C").lexOrderMth(2, 0).stream().toList();

        //all multiset combination in lex order
        options = new LinkedHashMap<>(Map.of("A", 1, "B", 2, "C", 1));
        JNumberTools.combinations().multiset(options, 2).lexOrder().stream().toList();

        //every mth repetitive combinations in lex order
        options = new LinkedHashMap<>(Map.of("A", 1, "B", 2, "C", 1));
        JNumberTools.combinations().multiset(options, 2).lexOrderMth(5, 0).forEach(System.out::println);
        //all 2^n subsets of elements in lex order
        JNumberTools.subsets().of("A","B","C").all().lexOrder().stream().toList();

        //every mth subset of elements
        JNumberTools.subsets().of("A","B","C").all().lexOrderMth(5,0).stream().toList();

        //all subsets of elements in a given range in lex order
        JNumberTools.subsets().of("A","B","C","D").inRange(2,4).lexOrder().stream().toList();

        //every mth subset of elements in a given range
        JNumberTools.subsets().of("A","B","C","D").inRange(2,4).lexOrderMth(5,0).stream().toList();

        //all values of simpleProductOf cartesian cartesianProduct in lex order
        JNumberTools.cartesianProduct().simpleProductOf(List.of("Small ","Medium","Large"))
                .and(List.of( "Ricotta","Mozzarella","Cheddar"))
                .and(List.of( "Ricotta","Mozzarella","Cheddar"))
                .lexOrder().stream().toList();

        //every mth values of cartesian cartesianProduct in lex order
        JNumberTools.cartesianProduct().simpleProductOf(List.of("Small ","Medium","Large"))
                .and(List.of( "Ricotta","Mozzarella","Cheddar"))
                .and(List.of( "Ricotta","Mozzarella","Cheddar"))
                .lexOrderMth(1500, 0).stream().toList();

        //all values of constrainedProductOf cartesian cartesianProduct in lex order
        JNumberTools.cartesianProduct().constrainedProductOf(1, List.of("Small ","Medium","Large"))
                .andDistinct(2, List.of( "Ricotta","Mozzarella","Cheddar"))
                .andMultiSelect(2, List.of( "Tomato Ketchup","White Sauce","Green Chutney"))
                .andInRange(2,3,List.of("tomato","capsicum","onion","paneer","corn"))
                .lexOrder().stream().toList();

        //every mth values of constrainedProductOf cartesian cartesianProduct in lex order
        JNumberTools.cartesianProduct().constrainedProductOf(1, List.of("Small ","Medium","Large"))
                .andDistinct(2, List.of( "Ricotta","Mozzarella","Cheddar"))
                .andMultiSelect(2, List.of( "Tomato Ketchup","White Sauce","Green Chutney"))
                .andInRange(2,3,List.of("tomato","capsicum","onion","paneer","corn"))
                .lexOrderMth(1500, 0).stream().toList();


        //rank of unique permutation
        out.println(JNumberTools.rankOf().uniquePermutation(1,0,2).longValue());

        //rank of repeated permutation
        out.println(JNumberTools.rankOf().repeatedPermutation(3,1,2,1).longValue());

        //rank of k-permutation
        out.println(JNumberTools.rankOf().kPermutation(8, 4,6,2,0).longValue());

        //rank of unique combination
        out.println(JNumberTools.rankOf().uniqueCombination(8, 1,2,3,4));

        //un-ranking: same as finding mth. Remove it
        out.println(Arrays.toString(JNumberTools.unrankOf().uniquePermutation(BigInteger.valueOf(23), 4)));
        out.println(Arrays.toString(JNumberTools.unrankOf().kPermutation(BigInteger.valueOf(1000), 8, 4)));
        out.println(Arrays.toString(JNumberTools.unrankOf().uniqueCombination(BigInteger.valueOf(35), 8, 4)));
        //JNumberTools.unrankOf().repeatedPermutation(3,1,2,1).longValue();
    }
}
