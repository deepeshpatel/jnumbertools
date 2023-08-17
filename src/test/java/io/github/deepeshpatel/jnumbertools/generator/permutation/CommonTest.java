package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class CommonTest {

    @Test
    public void all3NthPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {
            for(int increment = 1; increment<=10; increment++) {

                String uniquePermutationValues = JNumberTools.permutations().of(size).
                        uniqueNth(increment).stream().toList().toString();

                String multisetPermutationValues = JNumberTools.permutations().of(size)
                        .multisetNth(increment, Collections.nCopies(size, 1))
                        .stream().toList().toString();

                String kPermutationValues = JNumberTools.permutations().of(size).k(size)
                        .lexOrderNth(increment)
                        .stream().toList().toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
            }
        }
    }

    @Test
    public void all3NextPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {

                String uniquePermutationValues = JNumberTools.permutations().of(size).
                        unique().stream().toList().toString();

                String multisetPermutationValues = JNumberTools.permutations().of(size)
                        .multiset(Collections.nCopies(size, 1))
                        .stream().toList().toString();

                String kPermutationValues = JNumberTools.permutations().of(size).k(size)
                        .lexOrder()
                        .stream().toList().toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
        }
    }

    @Test
    public void all6LexOrderPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){
        for(int size=1; size<=5; size++) {
            int increment = 1;

            String uniquePermutationNth = JNumberTools.permutations().of(size).
                    uniqueNth(increment).stream().toList().toString();

            String multisetPermutationNth = JNumberTools.permutations().of(size)
                    .multisetNth(increment, Collections.nCopies(size, 1))
                    .stream().toList().toString();

            String kPermutationNth = JNumberTools.permutations().of(size).k(size)
                    .lexOrderNth(increment)
                    .stream().toList().toString();

            String uniquePermutation = JNumberTools.permutations().of(size).
                    unique().stream().toList().toString();

            String multisetPermutation = JNumberTools.permutations().of(size)
                    .multiset(Collections.nCopies(size, 1))
                    .stream().toList().toString();

            String kPermutation = JNumberTools.permutations().of(size).k(size)
                    .lexOrder()
                    .stream().toList().toString();

            Assert.assertEquals(uniquePermutation, uniquePermutationNth);
            Assert.assertEquals(uniquePermutation, kPermutation);
            Assert.assertEquals(uniquePermutation, kPermutationNth);
            Assert.assertEquals(uniquePermutation, multisetPermutation);
            Assert.assertEquals(uniquePermutation, multisetPermutationNth);
        }
    }
}
