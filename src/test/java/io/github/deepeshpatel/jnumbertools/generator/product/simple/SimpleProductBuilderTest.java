package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static io.github.deepeshpatel.jnumbertools.TestBase.errMsgNullInput;
import static org.junit.jupiter.api.Assertions.*;

class SimpleProductBuilderTest {

    private final List<String> setA = List.of("A", "B");
    private final List<Integer> setB = List.of(1, 2, 3);

    @Test
    void and() {
        var builder = cartesianProduct.simpleProductOf(setA);

        // Test adding dimension
        var builderWithB = builder.and(setB);
        assertNotSame(builder, builderWithB);
        assertEquals(BigInteger.valueOf(6), builderWithB.count());

        // Test null input
        var exp = assertThrows(NullPointerException.class, ()-> builder.and((List<?>) null));
        assertEquals(errMsgNullInput, exp.getMessage());

        // Test empty list - A × ∅ = ∅, so count should be 0
        var builderWithEmpty = builder.and(Collections.emptyList());
        assertEquals(BigInteger.ZERO, builderWithEmpty.count());  // Changed from ONE to ZERO
    }

    @Test
    void lexOrder() {
        var builder = cartesianProduct.simpleProductOf(setA);

        // Test returns generator (not null)
        assertNotNull(builder.lexOrder());

        // Test can be called multiple times
        var gen1 = builder.lexOrder();
        var gen2 = builder.lexOrder();
        assertNotSame(gen1, gen2);
    }

    @Test
    void lexOrderMth() {
        var builder = cartesianProduct.simpleProductOf(setA).and(setB);

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
        var builder = cartesianProduct.simpleProductOf(setA).and(setB);

        // Test returns generator for valid ranks
        var ranks = List.of(BigInteger.ZERO, BigInteger.ONE);
        assertNotNull(builder.byRanks(ranks));

        // Test null ranks throws
        assertThrows(IllegalArgumentException.class, () -> builder.byRanks(null));

        // Test empty ranks is allowed
        assertNotNull(builder.byRanks(Collections.emptyList()));
    }

    @Test
    void choice() {
        var builder = cartesianProduct.simpleProductOf(setA).and(setB);

        // Test returns generator for valid sample size
        assertNotNull(builder.choice(5, TestBase.random));

        // Test negative sample size throws
        assertThrows(IllegalArgumentException.class, () -> builder.choice(-1, TestBase.random));

        // Test zero sample size? (check your implementation)
        assertThrows(IllegalArgumentException.class, () -> builder.choice(0, TestBase.random));
    }

    @Test
    void sample() {
        var builder = cartesianProduct.simpleProductOf(setA).and(setB);

        // Test returns generator for valid sample size
        assertNotNull(builder.sample(4, TestBase.random));

        // Test negative sample size throws
        assertThrows(IllegalArgumentException.class, () -> builder.sample(-1, TestBase.random));

        // Test sample size > count throws
        BigInteger total = builder.count();
        assertThrows(IllegalArgumentException.class, () -> builder.sample(total.intValue() + 1, TestBase.random));
    }

    @Test
    void count() {
        var builder = cartesianProduct.simpleProductOf(setA).and(setB);
        assertEquals(BigInteger.valueOf(6), builder.count());

        // A × ∅ = ∅, so count should be 0
        var builderWithEmpty = cartesianProduct.simpleProductOf(setA)
                .and(Collections.emptyList());
        assertEquals(BigInteger.ZERO, builderWithEmpty.count());  // Changed from ONE to ZERO

        var emptyListBuilder = cartesianProduct.simpleProductOf(Collections.emptyList());
        assertEquals(BigInteger.ZERO, emptyListBuilder.count());  // Since 1 empty list implies 0 results

        var nullaryBuilder = cartesianProduct.simpleProductOf();
        assertEquals(BigInteger.ONE, nullaryBuilder.count());  // 0-ary product returns 1 empty tuple
    }
}
