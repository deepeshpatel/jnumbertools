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

public class UniquePermutationMthTest {

    @Test
    void assertCount(){
        for(int n=0; n<6; n++) {
            var input = Collections.nCopies(n, "A");
            for(int increment=1; increment<=4; increment++) {
                long size = permutation
                        .unique(input)
                        .lexOrderMth(increment, 0)
                        .stream().count();
                double expected = Math.ceil(calculator.nPr(n,n).longValue()/(double)increment);
                assertEquals((long)expected, size);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation.unique("A", "B", "C").lexOrderMth(3, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateAllUniquePermutationsOf3Values(){
        var expected = List.of(
                of(1, 2, 3),
                of(2, 3, 1)
        );
        assertIterableEquals(expected, uniquePermutation(3,0,1,2,3));
    }

    @Test
    void shouldGenerateEmptyListForNullInput(){
        assertIterableEquals(listOfEmptyList, uniquePermutation(3, 0,(List<Object>) null));
    }

    @Test
    void shouldGenerateEmptyListForEmptyInput(){
        assertEquals(listOfEmptyList, uniquePermutation(2,0, new ArrayList<>()));
    }

    @Test
    void shouldGenerateUniqueMthPermutations() {
        var expected = List.of(
                of("Red", "Green", "Blue"),
                of("Green", "Red", "Blue"),
                of("Blue", "Red", "Green")
        );

        var actual = uniquePermutation(2,0,"Red", "Green", "Blue");
        assertIterableEquals(expected, actual);
    }

    @Test
    void shouldSupportVeryLargePermutations() {

        var input = List.of(0,1,2,3,4,5,6,7,8,9,10,11,12);

        var expected = List.of(
                of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                of(2, 1, 0, 8, 10, 7, 9, 12, 4, 6, 11, 3, 5),
                of(4, 2, 1, 3, 8, 5, 7, 11, 10, 6, 9, 0, 12),
                of(6, 3, 1, 10, 2, 11, 0, 9, 4, 5, 7, 8, 12),
                of(8, 4, 2, 3, 12, 5, 10, 7, 1, 9, 11, 0, 6),
                of(10, 5, 2, 11, 7, 12, 4, 3, 8, 1, 6, 0, 9),
                of(12, 6, 3, 5, 4, 8, 1, 7, 0, 2, 9, 10, 11)
        );

        var actual = permutation.unique(input)
                .lexOrderMth(1000_000_000, 0) // jump to 1 billionth permutation
                .stream().toList();

        assertIterableEquals(expected, actual);
    }

    @Test
    void shouldGenerateMthPermutations() {
        var input = of("A","B","C","D","E","F");
        for(int increment=1; increment<=32; increment++) {
            var expected = getExpectedResultViaOneByOneIteration(input, increment);
            var output   = getResultViaDirectIncrement(input, increment);
            assertIterableEquals(expected, output);
        }
    }

    private List<?> getResultViaDirectIncrement(List<?> elements, int increment) {
        return permutation.unique(elements).lexOrderMth(increment, 0).stream().toList();
    }

    private List<?> getExpectedResultViaOneByOneIteration(List<?> input, int increment) {
        var stream = permutation.unique(input).lexOrder().stream();
        return everyMthValue(stream, increment);
    }

    @Test
    void shouldHandleSingleElement() {
        var expected = List.of(of("A"));
        assertIterableEquals(expected, uniquePermutation(1, 0,"A"));
    }

    @Test
    void shouldHandleIncrementLargerThanPermutations() {
        var input = of("A", "B");
        assertTrue(uniquePermutation(5, 5,  input).isEmpty());
    }

    @Test
    void shouldThrowExceptionForNegativeIncrement() {
        assertThrows(IllegalArgumentException.class,
                () -> uniquePermutation(-1, 0,"A", "B", "C"));
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressTesting() {
        int n = 10;
        for(int x = 1000; x<5000; x+=100) {
            BigInteger nthOfFactorialX = calculator.factorial(x).divide(BigInteger.valueOf(n));
            long count = permutation.unique(x).lexOrderMth(nthOfFactorialX, BigInteger.ZERO).stream().count();
            assertEquals(n, count);
        }
    }

    private List<?> uniquePermutation(int increment, int start, Object... elements) {
        return permutation.unique(elements).lexOrderMth(increment, start).stream().toList();
    }

    private List<?> uniquePermutation(int increment, int start, List<?> elements) {
        return permutation.unique(elements).lexOrderMth(increment, start).stream().toList();
    }
}