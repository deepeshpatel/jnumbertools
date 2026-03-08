package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

class UniquePermutationBuilderTest {

    private final Permutations permutations = new Permutations(calculator);
    private final List<String> elements = List.of("A", "B", "C");
    private final UniquePermutationBuilder<String> builder = permutations.unique(elements);

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
        // Builder-specific count tests
        assertEquals(BigInteger.valueOf(6), builder.count());

        var emptyBuilder = permutations.unique(List.of());
        assertEquals(BigInteger.ONE, emptyBuilder.count());
    }
}