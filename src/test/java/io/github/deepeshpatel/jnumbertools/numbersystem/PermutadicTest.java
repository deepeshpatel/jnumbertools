/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link Permutadic} and {@link PermutadicAlgorithms}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Known representations for small values</li>
 *   <li>Relationship to Factoradic (when k = n)</li>
 *   <li>Round-trip: rank → permutation → rank</li>
 *   <li>Known last permutation (boundary)</li>
 *   <li>Known math-expression output</li>
 *   <li>equals / hashCode contracts</li>
 *   <li>Stress test (opt-in via system property)</li>
 * </ul>
 */
@DisplayName("Permutadic Number System")
class PermutadicTest {

    // =========================================================
    // 1. Known small values
    // =========================================================

    @Nested
    @DisplayName("Known representations")
    class KnownRepresentations {

        @Test
        @DisplayName("First 7 Permutadic values for size=8, k=5 (degree offset=3)")
        void first7Values() {
            String[] expected = {
                    "[0](3)",
                    "[1](3)",
                    "[2](3)",
                    "[3](3)",
                    "[1,0](3)",
                    "[1,1](3)",
                    "[1,2](3)"
            };
            for (int i = 0; i <= 6; i++) {
                assertEquals(expected[i],
                        Permutadic.of(BigInteger.valueOf(i), 8 - 5).toString(),
                        "Permutadic of " + i + " (offset=3)");
            }
        }

        @Test
        @DisplayName("Out-of-range rank wraps to next block correctly")
        void outOfPermutationRange() {
            // 8P4 = 1680; rank 1680 is just beyond last valid rank
            String output = Permutadic.of(BigInteger.valueOf(1680), 8 - 4).toString();
            assertEquals("[1,0,0,0,0](4)", output);
        }

        @Test
        @DisplayName("Known math expression for Long.MAX_VALUE")
        void knownMathExpression() {
            String expected =
                    "2(²⁸P₁₄) + 17(²⁷P₁₃) + 22(²⁶P₁₂) + 20(²⁵P₁₁) + 12(²⁴P₁₀) + " +
                            "10(²³P₉) + 2(²²P₈) + 17(²¹P₇) + 12(²⁰P₆) + 15(¹⁹P₅) + " +
                            "18(¹⁸P₄) + 2(¹⁷P₃) + 0(¹⁶P₂) + 8(¹⁵P₁) + 7(¹⁴P₀)";
            Permutadic permutadic = Permutadic.of(Long.MAX_VALUE, 14);
            assertEquals(expected, permutadic.toMathExpression());
        }
    }

    // =========================================================
    // 2. Relationship to Factoradic (k = n means full permutations)
    // =========================================================

    @Nested
    @DisplayName("Equivalence to Factoradic when k = n")
    class FactoradicEquivalence {

        @Test
        @DisplayName("Permutadic matches Factoradic when size offset = 0 (k = n)")
        void matchesFactoradicWhenKEqualsN() {
            int start = 565656565;
            for (int i = start; i <= start + 10; i++) {
                Factoradic f = Factoradic.of(i);
                var permutadicValues = Permutadic.of(BigInteger.valueOf(i), 0).permutadicValues;
                var factorialValues  = f.factoradicValues;
                assertEquals(factorialValues.toString(), permutadicValues.toString(),
                        "Mismatch at i=" + i);
            }
        }
    }

    // =========================================================
    // 3. Round-trip: rank ↔ k-permutation
    // =========================================================

    @Nested
    @DisplayName("Round-trip: rank ↔ k-permutation")
    class RoundTrip {

        @Test
        @DisplayName("Encode/decode round-trip for size=8, k=4 (all 1680 ranks)")
        void roundTripSize8K4() {
            int size = 8;
            int degree = 4;
            for (long i = 0; i < 1679; i++) {
                Permutadic p1 = Permutadic.of(BigInteger.valueOf(i), size - degree);
                int[] mth = p1.toMthPermutation(degree);
                Permutadic p2 = Permutadic.fromMthPermutation(mth, size - degree);
                assertEquals(i, p2.decimalValue.intValue(),
                        "round-trip failed at rank=" + i);
            }
        }

        @ParameterizedTest(name = "n={0}, k=3")
        @ValueSource(ints = {4, 5, 6, 7, 8})
        @DisplayName("Round-trip for nPk for varying n, k=3")
        void roundTripVaryingN(int n) {
            int k = 3;
            long total = calculator.nPr(n, k).longValue();
            for (long rank = 0; rank < total; rank++) {
                Permutadic p1 = Permutadic.of(BigInteger.valueOf(rank), n - k);
                int[] mth = p1.toMthPermutation(k);
                Permutadic p2 = Permutadic.fromMthPermutation(mth, n - k);
                assertEquals(p1, p2,
                        "n=" + n + " k=" + k + " rank=" + rank);
                assertEquals(p1.hashCode(), p2.hashCode(),
                        "hashCode mismatch at n=" + n + " k=" + k + " rank=" + rank);
            }
        }

        @Test
        @DisplayName("Full unrank-then-rank round-trip for size=8, k=4")
        void unrankRankRoundTrip() {
            int size = 8;
            int degree = 4;
            for (long i = 0; i < calculator.nPr(size, degree).longValue(); i++) {
                Permutadic p = Permutadic.of(i, size - degree);
                int[] perm = p.toMthPermutation(degree);
                BigInteger rank = Permutadic.fromMthPermutation(perm, size - degree).decimalValue;
                assertEquals(i, rank.longValue(),
                        "unrank→rank failed at rank=" + i);
            }
        }

        @Test
        @DisplayName("Full round-trip for size=7, k=3")
        void roundTripSize7K3() {
            int size = 7;
            int k = 3;
            for (long i = 0; i < calculator.nPr(size, k).longValue(); i++) {
                Permutadic p1 = Permutadic.of(BigInteger.valueOf(i), size - k);
                int[] mth = p1.toMthPermutation(k);
                Permutadic p2 = Permutadic.fromMthPermutation(mth, size - k);
                assertEquals(p1, p2);
                assertEquals(p1.hashCode(), p2.hashCode());
            }
        }
    }

    // =========================================================
    // 4. Boundary conditions
    // =========================================================

    @Nested
    @DisplayName("Boundary conditions")
    class Boundary {

        @Test
        @DisplayName("Last permutation of 8P4 = [7,6,5,4]")
        void lastPermutation8P4() {
            int[] expected = {7, 6, 5, 4};
            // 8P4 = 1680, so last rank is 1679
            int[] permutation = Permutadic.of(BigInteger.valueOf(1679), 8 - 4)
                    .toMthPermutation(4);
            assertArrayEquals(expected, permutation,
                    "last 8P4 permutation should be [7,6,5,4]");
        }

        @Test
        @DisplayName("Rank 0 produces the first permutation [0,1,...,k-1]")
        void rank0IsFirstPermutation() {
            int size = 5, k = 3;
            int[] perm = Permutadic.of(BigInteger.ZERO, size - k).toMthPermutation(k);
            assertArrayEquals(new int[]{0, 1, 2}, perm,
                    "rank 0 should be [0,1,2]");
        }

        @Test
        @DisplayName("k=1: single-element permutations [0] through [n-1]")
        void kEquals1() {
            int n = 6;
            for (int rank = 0; rank < n; rank++) {
                int[] perm = Permutadic.of(BigInteger.valueOf(rank), n - 1).toMthPermutation(1);
                assertEquals(1, perm.length);
                assertEquals(rank, perm[0], "k=1 rank=" + rank + " should be [" + rank + "]");
            }
        }
    }

    // =========================================================
    // 5. equals / hashCode contracts
    // =========================================================

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Two Permutadic instances from same inputs are equal")
        void equalInstances() {
            Permutadic p1 = Permutadic.of(BigInteger.valueOf(42), 3);
            Permutadic p2 = Permutadic.of(BigInteger.valueOf(42), 3);
            assertEquals(p1, p2);
        }

        @Test
        @DisplayName("hashCode is consistent with equals")
        void hashCodeConsistency() {
            Permutadic p1 = Permutadic.of(BigInteger.valueOf(42), 3);
            Permutadic p2 = Permutadic.of(BigInteger.valueOf(42), 3);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("Reflexivity: instance equals itself")
        void reflexive() {
            Permutadic p = Permutadic.of(BigInteger.valueOf(100), 3);
            assertEquals(p, p);
        }

        @Test
        @DisplayName("Different ranks produce unequal instances (same offset)")
        void differentRanksUnequal() {
            Permutadic p1 = Permutadic.of(BigInteger.valueOf(5), 3);
            Permutadic p2 = Permutadic.of(BigInteger.valueOf(6), 3);
            assertNotEquals(p1, p2);
        }
    }

    // =========================================================
    // 6. PermutadicAlgorithms (low-level, without boundary checks)
    // =========================================================

    @Nested
    @DisplayName("PermutadicAlgorithms low-level API")
    class LowLevelAlgorithms {

        @Test
        @DisplayName("unRankWithoutBoundCheck then rank round-trips for n=10, k=5")
        void lowLevelRoundTrip() {
            int n = 10, k = 5;
            long total = calculator.nPr(n, k).longValue();
            for (long rank = 0; rank < total; rank += 10) { // sample every 10th
                var array = PermutadicAlgorithms.unRankWithoutBoundCheck(
                        BigInteger.valueOf(rank), n, k);
                var recovered = PermutadicAlgorithms.rank(n, array);
                assertEquals(BigInteger.valueOf(rank), recovered,
                        "low-level round-trip failed at rank=" + rank);
            }
        }

        @Test
        @DisplayName("unRankWithoutBoundCheck at rank 0 gives [0,1,...,k-1]")
        void lowLevelRank0() {
            int n = 7, k = 4;
            var array = PermutadicAlgorithms.unRankWithoutBoundCheck(BigInteger.ZERO, n, k);
            assertArrayEquals(new int[]{0, 1, 2, 3}, array,
                    "rank 0 should produce identity k-permutation");
        }
    }

    // =========================================================
    // 7. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] Large n=200, k=100: 1001 ranks around midpoint round-trip")
    void stressTesting() {
        int n = 200;
        int k = 100;
        var rank = calculator.nPr(n, k).divide(BigInteger.TWO);
        var trillion = new BigInteger("1000000000000");
        for (int i = 0; i <= 1000; i++) {
            var array = PermutadicAlgorithms.unRankWithoutBoundCheck(rank, n, k);
            var calculatedRank = PermutadicAlgorithms.rank(n, array);
            assertEquals(rank, calculatedRank,
                    "stress test failed at iteration " + i + " rank=" + rank);
            rank = rank.add(trillion);
        }
    }
}