package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.api.Combinations;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepetitiveCombinationBuilderTest {

    /*
     * -----------------------------------------------------------------------------
     * --
     * Repetitive Combination Builder Validation Rules
     * -----------------------------------------------------------------------------
     * --
     * Method | Input Validation | Throws
     * --------------|---------------------------------------------------|----------
     * --------
     * lexOrder() | none | -
     * lexOrderMth() | m ≤ 0 | IllegalArgumentException
     * | start < 0 | IllegalArgumentException
     * | start ≥ count() | IllegalArgumentException
     * byRanks() | null ranks | IllegalArgumentException
     * choice() | sampleSize ≤ 0 | IllegalArgumentException
     * sample() | sampleSize ≤ 0 | IllegalArgumentException
     * | sampleSize > count() | IllegalArgumentException
     * count() | none (always valid) | -
     */

    private final Combinations combinations = new Combinations(calculator);
    private final List<String> elements = List.of("A", "B", "C");
    private final RepetitiveCombinationBuilder<String> builder = combinations.repetitive(2, elements);

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
        // Normal case: C(3+2-1,2) = C(4,2) = 6
        assertEquals(BigInteger.valueOf(6), builder.count());

        // r = 0 -> 1
        var zeroRBuilder = combinations.repetitive(0, elements);
        assertEquals(BigInteger.ONE, zeroRBuilder.count());

        // n = 0, r = 0 -> 1 (by convention)
        var emptyZeroBuilder = combinations.repetitive(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // n = 0, r > 0 -> 0
        var emptyPositiveBuilder = combinations.repetitive(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());

        // n > 0, r > 0 (always valid, no upper bound on r)
        var largeRBuilder = combinations.repetitive(10, elements);
        assertTrue(largeRBuilder.count().compareTo(BigInteger.ZERO) > 0);

        // Single element
        var singleBuilder = combinations.repetitive(3, "X");
        assertEquals(BigInteger.ONE, singleBuilder.count()); // Only one combination with repetitions
    }
}
