/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Derangement} (lexicographical derangement generator).
 */
class DerangementTest {

    @Test
    void assertCount() {
        // !n counts: !0=1, !1=0, !2=1, !3=2, !4=9, !5=44, !6=265
        long[] expected = {1, 0, 1, 2, 9, 44, 265};
        for (int n = 0; n < expected.length; n++) {
            long size = derangement.of(n).lexOrder().stream().count();
            assertEquals(expected[n], size, "Wrong count for n=" + n);
        }
    }

    @Test
    void assertCountAndContentForN0() {
        // n=0 → exactly one empty derangement: [[]]
        var zero = derangement.of(0).lexOrder().stream().toList();
        assertEquals(1, zero.size());
        assertTrue(zero.get(0).isEmpty());
    }

    @Test
    void assertCountAndContentForN1() {
        // n=1 → no derangement exists, iterator is empty
        var one = derangement.of(List.of("A")).lexOrder().stream().toList();
        assertTrue(one.isEmpty());
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        Derangement<String> iterable = derangement.of("A", "B", "C", "D").lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
        assertNotSame(lists1, lists2);
    }

    @Test
    void shouldGenerateAllDerangementsOf3ValuesInLexOrder() {
        // D_3 = 2; derangements of [A,B,C] are [B,C,A] and [C,A,B] in lex order.
        var expected = List.of(
                of("B", "C", "A"),
                of("C", "A", "B")
        );
        var actual = derangement.of("A", "B", "C").lexOrder().stream().toList();
        assertIterableEquals(expected, actual);
    }

    @Test
    void shouldGenerateAllDerangementsOf4Values() {
        // D_4 = 9; verify count + validity + lexicographic strict-increase property.
        var actual = derangement.of("A", "B", "C", "D").lexOrder().stream().toList();
        assertEquals(9, actual.size());
        assertEquals(9, new HashSet<>(actual).size(), "Derangements must be distinct");

        List<Character> origin = List.of('A', 'B', 'C', 'D');
        for (List<String> d : actual) {
            assertEquals(4, d.size());
            // no element should equal its origin position
            for (int i = 0; i < d.size(); i++) {
                assertNotEquals(origin.get(i).toString(), d.get(i),
                        "Fixed point at index " + i + " in " + d);
            }
            // and the multiset of values must equal the multiset of origins
            var sorted = new ArrayList<>(d);
            Collections.sort(sorted);
            assertEquals(List.of("A", "B", "C", "D"), sorted);
        }
    }

    @Test
    void shouldHandleMixedTypes() {
        // n=2 → only derangement is [b, a] (swap)
        var expected = List.of(of("A", 1));
        var actual = derangement.of(1, "A").lexOrder().stream().toList();
        assertIterableEquals(expected, actual);
    }

    @Test
    void shouldReturnImmutableOuterCollection() {
        var results = derangement.of("A", "B", "C", "D").lexOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(of("X")));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));
    }

    @Test
    void shouldMatchByRankUnranking() {
        // The k-th element of lexOrder() must equal unrankOf.derangement(k, n).
        int n = 5;
        var lexList = derangement.of(n).lexOrder().stream().toList();
        BigInteger total = calculator.subFactorial(n);
        assertEquals(total.intValue(), lexList.size());

        for (int rank = 0; rank < lexList.size(); rank++) {
            int[] viaUnrank = unrankOf.derangement(rank, n);
            var expected = new ArrayList<Integer>(n);
            for (int v : viaUnrank) expected.add(v);
            assertEquals(expected, lexList.get(rank),
                    "Mismatch at rank " + rank + " for n=" + n);
        }
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressTesting() {
        for (int n = 0; n <= 9; n++) {
            long count = derangement.of(n).lexOrder().stream().count();
            assertEquals(calculator.subFactorial(n).longValue(), count);
        }
    }
}

