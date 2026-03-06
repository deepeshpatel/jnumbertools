package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleCartesianProductTest {
    @Test
    void assertCount() {
        // Cartesian product: n₁·n₂·...·nₖ
        var product = cartesianProduct.simpleProductOf(List.of(0, 1))
                .and(List.of('A', 'B'))
                .and(List.of(1, 2, 3));
        long expected = 2 * 2 * 3; // 12
        long actual = product.lexOrder().stream().count();
        assertEquals(expected, actual);
    }

    @Test
    void shouldHandleNullInput() {
        // Mathematical note:
        // - Empty set (∅) as input represents a single dimension with no elements
        // - Conceptually there is one empty tuple, but since there are no elements,
        //   the iterator returns nothing
        // - count() returns 1, iteration returns 0 elements

        var builder = cartesianProduct.simpleProductOf((List<?>) null);
        assertEquals(BigInteger.ONE, builder.count(), "Count should be 1 (empty set exists)");

        var result = builder.lexOrder().stream().toList();
        assertTrue(result.isEmpty(), "Iterator should return no elements");
    }

    @Test
    void shouldHandleNullInAnd() {
        // Mathematical note:
        // - First dimension: ["A", "B"] (2 elements)
        // - Second dimension: null (empty set)
        // - The Cartesian product A × ∅ = ∅ (empty set)
        // - Since one dimension is empty, the entire product is empty
        // - count() should be 0, iterator should return no elements

        var builder = cartesianProduct.simpleProductOf(List.of("A", "B"))
                .and((List<?>) null);

        assertEquals(BigInteger.ZERO, builder.count(),
                "Count should be 0 (product with empty set is empty)");

        var result = builder.lexOrder().stream().toList();
        assertTrue(result.isEmpty(), "Iterator should return no elements");
    }

    @Test
    void shouldHandleMixedNullAndEmpty() {
        var builder = cartesianProduct.simpleProductOf(List.of("A", "B"))
                .and(Collections.emptyList())
                .and((List<?>) null);
        assertEquals(BigInteger.ZERO, builder.count());
        assertTrue(builder.lexOrder().stream().toList().isEmpty());
    }

    @Test
    void shouldGenerateAllElementsOfCartesianProduct() {
        var expected = List.of(
                List.of(0, 'A', 1), List.of(0, 'A', 2), List.of(0, 'A', 3),
                List.of(0, 'B', 1), List.of(0, 'B', 2), List.of(0, 'B', 3),
                List.of(1, 'A', 1), List.of(1, 'A', 2), List.of(1, 'A', 3),
                List.of(1, 'B', 1), List.of(1, 'B', 2), List.of(1, 'B', 3)
        );
        var list = cartesianProduct
                .simpleProductOf(List.of(0, 1))
                .and(A_B)
                .and(num_1_2_3)
                .lexOrder().stream().toList();
        assertIterableEquals(expected, list);
    }

    @Test
    void shouldGenerateIdentityForSingleList() {
        var list = List.of("A", "B", "C");
        var product = cartesianProduct.simpleProductOf(list).lexOrder().stream().toList();
        assertEquals(list.size(), product.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(List.of(list.get(i)), product.get(i));
        }
    }

    @Test
    void shouldHandleEmptyProduct() {
        var product = cartesianProduct.simpleProductOf(List.of()).lexOrder().stream().toList();
        assertTrue(product.isEmpty());
    }

}
