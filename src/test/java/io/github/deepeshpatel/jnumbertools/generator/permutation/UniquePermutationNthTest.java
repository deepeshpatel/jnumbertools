package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNthTest.collectEveryNthValue;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;
import static org.junit.Assert.assertEquals;

public class UniquePermutationNthTest {

    @Test
    public void assertCount(){
        for(int n=0; n<6; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int increment=1; increment<=4; increment++) {
                long size = JNumberTools.permutationsOf(input).uniqueNth(increment).stream().count();
                double expected = Math.ceil(nPr(n,n)/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        UniquePermutationsNth<String> iterable = JNumberTools.permutationsOf("A", "B", "C").uniqueNth(3);
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2, 3], [2, 1, 3], [3, 1, 2]]";
        String actual   = JNumberTools.permutationsOf("1", "2", "3")
                .uniqueNth(2)
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateEmptyListForNullInput(){
        String actual  = JNumberTools.permutationsOf((Collection<Object>) null)
                .uniqueNth(2)
                .stream().collect(Collectors.toList()).toString();
        assertEquals("[[]]",actual);
    }

    @Test
    public void shouldGenerateEmptyListForEmptyInput(){
        String actual   = JNumberTools.permutationsOf(new ArrayList<String>())
                .uniqueNth(2)
                .stream().collect(Collectors.toList()).toString();

        assertEquals("[[]]",actual);
    }

    @Test
    public void shouldGenerateUniqueNthPermutations() {
        String expected = "[[Red, Green, Blue]," +
                " [Green, Red, Blue]," +
                " [Blue, Red, Green]]";

        List<List<String>> output = JNumberTools.permutationsOf("Red", "Green", "Blue")
                .uniqueNth(2) //increment to every 2nd permutation
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldSupportVeryLargePermutations() {

        List<String> input = Arrays.asList("0", "1","2","3","4","5","6","7","8","9","10","11","12");

        String[] expected = new String[]{
                "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]",
                "[2, 1, 0, 8, 10, 7, 9, 12, 4, 6, 11, 3, 5]",
                "[4, 2, 1, 3, 8, 5, 7, 11, 10, 6, 9, 0, 12]",
                "[6, 3, 1, 10, 2, 11, 0, 9, 4, 5, 7, 8, 12]",
                "[8, 4, 2, 3, 12, 5, 10, 7, 1, 9, 11, 0, 6]",
                "[10, 5, 2, 11, 7, 12, 4, 3, 8, 1, 6, 0, 9]",
                "[12, 6, 3, 5, 4, 8, 1, 7, 0, 2, 9, 10, 11]"
        };

        List<List<String>> actual = JNumberTools.permutationsOf(input)
                .uniqueNth(1000_000_000)// jump to 1 billionth permutation
                .stream().collect(Collectors.toList());

        for(int i=0; i<expected.length; i++) {
            assertEquals(expected[i],actual.get(i).toString());
        }
    }

    @Test
    public void shouldGenerateNthPermutations() {

        List<String> input = Arrays.asList("A","B","C","D","E","F");
        for(int increment=1; increment<=32;increment++) {


            String expected = getExpectedResultViaOneByOneIteration(input, increment);
            String output   = getResultViaDirectIncrement(input,increment);
            Assert.assertEquals(expected,output);
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int increment) {
        return JNumberTools.permutationsOf(input)
                .uniqueNth(increment).stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int increment) {
        Stream<List<String>> stream = JNumberTools.permutationsOf(input).unique().stream();
        return collectEveryNthValue(stream, increment).toString();
    }
}

