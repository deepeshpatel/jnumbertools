package io.github.deepeshpatel.jnumbertools.generator.subset;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.subsets;
import static org.junit.Assert.assertEquals;

public class SubsetGeneratorTest {

    @Test
    public void assertCountOfAllSubsets() {
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            int count = (int) subsets.of(input)
                    .all().lexOrder()
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
                int count = (int) subsets.of(input)
                        .inRange(from, range).lexOrder()
                        .stream().count();
                assertSize(count,n,from,range);
            }
        }
    }

    private void assertSize(int count, int n, int from, int to) {
        long expected = 0;
        for(int i=from ; i<=to; i++){
            expected += calculator.nCr(n,i).longValue() ;
        }
        Assert.assertEquals(expected, count);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowSubsetOfNullCollection() {
        subsets.of((Collection<String>)null).all();
    }

    @Test
    public void shouldReturnAllSubsetsInInputOrderByDefault() {

        String expected = "[[C], [B], [A], [C, B], [C, A], [B, A], [C, B, A]]";

        String output = subsets
                .of("C", "B", "A")
                .inRange(1, 3).lexOrder()
                .stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldReturnAllSubsetsInGivenRange() {

        String expected = "[[A, B], [A, C], [B, C], [A, B, C]]";

        String output = subsets
                .of("A", "B", "C")
                .inRange(2, 3).lexOrder()
                .stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateAllPossibleSubsetsWithAllMethod() {
        String expected = "[[], [A], [B], [C], [A, B], [A, C], [B, C], [A, B, C]]";
        String output = subsets.of("A", "B", "C")
                .all().lexOrder().stream().toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateEmptyForRangeFromZeroToZero() {
        String output = subsets
                .of("A", "B", "C")
                .inRange(0, 0).lexOrder()
                .stream().toList().toString();

        assertEquals("[[]]", output);
    }

    @Test
    public void shouldGenerateSubsetFromGivenInputSize() {
        String output = subsets
                .of(3)
                .all().lexOrder()
                .stream().toList().toString();

        assertEquals("[[], [0], [1], [2], [0, 1], [0, 2], [1, 2], [0, 1, 2]]", output);
    }

}
