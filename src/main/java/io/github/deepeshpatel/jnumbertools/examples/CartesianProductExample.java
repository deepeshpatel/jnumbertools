package io.github.deepeshpatel.jnumbertools.examples;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;

public class CartesianProductExample {
    public static void main(String[] args) {

        String heading = """
                Print all combinations of -
                any 1 pizza base and any 1 cheese and any 1 sauce and any 1 topping
                """;

        System.out.println(heading);

        var pizzaBase = List.of("Small ","Medium");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta ","Mozzarella","Cheddar");
        var toppings = List.of("Tomato","Capsicum");

        var iterable = JNumberTools
                .cartesianProduct().simpleProductOf(pizzaBase)
                .and(cheese)
                .and(sauce)
                .and(toppings)
                .lexOrder();

        int i=1;
        for(var e: iterable) {
            System.out.println(i++ + " " + e);
        }
    }
}
