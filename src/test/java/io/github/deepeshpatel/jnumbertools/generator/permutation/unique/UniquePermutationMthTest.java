package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static io.github.deepeshpatel.jnumbertools.TestBase.everyMthValue;
import static org.junit.Assert.assertEquals;

public class UniquePermutationMthTest {

    @Test
    public void assertCount(){
        for(int n=0; n<6; n++) {
            var input = Collections.nCopies(n, "A");
            for(int increment=1; increment<=4; increment++) {
                long size = permutation
                        .unique(input)
                        .lexOrderMth(increment)
                        .stream().count();
                double expected = Math.ceil(calculator.nPr(n,n).longValue()/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        UniquePermutationsMth<String> iterable = permutation.unique("A", "B", "C").lexOrderMth(3);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2, 3], [2, 3, 1]]";
        assertEquals(expected, output(3, 1,2,3));
    }

    @Test
    public void shouldGenerateEmptyListForNullInput(){
        assertEquals("[[]]", output(3, (List<Object>) null));
    }

    @Test
    public void shouldGenerateEmptyListForEmptyInput(){
        assertEquals("[[]]", output(2, new ArrayList<>()));
    }

    @Test
    public void shouldGenerateUniqueMthPermutations() {
        String expected =
                "[[Red, Green, Blue]," +
                " [Green, Red, Blue]," +
                " [Blue, Red, Green]]" ;

        Assert.assertEquals(expected, output(2, "Red", "Green", "Blue"));
    }

    @Test
    public void shouldSupportVeryLargePermutations() {

        var input = List.of(0,1,2,3,4,5,6,7,8,9,10,11,12);

        String[] expected = new String[]{
                "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]",
                "[2, 1, 0, 8, 10, 7, 9, 12, 4, 6, 11, 3, 5]",
                "[4, 2, 1, 3, 8, 5, 7, 11, 10, 6, 9, 0, 12]",
                "[6, 3, 1, 10, 2, 11, 0, 9, 4, 5, 7, 8, 12]",
                "[8, 4, 2, 3, 12, 5, 10, 7, 1, 9, 11, 0, 6]",
                "[10, 5, 2, 11, 7, 12, 4, 3, 8, 1, 6, 0, 9]",
                "[12, 6, 3, 5, 4, 8, 1, 7, 0, 2, 9, 10, 11]"
        };

        var actual = permutation.unique(input)
                .lexOrderMth(1000_000_000)// jump to 1 billionth permutation
                .stream().toList();

        for(int i=0; i<expected.length; i++) {
            assertEquals(expected[i],actual.get(i).toString());
        }
    }

    @Test
    public void shouldGenerateMthPermutations() {
        var input = List.of("A","B","C","D","E","F");
        for(int increment=1; increment<=32;increment++) {
            String expected = getExpectedResultViaOneByOneIteration(input, increment);
            String output   = getResultViaDirectIncrement(input,increment);
            Assert.assertEquals(expected, output);
        }
    }

    private String getResultViaDirectIncrement(List<String> elements, int increment) {
        return permutation.unique(elements).lexOrderMth(increment).stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int increment) {
        var stream = permutation.unique(input).lexOrder().stream();
        return everyMthValue(stream, increment).toString();
    }

    private String output(int increment, Object... elements) {
          return permutation.unique(elements).lexOrderMth(increment).stream().toList().toString();
    }

    private String output(int increment,List<?> elements) {
        return permutation.unique(elements).lexOrderMth(increment).stream().toList().toString();
    }
}

