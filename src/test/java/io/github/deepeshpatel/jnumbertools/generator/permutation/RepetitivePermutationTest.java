package io.github.deepeshpatel.jnumbertools.generator.permutation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.TestBase.tools;
import static org.junit.Assert.assertEquals;

public class RepetitivePermutationTest {

    @Test
    public void assertCount(){
        for(int n=1; n<3; n++) {
            var input = Collections.nCopies(n, "A");
            for(int r=0; r<=n+1; r++) {
                long size = tools.permutations().of(input)
                        .repetitive(r)
                        .stream().count();

                Assert.assertEquals((int)Math.pow(n,r), size);
            }
        }
    }

    @Test (expected = NoSuchElementException.class)
    public void shouldThrowExpIfIterateAfterLastElement(){
        var iterator = tools.permutations().of("A")
                .repetitive(1).iterator();

            iterator.next();
            iterator.next();
    }

    @Test
    public void shouldReturnSameResultViaDifferentIteratorObjects(){
        RepetitivePermutation<String> iterable = tools.permutations().of("A", "B", "C").repetitive(2);
        var lists1 = iterable.stream().toList();
        var lists2 =iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllPermutationsOf2Values() {
        String expected = "[[0, 0, 0], [0, 0, 1], [0, 1, 0], [0, 1, 1], " +
                "[1, 0, 0], [1, 0, 1], [1, 1, 0], [1, 1, 1]]";

        String actual   = tools.permutations().of(0,1)
                .repetitive(3)
                .stream().toList().toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateRepetitivePermutations() {
        String expected = "[[A, A], [A, B], [A, C], [B, A], [B, B], [B, C], [C, A], [C, B], [C, C]]";

        String output = tools.permutations().of("A", "B", "C")
                .repetitive(2)
                .stream().toList().toString();

        Assert.assertEquals(expected, output);
    }
}