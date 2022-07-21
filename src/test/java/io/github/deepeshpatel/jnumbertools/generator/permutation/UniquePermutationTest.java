package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;
import static org.junit.Assert.assertEquals;

public class UniquePermutationTest {

    @Test
    public void assertCount(){
        for(int n=0; n<6; n++) {
            List<String> input = Collections.nCopies(n, "A");
            long size = JNumberTools.permutationsOf(input)
                    .unique().stream().count();
            Assert.assertEquals(nPr(n,n), size);
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        UniquePermutation<String> iterable = JNumberTools.permutationsOf("A", "B", "C").unique();

        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]";
        String actual   = JNumberTools.permutationsOf("1", "2", "3")
                .unique()
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateUniquePermutations() {
        String expected = "[[Red, Green, Blue]," +
                " [Red, Blue, Green]," +
                " [Green, Red, Blue]," +
                " [Green, Blue, Red]," +
                " [Blue, Red, Green]," +
                " [Blue, Green, Red]]";

        List<List<String>> output = JNumberTools.permutationsOf("Red", "Green", "Blue")
                .unique()
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateEmptyListForEmptyInput(){
        String actual   = JNumberTools.permutationsOf(new ArrayList<String>())
                .unique()
                .stream().collect(Collectors.toList()).toString();

        assertEquals("[[]]",actual);
    }

    @Test
    public void shouldConsiderNullAsEmpty(){
        String actual   = JNumberTools.permutationsOf((java.util.Collection<String>) null)
                .unique()
                .stream().collect(Collectors.toList()).toString();

        assertEquals("[[]]",actual);
    }
}
