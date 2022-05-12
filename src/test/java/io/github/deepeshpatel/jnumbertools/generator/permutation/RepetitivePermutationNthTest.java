package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.generator.TestUtil.iteratorToList;
import static org.junit.Assert.assertEquals;

public class RepetitivePermutationNthTest {

    @Test
    public void assertCount(){
        int increment=4;
        for(int n=1; n<=5; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int size=0; size<=3; size++){
                long count = JNumberTools.permutationsOf(input)
                        .repetitiveNth(size,increment)
                        .stream().count();
                double expected = Math.ceil(Math.pow(n,size)/increment);
                Assert.assertEquals((long)expected, count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        Iterable<List<String>> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .repetitiveNth(2,2);

        List<List<String>> lists1 = iteratorToList(iterable.iterator());
        List<List<String>> lists2 = iteratorToList(iterable.iterator());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllPermutationsOf2Values() {
        String expected = "[[0, 0, 0], [0, 1, 0], [1, 0, 0], [1, 1, 0]]";
        String actual   = JNumberTools.permutationsOf("0","1")
                .repetitiveNth(3,2)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateRepetitiveNthPermutations() {
        String expected = "[[A, A], [A, C], [B, B], [C, A], [C, C]]";

        List<List<String>> output = JNumberTools.permutationsOf("A", "B", "C")
                .repetitiveNth(2,2)
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals(expected, output.toString());
    }
}