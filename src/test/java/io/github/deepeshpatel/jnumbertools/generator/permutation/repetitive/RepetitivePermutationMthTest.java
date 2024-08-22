package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static org.junit.Assert.assertEquals;

public class RepetitivePermutationMthTest {

    @Test
    public void assertCount(){
        int increment=4;
        for(int n=1; n<=5; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int size=0; size<=3; size++){
                long count = permutation.repetitive(size, input)
                        .lexOrderMth(increment)
                        .stream().count();
                double expected = Math.ceil(Math.pow(n,size)/increment);
                Assert.assertEquals((long)expected, count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        RepetitivePermutationMth<String> iterable = permutation
                .repetitive(2,"A", "B", "C")
                .lexOrderMth(2);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllPermutationsOf2Values() {
        String expected = "[[0, 0, 0], [0, 1, 0], [1, 0, 0], [1, 1, 0]]";
        String actual   = permutation.repetitive(3, 0,1)
                .lexOrderMth(2)
                .stream().toList().toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateRepetitiveMthPermutations() {
        String expected = "[[A, A], [A, C], [B, B], [C, A], [C, C]]";

        var output = permutation.repetitive(2,"A", "B", "C")
                .lexOrderMth(2)
                .stream()
                .toList();

        Assert.assertEquals(expected, output.toString());
    }
}