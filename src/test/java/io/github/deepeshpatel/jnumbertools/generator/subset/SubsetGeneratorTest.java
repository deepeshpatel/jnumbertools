package io.github.deepeshpatel.jnumbertools.generator.subset;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

public class SubsetGeneratorTest {

    @Test
    void assertCountOfAllSubsets() {
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
    void assertCountOfSubsetsInRange() {
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
    void shouldNotAllowSubsetOfNullCollection() {
        assertThrows(NullPointerException.class, () -> subsets.of((Collection<String>) null).all());
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
}
