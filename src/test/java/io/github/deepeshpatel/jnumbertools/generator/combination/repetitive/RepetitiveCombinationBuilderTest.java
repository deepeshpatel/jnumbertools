package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class RepetitiveCombinationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Repetitive Combination Builder Validation Rules
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

    // ---- Rules from ValidationRules.md: isEmpty, null elements, snapshot, immutability ----

    @Test
    void isEmpty() {
        assertFalse(combinations.repetitive(0, Collections.emptyList()).isEmpty()); // by convention 1 -> [[]]
        assertTrue(combinations.repetitive(2, Collections.emptyList()).isEmpty());   // n = 0, r > 0 -> 0
        assertFalse(combinations.repetitive(0, elements).isEmpty());                 // r = 0 -> 1 -> [[]]
        assertFalse(combinations.repetitive(2, elements).isEmpty());
        assertFalse(combinations.repetitive(7, elements).isEmpty());                 // r may exceed n with repetition

        // isEmpty() must agree with count() == 0 for every (n, r)
        for (int n = 0; n <= 4; n++) {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(i);
            for (int r = 0; r <= 6; r++) {
                var b = combinations.repetitive(r, list);
                assertEquals(b.count().signum() == 0, b.isEmpty(), "n=" + n + ", r=" + r);
            }
        }
    }

    @Test
    void builderTakesSnapshotOfElements() {
        List<String> live = new ArrayList<>(List.of("A", "B", "C", "D"));
        var snapshotBuilder = combinations.repetitive(2, live);
        BigInteger countBefore = snapshotBuilder.count();
        List<List<String>> lexBefore = snapshotBuilder.lexOrder().stream().toList();

        live.add("E");
        live.set(0, "Z");

        assertEquals(countBefore, snapshotBuilder.count());
        assertEquals(lexBefore, snapshotBuilder.lexOrder().stream().toList());
    }

    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        List<String> withNull = new ArrayList<>(Arrays.asList("A", null, "C"));

        // thrown at creation, not later during iteration
        assertThrows(NullPointerException.class, () -> combinations.repetitive(2, withNull));
    }

    @Test
    void resultsAreImmutable() {
        // r < n, r == n (once returned the builder's own list), r == 0, and r > n (nothing to return)
        for (int r : new int[]{2, 4, 0, 5}) {
            var b = combinations.repetitive(r, elements);
            List<List<String>> all = new ArrayList<>();
            b.lexOrder().forEach(all::add);
            if (!b.isEmpty()) {
                b.lexOrderMth(2, 0).forEach(all::add);
                b.byRanks(List.of(BigInteger.ZERO)).forEach(all::add);
                b.sample(1, new Random(1)).forEach(all::add);
                b.choice(2, new Random(1)).forEach(all::add);
                assertFalse(all.isEmpty(), "r=" + r + " produced no results to check");
            }
            for (List<String> combination : all) {
                assertThrows(UnsupportedOperationException.class, () -> combination.add("Z"), "r=" + r);
            }
        }
    }
}
