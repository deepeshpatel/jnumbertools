package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class SubsetGeneratorTest {

    @Test
    public void assertCountOfAllSubsets() {
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            int count = (int) JNumberTools.subsets().of(input)
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
            int from=0;
            for(int range=0; range<=n; range++) {
                int count = (int) JNumberTools.subsets().of(input)
                        .inRange(from, range)
                        .stream().count();
                assertSize(count,n,from,range);
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
        JNumberTools.subsets().of((Collection<String>)null).all();
    }

    @Test
    public void shouldReturnAllSubsetsInInputOrderByDefault() {

        String expected = "[[C], [B], [A], [C, B], [C, A], [B, A], [C, B, A]]";

        String output = JNumberTools.subsets()
                .of("C", "B", "A")
                .inRange(1, 3)
                .stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnAllSubsetsInGivenRange() {

        String expected = "[[A, B], [A, C], [B, C], [A, B, C]]";

        String output = JNumberTools.subsets()
                .of("A", "B", "C")
                .inRange(2, 3)
                .stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateAllPossibleSubsetsWithAllMethod() {
        String expected = "[[], [A], [B], [C], [A, B], [A, C], [B, C], [A, B, C]]";
        String output = JNumberTools.subsets().of("A", "B", "C")
                .all().stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateEmptyForRangeFromZeroToZero() {
        String output = JNumberTools.subsets()
                .of("A", "B", "C")
                .inRange(0, 0)
                .stream().toList().toString();

        assertEquals("[[]]", output);
    }

    @Test
    public void shouldGenerateSubsetFromGivenInputSize() {
        String output = JNumberTools.subsets()
                .of(3)
                .all()
                .stream().toList().toString();

        assertEquals("[[], [0], [1], [2], [0, 1], [0, 2], [1, 2], [0, 1, 2]]", output);
    }

}
