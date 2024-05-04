package io.github.deepeshpatel.jnumbertools.generator.combination;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.tools;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationTest {

    @Test
    public void assertCount() {

        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                long count = tools.combinations().of(input,r)
                        .unique().stream().count();
                Assert.assertEquals(calculator.nCr(n,r).longValue(), count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var input = List.of("A", "B", "C");
        var iterable = tools.combinations().of(input,2).unique();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();

        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void  shouldGenerateCombinationsInInputOrder() {

        String expected = "[[Red, Green], [Red, Blue], [Green, Blue]]";
        var input = List.of("Red", "Green", "Blue");

        String output = tools.combinations()
                .of(input,2)
                .unique()
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListForSizeEqualsZero() {

        String output = tools.combinations()
                .of(List.of("A","B"),0)
                .unique()
                .stream()
                .toList().toString();

        assertEquals("[[]]", output);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForSizeGreaterThanN() {

        tools.combinations().of(List.of("A","B"),3)
                .unique();
    }

    @Test
    public void  shouldGenerateEmptyListForNullInput() {
        String output = tools.combinations()
                .of(Collections.emptyList(),0)
                .unique()
                .stream()
                .toList().toString();

        assertEquals("[[]]", output);
    }
}
