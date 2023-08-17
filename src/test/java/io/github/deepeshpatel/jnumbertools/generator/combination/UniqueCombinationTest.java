package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationTest {

    @Test
    public void assertCount() {

        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                int count = (int) JNumberTools.combinations().of(input,r)
                        .unique().stream().count();
                Assert.assertEquals(nCr(n,r), count);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var input = List.of("A", "B", "C");
        var iterable = JNumberTools.combinations().of(input,2).unique();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();

        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void  shouldGenerateCombinationsInInputOrder() {

        String expected = "[[Red, Green], [Red, Blue], [Green, Blue]]";
        var input = List.of("Red", "Green", "Blue");

        String output = JNumberTools.combinations()
                .of(input,2)
                .unique()
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListForSizeEqualsZero() {

        String output = JNumberTools.combinations()
                .of(List.of("A","B"),0)
                .unique()
                .stream()
                .toList().toString();

        assertEquals("[[]]", output);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForSizeGreaterThanN() {

        JNumberTools.combinations().of(List.of("A","B"),3)
                .unique();
    }

    @Test
    public void  shouldGenerateEmptyListForNullInput() {
        String output = JNumberTools.combinations()
                .of(Collections.emptyList(),0)
                .unique()
                .stream()
                .toList().toString();

        assertEquals("[[]]", output);
    }
}
