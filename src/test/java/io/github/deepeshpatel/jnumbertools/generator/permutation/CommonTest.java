package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;

public class CommonTest {

    @Test
    public void all3NthPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {
            for(int increment = 1; increment<=10; increment++) {

                String uniquePermutationValues = JNumberTools.permutationsOfSize(size).
                        uniqueNth(increment).stream().collect(Collectors.toList()).toString();

                String multisetPermutationValues = JNumberTools.permutationsOfSize(size)
                        .multisetNth(increment, Collections.nCopies(size, 1))
                        .stream().collect(Collectors.toList()).toString();

                String kPermutationValues = JNumberTools.permutationsOfSize(size).k(size)
                        .lexOrderNth(increment)
                        .stream().collect(Collectors.toList()).toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
            }
        }
    }

    @Test
    public void all3NextPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {

                String uniquePermutationValues = JNumberTools.permutationsOfSize(size).
                        unique().stream().collect(Collectors.toList()).toString();

                String multisetPermutationValues = JNumberTools.permutationsOfSize(size)
                        .multiset(Collections.nCopies(size, 1))
                        .stream().collect(Collectors.toList()).toString();

                String kPermutationValues = JNumberTools.permutationsOfSize(size).k(size)
                        .lexOrder()
                        .stream().collect(Collectors.toList()).toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
        }
    }

    @Test
    public void all6LexOrderPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){
        for(int size=1; size<=5; size++) {
            int increment = 1;

            String uniquePermutationNth = JNumberTools.permutationsOfSize(size).
                    uniqueNth(increment).stream().collect(Collectors.toList()).toString();

            String multisetPermutationNth = JNumberTools.permutationsOfSize(size)
                    .multisetNth(increment, Collections.nCopies(size, 1))
                    .stream().collect(Collectors.toList()).toString();

            String kPermutationNth = JNumberTools.permutationsOfSize(size).k(size)
                    .lexOrderNth(increment)
                    .stream().collect(Collectors.toList()).toString();

            String uniquePermutation = JNumberTools.permutationsOfSize(size).
                    unique().stream().collect(Collectors.toList()).toString();

            String multisetPermutation = JNumberTools.permutationsOfSize(size)
                    .multiset(Collections.nCopies(size, 1))
                    .stream().collect(Collectors.toList()).toString();

            String kPermutation = JNumberTools.permutationsOfSize(size).k(size)
                    .lexOrder()
                    .stream().collect(Collectors.toList()).toString();

            Assert.assertEquals(uniquePermutation, uniquePermutationNth);
            Assert.assertEquals(uniquePermutation, kPermutation);
            Assert.assertEquals(uniquePermutation, kPermutationNth);
            Assert.assertEquals(uniquePermutation, multisetPermutation);
            Assert.assertEquals(uniquePermutation, multisetPermutationNth);
        }
    }
}
