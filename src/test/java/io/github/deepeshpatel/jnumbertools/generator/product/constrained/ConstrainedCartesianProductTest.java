package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstrainedCartesianProductTest {

    private final List<String> pizzaBase = of("Small ", "Medium", "Large");
    private final List<String> sauce = of("Tomato Ketchup", "White Sauce", "Green Chutney");
    private final List<String> cheese = of("Ricotta", "Mozzarella", "Cheddar");
    private final List<String> toppings = of("tomato", "capsicum", "onion", "paneer", "corn");

    @Test
    void assertCount() {
        var product = cartesianProduct.constrainedProductOf(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(1, 5, toppings);
        var list = product.lexOrder().stream().toList();
        assertEquals(product.count().longValue(), list.size());
    }

    @Test
    void shouldThrowExceptionWhenFromGreaterThanTo() {
        assertThrows(IllegalArgumentException.class, () ->
                cartesianProduct.constrainedProductOf(1, pizzaBase)
                        .andInRange(5, 3, toppings)  // from > to
                        .lexOrder()
        );
    }

    @Test
    void shouldHandleToExceedingElements() {
        // When 'to' exceeds available elements, it should cap at elements.size()
        var builder = cartesianProduct.constrainedProductOf(1, pizzaBase)
                .andInRange(1, 10, toppings);

        // Calculate expected: pizzaBase(3) × subsets of size 1-5 from toppings
        BigInteger expectedToppingsCount = BigInteger.ZERO;
        for (int i = 1; i <= 5; i++) {
            expectedToppingsCount = expectedToppingsCount.add(calculator.nCr(5, i));
        }
        BigInteger expectedTotal = BigInteger.valueOf(3).multiply(expectedToppingsCount);

        assertEquals(expectedTotal, builder.count());

        var result = builder.lexOrder().stream().toList();
        assertEquals(expectedTotal.intValue(), result.size());

        // Verify structure: each tuple is flat
        for (var tuple : result) {
            // First element: pizza base (String)
            assertTrue(pizzaBase.contains(tuple.get(0)));

            // Remaining elements: toppings (variable number, 1-5)
            int toppingsCount = tuple.size() - 1;
            assertTrue(toppingsCount >= 1 && toppingsCount <= 5);

            // Verify all toppings are from the original list
            for (int i = 1; i < tuple.size(); i++) {
                assertTrue(toppings.contains(tuple.get(i)));
            }
        }
    }


    @Test
    void shouldHandleNullElementsInAndDistinct() {
        var builder = cartesianProduct.constrainedProductOf(1, pizzaBase)
                .andDistinct(2, (List<?>) null);

        assertEquals(BigInteger.ZERO, builder.count());
        assertTrue(builder.lexOrder().stream().toList().isEmpty());
    }

    @Test
    void shouldHandleNullElementsInAndMultiSelect() {
        var builder = cartesianProduct.constrainedProductOf(1, pizzaBase)
                .andMultiSelect(2, (List<?>) null);

        assertEquals(BigInteger.ZERO, builder.count());
        assertTrue(builder.lexOrder().stream().toList().isEmpty());
    }

    @Test
    void shouldHandleNullElementsInAndInRange() {
        // According to the pattern with SimpleProductBuilder:
        // - null should be treated as empty set
        // - Empty set with range [1,3] has no valid subsets (since min size 1 > 0)
        // - Therefore this dimension contributes 0 combinations
        // - Product with any zero dimension is empty

        var builder = cartesianProduct.constrainedProductOf(1, pizzaBase)
                .andInRange(1, 3, (List<?>) null);
        assertEquals(BigInteger.ZERO, builder.count());
        assertTrue(builder.lexOrder().stream().toList().isEmpty());
    }

    @Test
    void shouldGenerateCorrectCombinations() {
        var product = cartesianProduct.constrainedProductOf(1, pizzaBase)
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

    @Test
    void constrained_withInvalidRange_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                cartesianProduct.constrainedProductOf(1, pizzaBase)
                        .andInRange(5, 3, toppings)
                        .lexOrder());
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