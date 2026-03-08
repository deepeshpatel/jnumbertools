package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Subsets;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

class SubsetBuilderTest {

    /*
    -------------------------------------------------------------------------------
    SUBSETS (Power Set) - Builder Validation Rules
    -------------------------------------------------------------------------------
    Method        | Input Validation                                  | Throws
    --------------|---------------------------------------------------|------------------
    all()         | none                                              | -
    inRange()     | from < 0                                          | IllegalArgumentException
                  | to < from                                         | IllegalArgumentException
                  | to > elements.size() (non-empty list)            | IllegalArgumentException
                  | from > 0 for empty list                           | IllegalArgumentException
    lexOrder()    | called without range specified                    | IllegalArgumentException
    lexOrderMth() | m ≤ 0                                             | IllegalArgumentException
                  | start < 0                                         | IllegalArgumentException
                  | start ≥ count()                                   | IllegalArgumentException
    byRanks()     | null ranks                                        | IllegalArgumentException
    choice()      | sampleSize < 0                                    | IllegalArgumentException
    sample()      | sampleSize < 0                                    | IllegalArgumentException
                  | sampleSize > count()                              | IllegalArgumentException
    */

    private final Subsets subsets = new Subsets(calculator);
    private final List<String> elements = List.of("A", "B", "C");

    @Test
    void all() {
        var builder = subsets.of(elements);

        // Should return new instance
        var allBuilder = builder.all();
        assertNotSame(builder, allBuilder);

        // Should configure range [0, n]
        assertEquals(BigInteger.valueOf(8), allBuilder.count());

        // Can be called multiple times
        var allBuilder2 = builder.all();
        assertNotSame(allBuilder, allBuilder2);
    }

    @Test
    void inRange() {
        var builder = subsets.of(elements);

        // Valid ranges - should not throw
        assertDoesNotThrow(() -> builder.inRange(0, 0));
        assertDoesNotThrow(() -> builder.inRange(0, 2));
        assertDoesNotThrow(() -> builder.inRange(1, 2));
        assertDoesNotThrow(() -> builder.inRange(2, 2));
        assertDoesNotThrow(() -> builder.inRange(0, 3));
        assertDoesNotThrow(() -> builder.inRange(3, 3));

        // Should return new instance
        var rangeBuilder = builder.inRange(1, 2);
        assertNotSame(builder, rangeBuilder);

        // Should calculate correct count
        assertEquals(BigInteger.valueOf(6), rangeBuilder.count()); // C(3,1)+C(3,2)=3+3=6

        // Invalid: from < 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.inRange(-1, 2));

        // Invalid: to < from
        assertThrows(IllegalArgumentException.class, () ->
                builder.inRange(3, 1));

        // Invalid: to > elements.size()
        assertThrows(IllegalArgumentException.class, () ->
                builder.inRange(1, 5));

        // Invalid: from > elements.size()
        assertThrows(IllegalArgumentException.class, () ->
                builder.inRange(4, 4));

        // Empty list special cases - all mathematically valid
        var emptyBuilder = subsets.of(Collections.emptyList());

        // [0,0] - valid, returns empty-set(∅)
        assertDoesNotThrow(() -> emptyBuilder.inRange(0, 0));
        assertEquals(BigInteger.ONE, emptyBuilder.inRange(0, 0).count());

        // [0,2] - valid, returns empty-set(∅)
        assertDoesNotThrow(() -> emptyBuilder.inRange(0, 2));
        assertEquals(BigInteger.ONE, emptyBuilder.inRange(0, 2).count());

        // [1,2] - valid mathematically (returns no subsets)
        assertDoesNotThrow(() -> emptyBuilder.inRange(1, 2));
        assertEquals(BigInteger.ZERO, emptyBuilder.inRange(1, 2).count());
    }

    @Test
    void lexOrder() {
        var builder = subsets.of(elements);

        // Should throw if range not specified
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrder());

        // Should return generator when range is specified
        var rangeBuilder = builder.inRange(1, 2);
        assertNotNull(rangeBuilder.lexOrder());

        // Can be called multiple times
        var gen1 = rangeBuilder.lexOrder();
        var gen2 = rangeBuilder.lexOrder();
        assertNotSame(gen1, gen2);
    }

    @Test
    void lexOrderMth() {
        var builder = subsets.of(elements).inRange(1, 2);
        BigInteger total = builder.count(); // 6

        // Valid parameters
        assertNotNull(builder.lexOrderMth(2, 1));

        // Invalid: m <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(0, 1));
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(-1, 1));

        // Invalid: start < 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(1, -1));

        // Invalid: start >= count
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(1, total.intValue()));
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(1, total.intValue() + 1));
    }

    @Test
    void byRanks() {
        var builder = subsets.of(elements).inRange(1, 2);

        // Valid ranks
        var ranks = List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(2));
        assertNotNull(builder.byRanks(ranks));

        // Empty ranks allowed
        assertNotNull(builder.byRanks(Collections.emptyList()));

        // Null ranks
        assertThrows(IllegalArgumentException.class, () ->
                builder.byRanks(null));

        // Should throw if called without range
        var unconfiguredBuilder = subsets.of(elements);
        assertThrows(IllegalStateException.class, () ->
                unconfiguredBuilder.byRanks(ranks));
    }

    @Test
    void choice() {
        var builder = subsets.of(elements).inRange(1, 2);

        // Valid sample size
        assertNotNull(builder.choice(3));

        // Invalid: negative sample size
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(-1));

        // Note: sampleSize=0? Check your implementation
        // assertThrows(IllegalArgumentException.class, () -> builder.choice(0));
    }

    @Test
    void sample() {
        var builder = subsets.of(elements).inRange(1, 2);
        BigInteger total = builder.count(); // 6

        // Valid sample size
        assertNotNull(builder.sample(3));

        // Invalid: negative sample size
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(-1));

        // Invalid: sample size > total
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(total.intValue() + 1));
    }

    @Test
    void count() {
        var builder = subsets.of(elements);

        // Before range specified - should return 0? (check your implementation)
        // assertEquals(BigInteger.ZERO, builder.count());

        // Full power set
        var allBuilder = builder.all();
        assertEquals(BigInteger.valueOf(8), allBuilder.count());

        // Range [1,2]
        var rangeBuilder = builder.inRange(1, 2);
        assertEquals(BigInteger.valueOf(6), rangeBuilder.count());

        // Empty list with [0,0]
        var emptyZeroBuilder = subsets.of(Collections.emptyList()).inRange(0, 0);
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // Empty list with [0,2]
        var emptyRangeBuilder = subsets.of(Collections.emptyList()).inRange(0, 2);
        assertEquals(BigInteger.ONE, emptyRangeBuilder.count());

        // Empty list with invalid range - throws, so count not tested
    }

    @Test
    void isEmpty_forEmptyListWithRangeZeroZero_shouldReturnFalse() {
        // Empty list with range [0,0] should produce one element: [[]]
        // Therefore isEmpty() should return false
        var builder = subsets.of(Collections.emptyList()).inRange(0, 0);

        assertEquals(BigInteger.ONE, builder.count(), "Count should be 1");
        assertFalse(builder.isEmpty(), "Builder should not be empty as it produces [[]]");

        var result = builder.lexOrder().stream().toList();
        assertEquals(1, result.size(), "Should produce one element");
        assertEquals(List.of(), result.get(0), "That element should be empty list");
    }

    //TODO: add only if we keep isEmpty method in builders
//    @Test
//    void isEmpty() {
//        // Non-empty builder with valid range
//        var builder = subsets.of(elements).inRange(1, 2);
//        assertFalse(builder.isEmpty());
//
//        // Builder that will produce one element ([[]])
//        var emptyZeroBuilder = subsets.of(Collections.emptyList()).inRange(0, 0);
//        assertFalse(emptyZeroBuilder.isEmpty()); // Actually produces 1 element
//
//        // Builder that produces zero elements
//        var zeroBuilder = subsets.of(Collections.emptyList()).inRange(1, 2);
//        assertTrue(zeroBuilder.isEmpty());
//
//        // Unconfigured builder
//        var unconfigured = subsets.of(elements);
//        assertTrue(unconfigured.isEmpty()); // Should be empty until range specified
//    }
}