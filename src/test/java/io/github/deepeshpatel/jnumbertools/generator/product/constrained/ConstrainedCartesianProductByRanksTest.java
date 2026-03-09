package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class ConstrainedCartesianProductByRanksTest {

    private final List<String> pizzaBase = of("Small ", "Medium", "Large");
    private final List<String> cheese = of("Ricotta", "Mozzarella", "Cheddar");
    private final List<String> toppings = of("tomato", "capsicum", "onion", "paneer", "corn");

    @Nested
    class Mth {

        @Test
        void assertCount() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andInRange(1, 3, toppings);

            for (int m = 1; m <= 3; m++) {
                long mthCount = product.lexOrderMth(m, 0).stream().count();
                long expected = (long) Math.ceil(product.count().doubleValue() / m);
                assertEquals(expected, mthCount);
            }
        }

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: Single dimension with empty list and quantity 0 -> count=1, mth should return [[]]
            var singleEmptyZero = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList());
            var result1 = singleEmptyZero.lexOrderMth(1, 0).stream().toList();
            assertEquals(1, result1.size());
            assertEquals(List.of(), result1.get(0));

            // Case 2: Single dimension with empty list and quantity > 0 -> count=0, mth should return []
            var singleEmptyPositive = cartesianProduct.constrainedProductOfDistinct(2, Collections.emptyList());
            assertTrue(singleEmptyPositive.lexOrderMth(1, 0).stream().toList().isEmpty());

            // Case 3: Multiple dimensions with any empty -> count=0, mth should return []
            var multiWithEmpty = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, Collections.emptyList());
            assertTrue(multiWithEmpty.lexOrderMth(1, 0).stream().toList().isEmpty());

            // Case 4: Multiple dimensions with all count=1 -> count=1, mth should return [[]]
            var allOnes = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList())
                    .andDistinct(0, Collections.emptyList());
            var result4 = allOnes.lexOrderMth(1, 0).stream().toList();
            assertEquals(1, result4.size());
            assertEquals(List.of(), result4.get(0));
        }

        @Test
        void shouldGenerateMultipleMthValues() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            int start = 2;
            int m = 3;
            var all = product.lexOrder().stream().toList();
            var mth = product.lexOrderMth(m, start).stream().toList();

            int expectedSize = (int) Math.ceil((all.size() - start) / (double) m);
            assertEquals(expectedSize, mth.size());

            for (int i = 0; i < mth.size(); i++) {
                assertEquals(all.get(start + i * m), mth.get(i));
            }
        }

        @Test
        void shouldGenerateCorrectMthValue() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese)
                    .andInRange(1, 3, toppings);

            int start = 10;
            int m = 5;
            var all = product.lexOrder().stream().toList();
            var mth = product.lexOrderMth(m, start).stream().toList();

            int expectedSize = (int) Math.ceil((all.size() - start) / (double) m);
            assertEquals(expectedSize, mth.size());

            for (int i = 0; i < mth.size(); i++) {
                assertEquals(all.get(start + i * m), mth.get(i));
            }
        }

        @Test
        void testFailFastForLexOrderMth() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            // m <= 0
            assertThrows(IllegalArgumentException.class, () ->
                    product.lexOrderMth(0, 1)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    product.lexOrderMth(-1, 1)
            );

            // start < 0
            assertThrows(IllegalArgumentException.class, () ->
                    product.lexOrderMth(1, -1)
            );

            // start >= count
            BigInteger total = product.count();
            assertThrows(IllegalArgumentException.class, () ->
                    product.lexOrderMth(1, total.intValue() + 1)
            );
        }
    }

    @Nested
    class Choice {
        @Test
        void shouldGenerateRandomChoiceWithReplacement() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            int sampleSize = 5;
            var result = product.choice(sampleSize, TestBase.random).stream().toList();

            assertEquals(sampleSize, result.size());
            // Duplicates are allowed, so we don't check uniqueness
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            assertThrows(IllegalArgumentException.class, () ->
                    product.choice(-1, TestBase.random)
            );
        }

        @Test
        void shouldHandleEmptyProduct() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, Collections.emptyList());

            var result = product.choice(3, TestBase.random).stream().toList();
            assertEquals(0, result.size());
        }

        @Test
        void shouldGenerateValidElements() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            var result = product.choice(3, TestBase.random).stream().toList();

            assertEquals(3, result.size());
            for (var tuple : result) {
                // First element: pizza base
                assertTrue(pizzaBase.contains(tuple.get(0)));

                // Next two elements: two distinct cheeses
                assertEquals(3, tuple.size()); // base + 2 cheeses
                assertTrue(cheese.contains(tuple.get(1)));
                assertTrue(cheese.contains(tuple.get(2)));
                assertNotEquals(tuple.get(1), tuple.get(2)); // distinct
            }
        }
    }

    @Nested
    class Sample {
        @Test
        void shouldGenerateRandomSampleWithoutReplacement() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            int sampleSize = 4;
            var result = product.sample(sampleSize, TestBase.random).stream().toList();

            assertEquals(sampleSize, result.size());
            assertEquals(sampleSize, result.stream().distinct().count());
        }

        @Test
        void shouldThrowExceptionForSampleSizeExceedingTotal() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            BigInteger total = product.count();
            assertThrows(IllegalArgumentException.class, () ->
                    product.sample(total.intValue() + 1, TestBase.random)
            );
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            assertThrows(IllegalArgumentException.class, () ->
                    product.sample(-1, TestBase.random)
            );
        }

        @Test
        void shouldGenerateUniqueElements() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            int sampleSize = 5;
            var result = product.sample(sampleSize, TestBase.random).stream().toList();

            assertEquals(sampleSize, result.size());
            assertEquals(sampleSize, result.stream().distinct().count());

            for (var tuple : result) {
                // Verify structure
                assertTrue(pizzaBase.contains(tuple.get(0)));
                assertEquals(3, tuple.size());
                assertTrue(cheese.contains(tuple.get(1)));
                assertTrue(cheese.contains(tuple.get(2)));
                assertNotEquals(tuple.get(1), tuple.get(2));
            }
        }
    }

    @Nested
    class ByRanks {
        @Test
        void shouldGenerateSingleRank() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            var result = product.byRanks(List.of(BigInteger.ZERO)).stream().toList();

            assertEquals(1, result.size());
            assertEquals(product.lexOrder().stream().findFirst().get(), result.get(0));
        }

        @Test
        void shouldGenerateMultipleRanks() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            // Total = 3 × 3 = 9, valid ranks 0-8
            var ranks = List.of(
                    BigInteger.ZERO,
                    BigInteger.valueOf(4),  // Use valid rank within 0-8
                    BigInteger.valueOf(7)
            );

            var result = product.byRanks(ranks).stream().toList();
            var all = product.lexOrder().stream().toList();

            assertEquals(3, result.size());
            assertEquals(all.get(0), result.get(0));
            assertEquals(all.get(4), result.get(1));
            assertEquals(all.get(7), result.get(2));
        }

        @Test
        void shouldThrowExceptionForNullRanks() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            assertThrows(IllegalArgumentException.class, () ->
                    product.byRanks(null)
            );
        }

        @Test
        void shouldThrowExceptionForNegativeRank() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            var result = product.byRanks(List.of(BigInteger.valueOf(-1)));

            assertThrows(IllegalArgumentException.class, () ->
                    result.stream().toList()
            );
        }

        @Test
        void shouldThrowExceptionForOutOfBoundRank() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            BigInteger total = product.count();
            var result = product.byRanks(List.of(total));

            assertThrows(IllegalArgumentException.class, () ->
                    result.stream().toList()
            );
        }

        @Test
        void shouldHandleEmptyRankSequence() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            var result = product.byRanks(Collections.emptyList()).stream().toList();

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldPreserveRankOrder() {
            var product = cartesianProduct.constrainedProductOfDistinct(1, pizzaBase)
                    .andDistinct(2, cheese);

            // Total = 3 × 3 = 9, valid ranks 0-8
            var ranks = List.of(
                    BigInteger.valueOf(7),
                    BigInteger.ZERO,
                    BigInteger.valueOf(4)
            );

            var result = product.byRanks(ranks).stream().toList();
            var all = product.lexOrder().stream().toList();

            assertEquals(3, result.size());
            assertEquals(all.get(7), result.get(0));  // rank 7 first
            assertEquals(all.get(0), result.get(1));  // rank 0 second
            assertEquals(all.get(4), result.get(2));  // rank 4 third
        }
    }
}