package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class MultisetCombinationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Multiset Combination Builder Validation Rules
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
    private final LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
    private final MultisetCombinationBuilder<String> builder;

    {
        options.put("A", 3);
        options.put("B", 2);
        options.put("C", 1);
        builder = combinations.multiset(options, 2);
    }

    @Test
    void lexOrder() {
        assertNotNull(builder.lexOrder());
        var gen1 = builder.lexOrder();
        var gen2 = builder.lexOrder();
        assertNotSame(gen1, gen2);
    }

    @Test
    void lexOrderMth() {
        // Valid parameters should not throw
        assertNotNull(builder.lexOrderMth(BigInteger.valueOf(2), BigInteger.ONE));

        // m <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ZERO, BigInteger.ONE));
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.valueOf(-1), BigInteger.ONE));

        // start < 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ONE, BigInteger.valueOf(-1)));

        // start >= count
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ONE, builder.count()));
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ONE, builder.count().add(BigInteger.ONE)));
    }

    @Test
    void byRanks() {
        // Valid ranks
        var ranks = List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(2));
        assertNotNull(builder.byRanks(ranks));

        // Empty ranks allowed
        assertNotNull(builder.byRanks(Collections.emptyList()));

        // Null ranks
        assertThrows(IllegalArgumentException.class, () ->
                builder.byRanks(null));
    }

    @Test
    void choice() {
        // Valid sample size
        assertNotNull(builder.choice(3, TestBase.random));

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(-1, TestBase.random));

        // Note: sampleSize > total is allowed for choice (with replacement)
    }

    @Test
    void sample() {
        // Valid sample size
        assertNotNull(builder.sample(3, TestBase.random));

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(-1, TestBase.random));

        // sampleSize > total
        int tooLarge = builder.count().intValue() + 1;
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(tooLarge, TestBase.random));
    }

    @Test
    void count() {
        // Normal case: {A=3, B=2, C=1} with r=2
        assertTrue(builder.count().compareTo(BigInteger.ZERO) > 0);

        // r = 0 -> always 1
        var zeroRBuilder = combinations.multiset(options, 0);
        assertEquals(BigInteger.ONE, zeroRBuilder.count());

        // Empty map, r = 0 -> 1
        var emptyZeroBuilder = combinations.multiset(new LinkedHashMap<>(), 0);
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // Empty map, r > 0 -> 0
        var emptyPositiveBuilder = combinations.multiset(new LinkedHashMap<>(), 2);
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());

        // r > total available -> 0
        var greaterRBuilder = combinations.multiset(options, 10);
        assertEquals(BigInteger.ZERO, greaterRBuilder.count());

        // Single element with frequency
        var singleOptions = new LinkedHashMap<String, Integer>();
        singleOptions.put("X", 5);
        var singleBuilder = combinations.multiset(singleOptions, 3);
        assertEquals(BigInteger.ONE, singleBuilder.count()); // Only one way to choose 3 from 5 identical items
    }

    // ---- Rules from ValidationRules.md: isEmpty, null elements, snapshot, zero frequencies, immutability ----

    @Test
    void isEmpty() {
        assertFalse(combinations.multiset(new LinkedHashMap<String, Integer>(), 0).isEmpty()); // one empty multiset
        assertTrue(combinations.multiset(new LinkedHashMap<String, Integer>(), 2).isEmpty());  // nothing to select from
        assertFalse(combinations.multiset(options, 0).isEmpty());                              // [{}]
        assertFalse(combinations.multiset(options, 6).isEmpty());                              // 3 + 2 + 1 available
        assertTrue(combinations.multiset(options, 7).isEmpty());                               // r > sum of frequencies

        var zeroOnly = new LinkedHashMap<String, Integer>();
        zeroOnly.put("A", 0);                                                                  // zero frequency = absent
        assertTrue(combinations.multiset(zeroOnly, 1).isEmpty());
        assertFalse(combinations.multiset(zeroOnly, 0).isEmpty());

        // isEmpty() must agree with count() == 0 for every r
        for (int r = 0; r <= 8; r++) {
            var b = combinations.multiset(options, r);
            assertEquals(b.count().signum() == 0, b.isEmpty(), "r=" + r);
        }
    }

    @Test
    void builderTakesSnapshotOfOptions() {
        var live = new LinkedHashMap<String, Integer>();
        live.put("a", 2);
        live.put("b", 2);
        var snapshotBuilder = combinations.multiset(live, 2);
        BigInteger countBefore = snapshotBuilder.count();
        List<Map<String, Integer>> lexBefore = snapshotBuilder.lexOrder().stream().toList();

        live.put("a", 1);   // change an existing frequency
        live.put("c", 3);   // add a new element

        assertEquals(countBefore, snapshotBuilder.count());
        assertEquals(lexBefore, snapshotBuilder.lexOrder().stream().toList());
    }

    @Test
    void nullKeysAndNullFrequenciesAreRejectedWhenBuilderIsCreated() {
        var nullKey = new LinkedHashMap<String, Integer>();
        nullKey.put(null, 2);
        assertThrows(NullPointerException.class, () -> combinations.multiset(nullKey, 1));

        var nullFrequency = new LinkedHashMap<String, Integer>();
        nullFrequency.put("A", null);
        assertThrows(NullPointerException.class, () -> combinations.multiset(nullFrequency, 1));
    }

    @Test
    void zeroFrequencyIsTreatedAsAbsent() {
        var withZero = new LinkedHashMap<String, Integer>();
        withZero.put("A", 0);
        withZero.put("B", 2);
        var withoutZero = new LinkedHashMap<String, Integer>();
        withoutZero.put("B", 2);

        for (int r = 0; r <= 3; r++) {
            var a = combinations.multiset(withZero, r);
            var b = combinations.multiset(withoutZero, r);
            assertEquals(b.count(), a.count(), "r=" + r);
            assertEquals(b.lexOrder().stream().toList(), a.lexOrder().stream().toList(), "r=" + r);
        }
    }

    @Test
    void resultsAreImmutable() {
        for (int r : new int[]{0, 1, 3, 6, 7}) {   // r = 7 exceeds the 6 available: nothing to return
            var b = combinations.multiset(options, r);
            List<Map<String, Integer>> all = new ArrayList<>();
            b.lexOrder().forEach(all::add);
            if (!b.isEmpty()) {
                b.lexOrderMth(2, 0).forEach(all::add);
                b.byRanks(List.of(BigInteger.ZERO)).forEach(all::add);
                b.sample(1, new Random(1)).forEach(all::add);
                b.choice(2, new Random(1)).forEach(all::add);
                assertFalse(all.isEmpty(), "r=" + r + " produced no results to check");
            }
            for (Map<String, Integer> combination : all) {
                assertThrows(UnsupportedOperationException.class, () -> combination.put("Z", 1), "r=" + r);
            }
        }
    }

    @Test
    void resultsAreImmutable_forLargeR() {
        // r >= 1000 is generated by a different iterator (frequency-vector based) than small r
        var big = new LinkedHashMap<String, Integer>();
        big.put("A", 1000);
        big.put("B", 1000);

        List<Map<String, Integer>> firstFew = combinations.multiset(big, 1000).lexOrder().stream().limit(3).toList();

        assertEquals(3, firstFew.size());
        for (Map<String, Integer> combination : firstFew) {
            assertThrows(UnsupportedOperationException.class, () -> combination.put("Z", 1));
        }
    }
}
