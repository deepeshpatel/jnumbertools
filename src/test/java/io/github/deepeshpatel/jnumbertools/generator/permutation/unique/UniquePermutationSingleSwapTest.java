package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class UniquePermutationSingleSwapTest {

    @Test
    void should_have_only_single_swap_when_compared_to_previous() {
        var list = permutation.unique('a', 'b', 'c', 'd', 'e').singleSwap().stream().toList();
        assertEquals(120, list.size());
        for(int i=1; i<list.size(); i++) {
            int difCount = numOfElementsSwapped(list.get(i-1), list.get(i));
            assertEquals(2, difCount);
        }
    }

    @Test
    void shouldReturnSingleElementForSingleInput() {
        var expected = List.of('a');
        var list = permutation.unique(expected).singleSwap().stream().toList();
        assertEquals(1, list.size());
        assertEquals(expected, list.get(0));
    }

    @Test
    void shouldReturnTwoPermutationsForTwoElements() {
        var expected = List.of(
                List.of('A', 'B'),
                List.of('B', 'A')
        );

        var output = permutation.unique(A_B).singleSwap().stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void shouldHandleLargeInput() {
        var list = permutation
                .unique('a', 'b', 'c', 'd', 'e', 'f', 'g')
                .singleSwap().stream().toList();

        // The size should be factorial of the number of elements
        assertEquals(5040, list.size());
        // Perform a quick check to ensure swaps are correct
        for(int i=1; i<list.size(); i++) {
            assertEquals(2, numOfElementsSwapped(list.get(i-1), list.get(i)));
        }
    }

    @Test
    void shouldReturnOneEmptyElementForEmptyInput() {
        var list = permutation.unique().singleSwap().stream().toList();
        assertIterableEquals(listOfEmptyList, list);
    }

    @Test
    void shouldHandleIdenticalElementsWithDifferentDataTypes() {
        var list = permutation.unique('a', 1, 'a', 1).singleSwap().stream().toList();
        Assertions.assertTrue(list.contains(List.of('a', 1, 'a', 1)));
        Assertions.assertTrue(list.contains(List.of(1, 'a', 'a', 1)));
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressTesting() {
        for(int n=0; n<=10; n++) {
            long count = permutation.unique(n).singleSwap().stream().count();
            assertEquals(calculator.factorial(n).longValue(), count);
        }
    }

    private <T> int numOfElementsSwapped(List<T> first, List<T> second) {
        int sum = 0;
        for(int i=0; i< first.size(); i++) {
            if(!first.get(i).equals(second.get(i))) {
                sum++;
            }
        }
        return sum;
    }
}
