package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.api.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

class KPermutationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    K-Permutation Builder Validation Rules
    -------------------------------------------------------------------------------
    Method              | Input Validation                                  | Throws
    --------------------|---------------------------------------------------|------------------
    lexOrder()          | none                                              | -
    combinationOrder()  | none                                              | -
    lexOrderMth()       | m ≤ 0                                             | IllegalArgumentException
                        | start < 0                                         | IllegalArgumentException
                        | start ≥ count()                                   | IllegalArgumentException
    combinationOrderMth()| m ≤ 0                                            | IllegalArgumentException
                        | start < 0                                         | IllegalArgumentException
                        | start ≥ count()                                   | IllegalArgumentException
    byRanks()           | null ranks                                        | IllegalArgumentException
    choice()            | sampleSize ≤ 0                                    | IllegalArgumentException
    sample()            | sampleSize ≤ 0                                    | IllegalArgumentException
                        | sampleSize > count()                              | IllegalArgumentException
    count()             | none (always valid)                               | -
    */

    private final Permutations permutations = new Permutations(calculator);
    private final List<String> elements = List.of("A", "B", "C", "D");
    private final KPermutationBuilder<String> builder = permutations.nPk(2, elements);

    @Test
    void lexOrder() {
        assertNotNull(builder.lexOrder());
        var gen1 = builder.lexOrder();
        var gen2 = builder.lexOrder();
        assertNotSame(gen1, gen2);
    }

    @Test
    void combinationOrder() {
        assertNotNull(builder.combinationOrder());
        var gen1 = builder.combinationOrder();
        var gen2 = builder.combinationOrder();
        assertNotSame(gen1, gen2);
    }

    @Test
    void lexOrderMth() {
        BuilderTestHelper.testLexOrderMth(builder);
    }

    @Test
    void combinationOrderMth() {
        // Valid parameters should not throw
        assertNotNull(builder.combinationOrderMth(2, 1));

        // m <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.combinationOrderMth(0, 1));
        assertThrows(IllegalArgumentException.class, () ->
                builder.combinationOrderMth(-1, 1));

        // start < 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.combinationOrderMth(1, -1));

        // start >= count
        BigInteger total = builder.count();
        assertThrows(IllegalArgumentException.class, () ->
                builder.combinationOrderMth(1, total.intValue()));
        assertThrows(IllegalArgumentException.class, () ->
                builder.combinationOrderMth(1, total.intValue() + 1));
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
        // Normal case: n=4, k=2 -> P(4,2) = 12
        assertEquals(BigInteger.valueOf(12), builder.count());

        // k = 0 -> P(n,0) = 1
        var zeroKBuilder = permutations.nPk(0, elements);
        assertEquals(BigInteger.ONE, zeroKBuilder.count());

        // k = n -> P(n,n) = n!
        var fullBuilder = permutations.nPk(4, elements);
        assertEquals(BigInteger.valueOf(24), fullBuilder.count());

        // Empty list with k=0 -> ⁰P₀ = 1
        var emptyZeroBuilder = permutations.nPk(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // Empty list with k>0 -> ⁰Pₖ = 0
        var emptyPositiveBuilder = permutations.nPk(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());

        // k > n -> mathematically valid, count = 0 (tested in generator)
        var greaterKBuilder = permutations.nPk(5, elements);
        assertEquals(BigInteger.ZERO, greaterKBuilder.count());
    }
}