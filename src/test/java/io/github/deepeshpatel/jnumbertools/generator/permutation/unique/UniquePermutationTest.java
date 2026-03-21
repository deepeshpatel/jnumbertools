package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class UniquePermutationTest {

    @Test
    void assertCount() {
        // n! permutations
        for (int n = 1; n < 6; n++) {
            var input = Collections.nCopies(n, "A");
            long size = permutation
                    .unique(input)
                    .lexOrder()
                    .stream().count();
            assertEquals(calculator.nPr(n, n).longValue(), size);
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        var zeroBuilder = permutation.unique(0);
        assertEquals(BigInteger.ONE, zeroBuilder.count());
        assertTrue(zeroBuilder.lexOrder().stream().toList().get(0).isEmpty());
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        UniquePermutation<String> iterable = permutation.unique("A", "B", "C").lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
        assertNotSame(lists1, lists2);
    }

    @Test
    void shouldReturnImmutableOuterCollection() {
        var results = permutation.unique("A", "B").lexOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X")));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));
    }

    @Test
    void shouldReturnImmutableInnerLists() {
        var results = permutation.unique("A", "B").lexOrder().stream().toList();
        var first =  results.get(0);
        assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
        assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
    }

    @Test
    void shouldGenerateAllUniquePermutationsOf3Values() {
        var expected = List.of(
                of(1, 2, 3),
                of(1, 3, 2),
                of(2, 1, 3),
                of(2, 3, 1),
                of(3, 1, 2),
                of(3, 2, 1)
        );

        assertIterableEquals(expected, permutationsOf(1, 2, 3));
    }

    @Test
    void shouldGenerateUniquePermutations() {
        var expected = List.of(
                of("Red", "Green", "Blue"),
                of("Red", "Blue", "Green"),
                of("Green", "Red", "Blue"),
                of("Green", "Blue", "Red"),
                of("Blue", "Red", "Green"),
                of("Blue", "Green", "Red")
        );

        assertIterableEquals(expected, permutationsOf("Red", "Green", "Blue"));
    }

    @Test
    void shouldGenerateEmptyListForEmptyInput() {
        assertIterableEquals(listOfEmptyList, permutationsOf(new ArrayList<>()));
    }

    @Test
    void shouldHandleMixedTypes() {
        var expected = List.of(
                of(1,"A"),
                of("A", 1)
        );
        assertIterableEquals(expected, permutationsOf(1, "A"));
    }

    @Test
    void shouldReturnSingleElement() {
        var expected = List.of(of("A"));
        assertIterableEquals(expected, permutationsOf("A"));
    }

    @Test
    void shouldGeneratePermutationsForNonStringElements() {
        var expected = List.of(of(1, 2), of(2, 1));
        assertIterableEquals(expected, permutationsOf(1, 2));
    }

    @Test
    void shouldGeneratePermutationsForImmutableList() {
        var input = of("A", "B");
        var expected = List.of(
                of("A", "B"),
                of("B", "A")
        );
        assertIterableEquals(expected, permutationsOf(input));
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressTesting() {
        for (int n = 0; n <= 10; n++) {
            long count = permutation.unique(n).lexOrder().stream().count();
            assertEquals(calculator.factorial(n).longValue(), count);
        }
    }

    private List<List<Object>> permutationsOf(Object... elements) {
        return permutation
                .unique(elements)
                .lexOrder()
                .stream().toList();
    }

    private List<?> permutationsOf(List<?> elements) {
        return permutation
                .unique(elements)
                .lexOrder()
                .stream().toList();
    }
}
