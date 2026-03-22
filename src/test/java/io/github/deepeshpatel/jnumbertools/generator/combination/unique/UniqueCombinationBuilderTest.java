package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.api.Combinations;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.unique.UniqueCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniqueCombinationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Unique Combination Builder Validation Rules
    -------------------------------------------------------------------------------
    Method        | Input Validation                                  | Throws
    --------------|---------------------------------------------------|------------------
    lexOrder()    | none                                              | -
    lexOrderMth() | m ≤ 0                                             | IllegalArgumentException
                  | start < 0                                         | IllegalArgumentException
                  | start ≥ count()                                   | IllegalArgumentException
    byRanks()     | null ranks                                        | IllegalArgumentException
    choice()      | sampleSize ≤ 0                                    | IllegalArgumentException
    sample()      | sampleSize ≤ 0                                    | IllegalArgumentException
                  | sampleSize > count()                              | IllegalArgumentException
    count()       | none (always valid)                               | -
    */

    private final Combinations combinations = new Combinations(calculator);
    private final List<String> elements = List.of("A", "B", "C", "D");
    private final UniqueCombinationBuilder<String> builder = combinations.unique(2, elements);

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
        // Normal case: C(4,2) = 6
        assertEquals(BigInteger.valueOf(6), builder.count());

        // r = 0 -> C(n,0) = 1
        var zeroRBuilder = combinations.unique(0, elements);
        assertEquals(BigInteger.ONE, zeroRBuilder.count());

        // r = n -> C(n,n) = 1
        var fullBuilder = combinations.unique(4, elements);
        assertEquals(BigInteger.ONE, fullBuilder.count());

        // n = 0, r = 0 -> ⁰C₀ = 1
        var emptyZeroBuilder = combinations.unique(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // n = 0, r > 0 -> ⁰Cᵣ = 0
        var emptyPositiveBuilder = combinations.unique(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());

        // r > n -> mathematically valid, count = 0
        var greaterRBuilder = combinations.unique(5, elements);
        assertEquals(BigInteger.ZERO, greaterRBuilder.count());
    }

    @Test
    void shouldReturnImmutableOuterAndInnerCollection() {
        var results = builder.lexOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X")));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));

        var first = results.get(0);
        assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
        assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
    }
}
