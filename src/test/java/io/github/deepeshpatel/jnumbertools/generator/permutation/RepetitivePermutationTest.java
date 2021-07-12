package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        List<List<String>> output = JNumberTools.permutationsOf("A", "B", "C")
                .repetitive(2)
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals(expected, output.toString());
    }
}