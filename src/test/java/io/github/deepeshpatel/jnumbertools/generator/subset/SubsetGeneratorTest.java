package io.github.deepeshpatel.jnumbertools.generator.subset;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

public class SubsetGeneratorTest {

    @Test
    void assertCount() {
        // Test full power set (all subsets)
        for (int n = 0; n <= 4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            int count = (int) subsets.of(input)
                    .all().lexOrder()
                    .stream().count();

            int expected = (int) (Math.pow(2, n));
            assertEquals(expected, count);
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        // n=0, range [0,0] -> 2⁰ = 1 -> count=1, returns [[]]
        var zeroZeroBuilder = subsets.of(Collections.emptyList()).inRange(0, 0);
        assertEquals(BigInteger.ONE, zeroZeroBuilder.count());
        var zeroZeroResult = zeroZeroBuilder.lexOrder().stream().toList();
        assertEquals(1, zeroZeroResult.size());
        assertTrue(zeroZeroResult.get(0).isEmpty());

        // n=0, range [0,2] -> only empty subset exists -> count=1, returns [[]]
        var zeroRangeBuilder = subsets.of(Collections.emptyList()).inRange(0, 2);
        assertEquals(BigInteger.ONE, zeroRangeBuilder.count());
        var zeroRangeResult = zeroRangeBuilder.lexOrder().stream().toList();
        assertEquals(1, zeroRangeResult.size());
        assertTrue(zeroRangeResult.get(0).isEmpty());

        // n=0, range [1,2] -> no non-empty subsets -> count=0, returns []
        var zeroPositiveBuilder = subsets.of(Collections.emptyList()).inRange(1, 2);
        assertEquals(BigInteger.ZERO, zeroPositiveBuilder.count());
        assertTrue(zeroPositiveBuilder.lexOrder().stream().toList().isEmpty());

        // n>0, range [0,0] -> one empty subset -> count=1, returns [[]]
        var positiveZeroBuilder = subsets.of(A_B_C).inRange(0, 0);
        assertEquals(BigInteger.ONE, positiveZeroBuilder.count());
        var positiveZeroResult = positiveZeroBuilder.lexOrder().stream().toList();
        assertEquals(1, positiveZeroResult.size());
        assertTrue(positiveZeroResult.get(0).isEmpty());
    }

    @Test
    void assertCountOfSubsetsInRange() {
        // Test subsets in specific ranges
        for (int n = 0; n <= 4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            int from = 0;
            for (int range = 0; range <= n; range++) {
                int count = (int) subsets.of(input)
                        .inRange(from, range).lexOrder()
                        .stream().count();
                assertSize(count, n, from, range);
            }
        }
    }

    private void assertSize(int count, int n, int from, int to) {
        long expected = 0;
        for (int i = from; i <= to; i++) {
            expected += calculator.nCr(n, i).longValue();
        }
        assertEquals(expected, count);
    }

    @Test
    void shouldReturnAllSubsetsInInputOrderByDefault() {
        String expected = "[[C], [B], [A], [C, B], [C, A], [B, A], [C, B, A]]";

        String output = subsets
                .of("C", "B", "A")
                .inRange(1, 3).lexOrder()
                .stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    void shouldReturnAllSubsetsInGivenRange() {
        String expected = "[[A, B], [A, C], [B, C], [A, B, C]]";

        String output = subsets
                .of(A_B_C)
                .inRange(2, 3).lexOrder()
                .stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    void shouldGenerateAllPossibleSubsetsWithAllMethod() {
        String expected = "[[], [A], [B], [C], [A, B], [A, C], [B, C], [A, B, C]]";
        String output = subsets.of(A_B_C)
                .all().lexOrder().stream().toList().toString();
        assertEquals(expected, output);
    }

    @Test
    void shouldGenerateEmptyForRangeFromZeroToZero() {
        var output = subsets
                .of(A_B_C)
                .inRange(0, 0).lexOrder()
                .stream().toList();

        assertIterableEquals(listOfEmptyList, output);
    }

    @Test
    void shouldGenerateSubsetFromGivenInputSize() {
        String output = subsets
                .of(3)
                .all().lexOrder()
                .stream().toList().toString();

        assertEquals("[[], [0], [1], [2], [0, 1], [0, 2], [1, 2], [0, 1, 2]]", output);
    }

    @Test
    void shouldReturnImmutableOuterAndInnerCollection() {
        var results = subsets.of("A", "B").all().lexOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X")));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));

        var first = results.get(0);
        assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
        assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
    }
}
