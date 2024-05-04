package io.github.deepeshpatel.jnumbertools.generator.combination;


import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.tools;
import static org.junit.Assert.assertEquals;

public class RepetitiveCombinationTest {

    @Test
    public void assertCount() {

        for(int n=1; n<=4; n++) {
            var input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                long count = tools.combinations().of(input,r)
                        .repetitive().stream().count();
                long expectedCount = calculator.nCr(n+r-1,r).longValue();
                Assert.assertEquals(expectedCount, count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = tools.combinations()
                .of(List.of("A", "B", "C"),2).repetitive();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCombinationsInInputOrder() {

        String expected = "[[Red, Red], [Red, Green], [Red, Blue], [Green, Green], [Green, Blue], [Blue, Blue]]";

        String output = tools.combinations()
                .of(List.of("Red", "Green", "Blue"),2)
                .repetitive()
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListForSizeLessThanOrEqualsZero() {

        String expected ="[[]]";

        String output = tools.combinations()
                .of(List.of("A","B"),0)
                .repetitive()
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldAbleToGenerateRepetitivePermutationForKGreaterThanN() {

        String expected ="[[A, A, A], [A, A, B], [A, B, B], [B, B, B]]";

        String output = tools.combinations()
                .of(List.of("A","B"),3)
                .repetitive()
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }
}