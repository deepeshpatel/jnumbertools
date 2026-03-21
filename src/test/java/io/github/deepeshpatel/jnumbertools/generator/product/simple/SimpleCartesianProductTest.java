package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
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
    void assertCountAndContentForSpecialCase() {
        // Case 1: No dimensions (empty builder) -> count=1, returns [[]]
        // Note: This scenario occurs when builder is created but no dimensions added?
        // Actually, builder always has at least one dimension from construction

        // Case 1: 0-dimensional product -> count=1, returns [[]]
        var nullaryBuilder = cartesianProduct.simpleProductOf();
        assertEquals(BigInteger.ONE, nullaryBuilder.count());
        var nullaryResult = nullaryBuilder.lexOrder().stream().toList();
        assertEquals(1, nullaryResult.size());
        assertTrue(nullaryResult.get(0).isEmpty());

        // Case 2: Single empty dimension -> count=0, returns []
        var singleEmptyBuilder = cartesianProduct.simpleProductOf(Collections.emptyList());
        assertEquals(BigInteger.ZERO, singleEmptyBuilder.count());
        assertTrue(singleEmptyBuilder.lexOrder().stream().toList().isEmpty());

        // Case 3: Multiple dimensions, all non-empty -> normal product
        // (tested in other methods)

        // Case 4: Multiple dimensions, any empty -> count=0, returns [] (empty iterator)
        var multiWithEmptyBuilder = cartesianProduct.simpleProductOf(A_B)
                .and(Collections.emptyList());
        assertEquals(BigInteger.ZERO, multiWithEmptyBuilder.count());
        assertTrue(multiWithEmptyBuilder.lexOrder().stream().toList().isEmpty());

        // Case 5: Multiple dimensions, all empty -> count=0, returns []
        var allEmptyBuilder = cartesianProduct.simpleProductOf(Collections.emptyList())
                .and(Collections.emptyList());
        assertEquals(BigInteger.ZERO, allEmptyBuilder.count());
        assertTrue(allEmptyBuilder.lexOrder().stream().toList().isEmpty());
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
    void shouldHandleNullaryProduct() {
        var product = cartesianProduct.simpleProductOf().lexOrder().stream().toList();
        assertEquals(1, product.size());
        assertTrue(product.get(0).isEmpty());
    }

    @Test
    void shouldHandleEmptyFirstDimension() {
        var builder = cartesianProduct.simpleProductOf(Collections.emptyList());
        var generator = builder.lexOrder();
        var result = generator.stream().toList();
        assertEquals(BigInteger.ZERO, builder.count());
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleEmptySecondDimension() {
        // Test empty list added as second dimension
        var builder = cartesianProduct.simpleProductOf(A_B)
                .and(Collections.emptyList());

        // A × ∅ = ∅ (empty product) - count should be 0, not 1
        assertEquals(BigInteger.ZERO, builder.count());  // Changed from ONE to ZERO

        // Iterator should return no elements
        var result = builder.lexOrder().stream().toList();
        assertTrue(result.isEmpty());  // Should be empty list, not [[]]
    }

    @Test
    void shouldHandleEmptyFirstAndSecondDimensions() {
        // Test both dimensions empty
        var builder = cartesianProduct.simpleProductOf(Collections.emptyList())
                .and(Collections.emptyList());

        // ∅ × ∅ = ∅ (empty product) - count should be 0
        assertEquals(BigInteger.ZERO, builder.count());  // Changed from ONE to ZERO

        var result = builder.lexOrder().stream().toList();
        assertTrue(result.isEmpty());  // Should be empty
    }

    @Test
    void shouldHandleEmptyFirstWithAdditionalDimensions() {
        // Empty first dimension followed by non-empty dimensions
        var builder = cartesianProduct.simpleProductOf(Collections.emptyList())
                .and(A_B)
                .and(num_1_2_3);

        // ∅ × A × B = ∅ (empty product) - count should be 0
        assertEquals(BigInteger.ZERO, builder.count());  // Changed from ONE to ZERO
        assertTrue(builder.lexOrder().stream().toList().isEmpty());
    }

    @Test
    void shouldHandleNonEmptyFirstWithEmptyMiddle() {
        // Non-empty, empty, non-empty dimensions
        var builder = cartesianProduct.simpleProductOf(A_B)
                .and(Collections.emptyList())
                .and(num_1_2_3);

        // A × ∅ × B = ∅ (empty product) - count should be 0
        assertEquals(BigInteger.ZERO, builder.count());  // Changed from ONE to ZERO
        assertTrue(builder.lexOrder().stream().toList().isEmpty());
    }

    @Test
    void shouldHandleEmptyListAfterAnd() {
        // Test and() with empty list after builder creation
        var builder1 = cartesianProduct.simpleProductOf(A_B);
        var builder2 = builder1.and(Collections.emptyList());

        // Original builder unchanged (immutability)
        assertEquals(BigInteger.valueOf(2), builder1.count());

        // New builder with empty dimension - A × ∅ = ∅, count should be 0
        assertEquals(BigInteger.ZERO, builder2.count());  // Changed from ONE to ZERO
        assertTrue(builder2.lexOrder().stream().toList().isEmpty());
    }
}
