package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;
import static org.junit.Assert.assertEquals;

public class UniquePermutationTest {

    @Test
    public void assertCount(){
        for(int n=0; n<6; n++) {
            var input = Collections.nCopies(n, "A");
            long size = JNumberTools.permutations().of(input)
                    .unique().stream().count();
            Assert.assertEquals(nPr(n,n), size);
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        UniquePermutation<String> iterable = JNumberTools.permutations().of("A", "B", "C").unique();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]";
        String actual   = JNumberTools.permutations().of("1", "2", "3")
                .unique()
                .stream().toList().toString();

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

        var output = JNumberTools.permutations().of("Red", "Green", "Blue")
                .unique()
                .stream()
                .toList();

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateEmptyListForEmptyInput(){
        String actual   = JNumberTools.permutations().of(new ArrayList<String>())
                .unique()
                .stream().toList().toString();

        assertEquals("[[]]",actual);
    }

    @Test
    public void shouldConsiderNullAsEmpty(){
        String actual   = JNumberTools.permutations().of((java.util.Collection<String>) null)
                .unique()
                .stream().toList().toString();

        assertEquals("[[]]",actual);
    }
}
