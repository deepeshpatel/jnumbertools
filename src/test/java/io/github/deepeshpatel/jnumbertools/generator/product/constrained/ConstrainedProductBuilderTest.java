package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.TestBase;
import io.github.deepeshpatel.jnumbertools.api.CartesianProduct;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.errMsgNullInput;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class ConstrainedProductBuilderTest {

    private final CartesianProduct cartesianProduct = new CartesianProduct(calculator);
    private final List<String> setA = of("A", "B", "C");
    private final List<Integer> setB = of(1, 2, 3, 4);

    @Test
    void andDistinct() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA);

        // Test adding distinct dimension
        var builderWithB = builder.andDistinct(2, setB);
        assertNotSame(builder, builderWithB);
        assertEquals(calculator.nCr(3,2).multiply(calculator.nCr(4,2)), builderWithB.count());

        // Test null input
        var exp = assertThrows(NullPointerException.class, () -> builder.andDistinct(2, null));
        assertEquals(errMsgNullInput, exp.getMessage());

        // Test empty list with quantity > 0 - results in zero count
        var builderWithEmpty = builder.andDistinct(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, builderWithEmpty.count());

        // Test quantity = 0 with empty list - results in one empty combination
        var builderWithZero = builder.andDistinct(0, Collections.emptyList());
        assertEquals(calculator.nCr(3,2), builderWithZero.count()); // First dimension still contributes
    }

    @Test
    void andMultiSelect() {
        var builder = cartesianProduct.constrainedProductOfMultiSelect(2, setA);

        // Test adding multi-select dimension
        var builderWithB = builder.andMultiSelect(2, setB);
        assertNotSame(builder, builderWithB);
        assertEquals(calculator.nCrRepetitive(3,2).multiply(calculator.nCrRepetitive(4,2)), builderWithB.count());

        // Test null input
        var exp = assertThrows(NullPointerException.class, () -> builder.andMultiSelect(2, null));
        assertEquals(errMsgNullInput, exp.getMessage());

        // Test empty list with quantity > 0 - results in zero count
        var builderWithEmpty = builder.andMultiSelect(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, builderWithEmpty.count());

        // Test quantity = 0 with empty list
        var builderWithZero = builder.andMultiSelect(0, Collections.emptyList());
        assertEquals(calculator.nCrRepetitive(3,2), builderWithZero.count());
    }

    @Test
    void andInRange() {
        var builder = cartesianProduct.constrainedProductOfInRange(1, 2, setA);

        // Test adding in-range dimension
        var builderWithB = builder.andInRange(1, 3, setB);
        assertNotSame(builder, builderWithB);

        BigInteger expectedFirst = calculator.totalSubsetsInRange(1, 2, 3); // C(3,1)+C(3,2)=3+3=6
        BigInteger expectedSecond = calculator.totalSubsetsInRange(1, 3, 4); // C(4,1)+C(4,2)+C(4,3)=4+6+4=14
        assertEquals(expectedFirst.multiply(expectedSecond), builderWithB.count());

        // Test null input
        var exp = assertThrows(NullPointerException.class, () -> builder.andInRange(1, 2, null));
        assertEquals(errMsgNullInput, exp.getMessage());

        // Test invalid range (from > to)
        assertThrows(IllegalArgumentException.class, () -> builder.andInRange(3, 1, setB));

        // Test range exceeding element count
        assertThrows(IllegalArgumentException.class, () -> builder.andInRange(1, 5, setB));

        // Test empty list with valid range [0,0] - should add dimension with count=1
        var builderWithEmpty = builder.andInRange(0, 0, Collections.emptyList());
        assertEquals(expectedFirst.multiply(BigInteger.ONE), builderWithEmpty.count()); // 6 × 1 = 6
    }

    @Test
    void lexOrder() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andMultiSelect(2, setB);

        // Test returns generator (not null)
        assertNotNull(builder.lexOrder());

        // Test can be called multiple times
        var gen1 = builder.lexOrder();
        var gen2 = builder.lexOrder();
        assertNotSame(gen1, gen2);
    }

    @Test
    void lexOrderMth() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andMultiSelect(2, setB);

        // Test returns generator for valid params
        assertNotNull(builder.lexOrderMth(2, 1));

        // Test fail fast for invalid params
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(0, 1));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(1, -1));

        BigInteger total = builder.count();
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(1, total.intValue() + 1));
    }

    @Test
    void byRanks() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andMultiSelect(2, setB);

        // Test returns generator for valid ranks
        var ranks = List.of(BigInteger.ZERO, BigInteger.ONE);
        assertNotNull(builder.byRanks(ranks));

        // Test null ranks throws
        assertThrows(IllegalArgumentException.class, () -> builder.byRanks(null));

        // Test empty ranks is allowed
        assertNotNull(builder.byRanks(Collections.emptyList()));
    }

    @Test
    void count() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andMultiSelect(2, setB);

        BigInteger expected = calculator.nCr(3,2).multiply(calculator.nCrRepetitive(4,2)); // 3 × 10 = 30
        assertEquals(expected, builder.count());

        // Test with empty dimension (quantity=0) - count unchanged
        var builderWithEmpty = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andDistinct(0, Collections.emptyList());
        assertEquals(calculator.nCr(3,2), builderWithEmpty.count()); // Only first dimension counts (3)

        // Test empty first dimension with quantity > 0
        var emptyFirst = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList())
                .andMultiSelect(2, setB);
        assertEquals(calculator.nCrRepetitive(4,2), emptyFirst.count()); // 1 × 10 = 10

        // Test empty input (Φ) - should have count 1
        var emptyBuilder = cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, emptyBuilder.count());

        // Test product with empty dimension (quantity>0) - should be zero
        var productWithEmpty = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andDistinct(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, productWithEmpty.count());
    }

    @Test
    void    choice() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andMultiSelect(2, setB);

        // Test returns generator for valid sample size
        assertNotNull(builder.choice(5, TestBase.random));

        // Test negative sample size throws
        assertThrows(IllegalArgumentException.class, () -> builder.choice(-1, TestBase.random));

        // Test zero sample size? (check implementation)
        // assertThrows(IllegalArgumentException.class, () -> builder.choice(0, TestBase.random));
    }

    @Test
    void sample() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, setA)
                .andMultiSelect(2, setB);

        BigInteger total = builder.count();

        // Test returns generator for valid sample size
        assertNotNull(builder.sample(5, TestBase.random));

        // Test negative sample size throws
        assertThrows(IllegalArgumentException.class, () -> builder.sample(-1, TestBase.random));

        // Test sample size > total throws
        assertThrows(IllegalArgumentException.class, () -> builder.sample(total.intValue() + 1, TestBase.random));
    }
}
