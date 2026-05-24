/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link RepetitiveCombination} (lexicographic order).
 *
 * <p>Generates all multiset combinations of size r chosen from n elements
 * with repetition allowed. Count = C(n+r-1, r).
 */
@DisplayName("Repetitive Combinations (Lex Order)")
class RepetitiveCombinationTest {

    // =========================================================
    // 1. Count correctness: C(n+r-1, r)
    // =========================================================

    @Nested
    @DisplayName("Count: C(n+r-1, r)")
    class CountTests {

        @Test
        @DisplayName("Count equals C(n+r-1, r) for small n and r")
        void countFormula() {
            for (int n = 1; n <= 4; n++) {
                List<String> input = Collections.nCopies(n, "A");
                for (int r = 1; r <= 4; r++) {
                    long count = combination.repetitive(r, input).lexOrder().stream().count();
                    long expected = calculator.nCr(n + r - 1, r).longValue();
                    assertEquals(expected, count,
                            "C(" + n + "+" + r + "-1," + r + ") mismatch");
                }
            }
        }

        @Test
        @DisplayName("n=1, any r: only 1 combination (all same element)")
        void n1AnyR() {
            for (int r = 1; r <= 5; r++) {
                long count = combination.repetitive(r, "X").lexOrder().stream().count();
                assertEquals(1L, count, "n=1 r=" + r + " should have 1 combination");
            }
        }

        @Test
        @DisplayName("r=0: always 1 combination — the empty selection")
        void r0AlwaysOneEmpty() {
            var builder = combination.repetitive(0, "A", "B", "C");
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=0, r>0: no combinations possible")
        void n0PositiveR() {
            var builder = combination.repetitive(2, Collections.emptyList());
            assertEquals(BigInteger.ZERO, builder.count());
            assertTrue(builder.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("count() matches stream count for various (n,r)")
        void builderCountMatchesStreamCount() {
            int[][] pairs = {{3,2},{4,3},{5,2},{2,4}};
            for (int[] p : pairs) {
                int n = p[0], r = p[1];
                var input = Collections.nCopies(n, "X");
                var builder = combination.repetitive(r, input);
                long streamCount = builder.lexOrder().stream().count();
                assertEquals(builder.count().longValue(), streamCount,
                        "n=" + n + " r=" + r + " count mismatch");
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
        @DisplayName("All C(3+2-1,2)=6 repetitive combinations of [A,B,C] choose 2")
        void allCombsOf3Choose2() {
            var expected = List.of(
                    of('A','A'), of('A','B'), of('A','C'),
                    of('B','B'), of('B','C'),
                    of('C','C')
            );
            var output = combination.repetitive(2, A_B_C).lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("All C(2+3-1,3)=4 repetitive combinations of [A,B] choose 3")
        void allCombsOf2Choose3() {
            var expected = List.of(
                    of('A','A','A'), of('A','A','B'),
                    of('A','B','B'), of('B','B','B')
            );
            var output = combination.repetitive(3, A_B).lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("Each combination has exactly r elements")
        void eachCombinationHasRElements() {
            int r = 3;
            var output = combination.repetitive(r, A_B_C).lexOrder().stream().toList();
            for (var combo : output) {
                assertEquals(r, combo.size(), "each combination must have r=" + r + " elements");
            }
        }

        @Test
        @DisplayName("Each combination element comes from the input set")
        void eachElementFromInput() {
            var output = combination.repetitive(3, A_B_C).lexOrder().stream().toList();
            var inputSet = new HashSet<>(A_B_C);
            for (var combo : output) {
                for (var elem : combo) {
                    assertTrue(inputSet.contains(elem),
                            "element " + elem + " not in input");
                }
            }
        }

        @Test
        @DisplayName("Combinations are in non-decreasing lexicographic order")
        void combsInLexOrder() {
            var output = combination.repetitive(3, A_B_C).lexOrder().stream().toList();
            for (int i = 1; i < output.size(); i++) {
                assertFalse(isLexGreater(output.get(i - 1), output.get(i)),
                        "rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }

        @Test
        @DisplayName("All combinations are distinct")
        void allCombinationsAreDistinct() {
            var output = combination.repetitive(3, A_B_C).lexOrder().stream().toList();
            assertEquals(new HashSet<>(output).size(), output.size(),
                    "all repetitive combinations must be distinct");
        }

        @Test
        @DisplayName("Elements within each combination are in non-decreasing order")
        void elementsNonDecreasingWithinCombination() {
            var output = combination.repetitive(3, List.of(1, 2, 3)).lexOrder().stream().toList();
            for (var combo : output) {
                List<Integer> c = (List<Integer>) combo;
                for (int i = 1; i < c.size(); i++) {
                    assertTrue(c.get(i - 1) <= c.get(i),
                            "elements must be non-decreasing in " + combo);
                }
            }
        }
    }

    // =========================================================
    // 3. mth combination
    // =========================================================

    @Nested
    @DisplayName("mth combination (lexOrderMth)")
    class MthCombination {

        @ParameterizedTest(name = "m={0}")
        @ValueSource(ints = {1, 2, 3, 4})
        @DisplayName("mth output matches every-m-th element of full lex stream")
        void mthMatchesEveryMth(int m) {
            var all = combination.repetitive(3, A_B_C).lexOrder();
            var mth = combination.repetitive(3, A_B_C).lexOrderMth(m, 0);
            assertEveryMthValue(all.stream(), mth.stream(), 0, m);
        }
    }

    // =========================================================
    // 4. Iterator contract
    // =========================================================

    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls produce equal results")
        void multipleStreamCallsAreEqual() {
            var iterable = combination.repetitive(2, A_B_C).lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] C(n+r-1,r) count correct for n in [1,10], r in [1,5]")
    void stressTesting() {
        for (int n = 1; n <= 10; n++) {
            for (int r = 1; r <= 5; r++) {
                var input = Collections.nCopies(n, "X");
                long count = combination.repetitive(r, input).lexOrder().stream().count();
                assertEquals(calculator.nCr(n + r - 1, r).longValue(), count,
                        "n=" + n + " r=" + r + " count mismatch");
            }
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    @SuppressWarnings("unchecked")
    private boolean isLexGreater(List<?> a, List<?> b) {
        List<Comparable> ca = (List<Comparable>) a;
        List<Comparable> cb = (List<Comparable>) b;
        for (int i = 0; i < Math.min(ca.size(), cb.size()); i++) {
            int cmp = ca.get(i).compareTo(cb.get(i));
            if (cmp > 0) return true;
            if (cmp < 0) return false;
        }
        return ca.size() > cb.size();
    }
}