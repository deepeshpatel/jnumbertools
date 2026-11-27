package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.builders.PermutationsBuilder;
import io.github.deepeshpatel.jnumbertools.entrypoint.Rank;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class UniquePermutationBuilderTest {

    private final PermutationsBuilder permutationsBuilder = new PermutationsBuilder(calculator);
    private final List<String> elements = List.of("A", "B", "C");
    private final UniquePermutationBuilder<String> builder = permutationsBuilder.unique(elements);

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

        var emptyBuilder = permutationsBuilder.unique(List.of());
        assertEquals(BigInteger.ONE, emptyBuilder.count());
    }

    @Test
    void isEmpty() {
        assertFalse(builder.isEmpty());
        assertFalse(permutationsBuilder.unique(List.of()).isEmpty());        // 0! = 1: the only result is []
        for (int n = 0; n <= 5; n++) {
            BuilderTestHelper.testIsEmpty(permutationsBuilder.unique(n));
        }
    }

    @Test
    void emptySetIsASingleEmptyResult() {
        BuilderTestHelper.testSingleEmptyResultDomain(permutationsBuilder.unique(List.of()));
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
        assertThrows(NullPointerException.class, () -> permutationsBuilder.unique((List<String>) null));
    }

    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        for (List<String> list : List.of(Arrays.asList(null, "B", "C"), Arrays.asList("A", null, "C"), Arrays.asList("A", "B", null))) {
            assertThrows(NullPointerException.class, () -> permutationsBuilder.unique(list), "unique " + list);
            assertThrows(NullPointerException.class, () -> new UniquePermutationBuilder<>(calculator, list), "constructor " + list);
        }
        assertThrows(NullPointerException.class, () -> permutationsBuilder.unique("A", null));
    }

    @Test
    void builderSnapshotsItsInput() {
        List<String> input = new ArrayList<>(List.of("A", "B", "C"));
        var snapshot = permutationsBuilder.unique(input);
        var lexBefore = snapshot.lexOrder().stream().toList();
        var generatorCreatedEarlier = snapshot.lexOrder();

        input.add("D");
        input.set(0, "Z");

        assertEquals(BigInteger.valueOf(6), snapshot.count());
        assertEquals(lexBefore, snapshot.lexOrder().stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
    }

    @Test
    void choiceDrawsFromTheWholeRangeWhenCountExceedsLong() {
        // 25! > Long.MAX_VALUE: ranks >= 2^63 must be reachable, not only the first 2^63 permutations
        BigInteger twoPow63 = BigInteger.ONE.shiftLeft(63);
        int draws = 200, above = 0;
        for (List<Integer> p : permutationsBuilder.unique(25).choice(draws, new Random(1))) {
            int[] indices = p.stream().mapToInt(Integer::intValue).toArray();
            if (Rank.permutation().unique(indices).compareTo(twoPow63) >= 0) {
                above++;
            }
        }
        assertTrue(above > draws * 0.95, "only " + above + " of " + draws + " draws had rank >= 2^63");
    }
}