package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

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
        var ranks = java.util.List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(2));
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
        assertNotNull(builder.choice(3));

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(0));
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(-1));

        // Note: sampleSize > total is allowed for choice (with replacement)
    }

    @Test
    void sample() {
        // Valid sample size
        assertNotNull(builder.sample(3));

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(0));
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(-1));

        // sampleSize > total
        int tooLarge = builder.count().intValue() + 1;
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(tooLarge));
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
}