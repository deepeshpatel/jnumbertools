/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link RepetitivePermutation} (lexicographic order).
 * Generates all n^r ordered selections with repetition from n elements chosen r at a time.
 */
@DisplayName("Repetitive Permutations (Lex Order)")
class RepetitivePermutationTest {

    // =========================================================
    // 1. Count correctness: n^r
    // =========================================================

    @Nested
    @DisplayName("Count: n^r permutations")
    class CountTests {

        @Test
        @DisplayName("Count equals n^r for n in [1,3], r in [1,n+1]")
        void countIsNPowerR() {
            for (int n = 1; n <= 3; n++) {
                var input = Collections.nCopies(n, "A");
                for (int r = 1; r <= n + 1; r++) {
                    long size = permutation.repetitive(r, input).lexOrder().stream().count();
                    assertEquals((long) Math.pow(n, r), size,
                            "n=" + n + " r=" + r + " should produce " + (long)Math.pow(n,r));
                }
            }
        }

        @Test
        @DisplayName("n=0, r=0 → 0^0 = 1, returns [[]]")
        void zeroChooseZero() {
            var builder = permutation.repetitive(0, Collections.emptyList());
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=0, r>0 → 0^r = 0, returns []")
        void zeroElementsPositiveWidth() {
            var builder = permutation.repetitive(2, Collections.emptyList());
            assertEquals(BigInteger.ZERO, builder.count());
            assertTrue(builder.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("n>0, r=0 → n^0 = 1, returns [[]]")
        void positiveElementsZeroWidth() {
            var builder = permutation.repetitive(0, "A", "B");
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("count() matches stream count for various (n, r)")
        void builderCountMatchesStreamCount() {
            int[][] pairs = {{2, 3}, {3, 2}, {4, 2}, {2, 5}};
            for (int[] p : pairs) {
                int n = p[0], r = p[1];
                var input = Collections.nCopies(n, "X");
                var builder = permutation.repetitive(r, input);
                long streamCount = builder.lexOrder().stream().count();
                assertEquals(builder.count().longValue(), streamCount,
                        "n=" + n + " r=" + r + ": count() vs stream mismatch");
            }
        }
    }

    // =========================================================
    // 2. Content correctness
    // =========================================================

    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("All 8 width-3 repetitive permutations of [0,1] in lex order")
        void allPermsOf2ElementsWidth3() {
            var expected = List.of(
                    of(0, 0, 0), of(0, 0, 1),
                    of(0, 1, 0), of(0, 1, 1),
                    of(1, 0, 0), of(1, 0, 1),
                    of(1, 1, 0), of(1, 1, 1)
            );
            var output = permutation.repetitive(3, 0, 1).lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("All 9 width-2 repetitive permutations of [A,B,C] in lex order")
        void allPermsOf3ElementsWidth2() {
            var expected = List.of(
                    of('A', 'A'), of('A', 'B'), of('A', 'C'),
                    of('B', 'A'), of('B', 'B'), of('B', 'C'),
                    of('C', 'A'), of('C', 'B'), of('C', 'C')
            );
            var output = permutation.repetitive(2, A_B_C).lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("Each tuple has exactly r elements")
        void eachTupleHasRElements() {
            int r = 3;
            var output = permutation.repetitive(r, "A", "B").lexOrder().stream().toList();
            for (var tuple : output) {
                assertEquals(r, tuple.size(), "each tuple must have r=" + r + " elements");
            }
        }

        @Test
        @DisplayName("Each tuple element comes from the input set")
        void eachElementFromInputSet() {
            var input = of("X", "Y");
            var output = permutation.repetitive(3, input).lexOrder().stream().toList();
            var inputSet = new HashSet<>(input);
            for (var tuple : output) {
                for (var elem : tuple) {
                    assertTrue(inputSet.contains(elem),
                            "element " + elem + " not in input set");
                }
            }
        }

        @Test
        @DisplayName("All tuples are distinct")
        void allTuplesAreDistinct() {
            var output = permutation.repetitive(3, "A", "B").lexOrder().stream().toList();
            assertEquals(new HashSet<>(output).size(), output.size(),
                    "all tuples must be distinct");
        }

        @Test
        @DisplayName("Tuples are in non-decreasing lexicographic order")
        void tuplesInLexOrder() {
            var output = permutation.repetitive(3, 0, 1, 2).lexOrder().stream().toList();
            for (int i = 1; i < output.size(); i++) {
                List<Integer> prev = (List<Integer>) output.get(i - 1);
                List<Integer> curr = (List<Integer>) output.get(i);
                assertTrue(isLexLessOrEqual(prev, curr),
                        "rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }

        @Test
        @DisplayName("Width-1: each element appears exactly once in lex order")
        void width1ProducesEachElementOnce() {
            var output = permutation.repetitive(1, "A", "B", "C").lexOrder().stream().toList();
            assertEquals(List.of(of("A"), of("B"), of("C")), output);
        }
    }

    // =========================================================
    // 3. mth permutation
    // =========================================================

    @Nested
    @DisplayName("mth permutation (lexOrderMth)")
    class MthPermutation {

        @Test
        @DisplayName("mth output matches every-m-th element of full lex stream")
        void mthMatchesEveryMth() {
            for (int m = 1; m <= 5; m++) {
                var all = permutation.repetitive(3, "A", "B", "C").lexOrder();
                var mth = permutation.repetitive(3, "A", "B", "C").lexOrderMth(m, 0);
                assertEveryMthValue(all.stream(), mth.stream(), 0, m);
            }
        }

        @ParameterizedTest(name = "start={0}")
        @ValueSource(ints = {0, 1, 2, 5})
        @DisplayName("mth with start offset matches full stream from that offset")
        void mthWithStartOffset(int start) {
            var all = permutation.repetitive(3, 0, 1).lexOrder();
            var mth = permutation.repetitive(3, 0, 1).lexOrderMth(3, start);
            assertEveryMthValue(all.stream(), mth.stream(), start, 3);
        }
    }

    // =========================================================
    // 4. Iterator contract
    // =========================================================

    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Iterating past the last element throws NoSuchElementException")
        void throwsAfterLastElement() {
            var iterator = permutation.repetitive(1, "A").lexOrder().iterator();
            iterator.next();
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        @DisplayName("Multiple stream() calls produce equal results")
        void multipleStreamCallsAreEqual() {
            RepetitivePermutation<String> iterable =
                    permutation.repetitive(2, "A", "B", "C").lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertEquals(list1, list2);
        }

        @Test
        @DisplayName("Negative width throws IllegalArgumentException")
        void negativeWidthThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> permutation.repetitive(-1, "A", "B").lexOrder());
        }

        @Test
        @DisplayName("Large width (2^20) produces correct count")
        void largeWidthCount() {
            long count = permutation.repetitive(20, 0, 1).lexOrder().stream().count();
            assertEquals(1_048_576L, count); // 2^20
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] n^r count correct for n in [20,24], r in [0,5]")
    void stressTesting() {
        for (int n = 20; n <= 24; n++) {
            for (int r = 0; r <= 5; r++) {
                long count = permutation.repetitive(r, n).lexOrder().stream().count();
                assertEquals(calculator.power(n, r).longValue(), count);
            }
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private boolean isLexLessOrEqual(List<Integer> a, List<Integer> b) {
        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
            int cmp = Integer.compare(a.get(i), b.get(i));
            if (cmp < 0) return true;
            if (cmp > 0) return false;
        }
        return a.size() <= b.size();
    }
}