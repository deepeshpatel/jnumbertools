package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.generator.TestUtil.iteratorToList;
import static org.junit.Assert.assertEquals;

public class RepetitivePermutationTest {

    @Test
    public void assertCount(){
        for(int n=1; n<3; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int r=0; r<=n+1; r++) {
                long size = JNumberTools.permutationsOf(input)
                        .repetitive(r)
                        .stream().count();

                Assert.assertEquals((int)Math.pow(n,r), size);
            }
        }
    }

    @Test (expected = NoSuchElementException.class)
    public void shouldThrowExpIfIterateAfterLastElement(){
        Iterator<List<String>> iterator = JNumberTools.permutationsOf("A")
                .repetitive(1).iterator();

            iterator.next();
            iterator.next();
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        Iterable<List<String>> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .repetitive(2);

        List<List<String>> lists1 = iteratorToList(iterable.iterator());
        List<List<String>> lists2 = iteratorToList(iterable.iterator());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllPermutationsOf2Values() {
        String expected = "[[0, 0, 0], [0, 0, 1], [0, 1, 0], [0, 1, 1], " +
                "[1, 0, 0], [1, 0, 1], [1, 1, 0], [1, 1, 1]]";

        String actual   = JNumberTools.permutationsOf("0", "1")
                .repetitive(3)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateRepetitivePermutations() {
        String expected = "[[A, A], [A, B], [A, C], [B, A], [B, B], [B, C], [C, A], [C, B], [C, C]]";

        String output = JNumberTools.permutationsOf("A", "B", "C")
                .repetitive(2)
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals(expected, output);
    }
}