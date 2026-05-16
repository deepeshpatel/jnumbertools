package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.base.Derangements;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

class DerangementBuilderTest {

    private final Derangements derangements = new Derangements(calculator);
    // n=4 → D_4 = 9: large enough for BuilderTestHelper (needs count ≥ a few).
    private final List<String> elements = List.of("A", "B", "C", "D");
    private final DerangementBuilder<String> builder = derangements.of(elements);

    @Test
    void lexOrder() {
        BuilderTestHelper.testLexOrder(builder);
    }

    @Test
    void lexOrderMth() {
        BuilderTestHelper.testLexOrderMth(builder);
    }

    @Test
    void byRanks() {
        BuilderTestHelper.testByRanks(builder);
    }

    @Test
    void choice() {
        BuilderTestHelper.testChoice(builder);
    }

    @Test
    void sample() {
        BuilderTestHelper.testSample(builder);
    }

    @Test
    void count() {
        // !4 = 9
        assertEquals(BigInteger.valueOf(9), builder.count());

        // Edge cases: !0 = 1, !1 = 0
        assertEquals(BigInteger.ONE,  derangements.of(List.of()).count());
        assertEquals(BigInteger.ZERO, derangements.of(List.of("X")).count());
    }

    @Test
    void isEmpty() {
        assertFalse(builder.isEmpty());
        assertTrue(derangements.of(List.of()).isEmpty());
    }

    @Test
    void toStringContainsCount() {
        String s = builder.toString();
        assertTrue(s.contains("DerangementBuilder"));
        assertTrue(s.contains("count=9"));
    }
}

