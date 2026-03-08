package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstrainedCartesianProductTest {

    static final List<String> pizzaBase = of("Small ", "Medium", "Large");
    static final List<String> sauce = of("Tomato Ketchup", "White Sauce", "Green Chutney");
    static final List<String> cheese = of("Ricotta", "Mozzarella", "Cheddar");
    static final List<String> toppings = of("tomato", "capsicum", "onion", "paneer", "corn");

    @Test
    void assertCount() {
        var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1, 5, toppings);
        var list = product.lexOrder().stream().toList();
        assertEquals(product.count().longValue(), list.size());
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        // Case 1: Single dimension with empty list and quantity 0 -> count=1, returns [[]]
        var singleEmptyZero = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, singleEmptyZero.count());
        var result1 = singleEmptyZero.lexOrder().stream().toList();
        assertEquals(1, result1.size());
        assertEquals(List.of(), result1.get(0));

        // Case 2: Single dimension with empty list and quantity > 0 -> count=0, returns []
        var singleEmptyPositive = cartesianProduct.constrainedProductOfDistinct(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, singleEmptyPositive.count());
        assertTrue(singleEmptyPositive.lexOrder().stream().toList().isEmpty());

        // Case 3: Multiple dimensions with any empty (quantity>0) -> count=0, returns []
        var multiWithEmpty = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                .andDistinct(2, Collections.emptyList())
                .andMultiSelect(2, sauce);
        assertEquals(BigInteger.ZERO, multiWithEmpty.count());
        assertTrue(multiWithEmpty.lexOrder().stream().toList().isEmpty());

        // Case 4: Multiple dimensions with all count=1 -> count=1, returns [[]]
        var allOnes = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList())
                .andDistinct(0, Collections.emptyList())
                .andMultiSelect(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, allOnes.count());
        var result4 = allOnes.lexOrder().stream().toList();
        assertEquals(1, result4.size());
        assertEquals(List.of(), result4.get(0));
    }

    @Test
    void shouldGenerateCorrectCombinations() {
        var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1, 5, toppings);
        var list = product.lexOrder().stream().toList();
        assertEquals(product.count().longValue(), list.size());
        assertEquals(
                of("Small ", "Ricotta", "Mozzarella", "Tomato Ketchup", "Tomato Ketchup", "tomato"),
                list.get(0)
        );
        assertEquals(
                of("Small ", "Ricotta", "Mozzarella", "Tomato Ketchup", "Green Chutney", "tomato", "onion", "paneer"),
                list.get(80)
        );
        assertEquals(
                of("Small ", "Ricotta", "Cheddar", "Green Chutney", "Green Chutney", "onion", "corn"),
                list.get(354)
        );
        assertEquals(
                of("Medium", "Ricotta", "Mozzarella", "Tomato Ketchup", "White Sauce", "capsicum", "paneer"),
                list.get(599)
        );
        assertEquals(
                of("Medium", "Ricotta", "Cheddar", "Tomato Ketchup", "White Sauce", "corn"),
                list.get(779)
        );
        assertEquals(
                of("Medium", "Mozzarella", "Cheddar", "Tomato Ketchup", "White Sauce", "tomato", "capsicum", "onion", "paneer", "corn"),
                list.get(991)
        );
        assertEquals(
                of("Large", "Mozzarella", "Cheddar", "Green Chutney", "Green Chutney", "tomato", "capsicum", "onion", "paneer", "corn"),
                list.get(list.size() - 1)
        );
    }


    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressLargeProduct() {
        var builder = cartesianProduct.simpleProductOf(List.of(0,1,2,3,4,5,6,7,8,9))
                .and(List.of(0,1,2,3,4,5,6,7,8,9))
                .and(List.of(0,1,2,3,4,5,6,7,8,9));
        long count = builder.lexOrder().stream().count();
        assertEquals(1000, count);
    }
}