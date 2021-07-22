package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;
import static org.junit.Assert.assertEquals;

public class UniquePermutationNthTest {

    @Test
    public void assertCount(){
        for(int n=0; n<6; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int skip=1; skip<=4; skip++) {
                long size = JNumberTools.permutationsOf(input).uniqueNth(skip).stream().count();
                double expected = Math.ceil(nPr(n,n)/(double)skip);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2, 3], [2, 1, 3], [3, 1, 2]]";
        String actual   = JNumberTools.permutationsOf("1", "2", "3")
                .uniqueNth(2)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test (expected = NullPointerException.class)
    public void shouldThrowExceptionWithNullInput(){
        JNumberTools.permutationsOf((Collection<Object>) null)
                .uniqueNth(2);
    }

    @Test
    public void shouldGenerateEmptyListForEmptyInput(){
        String expected = "[[]]";
        String actual   = JNumberTools.permutationsOf(new ArrayList<String>())
                .uniqueNth(2)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateUniqueNthPermutations() {
        String expected = "[[Red, Green, Blue]," +
                " [Green, Red, Blue]," +
                " [Blue, Red, Green]]";// +

        List<List<String>> output = JNumberTools.permutationsOf("Red", "Green", "Blue")
                .uniqueNth(2) //skip every 1 permutation in between
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldSupportVeryLargePermutations() {

        List<String> input = Arrays.asList("0", "1","2","3","4","5","6","7","8","9","10","11","12");

        String expected = "[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], " +
                "[2, 1, 0, 8, 10, 7, 9, 12, 4, 6, 11, 3, 5], " +
                "[4, 2, 1, 3, 8, 5, 7, 11, 10, 6, 9, 0, 12], " +
                "[6, 3, 1, 10, 2, 11, 0, 9, 4, 5, 7, 8, 12], " +
                "[8, 4, 2, 3, 12, 5, 10, 7, 1, 9, 11, 0, 6], " +
                "[10, 5, 2, 11, 7, 12, 4, 3, 8, 1, 0, 6, 9], " +
                "[12, 6, 3, 5, 4, 8, 1, 7, 0, 2, 2, 10, 11]]";

        String actual   =

                JNumberTools.permutationsOf(input)
                        .uniqueNth(1000_000_000)// jump to 1 billionth permutation
                        .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGeneratePermutationsSkippingInBetween() {

        List<String> input = Arrays.asList("A","B","C","D","E","F");
        for(int skip=1; skip<=32;skip++) {
            String expected = getExpectedResultViaOneByOneIteration(input, skip);
            String output   = getResultViaDirectSkipping(input,skip);
            Assert.assertEquals(expected,output);
        }
    }

    private String getResultViaDirectSkipping(List<String> input, int skip) {
        return JNumberTools.permutationsOf(input)
                .uniqueNth(skip).stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int skip) {
        Iterable<List<String>> iterable = JNumberTools.permutationsOf(input)
                .unique();

        return TestUtil.collectSkippedValues(iterable, skip).toString();
    }
}

