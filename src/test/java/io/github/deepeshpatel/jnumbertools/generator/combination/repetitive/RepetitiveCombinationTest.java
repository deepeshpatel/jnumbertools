package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;


import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static org.junit.Assert.assertEquals;

public class RepetitiveCombinationTest {

    @Test
    public void assertCount() {

        for(int n=1; n<=4; n++) {
            var input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                long count = combination.repetitive(r, input)
                        .lexOrder().stream().count();
                long expectedCount = calculator.nCrRepetitive(n,r).longValue();
                Assert.assertEquals(expectedCount, count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = combination
                .repetitive(2, "A", "B", "C").lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCombinationsInInputOrder() {
        String expected = "[[Red, Red], [Red, Green], [Red, Blue], [Green, Green], [Green, Blue], [Blue, Blue]]";
        assertEquals(expected, output(2,"Red", "Green", "Blue"));
    }

    @Test
    public void shouldReturnEmptyListForSizeLessThanOrEqualsZero() {
        String expected ="[[]]";
        assertEquals(expected, output(0,"A","B"));
    }

    @Test
    public void shouldAbleToGenerateRepetitivePermutationForSizeGreaterThanN() {
        String expected ="[[A, A, A], [A, A, B], [A, B, B], [B, B, B]]";
        assertEquals(expected, output(3,"A","B" ));
    }

    private String output(int size, String... elements) {
        return combination
                .repetitive(size,elements)
                .lexOrder()
                .stream()
                .toList().toString();
    }

    @Test (expected = IllegalArgumentException.class)
    public void should_throw_exception_for_negative_r_value() {
        combination.repetitive(3, -2).lexOrder();
    }
}