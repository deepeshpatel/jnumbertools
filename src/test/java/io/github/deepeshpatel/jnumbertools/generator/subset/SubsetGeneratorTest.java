/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link SubsetGenerator} (lexicographic order).
 *
 * <p>Generates subsets of a set in lexicographic order, either all 2ⁿ subsets
 * or those in a given size range [from, to].
 */
@DisplayName("Subset Generator (Lex Order)")
class SubsetGeneratorTest {

    // =========================================================
    // 1. Count correctness
    // =========================================================

    @Nested
    @DisplayName("Count: 2^n total, Σ C(n,i) for ranges")
    class CountTests {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {0, 1, 2, 3, 4})
        @DisplayName("Full power-set count equals 2^n")
        void fullPowerSetCount(int n) {
            List<String> input = Collections.nCopies(n, "A");
            long count = subsets.of(input).all().lexOrder().stream().count();
            assertEquals(1L << n, count, "2^" + n + " subsets expected");
        }

        @Test
        @DisplayName("count() matches stream count for all() and various inRange() calls")
        void builderCountMatchesStreamCount() {
            for (int n = 0; n <= 4; n++) {
                List<String> input = Collections.nCopies(n, "A");
                // all()
                var allBuilder = subsets.of(input).all();
                assertEquals(allBuilder.count().longValue(),
                        allBuilder.lexOrder().stream().count(),
                        "all() count mismatch for n=" + n);
                // inRange(0, n)
                for (int to = 0; to <= n; to++) {
                    var rangeBuilder = subsets.of(input).inRange(0, to);
                    assertEquals(rangeBuilder.count().longValue(),
                            rangeBuilder.lexOrder().stream().count(),
                            "inRange(0," + to + ") count mismatch for n=" + n);
                }
            }
        }

        @Test
        @DisplayName("inRange(from, to) count equals Σ C(n,i) for i in [from,to]")
        void rangeCountIsCorrect() {
            for (int n = 0; n <= 4; n++) {
                List<String> input = Collections.nCopies(n, "A");
                for (int from = 0; from <= n; from++) {
                    for (int to = from; to <= n; to++) {
                        long count = subsets.of(input).inRange(from, to)
                                .lexOrder().stream().count();
                        long expected = 0;
                        for (int i = from; i <= to; i++) {
                            expected += calculator.nCr(n, i).longValue();
                        }
                        assertEquals(expected, count,
                                "n=" + n + " inRange(" + from + "," + to + ") mismatch");
                    }
                }
            }
        }

        @Test
        @DisplayName("n=0, all() → 1 subset: [[]]")
        void emptySetHasOneSubset() {
            var builder = subsets.of(Collections.emptyList()).all();
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=0, inRange(1,2) → 0 subsets")
        void emptySetInRange1to2() {
            var builder = subsets.of(Collections.emptyList()).inRange(1, 2);
            assertEquals(BigInteger.ZERO, builder.count());
            assertTrue(builder.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("inRange(0,0) → always 1 subset: [[]]")
        void range0to0ProducesEmptySubset() {
            var result = subsets.of(A_B_C).inRange(0, 0).lexOrder().stream().toList();
            assertIterableEquals(listOfEmptyList, result);
        }
    }

    // =========================================================
    // 2. Content correctness
    // =========================================================

    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("Full power-set of [A,B,C] in lex order")
        void fullPowerSetOf3() {
            String expected = "[[], [A], [B], [C], [A, B], [A, C], [B, C], [A, B, C]]";
            String output = subsets.of(A_B_C).all().lexOrder().stream().toList().toString();
            assertEquals(expected, output);
        }

        @Test
        @DisplayName("Input order preserved: [C,B,A] enumerates in input-position order")
        void inputOrderPreserved() {
            String expected = "[[C], [B], [A], [C, B], [C, A], [B, A], [C, B, A]]";
            String output = subsets.of("C", "B", "A").inRange(1, 3)
                    .lexOrder().stream().toList().toString();
            assertEquals(expected, output);
        }

        @Test
        @DisplayName("inRange(2,3) of [A,B,C] returns size-2 and size-3 subsets")
        void range2to3OfABC() {
            String expected = "[[A, B], [A, C], [B, C], [A, B, C]]";
            String output = subsets.of(A_B_C).inRange(2, 3)
                    .lexOrder().stream().toList().toString();
            assertEquals(expected, output);
        }

        @Test
        @DisplayName("of(n) generates numeric subsets [0..n-1]")
        void numericSubsets() {
            String output = subsets.of(3).all().lexOrder().stream().toList().toString();
            assertEquals("[[], [0], [1], [2], [0, 1], [0, 2], [1, 2], [0, 1, 2]]", output);
        }

        @Test
        @DisplayName("Each subset contains only elements from input with no duplicates")
        void eachSubsetHasUniqueElementsFromInput() {
            var input = A_B_C_D;
            var subsetList = subsets.of(input).all().lexOrder().stream().toList();
            for (var sub : subsetList) {
                assertEquals(new HashSet<>(sub).size(), sub.size(),
                        "subset must have no duplicate elements: " + sub);
                assertTrue(new HashSet<>(input).containsAll(sub),
                        "subset must only contain input elements: " + sub);
            }
        }

        @Test
        @DisplayName("All subsets are distinct")
        void allSubsetsAreDistinct() {
            var subsetList = subsets.of(A_B_C_D).all().lexOrder().stream().toList();
            assertEquals(new HashSet<>(subsetList).size(), subsetList.size(),
                    "all subsets must be distinct");
        }

        @Test
        @DisplayName("Subset sizes within range stay within [from, to]")
        void subsetSizesWithinRange() {
            int from = 1, to = 3;
            var subsetList = subsets.of(A_B_C_D).inRange(from, to).lexOrder().stream().toList();
            for (var sub : subsetList) {
                assertTrue(sub.size() >= from && sub.size() <= to,
                        "subset size " + sub.size() + " out of range [" + from + "," + to + "]");
            }
        }
    }

    // =========================================================
    // 3. Iterator contract
    // =========================================================

    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls on same iterable produce equal results")
        void multipleStreamCallsAreEqual() {
            var iterable = subsets.of(A_B_C).all().lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
            assertNotSame(list1, list2);
        }
    }

    // =========================================================
    // 4. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] 2^n subsets correct for n in [0,20]")
    void stressTesting() {
        for (int n = 0; n <= 20; n++) {
            List<Integer> input = java.util.stream.IntStream.range(0, n).boxed().toList();
            long count = subsets.of(input).all().lexOrder().stream().count();
            assertEquals(1L << n, count, "n=" + n + " count mismatch");
        }
    }
}