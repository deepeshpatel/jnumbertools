package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleCartesianProductByRanksTest {

    @Nested
    class Mth {
        @Test
        void shouldGenerateOutputSimilarToRepetitivePermMthWhenProductIsWithSameListMultipleTimes() {
            var decimalDigits = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            for (int m = 2; m < 1000; m += 2) {
                var result = cartesianProduct.simpleProductOf(decimalDigits)
                        .and(decimalDigits)
                        .and(decimalDigits)
                        .lexOrderMth(m, 0).stream().toList();
                var expected = permutation.repetitive(3, decimalDigits)
                        .lexOrderMth(m, 0).stream().toList();
                assertEquals(expected, result);
            }
        }

        @Test
        void shouldGenerateOutputSimilarToRepetitivePermWhenMEquals1AndProductIsWithSameListMultipleTimes() {
            var result = cartesianProduct.simpleProductOf(A_B_C)
                    .and(A_B_C)
                    .and(A_B_C)
                    .lexOrderMth(1, 0).stream().toList();
            var expected = permutation.repetitive(3, A_B_C)
                    .lexOrder().stream().toList();
            assertIterableEquals(expected, result);
        }

        @Test
        void shouldGenerateCorrectMthOutputRelativeToListOfAllCartesianValues() {
            int max = A_B_C.size() * num_1_2_3.size() * num_1_to_4.size();
            int start = 3;
            for (int m = 2; m <= max / 2; m++) {
                var builder = cartesianProduct
                        .simpleProductOf(A_B_C)
                        .and(num_1_2_3)
                        .and(num_1_to_4);
                var all = builder.lexOrder().stream();
                var mth = builder.lexOrderMth(m, start).stream();
                assertEveryMthValue(all, mth, start, m);
            }
        }

        @Test
        void shouldGenerateCorrectOutputForDifferentStartPositions() {
            var builder = cartesianProduct.simpleProductOf(A_B_C).and(num_1_2_3);
            assertEquals(
                    List.of(List.of('A', 1), List.of('A', 3), List.of('B', 2), List.of('C', 1), List.of('C', 3)),
                    builder.lexOrderMth(2, 0).stream().toList()
            );
            assertEquals(
                    List.of(List.of('A', 2), List.of('B', 1), List.of('B', 3), List.of('C', 2)),
                    builder.lexOrderMth(2, 1).stream().toList()
            );
            assertEquals(
                    List.of(List.of('B', 1), List.of('B', 3), List.of('C', 2)),
                    builder.lexOrderMth(2, 3).stream().toList()
            );
        }

        @Test
        void shouldReturnCorrectTupleAtSpecificRank() {
            var builder = cartesianProduct
                    .simpleProductOf(List.of('A', 'B', 'C'))   // size 3
                    .and(List.of(1, 2, 3, 4))                  // size 4
                    .and(List.of("X", "Y"));                   // size 2

            // Rank 6 in lex order should be: C,1,X  (0-based: A1X=0, A1Y=1, A2X=2, A3X=3, A4X=4, B1X=5, C1X=6)
            var result = builder.byRanks(List.of(BigInteger.valueOf(6))).stream().toList();

            assertEquals(1, result.size());
            assertEquals(List.of('A', 4, "X"), result.get(0));
        }

        @Test
        void testFailFastForLexOrderMth() {
            var builder = cartesianProduct.simpleProductOf(A_B_C).and(num_1_2_3);

            // m <= 0
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> builder.lexOrderMth(0, 1));
            assertEquals(errMsgForIncrement, exception.getMessage());

            exception = assertThrows(IllegalArgumentException.class,
                    () -> builder.lexOrderMth(-1, 1));
            assertEquals(errMsgForIncrement, exception.getMessage());

            // start < 0
            exception = assertThrows(IllegalArgumentException.class,
                    () -> builder.lexOrderMth(1, -1));
            assertTrue(exception.getMessage().startsWith("Element should be in range"));

            // start >= count
            exception = assertThrows(IllegalArgumentException.class,
                    () -> builder.lexOrderMth(1, 100));
            assertTrue(exception.getMessage().startsWith("Element should be in range"));
        }
    }

    @Nested
    class Choice {
        @Test
        void shouldGenerateRandomChoice() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of("X", "Y"))
                    .choice(3);
            var list = product.stream().toList();
            assertEquals(3, list.size());
            for (var item : list) {
                assertEquals(2, item.size());
                assertTrue(List.of("A", "B").contains(item.get(0)));
                assertTrue(List.of("X", "Y").contains(item.get(1)));
            }
        }

        @Test
        void shouldGenerateSingleListChoice() {
            var product = cartesianProduct.simpleProductOf(List.of(0, 1, 2)).choice(2);
            var list = product.stream().toList();
            assertEquals(2, list.size());
            for (var item : list) {
                assertEquals(1, item.size());
                assertTrue(List.of(0, 1, 2).contains(item.get(0)));
            }
        }

        @Test
        void choice_withSingleCombination() {
            var product = cartesianProduct.simpleProductOf(List.of("A")).choice(5);
            var list = product.stream().toList();
            assertEquals(5, list.size());
            assertEquals(List.of("A"), list.get(0));
        }
    }

    @Nested
    class Sample {
        @Test
        void shouldGenerateRandomSample() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of("X", "Y"))
                    .sample(2);
            var list = product.stream().toList();
            assertEquals(2, list.size());
            assertEquals(2, list.stream().distinct().count()); // Ensure uniqueness
            for (var item : list) {
                assertEquals(2, item.size());
                assertTrue(List.of("A", "B").contains(item.get(0)));
                assertTrue(List.of("X", "Y").contains(item.get(1)));
            }
        }

        @Test
        void shouldGenerateSingleListSample() {
            var product = cartesianProduct.simpleProductOf(List.of(0, 1, 2)).sample(2);
            var list = product.stream().toList();
            assertEquals(2, list.size());
            assertEquals(2, list.stream().distinct().count());
            for (var item : list) {
                assertEquals(1, item.size());
                assertTrue(List.of(0, 1, 2).contains(item.get(0)));
            }
        }
    }

    @Nested
    class ByRanks {

        @Test
        void shouldGenerateSingleRank() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2));

            var result = product.byRanks(List.of(BigInteger.valueOf(2))).stream().toList();

            assertEquals(1, result.size());
            assertEquals(List.of("B", 1), result.get(0)); // Rank 2: [B,1]
        }

        @Test
        void shouldGenerateMultipleRanks() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2))
                    .and(List.of("X", "Y"));

            // Total 2×2×2 = 8 tuples
            // Ranks: 0=[A,1,X], 3=[A,2,Y], 6=[B,2,X]
            var ranks = List.of(
                    BigInteger.ZERO,
                    BigInteger.valueOf(3),
                    BigInteger.valueOf(6)
            );

            var result = product.byRanks(ranks).stream().toList();

            assertEquals(3, result.size());
            assertEquals(List.of("A", 1, "X"), result.get(0));
            assertEquals(List.of("A", 2, "Y"), result.get(1));
            assertEquals(List.of("B", 2, "X"), result.get(2));
        }

        @Test
        void shouldHandleEmptyRankSequence() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2));

            var result = product.byRanks(Collections.emptyList()).stream().toList();

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldThrowExceptionForNullRanks() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2));

            assertThrows(IllegalArgumentException.class, () ->
                    product.byRanks(null)
            );
        }

        @Test
        void shouldThrowExceptionForNegativeRank() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2));

            var result = product.byRanks(List.of(BigInteger.valueOf(-1)));

            // Negative rank validation is deferred to iteration
            assertThrows(IllegalArgumentException.class, () ->
                    result.stream().toList()
            );
        }

        @Test
        void shouldThrowExceptionForOutOfBoundRank() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2)); // Total 4 tuples, valid ranks 0-3

            var result = product.byRanks(List.of(BigInteger.valueOf(4)));

            // Out-of-bounds validation is deferred to iteration
            assertThrows(IllegalArgumentException.class, () ->
                    result.stream().toList()
            );
        }

        @Test
        void shouldHandleMixedValidAndInvalidRanks() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2)); // Total 4 tuples

            var ranks = List.of(
                    BigInteger.ZERO,      // valid
                    BigInteger.valueOf(4), // invalid - out of bounds
                    BigInteger.valueOf(2)  // valid
            );

            var result = product.byRanks(ranks);
            var iterator = result.iterator();

            // First rank should work
            assertEquals(List.of("A", 1), iterator.next());

            // Second rank should throw during iteration
            assertThrows(IllegalArgumentException.class, iterator::next);
        }

        @Test
        void shouldWorkWithSingleListProduct() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B", "C"));

            var ranks = List.of(
                    BigInteger.ZERO,
                    BigInteger.valueOf(2)
            );

            var result = product.byRanks(ranks).stream().toList();

            assertEquals(2, result.size());
            assertEquals(List.of("A"), result.get(0));
            assertEquals(List.of("C"), result.get(1));
        }


        @Test
        void shouldHandleEmptyProduct() {
            var product = cartesianProduct.simpleProductOf(Collections.emptyList());

            var result = product.byRanks(List.of(BigInteger.ZERO)).stream().toList();

            // Empty product has one empty tuple at rank 0
            assertEquals(1, result.size());
            assertEquals(List.of(), result.get(0));
        }

        @Test
        void shouldThrowExceptionForEmptyProductWithNonZeroRank() {
            var product = cartesianProduct.simpleProductOf(Collections.emptyList());

            var result = product.byRanks(List.of(BigInteger.ONE));

            assertThrows(IllegalArgumentException.class, () ->
                    result.stream().toList()
            );
        }

        @Test
        void shouldHandleLargeRanks() {
            var product = cartesianProduct.simpleProductOf(List.of(0, 1, 2, 3, 4))
                    .and(List.of(0, 1, 2, 3, 4))
                    .and(List.of(0, 1, 2, 3, 4)); // 5×5×5 = 125 total

            BigInteger rank = BigInteger.valueOf(100);
            var result = product.byRanks(List.of(rank)).stream().toList();

            assertEquals(1, result.size());
            // Just verify it doesn't throw and returns something
            assertNotNull(result.get(0));
            assertEquals(3, result.get(0).size());
        }

        @Test
        void shouldPreserveOrderOfRanks() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of(1, 2)); // 4 tuples: 0=[A,1], 1=[A,2], 2=[B,1], 3=[B,2]

            var ranks = List.of(
                    BigInteger.valueOf(3),
                    BigInteger.ZERO,
                    BigInteger.valueOf(2)
            );

            var result = product.byRanks(ranks).stream().toList();

            assertEquals(3, result.size());
            assertEquals(List.of("B", 2), result.get(0)); // rank 3 first
            assertEquals(List.of("A", 1), result.get(1)); // rank 0 second
            assertEquals(List.of("B", 1), result.get(2)); // rank 2 third
        }
    }
}