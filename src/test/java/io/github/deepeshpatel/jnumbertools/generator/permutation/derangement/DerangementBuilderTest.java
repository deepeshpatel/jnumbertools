package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.base.Derangements;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

class DerangementBuilderTest {

    private final Derangements derangements = new Derangements(calculator);
    // n=4 → D_4 = 9: large enough for BuilderTestHelper (needs count ≥ a few).
    private final List<String> elements = List.of("A", "B", "C", "D");
    private final DerangementBuilder<String> builder = derangements.of(elements);

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
        // !4 = 9
        assertEquals(BigInteger.valueOf(9), builder.count());

        // Edge cases: !0 = 1, !1 = 0
        assertEquals(BigInteger.ONE,  derangements.of(List.of()).count());
        assertEquals(BigInteger.ZERO, derangements.of(List.of("X")).count());
    }

    @Test
    void isEmpty() {
        assertFalse(builder.isEmpty());
        // !0 = 1: the empty set has one derangement ([]), so the builder is NOT empty
        assertFalse(derangements.of(List.of()).isEmpty());
        // !1 = 0: no derangement exists for a single element
        assertTrue(derangements.of(List.of("X")).isEmpty());
    }

    @Test
    void toStringContainsCount() {
        String s = builder.toString();
        assertTrue(s.contains("DerangementBuilder"));
        assertTrue(s.contains("count=9"));
    }

    @Test
    void isEmptyFollowsCountForEverySize() {
        for (int n = 0; n <= 7; n++) {
            BuilderTestHelper.testIsEmpty(derangements.of(IntStream.range(0, n).boxed().toList()));
        }
    }

    @Test
    void emptySetIsASingleEmptyResult() {
        BuilderTestHelper.testSingleEmptyResultDomain(derangements.of(List.of()));
    }

    @Test
    void singleElementIsAnEmptyDomain() {
        BuilderTestHelper.testEmptyDomain(derangements.of(List.of("X")));
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
        assertThrows(NullPointerException.class, () -> derangements.of((List<String>) null));
    }

    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        for (List<String> list : List.of(Arrays.asList(null, "B", "C"), Arrays.asList("A", null, "C"), Arrays.asList("A", "B", null))) {
            assertThrows(NullPointerException.class, () -> derangements.of(list), "derangements " + list);
            assertThrows(NullPointerException.class, () -> new DerangementBuilder<>(calculator, list), "constructor " + list);
        }
    }

    @Test
    void builderSnapshotsItsInput() {
        List<String> input = new ArrayList<>(List.of("A", "B", "C"));
        var snapshot = derangements.of(input);
        var lexBefore = snapshot.lexOrder().stream().toList();
        var generatorCreatedEarlier = snapshot.lexOrder();

        input.add("D");
        input.set(0, "Z");

        assertEquals(BigInteger.TWO, snapshot.count());                          // !3 = 2, not !4 = 9
        assertEquals(lexBefore, snapshot.lexOrder().stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
    }
}