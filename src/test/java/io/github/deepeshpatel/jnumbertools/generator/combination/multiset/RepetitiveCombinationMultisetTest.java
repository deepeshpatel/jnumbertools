package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.combination;

public class RepetitiveCombinationMultisetTest {

    @Test
    public void assertCount() {
        var elements = List.of("A", "B", "C");
        int[] frequencies = {2, 3, 2};
        int size = 2;

        long actualCount = combination
                .multiset(elements, frequencies, size)
                .lexOrder()
                .stream().count();

        Assert.assertEquals(6, actualCount);
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects() {
        var elements = List.of("A", "B", "C");
        int[] frequencies = {2, 3, 2};

        RepetitiveCombinationMultiset<String> iterable = combination
                .multiset(elements, frequencies, 2)
                .lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCorrectCombinationsOfMultiset() {
        String expected = "[[Red, Red, Red], " +
                "[Red, Red, Green], " +
                "[Red, Red, Blue], " +
                "[Red, Red, Yellow], " +
                "[Red, Green, Green], " +
                "[Red, Green, Blue], " +
                "[Red, Green, Yellow], " +
                "[Red, Blue, Yellow], " +
                "[Green, Green, Blue], " +
                "[Green, Green, Yellow], " +
                "[Green, Blue, Yellow]]";

        var elements = List.of("Red", "Green", "Blue", "Yellow");
        int[] frequencies = {3, 2, 1, 1};  // 3 red, 2 green, 1 blue, 1 yellow
        int size = 3;
        Assert.assertEquals(expected, output(elements, frequencies, size));
    }

    @Test
    public void shouldReturnEmptyListWhenCombinationSizeIsEqualToZero() {
        Assert.assertEquals("[[]]", output(List.of("A"), new int[]{3}, 0));
    }

    @Test
    public void shouldReturnEmptyListWhenInputListIsEmpty() {
        Assert.assertEquals("[[]]", output(Collections.emptyList(), new int[]{}, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMultisetFreqArrayIsNull() {
        output(List.of('A'), null, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenFrequenciesDoNotMatch() {
        output(List.of("A", "B"), new int[]{3}, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNegativeFrequency() {
        output(List.of("A", "B"), new int[]{-1, 2}, 2);
    }

    @Test
    public void shouldHandleLargeFrequencies() {
        var elements = List.of("A", "B", "C");
        int[] frequencies = {100, 100, 100};
        int size = 3;

        // Optionally, add a check for performance or size of the output
        // For very large inputs, be cautious with performance and memory usage
        Assert.assertNotNull(output(elements, frequencies, size));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCombinationSizeIsGreaterThanNumberOfElements() {
        var elements = List.of("A", "B");
        int[] frequencies = {2, 2};
        int size = 3;

        output(elements, frequencies, size);
    }

    private String output(List<?> elements, int[] frequencies, int size) {
        return combination.multiset(elements, frequencies, size)
                .lexOrder()
                .stream().toList().toString();
    }
}
