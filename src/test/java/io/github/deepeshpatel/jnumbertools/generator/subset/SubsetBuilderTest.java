package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

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
    lexOrderMth() | m ≤ 0                                             | IllegalArgumentException
                  | start < 0                                         | IllegalArgumentException
                  | start ≥ count() (count > 0)                       | IllegalArgumentException
                  | count() = 0 and start ≥ 0                         | accepted, yields nothing
    byRanks()     | null ranks                                        | IllegalArgumentException
                  | rank < 0 or ≥ count()                             | IllegalArgumentException (lazily, on iteration)
    choice()      | sampleSize ≤ 0                                    | IllegalArgumentException
                  | count() = 0                                       | IllegalArgumentException
                  | sampleSize > count() (count > 0)                  | accepted (with replacement)
                  | null random                                       | NullPointerException
    sample()      | sampleSize ≤ 0                                    | IllegalArgumentException
                  | sampleSize > count() (incl. count() = 0)          | IllegalArgumentException
                  | null random                                       | NullPointerException
    constructor   | null list / null element                          | NullPointerException
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

        // The selector has no lexOrder(): a size range must be chosen first (checked by the compiler).
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
    }

    @Test
    void choice() {
        var builder = subsets.of(elements).inRange(1, 2);

        // Valid sample size
        assertNotNull(builder.choice(3, TestBase.random));

        // Invalid: negative sample size
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(-1, TestBase.random));

        // Note: sampleSize=0? Check your implementation
        // assertThrows(IllegalArgumentException.class, () -> builder.choice(0, TestBase.random));
    }

    @Test
    void sample() {
        var builder = subsets.of(elements).inRange(1, 2);
        BigInteger total = builder.count(); // 6

        // Valid sample size
        assertNotNull(builder.sample(3, TestBase.random));

        // Invalid: negative sample size
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(-1, TestBase.random));

        // Invalid: sample size > total
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(total.intValue() + 1, TestBase.random));
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


    @Test
    void isEmpty() {
        // Non-empty builder with valid range
        var builder = subsets.of(elements).inRange(1, 2);
        assertFalse(builder.isEmpty());

        // Builder that will produce one element ([[]])
        var emptyZeroBuilder = subsets.of(Collections.emptyList()).inRange(0, 0);
        assertFalse(emptyZeroBuilder.isEmpty()); // Actually produces 1 element

        // Builder that produces zero elements
        var zeroBuilder = subsets.of(Collections.emptyList()).inRange(1, 2);
        assertTrue(zeroBuilder.isEmpty());
    }

    // ---- Rules from ValidationRules.md: isEmpty, null input, snapshot, immutability, sampling, traversal ----

    @Test
    void isEmptyIffCountIsZero() {
        // isEmpty() must agree with count() == 0 for every (n, from, to)
        for (int n = 0; n <= 4; n++) {
            List<Integer> list = IntStream.range(0, n).boxed().toList();
            for (int from = 0; from <= 5; from++) {
                for (int to = from; to <= 5; to++) {
                    if (n > 0 && to > n) continue; // rejected by inRange(), see inRange()
                    var b = subsets.of(list).inRange(from, to);
                    assertEquals(b.count().signum() == 0, b.isEmpty(),
                            "n=" + n + ", range [" + from + "," + to + "]");
                }
            }
        }

        // a builder whose only result is the empty subset ([[]]) is not empty
        assertFalse(subsets.of(elements).inRange(0, 0).isEmpty());
        assertFalse(subsets.of(Collections.emptyList()).all().isEmpty());
        // ... but no subsets at all is
        assertTrue(subsets.of(Collections.emptyList()).inRange(1, 3).isEmpty());
    }

    @Test
    void nullInputThrowsNullInputError() {
        assertThrows(NullPointerException.class, () -> new SubsetSelector<String>(null, calculator));
        assertThrows(NullPointerException.class, () -> subsets.of((List<String>) null));
    }

    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        List<String> withNull = new ArrayList<>(Arrays.asList("A", null, "C"));

        // thrown at creation, not later during iteration
        assertThrows(NullPointerException.class, () -> subsets.of(withNull));
        assertThrows(NullPointerException.class, () -> new SubsetSelector<>(withNull, calculator));
    }

    @Test
    void builderTakesSnapshotOfElements() {
        List<String> live = new ArrayList<>(List.of("A", "B", "C"));
        var builder = subsets.of(live).inRange(1, 2);

        BigInteger countBefore = builder.count();
        var lexBefore = builder.lexOrder().stream().toList();
        var mthBefore = builder.lexOrderMth(2, 1).stream().toList();
        var ranksBefore = builder.byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(4))).stream().toList();

        // generators created before the change must be unaffected as well
        var generatorCreatedEarlier = builder.lexOrder();
        var byRanksCreatedEarlier = builder.byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(4)));

        live.add("D");
        live.set(0, "Z");

        assertEquals(countBefore, builder.count());
        assertEquals(lexBefore, builder.lexOrder().stream().toList());
        assertEquals(mthBefore, builder.lexOrderMth(2, 1).stream().toList());
        assertEquals(ranksBefore, builder.byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(4))).stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
        assertEquals(ranksBefore, byRanksCreatedEarlier.stream().toList());
    }

    @Test
    void resultsAreImmutable() {
        var builder = subsets.of(List.of('A', 'B', 'C')).all();
        List<List<Character>> all = new ArrayList<>();

        builder.lexOrder().forEach(all::add);                       // includes [] and the whole set
        builder.lexOrderMth(2, 0).forEach(all::add);
        all.add(builder.lexOrderMth(BigInteger.ONE, BigInteger.valueOf(5)).build());
        builder.byRanks(List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(7))).forEach(all::add);
        builder.sample(3, new Random(1)).forEach(all::add);
        builder.choice(3, new Random(1)).forEach(all::add);

        assertFalse(all.isEmpty());
        for (List<Character> subset : all) {
            assertThrows(UnsupportedOperationException.class, () -> subset.add('Z'), "subset=" + subset);
        }
    }

    @Test
    void choiceAndSampleRules() {
        var builder = subsets.of(elements).inRange(1, 2);                 // count = 6
        var emptyDomain = subsets.of(Collections.emptyList()).inRange(1, 2); // count = 0

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () -> builder.choice(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> builder.choice(-1, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(-1, TestBase.random));

        // sample is without replacement: s > count is invalid, s == count is fine
        assertThrows(IllegalArgumentException.class, () -> builder.sample(7, TestBase.random));
        var everything = builder.sample(6, new Random(1)).stream().toList();
        assertEquals(6, new HashSet<>(everything).size());

        // choice is with replacement: s > count is accepted
        assertEquals(20, builder.choice(20, new Random(1)).stream().count());

        // nothing can be drawn from an empty domain
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.choice(1, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.sample(1, TestBase.random));

        // null random source
        assertThrows(NullPointerException.class, () -> builder.choice(1, null));
        assertThrows(NullPointerException.class, () -> builder.sample(1, null));

        // count = 1 and the only element is the empty list: choice yields s empty results
        var onlyEmptySubset = subsets.of(Collections.emptyList()).all();
        assertEquals(List.of(List.of(), List.of(), List.of()),
                onlyEmptySubset.choice(3, new Random(1)).stream().toList());
    }

    @Test
    void samplingAndRanksWorkBeyondLongRange() {
        var big = subsets.of(100).all();                         // 2^100 subsets, far beyond a long
        BigInteger count = BigInteger.TWO.pow(100);
        assertEquals(count, big.count());

        for (var subset : big.choice(20, new Random(7)).stream().toList()) {
            assertStrictlyIncreasing(subset);
        }
        var sampled = big.sample(20, new Random(7)).stream().toList();
        assertEquals(20, new HashSet<>(sampled).size());
        sampled.forEach(SubsetBuilderTest::assertStrictlyIncreasing);

        // first and last rank of the whole range: [] and the full set
        var edges = big.byRanks(List.of(BigInteger.ZERO, count.subtract(BigInteger.ONE))).stream().toList();
        assertEquals(List.of(), edges.get(0));
        assertEquals(IntStream.range(0, 100).boxed().toList(), edges.get(1));

        // rank == count is out of range
        var tooBig = big.byRanks(List.of(count));
        assertThrows(IllegalArgumentException.class, () -> tooBig.stream().toList());
    }

    private static void assertStrictlyIncreasing(List<Integer> subset) {
        for (int i = 1; i < subset.size(); i++) {
            assertTrue(subset.get(i - 1) < subset.get(i), "not a valid subset: " + subset);
        }
    }

    @Test
    void lexOrderMthRules() {
        var emptyDomain = subsets.of(Collections.emptyList()).inRange(1, 2); // count = 0

        // count = 0 and start >= 0: accepted, yields nothing
        assertTrue(emptyDomain.lexOrderMth(1, 0).stream().toList().isEmpty());
        assertTrue(emptyDomain.lexOrderMth(3, 5).stream().toList().isEmpty());
        // ... but m <= 0 and start < 0 stay invalid
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.lexOrderMth(0, 0));
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.lexOrderMth(1, -1));

        // count = 1: only start = 0 is valid
        var onlyEmptySubset = subsets.of(Collections.emptyList()).all();
        assertEquals(List.of(List.of()), onlyEmptySubset.lexOrderMth(1, 0).stream().toList());
        assertThrows(IllegalArgumentException.class, () -> onlyEmptySubset.lexOrderMth(1, 1));
    }

    @Test
    void emptySetAcceptsAnyRangeEvenHugeOnes() {
        // The table lists [0, m] and [1, m] for n = 0 for any m, so a huge `to` must be cheap
        var withEmptySubset = subsets.of(Collections.emptyList()).inRange(0, Integer.MAX_VALUE);
        assertEquals(BigInteger.ONE, withEmptySubset.count());
        assertEquals(List.of(List.of()), withEmptySubset.lexOrder().stream().toList());
        assertEquals(List.of(List.of()), withEmptySubset.lexOrderMth(1, 0).stream().toList());
        assertEquals(List.of(List.of()), withEmptySubset.byRanks(List.of(BigInteger.ZERO)).stream().toList());

        var noSubsets = subsets.of(Collections.emptyList()).inRange(1, Integer.MAX_VALUE);
        assertEquals(BigInteger.ZERO, noSubsets.count());
        assertTrue(noSubsets.isEmpty());
        assertTrue(noSubsets.lexOrder().stream().toList().isEmpty());
        assertTrue(noSubsets.lexOrderMth(1, 0).stream().toList().isEmpty());
    }

    // ---- SubsetSelector: the step before a size range is chosen ----

    @Test
    void selectorOffersAllAndInRangeAndNothingElse() {
        // A selector cannot count or generate: only all() and inRange(...) lead to a builder.
        var selector = subsets.of(elements);
        assertTrue(selector instanceof SubsetSelector);
        assertFalse((Object) selector instanceof io.github.deepeshpatel.jnumbertools.generator.base.Builder);
        assertEquals(java.math.BigInteger.valueOf(1L << elements.size()), selector.all().count());
        assertEquals(selector.inRange(0, elements.size()).count(), selector.all().count());
    }

    @Test
    void selectorCanBeReusedForDifferentRanges() {
        var selector = subsets.of(elements);
        var all = selector.all();
        var some = selector.inRange(1, 1);
        assertEquals(java.math.BigInteger.valueOf(elements.size()), some.count());
        // choosing one range does not change what the selector or another builder yields
        assertEquals(java.math.BigInteger.valueOf(1L << elements.size()), all.count());
        assertEquals(java.math.BigInteger.valueOf(1L << elements.size()), selector.all().count());
    }

    @Test
    void selectorSnapshotsItsElements() {
        List<String> mutable = new ArrayList<>(List.of("A", "B"));
        var selector = new SubsetSelector<>(mutable, calculator);
        mutable.add("C");
        assertEquals(java.math.BigInteger.valueOf(4), selector.all().count());
    }
}