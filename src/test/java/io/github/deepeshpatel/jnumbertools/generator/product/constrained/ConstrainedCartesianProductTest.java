/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ConstrainedProductBuilder, covering all, mth, choice, and sample operations.
 */
public class ConstrainedCartesianProductTest {

    private final List<String> pizzaBase = of("Small ", "Medium", "Large");
    private final List<String> sauce = of("Tomato Ketchup", "White Sauce", "Green Chutney");
    private final List<String> cheese = of("Ricotta", "Mozzarella", "Cheddar");
    private final List<String> toppings = of("tomato", "capsicum", "onion", "paneer", "corn");

    @Nested
    class All {
        @Test
        void shouldGenerateCorrectNumOfCombinations() {
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
    }

    @Nested
    class Mth {
        @Test
        void shouldGenerateCorrectlyForDifferentMValues() {
            var combProduct = cartesianProduct
                    .constrainedProductOf(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andMultiSelect(2, sauce)
                    .andInRange(3, toppings.size(), toppings);
            for (int m = 2; m <= 10; m++) {
                List<?> expected = combProduct.lexOrder().stream().toList().get(m);
                List<?> result = (List<?>) combProduct.lexOrderMth(m, m).stream().toList().get(0);
                assertIterableEquals(expected, result);
            }
        }

        @Test
        void shouldGenerateCorrectMthValue() {
            var combProduct = cartesianProduct
                    .constrainedProductOf(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andMultiSelect(2, sauce)
                    .andInRange(3, toppings.size(), toppings);
            int start = 800;
            int m = 50;
            var all = combProduct.lexOrder().stream().toList();
            var mth = combProduct.lexOrderMth(m, start).iterator();
            assertEquals(all.get(start), mth.next());
            assertEquals(all.get(start + m), mth.next());
        }

        @Test
        void shouldGenerateCorrectCombinationForVeryLargeM() {
            var smallAlphabets = IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).toList();
            var largeAlphabets = IntStream.rangeClosed('A', 'Z').mapToObj(c -> (char) c).toList();
            var numbers = IntStream.rangeClosed(0, 20).boxed().toList();
            var productBuilder = cartesianProduct
                    .constrainedProductOf(13, smallAlphabets)
                    .andDistinct(13, largeAlphabets)
                    .andInRange(0, 21, numbers);
            List<List<?>> expectedRows = List.of(
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M'),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'O', 'X', 0, 1, 2, 4, 6, 8, 9, 10, 12, 13, 18, 20),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'V', 'W', 0, 1, 8, 9, 12, 13, 15, 17, 18, 20),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'L', 'P', 'S', 0, 3, 8, 9, 10, 11, 19),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'L', 'W', 'X', 0, 3, 4, 5, 7, 8, 9, 11, 12, 16, 18, 20),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'M', 'R', 'S', 1, 2, 3, 6, 7, 10, 13, 16, 17, 19),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'N', 'P', 'R', 0, 1, 4, 5, 14, 15, 16, 19),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'N', 'V', 'Z', 2, 3, 4, 5, 7, 8, 9, 10, 12, 14, 18, 20),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'O', 'U', 'W', 2, 4, 5, 7, 11, 13, 14, 15, 16, 18),
                    of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'P', 'U', 'Z', 1, 6, 8, 11, 12, 17, 18, 19)            );
            testForEveryMth(100000000L, expectedRows, productBuilder);
        }

        private void testForEveryMth(long m, List<List<?>> rows, ConstrainedProductBuilder builder) {
            long mm = -m;
            for (List<?> row : rows) {
                mm += m;
                assertEquals(row, builder.lexOrderMth(mm, mm).iterator().next());
            }
        }
    }

    @Nested
    class Choice {
        @Test
        void shouldGenerateRandomChoice() {
            var product = cartesianProduct.constrainedProductOf(1, pizzaBase)
                    .andDistinct(1, cheese)
                    .choice(3);
            var list = product.stream().toList();
            assertEquals(3, list.size());
            for (var item : list) {
                List innerList = (List) item;
                assertEquals(2, innerList.size());
                assertTrue(pizzaBase.contains(innerList.get(0)));
                assertTrue(cheese.contains(innerList.get(1)));
            }
        }
    }

    @Nested
    class Sample {
        @Test
        void shouldGenerateRandomSample() {
            var product = cartesianProduct.constrainedProductOf(1, pizzaBase)
                    .andDistinct(1, cheese)
                    .sample(3);
            var list = product.stream().toList();
            assertEquals(3, list.size());
            assertEquals(3, list.stream().distinct().count()); // Ensure uniqueness
            for (var item : list) {
                List innerList = (List) item;
                assertEquals(2, innerList.size());
                assertTrue(pizzaBase.contains(innerList.get(0)));
                assertTrue(cheese.contains(innerList.get(1)));
            }
        }
    }
}