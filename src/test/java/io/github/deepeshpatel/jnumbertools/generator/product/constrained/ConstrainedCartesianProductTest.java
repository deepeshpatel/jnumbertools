/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the constrained Cartesian product builder:
 * <pre>{@code cartesianProduct.constrainedProductOfDistinct(qty, list)
 *                     .andDistinct(qty, list)
 *                     .andMultiSelect(qty, list)
 *                     .andInRange(min, max, list)
 * }</pre>
 *
 * Each dimension can pick a fixed number of <i>distinct</i> elements, a fixed
 * number of elements with repetition (<i>multiSelect</i>), or a variable number
 * of elements within a range.
 */
@DisplayName("Constrained Cartesian Product")
class ConstrainedCartesianProductTest {

    static final List<String> pizzaBase = of("Small ", "Medium", "Large");
    static final List<String> sauce     = of("Tomato Ketchup", "White Sauce", "Green Chutney");
    static final List<String> cheese    = of("Ricotta", "Mozzarella", "Cheddar");
    static final List<String> toppings  = of("tomato", "capsicum", "onion", "paneer", "corn");

    // =========================================================
    // 1. Count correctness
    // =========================================================
    @Nested
    @DisplayName("Count correctness")
    class CountTests {

        @Test
        @DisplayName("Pizza builder: count() agrees with stream().count()")
        void builderCountMatchesStreamCount() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andMultiSelect(2, sauce)
                    .andInRange(1, 5, toppings);
            var list = product.lexOrder().stream().toList();
            assertEquals(product.count().longValue(), list.size());
        }
    }

    // =========================================================
    // 2. Edge cases
    // =========================================================
    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Single distinct dimension, qty=0, empty list: count=1, [[]]")
        void singleEmptyZeroQty() {
            var b = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList());
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(List.of(), result.get(0));
        }

        @Test
        @DisplayName("Single distinct dimension, qty>0, empty list: count=0, []")
        void singleEmptyPositiveQty() {
            var b = cartesianProduct.constrainedProductOfDistinct(2, Collections.emptyList());
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Any empty distinct dimension with qty>0 makes whole product empty")
        void anyEmptyDistinctMakesWholeEmpty() {
            var b = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, Collections.emptyList())
                    .andMultiSelect(2, sauce);
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("All-empty dimensions with all qty=0: single empty tuple")
        void allEmptyZeroQty() {
            var b = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList())
                    .andDistinct(0, Collections.emptyList())
                    .andMultiSelect(0, Collections.emptyList());
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(List.of(), result.get(0));
        }
    }

    // =========================================================
    // 3. Content correctness — sample of known indices in the pizza ordering
    // =========================================================
    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("Specific indices in the pizza product match known tuples")
        void specificIndicesAreCorrect() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andMultiSelect(2, sauce)
                    .andInRange(1, 5, toppings);
            var list = product.lexOrder().stream().toList();
            assertEquals(product.count().longValue(), list.size());

            assertEquals(
                    of("Small ", "Ricotta", "Mozzarella", "Tomato Ketchup", "Tomato Ketchup", "tomato"),
                    list.get(0));
            assertEquals(
                    of("Small ", "Ricotta", "Mozzarella", "Tomato Ketchup", "Green Chutney", "tomato", "onion", "paneer"),
                    list.get(80));
            assertEquals(
                    of("Small ", "Ricotta", "Cheddar", "Green Chutney", "Green Chutney", "onion", "corn"),
                    list.get(354));
            assertEquals(
                    of("Medium", "Ricotta", "Mozzarella", "Tomato Ketchup", "White Sauce", "capsicum", "paneer"),
                    list.get(599));
            assertEquals(
                    of("Medium", "Ricotta", "Cheddar", "Tomato Ketchup", "White Sauce", "corn"),
                    list.get(779));
            assertEquals(
                    of("Medium", "Mozzarella", "Cheddar", "Tomato Ketchup", "White Sauce", "tomato", "capsicum", "onion", "paneer", "corn"),
                    list.get(991));
            assertEquals(
                    of("Large", "Mozzarella", "Cheddar", "Green Chutney", "Green Chutney", "tomato", "capsicum", "onion", "paneer", "corn"),
                    list.get(list.size() - 1));
        }
    }

    // =========================================================
    // 4. Iterator contract
    // =========================================================
    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls produce equal results")
        void multipleStreamCallsAreEqual() {
            var p = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andMultiSelect(2, sauce)
                    .andInRange(1, 5, toppings)
                    .lexOrder();
            var list1 = p.stream().toList();
            var list2 = p.stream().toList();
            assertIterableEquals(list1, list2);
        }

        @Test
        @Disabled("TODO : currently not implemented as immutable. Should we make the inner lists immutable?")
        @DisplayName("Inner lists are immutable")
        void innerListsAreImmutable() {
            var results = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andMultiSelect(2, sauce)
                    .andInRange(1, 5, toppings)
                    .lexOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.get(0).add("X"));
        }
    }

    // =========================================================
    // 5. Stress test (opt-in) — exercises the constrained product
    // =========================================================
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] constrained pizza product stream count == count()")
    void stressTesting() {
        // Larger pizza: 3 bases × C(5,2)=10 cheese pairs × multiSelect(3,3)=27 sauces
        // × inRange(1..5, 7) toppings
        var bigToppings = of("tomato", "capsicum", "onion", "paneer", "corn", "olive", "jalapeno");
        var bigCheese   = of("Ricotta", "Mozzarella", "Cheddar", "Parmesan", "Feta");
        var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                .andDistinct(2, bigCheese)
                .andMultiSelect(3, sauce)
                .andInRange(1, 5, bigToppings);
        long streamCount = product.lexOrder().stream().count();
        assertEquals(product.count().longValue(), streamCount);
    }
}