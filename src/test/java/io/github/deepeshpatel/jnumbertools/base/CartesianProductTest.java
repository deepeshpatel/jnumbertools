package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CartesianProduct Factory Validation")
class CartesianProductTest {

    @Test
    @DisplayName("simpleProductOf: null input throws NPE, empty input allowed")
    void simpleProductOf() {
        var exp = assertThrows(NullPointerException.class, ()-> cartesianProduct.simpleProductOf((List<?>)null));
        assertTrue(exp.getMessage().startsWith(errMsgNullInput));

        // Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.simpleProductOf(Collections.emptyList());
    }

    @Nested
    @DisplayName("constrainedProductOfDistinct")
    class ConstrainedProductOfDistinctTests {

        @Test
        @DisplayName("null input throws NPE")
        void nullInput() {
            var exp1 = assertThrows(NullPointerException.class, ()->
                    cartesianProduct.constrainedProductOfDistinct(1, (List<?>)null));
            assertTrue(exp1.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("empty input allowed")
        void emptyInput() {
            cartesianProduct.constrainedProductOfDistinct(10, Collections.emptyList());
            cartesianProduct.constrainedProductOfDistinct(5, Collections.emptyList());
        }

        @Test
        @DisplayName("negative quantity throws IAE with message")
        void negativeQuantity() {
            var exp2 = assertThrows(IllegalArgumentException.class, ()->
                    cartesianProduct.constrainedProductOfDistinct(-1, Collections.emptyList()));
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
                    cartesianProduct.constrainedProductOfMultiSelect(1, (List<?>)null));
            assertTrue(exp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("empty input allowed")
        void emptyInput() {
            cartesianProduct.constrainedProductOfMultiSelect(3, Collections.emptyList());
            cartesianProduct.constrainedProductOfMultiSelect(0, Collections.emptyList());
        }

        @Test
        @DisplayName("negative quantity throws IAE with message")
        void negativeQuantity() {
            var exp2 = assertThrows(IllegalArgumentException.class, ()->
                    cartesianProduct.constrainedProductOfMultiSelect(-1, Collections.emptyList()));
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
                    cartesianProduct.constrainedProductOfInRange(1, 2, (List<?>)null));
            assertTrue(exp1.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("invalid from index throws IAE")
        void invalidFromIndex() {
            var exp2 = assertThrows(IllegalArgumentException.class, ()->
                    cartesianProduct.constrainedProductOfInRange(-1, 5, A_B));
            assertEquals("Invalid range: from=-1, to=5 - must satisfy 0 ≤ from ≤ to", exp2.getMessage());
        }

        @Test
        @DisplayName("to index exceeding input size throws IAE")
        void toIndexExceedsSize() {
            var exp = assertThrows(IllegalArgumentException.class, () ->
                    cartesianProduct.constrainedProductOfInRange(1, 10, A_B_C));
            assertEquals("Invalid range: to cannot exceed 3", exp.getMessage());
        }

        @Test
        @DisplayName("empty input allowed for valid range")
        void emptyInput() {
            cartesianProduct.constrainedProductOfInRange(0, 0, Collections.emptyList());
        }
    }
}