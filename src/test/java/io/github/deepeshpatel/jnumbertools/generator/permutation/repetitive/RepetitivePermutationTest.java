package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class RepetitivePermutationTest {

    @Test
    void assertCount(){
        // nʳ permutations with repetition
        for(int n=1; n<3; n++) {
            var input = Collections.nCopies(n, "A");
            for(int r=1; r<=n+1; r++) {
                long size = permutation.repetitive(r, input)
                        .lexOrder()
                        .stream().count();

                assertEquals((int)Math.pow(n,r), size);
            }
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        //n=0 and r=0 -> 0⁰ = 1
        var zeroZeroBuilder = permutation.repetitive(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, zeroZeroBuilder.count());
        assertTrue(zeroZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

        //n=0 and r>0 -> 0ʳ = 0
        var zeroPositiveBuilder = permutation.repetitive(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, zeroPositiveBuilder.count());
        assertTrue(zeroPositiveBuilder.lexOrder().stream().toList().isEmpty());

        //n>0 and r=0 -> n⁰ = 1
        var positiveZeroBuilder = permutation.repetitive(0, "A", "B");
        assertEquals(BigInteger.ONE, positiveZeroBuilder.count());
        assertTrue(positiveZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());
    }

    
    @Test
    void shouldThrowExpIfIterateAfterLastElement(){
        var iterator = permutation.repetitive(1,"A")
                .lexOrder().iterator();

        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void shouldReturnSameResultViaDifferentIteratorObjects(){
        RepetitivePermutation<String> iterable = permutation
                .repetitive(2,"A", "B", "C")
                .lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateAllPermutationsOf2Values() {
        var expected = List.of(
                of(0, 0, 0),
                of(0, 0, 1),
                of(0, 1, 0),
                of(0, 1, 1),
                of(1, 0, 0),
                of(1, 0, 1),
                of(1, 1, 0),
                of(1, 1, 1)
        );

        var output = permutation.repetitive(3,0,1)
                .lexOrder()
                .stream().toList();

        assertIterableEquals(expected, output);
    }

    @Test
    void shouldGenerateRepetitivePermutations() {
        var expected = List.of(
                of('A', 'A'),
                of('A', 'B'),
                of('A', 'C'),
                of('B', 'A'),
                of('B', 'B'),
                of('B', 'C'),
                of('C', 'A'),
                of('C', 'B'),
                of('C', 'C')
        );

        var output = permutation.repetitive(2, A_B_C)
                .lexOrder()
                .stream().toList();

        assertIterableEquals(expected, output);
    }


    @Test
    void shouldThrowExceptionForNegativeWidth() {
        var exp = assertThrows(IllegalArgumentException.class, () ->
                permutation.repetitive(-1, "A", "B").lexOrder()
        );
        assertEquals(exp.getMessage(), "Width (r) cannot be negative for repetitive permutation generation");
    }


    @Test
    void shouldHandleMaximumWidthForSmallN() {
        // Test with n=2, width=20 (about 1 million permutations - count only)
        long count = permutation.repetitive(20, 0, 1)
                .lexOrder()
                .stream()
                .count();

        assertEquals(1_048_576L, count); // 2^20
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressTesting() {
        for(int n=20; n<=24; n++) {
            for(int width = 0; width<=5; width++) {
                long count = permutation.repetitive(width, n).lexOrder().stream().count();
                assertEquals(calculator.power(n, width).longValue(), count);
            }
        }
    }
}
