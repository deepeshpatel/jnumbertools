/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.api.examples.fordevs.basics;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;

public class CombinationProductExample {

    public static void main(String[] args) {
        constrainedCombination();
        constrainedCombinationMth();
    }

    public static void constrainedCombinationMth() {

        String heading = """
                Print 1000th combination of -
                any 1 pizza base  out of 3 and
                any 2 distinct cheese out of 3 and
                any 2 sauce (repeated allowed) out of 3 and
                any toppings in range 1 to 5
                """;

        System.out.println(heading);

        var pizzaBase = List.of("Small ","Medium", "Large");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta ","Mozzarella","Cheddar");
        var toppings = List.of(1,2,3,4);

        var mthList = JNumberTools
                .cartesianProduct().constrainedProductOfDistinct(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1,5,toppings)
                .lexOrderMth(1000, 1000).stream().limit(1);

        System.out.println(mthList);
    }

    private static void constrainedCombination() {
        String heading = """
                Print all combinations of -
                any 1 pizza base  out of 3 and
                any 2 distinct cheese out of 3 and
                any 2 sauce (repeated allowed) out of 3 and
                any toppings in range 1 to 5
                """;

        System.out.println(heading);

        var pizzaBase = List.of("Small ","Medium", "Large");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta ","Mozzarella","Cheddar");
        var toppings = List.of("tomato","capsicum","onion","paneer","corn");

        var iterable = JNumberTools
                .cartesianProduct().constrainedProductOfDistinct(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1,5,toppings)
                .lexOrder();

        int i=1;
        for(var e: iterable) {
            System.out.println(i++ + " " + e);
        }
    }
}
