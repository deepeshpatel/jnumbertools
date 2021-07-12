package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationTest {

    @Test
    public void assertCount() {

        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int r=0; r<=n; r++) {
                int count = (int) JNumberTools.combinationsOf(input)
                        .unique(r).stream().count();
                Assert.assertEquals(nCr(n,r), count);
            }
        }
    }

    @Test
    public void  shouldGenerateCombinationsInInputOrder() {

        String expected = "[[Red, Green], [Red, Blue], [Green, Blue]]";

        String output = JNumberTools
                .combinationsOf("Red", "Green", "Blue")
                .unique(2)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListForSizeLessThanOrEqualsZero() {

        String expected ="[[]]";

        String output = JNumberTools
                .combinationsOf("A","B")
                .unique(0)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListForSizeGreaterThanN() {

        String expected ="[]";

        String output = JNumberTools
                .combinationsOf("A","B")
                .unique(3)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void  shouldGenerateEmptyListForNullInput() {

        String expected = "[]";

        String output = JNumberTools
                .combinationsOf()
                .unique(2)
                .stream()
                .collect(Collectors.toList()).toString();

        //System.out.println(output);
        assertEquals(expected, output);
    }
}
