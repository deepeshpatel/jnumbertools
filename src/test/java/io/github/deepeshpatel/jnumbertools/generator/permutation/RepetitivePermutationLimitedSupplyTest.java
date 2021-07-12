package io.github.deepeshpatel.jnumbertools.generator.permutation;


import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RepetitivePermutationLimitedSupplyTest {

    @Test
    public void assertCount() {
        List<Long> fact = factorialList(20);
        for(int n=1; n<=6; n++){
            List<String> input = Collections.nCopies(n, "A");
            int[] supply =getRandomSupply(input.size());
            long count = JNumberTools.permutationsOf(input)
                    .repetitiveWithSupply(supply).stream().count();

            long numerator = fact.get((Arrays.stream(supply).reduce(0, Integer::sum)));
            long denominator = IntStream.of(supply).asLongStream().reduce(1, (a, b) -> (a * fact.get((int)b)));
            long expected = numerator/denominator;
            //( ∑ ai.si)! / Π(si!)
            Assert.assertEquals(expected,count);
        }
    }

    private int[] getRandomSupply(int length) {
        int[] supply = new int[length];
        Random random = new Random(System.currentTimeMillis());
        for(int i=0; i<supply.length; i++) {
            int value = random.nextInt(2)+1;
            supply[i] = value;
        }
        return supply;
    }

    private List<Long> factorialList(int n){
        long p=1;
        List<Long> factorials = new ArrayList<>(n+1);
        factorials.add(1L);
        for(long i=1; i<=n; i++){
            p *=i;
            factorials.add(p);
        }
        return factorials;
    }

    @Test
    public void shouldGenerateAllUniquePermutationsWithLimitedSupply(){
        String expected = "[[A, A, B, C], [A, A, C, B], [A, B, A, C], " +
                "[A, B, C, A], [A, C, A, B], [A, C, B, A], [B, A, A, C], " +
                "[B, A, C, A], [B, C, A, A], [C, A, A, B], [C, A, B, A], [C, B, A, A]]";

        String output = JNumberTools.permutationsOf("A", "B", "C")
                .repetitiveWithSupply(new int[]{2,1,1})
                .stream()
                .collect(Collectors.toList()).toString();

        Assert.assertEquals(expected,output);
    }
}