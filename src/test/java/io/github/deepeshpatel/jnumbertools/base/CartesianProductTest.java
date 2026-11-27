package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.A_B;
import static io.github.deepeshpatel.jnumbertools.TestBase.A_B_C;
import static io.github.deepeshpatel.jnumbertools.TestBase.errMsgNullInput;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Factory Validation")
class CartesianProductTest {

    @Test
    @DisplayName("simpleProductOf: null input throws NPE, empty input allowed")
    void simpleProductOf() {
        var exp = assertThrows(NullPointerException.class, ()-> Product.simple((List<?>)null));
        assertTrue(exp.getMessage().startsWith(errMsgNullInput));

        // Empty list is allowed and will be treated as empty-set(∅)
        Product.simple(Collections.emptyList());
    }

    @Test
    @DisplayName("simpleProductOf overload accepts two or more dimensions")
    void simpleProductOfMultipleDimensions() {
        var first = List.of("A", "B");
        var second = List.of(1, 2);
        var third = List.of("X", "Y");

        var product = Product.simple(first, second, third);

        assertEquals(8, product.count().intValueExact());
        assertEquals(List.of(
                List.of("A", 1, "X"),
                List.of("A", 1, "Y"),
                List.of("A", 2, "X"),
                List.of("A", 2, "Y"),
                List.of("B", 1, "X"),
                List.of("B", 1, "Y"),
                List.of("B", 2, "X"),
                List.of("B", 2, "Y")
        ), product.lexOrder().stream().toList());
    }

    @Test
    @DisplayName("simpleProductOf overload validates and snapshots every dimension")
    void simpleProductOfMultipleDimensionsValidationAndSnapshot() {
        var first = new java.util.ArrayList<>(List.of("A", "B"));
        var second = new java.util.ArrayList<>(List.of(1, 2));

        var product = Product.simple(first, second);
        first.clear();
        second.add(3);

        assertEquals(4, product.count().intValueExact());
        assertThrows(NullPointerException.class,
                () -> Product.simple(List.of("A"), null));
        assertThrows(NullPointerException.class,
                () -> Product.simple((List<?>[]) null));
        assertThrows(NullPointerException.class,
                () -> Product.simple(List.of("A"), List.of(1), (List<?>) null));
    }

    @Nested
    @DisplayName("constrainedProductOfDistinct")
    class ConstrainedProductOfDistinctTests {

        @Test
        @DisplayName("null input throws NPE")
        void nullInput() {
            var exp1 = assertThrows(NullPointerException.class, ()->
                    Product.constrained().ofDistinct(1, (List<?>)null));
            assertTrue(exp1.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("empty input allowed")
        void emptyInput() {
            Product.constrained().ofDistinct(10, Collections.emptyList());
            Product.constrained().ofDistinct(5, Collections.emptyList());
        }

        @Test
        @DisplayName("negative quantity throws IAE with message")
        void negativeQuantity() {
            var exp2 = assertThrows(IllegalArgumentException.class, ()->
                    Product.constrained().ofDistinct(-1, Collections.emptyList()));
            assertEquals("quantity must be ≥ 0", exp2.getMessage());
        }
    }

    @Nested
    @DisplayName("constrainedProductOfMultiSelect")
    class ConstrainedProductOfMultiSelectTests {

        @Test
        @DisplayName("null input throws NPE")
        void nullInput() {
            var exp = assertThrows(NullPointerException.class, ()->
                    Product.constrained().ofMultiSelect(1, (List<?>)null));
            assertTrue(exp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("empty input allowed")
        void emptyInput() {
            Product.constrained().ofMultiSelect(3, Collections.emptyList());
            Product.constrained().ofMultiSelect(0, Collections.emptyList());
        }

        @Test
        @DisplayName("negative quantity throws IAE with message")
        void negativeQuantity() {
            var exp2 = assertThrows(IllegalArgumentException.class, ()->
                    Product.constrained().ofMultiSelect(-1, Collections.emptyList()));
            assertEquals("quantity must be ≥ 0", exp2.getMessage());
        }
    }

    @Nested
    @DisplayName("constrainedProductOfInRange")
    class ConstrainedProductOfInRangeTests {

        @Test
        @DisplayName("null input throws NPE")
        void nullInput() {
            var exp1 = assertThrows(NullPointerException.class, ()->
                    Product.constrained().ofInRange(1, 2, (List<?>)null));
            assertTrue(exp1.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("invalid from index throws IAE")
        void invalidFromIndex() {
            var exp2 = assertThrows(IllegalArgumentException.class, ()->
                    Product.constrained().ofInRange(-1, 5, A_B));
            assertEquals("Invalid range: from=-1, to=5 - must satisfy 0 ≤ from ≤ to", exp2.getMessage());
        }

        @Test
        @DisplayName("to index exceeding input size throws IAE")
        void toIndexExceedsSize() {
            var exp = assertThrows(IllegalArgumentException.class, () ->
                    Product.constrained().ofInRange(1, 10, A_B_C));
            assertEquals("Invalid range: to cannot exceed 3", exp.getMessage());
        }

        @Test
        @DisplayName("empty input allowed for valid range")
        void emptyInput() {
            Product.constrained().ofInRange(0, 0, Collections.emptyList());
        }
    }
}