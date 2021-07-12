package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class SubsetGeneratorTest {

    @Test
    public void assertCountOfAllSubsets() {
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            int count = (int) JNumberTools.subsetsOf(input)
                    .all()
                    .stream().count();

            int expected = (int) (Math.pow(2,n));
            Assert.assertEquals(expected, count);
        }
    }

    @Test
    public void assertCountOfSubsetsInRange() {
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int range=2; range<=n+1; range++) {
                int count = (int) JNumberTools.subsetsOf(input)
                        .inRange(2, range)
                        .stream().count();
                assertSize(count,n,2,range);
            }
        }
    }

    private void assertSize(int count, int n, int from, int to) {
        int expected = 0;
        for(int i=from ; i<=to; i++){
            expected += nCr(n,i) ;
        }
        Assert.assertEquals(expected, count);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowSubsetOfNullCollection() {
        JNumberTools.subsetsOf((Collection<String>)null).all();
    }

    @Test
    public void shouldReturnAllSubsetsInInputOrderByDefault() {

        String expected = "[[C], [B], [A], [C, B], [C, A], [B, A], [C, B, A]]";

        String output = JNumberTools
                .subsetsOf("C", "B", "A")
                .inRange(1, 3)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnAllSubsetsInGivenRange() {

        String expected = "[[A, B], [A, C], [B, C], [A, B, C]]";

        String output = JNumberTools
                .subsetsOf("A", "B", "C")
                .inRange(2, 3)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateAllPossibleSubsetsWithAllMethod() {
        String expected = "[[], [A], [B], [C], [A, B], [A, C], [B, C], [A, B, C]]";
        String output = JNumberTools.subsetsOf("A", "B", "C")
                .all().stream().collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldNotGenerateForNegativeSizeButGenerateEmptyForZero() {
        String expected = "[[], [A], [B], [C]]";

        String output = JNumberTools
                .subsetsOf("A", "B", "C")
                .inRange(-3, 1)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

}
