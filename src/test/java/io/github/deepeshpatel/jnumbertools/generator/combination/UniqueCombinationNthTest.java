package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationNthTest {

    @Test
    public void assertCount(){

        for(int n=3; n<6; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int increment=1; increment<=4; increment++) {
                int combinationSize=input.size()/2;
                long size = JNumberTools.combinationsOf(input,combinationSize).uniqueNth(increment).stream().count();
                double expected = Math.ceil(nCr(n,combinationSize)/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        List<String> input = Arrays.asList("A", "B", "C");
        UniqueCombinationNth<String> iterable = JNumberTools.combinationsOf(input,2).uniqueNth(2);
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void  shouldGenerateCombinationsWithIncrementingToEvery3rdCombination() {

        String expected = "[[0, 1, 2], [0, 2, 3], [1, 2, 3], [2, 3, 4]]";

        String output = JNumberTools
                .combinationsOf(Arrays.asList("0","1","2","3","4"),3)
                .uniqueNth(3)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void  shouldSupportVeryLargeNthCombination() {

        String expected = "[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16], " +
                "[0, 3, 6, 9, 10, 11, 13, 15, 16, 18, 20, 23, 24, 25, 27, 31, 33], " +
                "[2, 4, 10, 13, 14, 17, 18, 20, 22, 23, 26, 27, 29, 30, 31, 32, 33]]";

//        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
//                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
//                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31","32", "33"
        String output = JNumberTools
                .combinationsOfnCr(34,17)
                //.combinationsOf(34,17)
                .uniqueNth(1000_000_000)// jump to 1 billionth combination
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateNthCombinationIncrementingValuesInBetween() {

        List<String> input = Arrays.asList("A","B","C","D","E","F","G","H","I","J");
        for(int k=1; k<=input.size()/2; k++) {
            for(int increment=1; increment<=32;increment++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,increment);
                String output   = getResultViaDirectIncrement(input,k,increment);
                Assert.assertEquals(expected,output);
            }
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int r, int increment) {
        return JNumberTools.combinationsOf(input,r)
                .uniqueNth(increment)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int r, int increment) {
        Stream<List<String>> stream = JNumberTools.combinationsOf(input,r).unique().stream();
        return collectEveryNthValue(stream, increment).toString();
    }

    public static <T> List<List<T>> collectEveryNthValue(Stream<List<T>> stream, int n) {
        final int[] j = {0};
        return stream.filter(e-> j[0]++ % n == 0).collect(Collectors.toList());
    }
}