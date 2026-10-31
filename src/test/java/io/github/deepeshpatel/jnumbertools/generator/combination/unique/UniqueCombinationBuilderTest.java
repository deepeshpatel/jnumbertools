package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

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

    // ---- Rules from ValidationRules.md: isEmpty, null elements, snapshot, immutability ----

    @Test
    void isEmpty() {
        assertFalse(combinations.unique(0, Collections.emptyList()).isEmpty()); // 0C0 = 1 -> [[]]
        assertTrue(combinations.unique(2, Collections.emptyList()).isEmpty());   // 0Cr = 0
        assertFalse(combinations.unique(0, elements).isEmpty());                 // nC0 = 1 -> [[]]
        assertFalse(combinations.unique(2, elements).isEmpty());
        assertFalse(combinations.unique(4, elements).isEmpty());                 // nCn = 1
        assertTrue(combinations.unique(5, elements).isEmpty());                  // r > n -> 0

        // isEmpty() must agree with count() == 0 for every (n, r)
        for (int n = 0; n <= 4; n++) {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(i);
            for (int r = 0; r <= 6; r++) {
                var b = combinations.unique(r, list);
                assertEquals(b.count().signum() == 0, b.isEmpty(), "n=" + n + ", r=" + r);
            }
        }
    }

    @Test
    void builderTakesSnapshotOfElements() {
        List<String> live = new ArrayList<>(List.of("A", "B", "C", "D"));
        var snapshotBuilder = combinations.unique(2, live);
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
        assertThrows(NullPointerException.class, () -> combinations.unique(2, withNull));
    }

    @Test
    void resultsAreImmutable() {
        // r < n, r == n (once returned the builder's own list), r == 0, and r > n (nothing to return)
        for (int r : new int[]{2, 4, 0, 5}) {
            var b = combinations.unique(r, elements);
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
