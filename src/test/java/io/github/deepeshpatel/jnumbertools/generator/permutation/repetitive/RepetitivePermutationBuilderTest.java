package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

class RepetitivePermutationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Repetitive Permutation Builder Validation Rules
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

    private final Permutations permutations = new Permutations(calculator);
    private final List<String> elements = List.of("A", "B");
    private final RepetitivePermutationBuilder<String> builder = permutations.repetitive(3, elements);

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
        // Normal case: n=2, r=3 -> 2³ = 8
        assertEquals(BigInteger.valueOf(8), builder.count());

        // r = 0 -> n⁰ = 1
        var zeroWidthBuilder = permutations.repetitive(0, elements);
        assertEquals(BigInteger.ONE, zeroWidthBuilder.count());

        // n = 0, r = 0 -> 0⁰ = 1
        var emptyZeroBuilder = permutations.repetitive(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // n = 0, r > 0 -> 0ʳ = 0
        var emptyPositiveBuilder = permutations.repetitive(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());

        // Single element
        var singleBuilder = permutations.repetitive(3, "X");
        assertEquals(BigInteger.ONE, singleBuilder.count()); // 1³ = 1
    }
}