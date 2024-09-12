package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.Assert.assertEquals;

public class RepetitivePermutationTest {

    @Test
    public void assertCount(){
        for(int n=1; n<3; n++) {
            var input = Collections.nCopies(n, "A");
            for(int r=0; r<=n+1; r++) {
                long size = permutation.repetitive(r, input)
                        .lexOrder()
                        .stream().count();

                Assert.assertEquals((int)Math.pow(n,r), size);
            }
        }
    }

    @Test (expected = NoSuchElementException.class)
    public void shouldThrowExpIfIterateAfterLastElement(){
        var iterator = permutation.repetitive(1,"A")
                .lexOrder().iterator();

            iterator.next();
            iterator.next();
    }

    @Test
    public void shouldReturnSameResultViaDifferentIteratorObjects(){
        RepetitivePermutation<String> iterable = permutation
                .repetitive(2,"A", "B", "C")
                .lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 =iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllPermutationsOf2Values() {
        String expected = "[[0, 0, 0], [0, 0, 1], [0, 1, 0], [0, 1, 1], " +
                "[1, 0, 0], [1, 0, 1], [1, 1, 0], [1, 1, 1]]";

        String actual   = permutation.repetitive(3,0,1)
                .lexOrder()
                .stream().toList().toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateRepetitivePermutations() {
        String expected = "[[A, A], [A, B], [A, C], [B, A], [B, B], [B, C], [C, A], [C, B], [C, C]]";

        String output = permutation.repetitive(2, "A", "B", "C")
                .lexOrder()
                .stream().toList().toString();

        Assert.assertEquals(expected, output);
    }

    @Test
    public void stressTesting() {
        assumeStressTesting();
        for(int n=20; n<=25; n++) {
            for(int width = 0; width<=5; width++) {
                long count = permutation.repetitive(width, n).lexOrder().stream().count();
                assertEquals(calculator.pow(n, width).longValue(), count);
            }
        }
    }
}