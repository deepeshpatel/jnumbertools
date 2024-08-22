package io.github.deepeshpatel.jnumbertools.generator.combination;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationTest {

    @Test
    public void assertCount() {

        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                long count = combination.unique(r, input)
                        .lexOrder().stream().count();
                Assert.assertEquals(calculator.nCr(n,r).longValue(), count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = combination.unique(2, "A", "B", "C").lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void  shouldGenerateCombinationsInInputOrder() {
        var elements = List.of("Red", "Green", "Blue");
        String expected = "[[Red, Green], [Red, Blue], [Green, Blue]]";
        assertEquals(expected, output(2, elements));
    }

    @Test
    public void shouldReturnEmptyListForSizeEqualsZero() {
        assertEquals("[[]]", output(0, List.of("A")));
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForSizeGreaterThanN() {
        output(3, List.of("A"));
    }

    @Test
    public void  shouldGenerateEmptyListForNullInput() {
        assertEquals("[[]]", output(0,Collections.emptyList()));
    }

    private String output(int size, List<String> elements) {
        return combination.unique(size, elements).lexOrder().stream().toList().toString();
    }
}
