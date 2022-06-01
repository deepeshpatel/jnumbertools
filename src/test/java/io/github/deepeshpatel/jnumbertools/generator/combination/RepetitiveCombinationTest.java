package io.github.deepeshpatel.jnumbertools.generator.combination;


import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class RepetitiveCombinationTest {

    @Test
    public void assertCount() {

        for(int n=1; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                int count = (int) JNumberTools.combinationsOf(input)
                        .repetitive(r).stream().count();
                int expectedCount = (int) nCr(n+r-1,r);
                Assert.assertEquals(expectedCount, count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        RepetitiveCombination<String> iterable = JNumberTools.combinationsOf("A", "B", "C").repetitive(2);
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCombinationsInInputOrder() {

        String expected = "[[Red, Red], [Red, Green], [Red, Blue], [Green, Green], [Green, Blue], [Blue, Blue]]";

        String output = JNumberTools
                .combinationsOf("Red", "Green", "Blue")
                .repetitive(2)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListForSizeLessThanOrEqualsZero() {

        String expected ="[[]]";

        String output = JNumberTools
                .combinationsOf("A","B")
                .repetitive(0)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldAbleToGenerateRepetitivePermutationForKGreaterThanN() {

        String expected ="[[A, A, A], [A, A, B], [A, B, B], [B, B, B]]";

        String output = JNumberTools
                .combinationsOf("A","B")
                .repetitive(3)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }
}