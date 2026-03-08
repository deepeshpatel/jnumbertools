package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.LinkedHashMap;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultisetPermutationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Multiset Permutation Builder Validation Rules
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
    private final LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
    private final MultisetPermutationBuilder<String> builder;

    {
        options.put("A", 2);
        options.put("B", 1);
        options.put("C", 1);
        builder = permutations.multiset(options);
    }

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
        // Normal case: {A=2, B=1, C=1} -> 4!/(2!·1!·1!) = 12
        assertEquals(BigInteger.valueOf(12), builder.count());

        // Single element with frequency 3 -> 3!/3! = 1
        var singleOptions = new LinkedHashMap<String, Integer>();
        singleOptions.put("X", 3);
        var singleBuilder = permutations.multiset(singleOptions);
        assertEquals(BigInteger.ONE, singleBuilder.count());

        // Empty map -> 0! = 1
        var emptyBuilder = permutations.multiset(new LinkedHashMap<>());
        assertEquals(BigInteger.ONE, emptyBuilder.count());

        // Map with zero frequencies (filtered out) -> treated as empty
        var zeroFreqOptions = new LinkedHashMap<String, Integer>();
        zeroFreqOptions.put("A", 0);
        zeroFreqOptions.put("B", 0);
        var zeroFreqBuilder = permutations.multiset(zeroFreqOptions);
        assertEquals(BigInteger.ONE, zeroFreqBuilder.count());
    }
}