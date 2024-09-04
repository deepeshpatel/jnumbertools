package io.github.deepeshpatel.jnumbertools.generator.product;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;

public class ComplexCartesianProductTest {

    @Test
    public void shouldGenerateCorrectNumOfCombinations() {

        var pizzaBase = List.of("Small ","Medium","Large");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta","Mozzarella","Cheddar");
        var toppings = List.of("tomato","capsicum","onion","paneer","corn");

       var product =  cartesianProduct.complexProductOf(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1,5,toppings).lexOrder();

        var list = product.stream().toList();

        Assert.assertEquals(product.count(), list.size());

        String row_0 = "[Small , Ricotta, Mozzarella, Tomato Ketchup, Tomato Ketchup, tomato]";
        String row_80 = "[Small , Ricotta, Mozzarella, Tomato Ketchup, Green Chutney, tomato, onion, paneer]";
        String row_354 = "[Small , Ricotta, Cheddar, Green Chutney, Green Chutney, onion, corn]";
        String row_599 = "[Medium, Ricotta, Mozzarella, Tomato Ketchup, White Sauce, capsicum, paneer]";
        String row_779 = "[Medium, Ricotta, Cheddar, Tomato Ketchup, White Sauce, corn]";
        String row_991 = "[Medium, Mozzarella, Cheddar, Tomato Ketchup, White Sauce, tomato, capsicum, onion, paneer, corn]";
        String lastRow = "[Large, Mozzarella, Cheddar, Green Chutney, Green Chutney, tomato, capsicum, onion, paneer, corn]";

        Assert.assertEquals(row_0, list.get(0).toString());
        Assert.assertEquals(row_80, list.get(80).toString());
        Assert.assertEquals(row_354, list.get(354).toString());
        Assert.assertEquals(row_599, list.get(599).toString());
        Assert.assertEquals(row_779, list.get(779).toString());
        Assert.assertEquals(row_991, list.get(991).toString());
        Assert.assertEquals(lastRow, list.get(list.size()-1).toString());
    }

}