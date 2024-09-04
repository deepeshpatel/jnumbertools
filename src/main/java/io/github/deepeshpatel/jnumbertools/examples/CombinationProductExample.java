package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.util.List;

public class CombinationProductExample {

    public static void main(String[] args) {

        String heading = """
                Print all combinations of -
                any 1 pizza base  out of 2 and
                any 2 distinct cheese out of 3 and
                any 2 sauce (repeated allowed) out of 3 and
                any toppings in range 1 to 2
                """;

        System.out.println(heading);

        var pizzaBase = List.of("Small ","Medium");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta ","Mozzarella","Cheddar");
        var toppings = List.of("Tomato","Capsicum");

        var iterable = JNumberTools
                .cartesianProduct().complexProductOf(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1,2,toppings)
                .lexOrder();

        int i=1;
        for(var e: iterable) {
            System.out.println(i++ + " " + e);
        }
    }
}