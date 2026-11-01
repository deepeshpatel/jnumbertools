package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

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

class RepetitivePermutationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Repetitive Permutation Builder Validation Rules
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
    private final List<String> elements = List.of("A", "B");
    private final RepetitivePermutationBuilder<String> builder = permutations.repetitive(3, elements);

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
        // Normal case: n=2, r=3 -> 2³ = 8
        assertEquals(BigInteger.valueOf(8), builder.count());

        // r = 0 -> n⁰ = 1
        var zeroWidthBuilder = permutations.repetitive(0, elements);
        assertEquals(BigInteger.ONE, zeroWidthBuilder.count());

        // n = 0, r = 0 -> 0⁰ = 1
        var emptyZeroBuilder = permutations.repetitive(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());

        // n = 0, r > 0 -> 0ʳ = 0
        var emptyPositiveBuilder = permutations.repetitive(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());

        // Single element
        var singleBuilder = permutations.repetitive(3, "X");
        assertEquals(BigInteger.ONE, singleBuilder.count()); // 1³ = 1
    }

    private static List<Integer> range(int n) {
        return IntStream.range(0, n).boxed().toList();
    }

    @Test
    void isEmpty() {
        assertFalse(builder.isEmpty());
        assertFalse(permutations.repetitive(0, elements).isEmpty());                // r = 0: the only result is []
        assertFalse(permutations.repetitive(0, Collections.emptyList()).isEmpty()); // 0⁰ = 1: the only result is []
        assertTrue(permutations.repetitive(2, Collections.emptyList()).isEmpty());  // 0ʳ = 0: nothing to generate
        for (int n = 0; n <= 4; n++) {
            for (int r = 0; r <= 4; r++) {
                BuilderTestHelper.testIsEmpty(permutations.repetitive(r, range(n)));
            }
        }
    }

    @Test
    void zeroWidthIsASingleEmptyResult() {
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.repetitive(0, elements));
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.repetitive(0, Collections.emptyList()));
    }

    @Test
    void emptySetWithPositiveWidthIsAnEmptyDomain() {
        BuilderTestHelper.testEmptyDomain(permutations.repetitive(2, Collections.emptyList()));
    }

    @Test
    void resultsAreImmutableAndReIterable() {
        BuilderTestHelper.testResultsAreImmutable(builder);
        BuilderTestHelper.testReIterable(builder);
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
        assertThrows(NullPointerException.class, () -> permutations.repetitive(2, (List<String>) null));
    }

    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        for (List<String> list : List.of(Arrays.asList(null, "B"), Arrays.asList("A", null))) {
            assertThrows(NullPointerException.class, () -> permutations.repetitive(2, list), "repetitive " + list);
            assertThrows(NullPointerException.class, () -> new RepetitivePermutationBuilder<>(2, list, calculator), "constructor " + list);
        }
        assertThrows(NullPointerException.class, () -> permutations.repetitive(1, "A", null));
    }

    @Test
    void builderSnapshotsItsInput() {
        List<String> input = new ArrayList<>(List.of("A", "B"));
        var snapshot = permutations.repetitive(2, input);
        var lexBefore = snapshot.lexOrder().stream().toList();
        var generatorCreatedEarlier = snapshot.lexOrder();

        input.add("C");
        input.set(0, "Z");

        assertEquals(BigInteger.valueOf(4), snapshot.count());                    // 2², not 3²
        assertEquals(lexBefore, snapshot.lexOrder().stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
    }

    @Test
    void choiceDrawsFromTheWholeRangeWhenCountExceedsLong() {
        // 100^100 >> Long.MAX_VALUE: the base-100 value of a result must reach ranks >= 2^63
        BigInteger twoPow63 = BigInteger.ONE.shiftLeft(63);
        BigInteger base = BigInteger.valueOf(100);
        int draws = 100, above = 0;
        for (List<Integer> p : permutations.repetitive(100, range(100)).choice(draws, new Random(1))) {
            BigInteger rank = BigInteger.ZERO;
            for (int digit : p) {
                rank = rank.multiply(base).add(BigInteger.valueOf(digit));
            }
            if (rank.compareTo(twoPow63) >= 0) {
                above++;
            }
        }
        assertTrue(above > draws * 0.95, "only " + above + " of " + draws + " draws had rank >= 2^63");
    }
}