/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link MultisetPermutation} (lexicographic order).
 *
 * <p>Generates all distinct permutations of a multiset with element frequencies.
 * Count formula: n! / (n₁! · n₂! · … · nₖ!) where n = Σnᵢ.
 */
@DisplayName("Multiset Permutations (Lex Order)")
class MultisetPermutationTest {

    // =========================================================
    // 1. Count correctness
    // =========================================================

    @Nested
    @DisplayName("Count: multinomial coefficient")
    class CountTests {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {2, 3, 4, 5})
        @DisplayName("Count equals multinomial(frequencies) for random frequency arrays")
        void countEqualsMult(int n) {
            var input = IntStream.range(0, n).boxed().toList();
            int[] frequency = getRandomMultisetFreqArray(random, input.size());
            LinkedHashMap<Object, Integer> options = createMap(input, frequency);
            long count = permutation.multiset(options).lexOrder().stream().count();
            assertEquals(calculator.multinomial(frequency).longValue(), count);
        }

        @Test
        @DisplayName("Empty map → 1 permutation: the empty list")
        void emptyMapProducesOneEmptyPermutation() {
            var emptyMap = new LinkedHashMap<String, Integer>();
            var builder = permutation.multiset(emptyMap);
            assertEquals(calculator.factorial(0), builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("Single element with frequency f: multinomial = 1, output is [[e,e,...]]")
        void singleElementMultipleFrequency() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 3);
            var result = permutation.multiset(options).lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(List.of("A", "A", "A"), result.get(0));
        }

        @Test
        @DisplayName("All-unique elements (freq=1 each): multinomial = n!, same as unique perm")
        void allUniqueFrequenciesGivesFactorial() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 1); options.put("B", 1); options.put("C", 1);
            long count = permutation.multiset(options).lexOrder().stream().count();
            assertEquals(6L, count);
        }

        @Test
        @DisplayName("count() matches stream count for {Red:2, Green:1, Blue:1}")
        void builderCountMatchesStreamCount() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("Red", 2); options.put("Green", 1); options.put("Blue", 1);
            var builder = permutation.multiset(options);
            long streamCount = builder.lexOrder().stream().count();
            assertEquals(builder.count().longValue(), streamCount);
        }
    }

    // =========================================================
    // 2. Content correctness
    // =========================================================

    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("All 12 permutations of {A:2, B:1, C:1} in lex order")
        void allPermsOfAA_B_C() {
            var expected = List.of(
                    of("A","A","B","C"), of("A","A","C","B"),
                    of("A","B","A","C"), of("A","B","C","A"),
                    of("A","C","A","B"), of("A","C","B","A"),
                    of("B","A","A","C"), of("B","A","C","A"),
                    of("B","C","A","A"),
                    of("C","A","A","B"), of("C","A","B","A"),
                    of("C","B","A","A")
            );
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2); options.put("B", 1); options.put("C", 1);
            var output = permutation.multiset(options).lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("All 12 permutations of {Red:2, Green:1, Blue:1} in lex order")
        void allPermsOfRedRed_Green_Blue() {
            var expected = of(
                    of("Red","Red","Green","Blue"), of("Red","Red","Blue","Green"),
                    of("Red","Green","Red","Blue"), of("Red","Green","Blue","Red"),
                    of("Red","Blue","Red","Green"), of("Red","Blue","Green","Red"),
                    of("Green","Red","Red","Blue"), of("Green","Red","Blue","Red"),
                    of("Green","Blue","Red","Red"),
                    of("Blue","Red","Red","Green"), of("Blue","Red","Green","Red"),
                    of("Blue","Green","Red","Red")
            );
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("Red", 2); options.put("Green", 1); options.put("Blue", 1);
            var output = permutation.multiset(options).lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("Each permutation contains the exact multiset of elements")
        void eachPermContainsCorrectMultiset() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2); options.put("B", 2);
            var perms = permutation.multiset(options).lexOrder().stream().toList();
            Map<String, Long> expectedFreq = Map.of("A", 2L, "B", 2L);
            for (var perm : perms) {
                Map<Object, Long> freq = new LinkedHashMap<>();
                for (var e : perm) freq.merge(e, 1L, Long::sum);
                assertEquals(expectedFreq, freq,
                        "multiset mismatch in permutation " + perm);
            }
        }

        @Test
        @DisplayName("All permutations are distinct")
        void allPermutationsAreDistinct() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2); options.put("B", 1); options.put("C", 1);
            var perms = permutation.multiset(options).lexOrder().stream().toList();
            assertEquals(new HashSet<>(perms).size(), perms.size(),
                    "all multiset permutations must be distinct");
        }

        @Test
        @DisplayName("Permutations are in non-decreasing lexicographic order")
        void permutationsInLexOrder() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2); options.put("B", 2);
            var perms = permutation.multiset(options).lexOrder().stream().toList();
            for (int i = 1; i < perms.size(); i++) {
                assertTrue(isLexLessOrEqual(perms.get(i - 1), perms.get(i)),
                        "rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }
    }

    // =========================================================
    // 3. Input validation
    // =========================================================

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @Test
        @DisplayName("Null multiset map throws NullPointerException")
        void nullMapThrows() {
            assertThrows(NullPointerException.class,
                    () -> permutation.multiset(null).lexOrder());
        }

        @Test
        @DisplayName("Negative frequency throws IllegalArgumentException")
        void negativeFrequencyThrows() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2); options.put("B", -1);
            assertThrows(IllegalArgumentException.class,
                    () -> permutation.multiset(options).lexOrder());
        }
    }

    // =========================================================
    // 4. Iterator contract
    // =========================================================

    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls on same iterable produce equal results")
        void multipleStreamCallsAreEqual() {
            var elements = of("A", "B", "C");
            int[] frequencies = {3, 2, 2};
            LinkedHashMap<Object, Integer> options = createMap(elements, frequencies);
            var iterable = permutation.multiset(options).lexOrder();
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
    @DisplayName("[STRESS] Random frequency arrays for n in [2,7] count correctly")
    void stressTesting() {
        for (int n = 2; n <= 7; n++) {
            var input = IntStream.range(0, n).boxed().toList();
            int[] freq = getRandomMultisetFreqArray(random, n);
            LinkedHashMap<Object, Integer> options = createMap(input, freq);
            long count = permutation.multiset(options).lexOrder().stream().count();
            assertEquals(calculator.multinomial(freq).longValue(), count,
                    "n=" + n + " multinomial mismatch");
        }
    }
}