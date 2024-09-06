package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static org.junit.Assert.assertEquals;

public class RepetitivePermutationMthTest {

    @Test
    public void assertCount(){
        int increment=4;
        for(int n=1; n<=5; n++) {
            var input = Collections.nCopies(n, 'A');
            for(int size=0; size<=3; size++){
                long count = permutation.repetitive(size, input)
                        .lexOrderMth(increment, 0)
                        .stream().count();
                double expected = Math.ceil(Math.pow(n,size)/increment);
                Assert.assertEquals((long)expected, count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation
                .repetitive(2,'A', 'B','C')
                .lexOrderMth(2, 0);

        Assert.assertEquals(iterable.stream().toList(), iterable.stream().toList());
    }

    @Test
    public void shouldGenerateAllPermutationsOf2Values() {
        String actual   = permutation.repetitive(3, 0,1)
                .lexOrderMth(2, 0)
                .stream().toList().toString();

        assertEquals("[[0, 0, 0], [0, 1, 0], [1, 0, 0], [1, 1, 0]]", actual);
    }

    @Test
    public void shouldGenerateRepetitiveMthPermutations() {
        var output = permutation.repetitive(2,"A", "B", "C")
                .lexOrderMth(2, 0).stream().toList();

        Assert.assertEquals("[[A, A], [A, C], [B, B], [C, A], [C, C]]", output.toString());
    }

    @Test
    public void test_start_parameter_greater_than_0() {
        var output = permutation.repetitive(2,"A", "B", "C")
                .lexOrderMth(2, 3).stream().toList().toString();
        Assert.assertEquals("[[B, A], [B, C], [C, B]]", output);

    }
}