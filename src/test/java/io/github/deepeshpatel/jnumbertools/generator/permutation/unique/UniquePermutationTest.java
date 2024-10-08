package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.Assert.assertEquals;

public class UniquePermutationTest {

    @Test
    public void assertCount(){
        for(int n=0; n<6; n++) {
            var input = Collections.nCopies(n, "A");
            long size = permutation
                    .unique(input)
                    .lexOrder()
                    .stream().count();
            Assert.assertEquals(calculator.nPr(n,n).longValue(), size);
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        UniquePermutation<String> iterable = permutation.unique("A", "B", "C").lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]";
        assertEquals(expected, output(1,2,3));
    }

    @Test
    public void shouldGenerateUniquePermutations() {
        String expected = "[[Red, Green, Blue]," +
                " [Red, Blue, Green]," +
                " [Green, Red, Blue]," +
                " [Green, Blue, Red]," +
                " [Blue, Red, Green]," +
                " [Blue, Green, Red]]";

        Assert.assertEquals(expected, output("Red", "Green", "Blue"));
    }

    @Test
    public void shouldGenerateEmptyListForEmptyInput(){
        assertEquals("[[]]", output(new ArrayList<>()));
    }

    @Test
    public void shouldConsiderNullAsEmpty(){
        assertEquals("[[]]", output((List<String>) null));
    }

    private String output(List<String> elements) {
        return permutation
                .unique(elements)
                .lexOrder()
                .stream().toList().toString();
    }

    @Test
    public void shouldHandleMixedTypes() {
        String expected = "[[1, A], [A, 1]]";
        assertEquals(expected, output(1, "A"));
    }

    @Test
    public void shouldReturnSingleElement() {
        String expected = "[[A]]";
        assertEquals(expected, output("A"));
    }

    @Test
    public void shouldGeneratePermutationsForNonStringElements() {
        String expected = "[[1, 2], [2, 1]]";
        assertEquals(expected, output(1, 2));
    }

    @Test
    public void shouldGeneratePermutationsForImmutableList() {
        List<String> input = List.of("A", "B");
        String expected = "[[A, B], [B, A]]";
        assertEquals(expected, output(input));
    }

    @Test
    public void stressTesting() {
        assumeStressTesting();
        for(int n=0; n<=10; n++) {
            long count = permutation.unique(n).lexOrder().stream().count();
            assertEquals(calculator.factorial(n).longValue(), count);
        }
    }


    private String output(Object... elements) {
        return permutation
                .unique(elements)
                .lexOrder()
                .stream().toList().toString();
    }
}
