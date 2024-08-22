package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;

public class ProductTest {

    @Test
    public void shouldGenerateCorrectNumOfCombinations() {

        var pizzaBase = List.of("Small ","Medium","Large");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta","Mozzarella","Cheddar");
        var toppings = List.of("tomato","capsicum","onion","paneer","corn");

        int numPizza = 1;
        int numCheese = 2;
        int numSauce = 2;
        int numMimToppings = 1;
        int numMaxToppings = 5;

        int nCrPizza = calculator.nCr(pizzaBase.size(), numPizza).intValue();
        int nCrCheese = calculator.nCr(cheese.size(), numCheese).intValue();
        int nCrSauce = calculator.nCr(sauce.size()+numSauce-1, numSauce).intValue();

        int nCrToppings = 0;
        for(int i=numMimToppings ; i<=numMaxToppings; i++){
            nCrToppings += calculator.nCr(toppings.size(),i).intValue() ;
        }

        var list = JNumberTools
                .product().of(numPizza, pizzaBase)
                .andDistinct(numCheese, cheese)
                .andMultiSelect(numSauce, sauce)
                .andInRange(numMimToppings,numMaxToppings,toppings)
                .build();

        int expected = nCrPizza * nCrSauce * nCrCheese * nCrToppings;
        Assert.assertEquals(expected, list.size());

        String lastRow= "[Large, Mozzarella, Cheddar, Green Chutney, Green Chutney, tomato, capsicum, onion, paneer, corn]";
        Assert.assertEquals(lastRow, list.get(list.size()-1).toString());
    }

}