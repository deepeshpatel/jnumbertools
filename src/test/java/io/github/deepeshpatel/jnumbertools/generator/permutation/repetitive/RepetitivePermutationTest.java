package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class RepetitivePermutationTest {

    @Test
    void assertCount(){
        for(int n=1; n<3; n++) {
            var input = Collections.nCopies(n, "A");
            for(int r=0; r<=n+1; r++) {
                long size = permutation.repetitive(r, input)
                        .lexOrder()
                        .stream().count();

                assertEquals((int)Math.pow(n,r), size);
            }
        }
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
