package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCr;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationNthTest {

    @Test
    public void assertCount(){
        int r=3;
        for(int n=0; n<6; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for(int skip=1; skip<=4; skip++) {
                long size = JNumberTools.combinationsOf(input).uniqueNth(r,skip).stream().count();
                double expected = Math.ceil(nCr(n,r)/(double)skip);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void  shouldGenerateCombinationsWithSkippingToEvery2ndCombination() {

        String expected = "[[0, 1, 2], [0, 2, 3], [1, 2, 3], [2, 3, 4]]";

        String output = JNumberTools
                .combinationsOf("0","1","2","3","4")
                .uniqueNth(3,3)
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }

    @Test
    public void  shouldSupportVeryLargeCombinationSkipping() {

        String expected = "[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16], " +
                "[0, 3, 6, 9, 10, 11, 13, 15, 16, 18, 20, 23, 24, 25, 27, 31, 33], " +
                "[2, 4, 10, 13, 14, 17, 18, 20, 22, 23, 26, 27, 29, 30, 31, 32, 33]]";

        String output = JNumberTools
                .combinationsOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31","32", "33")
//                )
                .uniqueNth(17, 1000_000_000)// jump to 1 billionth combination
                .stream()
                .collect(Collectors.toList()).toString();

        assertEquals(expected, output);
    }
}