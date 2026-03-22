package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CartesianProductTest {

    @Test
    void simpleProductOf() {
        //null input
        var expNullInput = assertThrows(NullPointerException.class, ()-> cartesianProduct.simpleProductOf(null));
        assertEquals(errMsgNullInput, expNullInput.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.simpleProductOf(Collections.emptyList());
    }

    @Test
    void constrainedProductOfDistinct() {
        //null input
        var expNullInput = assertThrows(NullPointerException.class, ()->
                cartesianProduct.constrainedProductOfDistinct(1, null));
        assertEquals(errMsgNullInput, expNullInput.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.constrainedProductOfDistinct(10, Collections.emptyList());

        //-ve quantity
        var exp2 = assertThrows(IllegalArgumentException.class, ()->
                cartesianProduct.constrainedProductOfDistinct(-1, Collections.emptyList()));
        assertEquals("quantity must be ≥ 0", exp2.getMessage());
    }

    @Test
    void constrainedProductOfMultiSelect() {
        //null input
        var exp = assertThrows(NullPointerException.class, ()->
                cartesianProduct.constrainedProductOfMultiSelect(1, null));
        assertEquals(errMsgNullInput, exp.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.constrainedProductOfMultiSelect(3, Collections.emptyList());

        //-ve quantity
        var exp2 = assertThrows(IllegalArgumentException.class, ()->
                cartesianProduct.constrainedProductOfMultiSelect(-1, Collections.emptyList()));
        assertEquals("quantity must be ≥ 0", exp2.getMessage());
    }

    @Test
    void constrainedProductOfInRange() {
        //null input
        var exp1 = assertThrows(NullPointerException.class, ()->
                cartesianProduct.constrainedProductOfInRange(1, 2, null));
        assertEquals(errMsgNullInput, exp1.getMessage());

        //invalid range
        var exp2 = assertThrows(IllegalArgumentException.class, ()->
                cartesianProduct.constrainedProductOfInRange(-1, 5, A_B));
        assertEquals("Invalid range: from=-1, to=5 - must satisfy 0 ≤ from ≤ to", exp2.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.constrainedProductOfInRange(0, 0, Collections.emptyList());
    }

    @Test
    void testConstrainedProductOfDistinct() {
        //null input
        var exp1 = assertThrows(NullPointerException.class, () ->
                cartesianProduct.constrainedProductOfDistinct(1, null));
        assertEquals(errMsgNullInput, exp1.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.constrainedProductOfDistinct(5,  Collections.emptyList());

        //-ve quantity
        var exp2 = assertThrows(IllegalArgumentException.class, ()->
                cartesianProduct.constrainedProductOfDistinct(-1, Collections.emptyList()));
        assertEquals("quantity must be ≥ 0", exp2.getMessage());
    }

    @Test
    void testConstrainedProductOfMultiSelect() {
        //null input
        var exp1 = assertThrows(NullPointerException.class, () ->
                cartesianProduct.constrainedProductOfMultiSelect(1, null));
        assertEquals(errMsgNullInput, exp1.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.constrainedProductOfMultiSelect(0, Collections.emptyList());

        //-ve quantity
        var exp2 = assertThrows(IllegalArgumentException.class, ()->
                cartesianProduct.constrainedProductOfMultiSelect(-1, Collections.emptyList()));
        assertEquals("quantity must be ≥ 0", exp2.getMessage());
    }

    @Test
    void testConstrainedProductOfInRange() {
        //null input
        var exp1 = assertThrows(NullPointerException.class, () ->
                cartesianProduct.constrainedProductOfInRange(1, 2, null));
        assertEquals(errMsgNullInput, exp1.getMessage());

        //invalid range
        var exp2 = assertThrows(IllegalArgumentException.class, () ->
                cartesianProduct.constrainedProductOfInRange(1, 10, A_B_C));
        //Invalid range: from, to should be in range [0,3)
        assertEquals("Invalid range: to cannot exceed 3", exp2.getMessage());

        //empty input: Empty list is allowed and will be treated as empty-set(∅)
        cartesianProduct.constrainedProductOfInRange(0, 0, Collections.emptyList());
    }
}
