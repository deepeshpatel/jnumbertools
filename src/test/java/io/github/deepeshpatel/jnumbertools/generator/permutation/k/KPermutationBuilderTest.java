package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Permutations;
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
import java.util.stream.IntStream;
import static io.github.deepeshpatel.jnumbertools.TestBase.rankOf;

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

    private static List<Integer> range(int n) {
        return IntStream.range(0, n).boxed().toList();
    }

    @Test
    void isEmpty() {
        assertFalse(builder.isEmpty());
        assertFalse(permutations.nPk(0, elements).isEmpty());                 // k = 0: the only result is []
        assertFalse(permutations.nPk(0, Collections.emptyList()).isEmpty());  // n = 0, k = 0: the only result is []
        assertTrue(permutations.nPk(5, elements).isEmpty());                  // k > n: nothing to generate
        assertTrue(permutations.nPk(2, Collections.emptyList()).isEmpty());   // n = 0, k > 0: nothing to generate
        for (int n = 0; n <= 5; n++) {
            for (int k = 0; k <= 7; k++) {
                BuilderTestHelper.testIsEmpty(permutations.nPk(k, range(n)));
            }
        }
    }

    @Test
    void zeroSelectionIsASingleEmptyResult() {
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.nPk(0, elements));
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.nPk(0, Collections.emptyList()));
    }

    @Test
    void selectionLargerThanTheSetIsAnEmptyDomain() {
        BuilderTestHelper.testEmptyDomain(permutations.nPk(5, elements));
        BuilderTestHelper.testEmptyDomain(permutations.nPk(2, Collections.emptyList()));
    }

    @Test
    void combinationOrderMthOnAnEmptyDomainIsAcceptedAndYieldsNothing() {
        var empty = permutations.nPk(5, elements);
        assertFalse(empty.combinationOrderMth(1, 0).iterator().hasNext());
        assertFalse(empty.combinationOrderMth(2, 5).iterator().hasNext());
    }

    @Test
    void resultsAreImmutableAndReIterable() {
        BuilderTestHelper.testResultsAreImmutable(builder);
        BuilderTestHelper.testReIterable(builder);
        for (List<String> result : builder.combinationOrder()) {
            assertThrows(UnsupportedOperationException.class, () -> result.add("X"));
        }
    }

    @Test
    void byRanksIsLazy() {
        BuilderTestHelper.testByRanksIsLazy(builder);
    }

    @Test
    void choiceAndSampleLimits() {
        BuilderTestHelper.testChoiceMayExceedCountAndNullRandomIsRejected(builder);
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(NullPointerException.class, () -> permutations.nPk(2, (List<String>) null));
    }

    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        for (List<String> list : List.of(Arrays.asList(null, "B", "C"), Arrays.asList("A", null, "C"), Arrays.asList("A", "B", null))) {
            assertThrows(NullPointerException.class, () -> permutations.nPk(2, list), "nPk " + list);
            assertThrows(NullPointerException.class, () -> new KPermutationBuilder<>(list, 2, calculator), "constructor " + list);
        }
        assertThrows(NullPointerException.class, () -> permutations.nPk(1, "A", null));
    }

    @Test
    void builderSnapshotsItsInput() {
        List<String> input = new ArrayList<>(List.of("A", "B", "C"));
        var snapshot = permutations.nPk(2, input);
        var lexBefore = snapshot.lexOrder().stream().toList();
        var combinationBefore = snapshot.combinationOrder().stream().toList();
        var generatorCreatedEarlier = snapshot.lexOrder();

        input.add("D");
        input.set(0, "Z");

        assertEquals(BigInteger.valueOf(6), snapshot.count());                    // 3P2, not 4P2
        assertEquals(lexBefore, snapshot.lexOrder().stream().toList());
        assertEquals(combinationBefore, snapshot.combinationOrder().stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
    }

    @Test
    void choiceDrawsFromTheWholeRangeWhenCountExceedsLong() {
        // 30P15 ~ 2.0e20 > Long.MAX_VALUE: ranks >= 2^63 must be reachable
        BigInteger twoPow63 = BigInteger.ONE.shiftLeft(63);
        var huge = permutations.nPk(15, range(30));
        assertTrue(huge.count().compareTo(twoPow63) > 0);
        int draws = 200, above = 0;
        for (List<Integer> p : huge.choice(draws, new Random(1))) {
            int[] indices = p.stream().mapToInt(Integer::intValue).toArray();
            if (rankOf.kPermutation(30, indices).compareTo(twoPow63) >= 0) {
                above++;
            }
        }
        // ~95.5% of all 30P15 ranks are >= 2^63; the old sampler never produced any of them
        assertTrue(above > draws * 0.85, "only " + above + " of " + draws + " draws had rank >= 2^63");
    }
}