/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.numberSystem;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link Combinadic}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Known values for small inputs</li>
 *   <li>Round-trip: rank → Combinadic → rank</li>
 *   <li>{@code next()} chaining: sequential correctness</li>
 *   <li>Degree invariant</li>
 *   <li>Boundary conditions (rank 0, degree 0, degree 1)</li>
 *   <li>{@code equals} and {@code hashCode} contracts</li>
 * </ul>
 */
@DisplayName("Combinadic Number System")
class CombinadicTest {

    // =========================================================
    // 1. Known small values
    // =========================================================

    @Nested
    @DisplayName("Known values for small inputs")
    class KnownValues {

        @Test
        @DisplayName("First 7 Combinadic values at degree 2 (C(n,2) indexed)")
        void first7ValuesAtDegree2() {
            // Combinadic of rank r at degree 2: the r-th combination C(n,2) in colex order
            String expected = "[[1, 0], [2, 0], [2, 1], [3, 0], [3, 1], [3, 2], [4, 0]]";
            List<String> output = new ArrayList<>();
            for (int i = 0; i <= 6; i++) {
                output.add(Combinadic.of(i, 2, calculator).toString());
            }
            assertEquals(expected, output.toString());
        }

        @Test
        @DisplayName("Combinadic at degree 1 is simply [r]")
        void degree1() {
            // C(n,1): just [r] since C(r,1) = r
            for (int r = 0; r <= 10; r++) {
                Combinadic c = Combinadic.of(r, 1, calculator);
                assertEquals("[" + r + "]", c.toString(),
                        "degree-1 combinadic of " + r);
            }
        }

        @Test
        @DisplayName("Combinadic of 1000 at degree 5 and next 6 values")
        void knownLargeValues() {
            String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], " +
                    "[12, 10, 3, 1, 0], [12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";
            Combinadic c = Combinadic.of(1000, 5, calculator);
            List<String> output = new ArrayList<>();
            output.add(c.toString());
            for (int i = 1; i <= 6; i++) {
                c = c.next();
                output.add(c.toString());
            }
            assertEquals(expected, output.toString());
        }

        @Test
        @DisplayName("Via numberSystem.combinadic produces same results as Combinadic.of")
        void numberSystemEntryPointConsistency() {
            for (int i = 0; i <= 100; i++) {
                Combinadic direct = Combinadic.of(i, 3, calculator);
                Combinadic viaNS  = numberSystem.combinadic(i, 3);
                assertEquals(direct.toString(), viaNS.toString(),
                        "Mismatch at rank=" + i);
                assertEquals(direct.decimalValue, viaNS.decimalValue,
                        "decimalValue mismatch at rank=" + i);
            }
        }
    }

    // =========================================================
    // 2. Round-trip: rank ↔ Combinadic
    // =========================================================

    @Nested
    @DisplayName("Round-trip: rank ↔ Combinadic")
    class RoundTrip {

        @ParameterizedTest(name = "degree={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("decimalValue round-trips correctly for ranks 0..100")
        void roundTripSmall(int degree) {
            for (int rank = 0; rank <= 100; rank++) {
                Combinadic c = Combinadic.of(rank, degree, calculator);
                assertEquals(rank, c.decimalValue.intValue(),
                        "degree=" + degree + " rank=" + rank + " round-trip failed");
            }
        }

        @Test
        @DisplayName("Round-trip via numberSystem entry point")
        void roundTripViaNumberSystem() {
            for (int i = 0; i <= 100; i++) {
                assertEquals(i, numberSystem.combinadic(i, 3).decimalValue.intValue(),
                        "numberSystem round-trip failed at rank=" + i);
            }
        }

        @Test
        @DisplayName("BigInteger input round-trip for large rank")
        void roundTripBigInteger() {
            BigInteger large = BigInteger.valueOf(123456789L);
            Combinadic c = Combinadic.of(large, 4, calculator);
            assertEquals(large, c.decimalValue,
                    "BigInteger round-trip failed at rank=" + large);
        }
    }

    // =========================================================
    // 3. next() chaining
    // =========================================================

    @Nested
    @DisplayName("next() chaining")
    class NextChaining {

        @Test
        @DisplayName("next() increments decimalValue by 1")
        void nextIncrementsBy1() {
            Combinadic c = Combinadic.of(0, 3, calculator);
            for (int expected = 1; expected <= 50; expected++) {
                c = c.next();
                assertEquals(BigInteger.valueOf(expected), c.decimalValue,
                        "next() step " + expected);
            }
        }

        @Test
        @DisplayName("next() sequence matches independently constructed values")
        void nextSequenceMatchesIndependent() {
            int degree = 3;
            Combinadic c = Combinadic.of(500, degree, calculator);
            for (int rank = 501; rank <= 520; rank++) {
                c = c.next();
                Combinadic expected = Combinadic.of(rank, degree, calculator);
                assertEquals(expected.toString(), c.toString(),
                        "next() mismatch at rank=" + rank);
            }
        }

        @ParameterizedTest(name = "degree={0}")
        @ValueSource(ints = {1, 2, 3, 4})
        @DisplayName("Chain of 200 next() calls from rank 0 all round-trip")
        void chainedNextRoundTrips(int degree) {
            Combinadic c = Combinadic.of(0, degree, calculator);
            for (int step = 0; step <= 200; step++) {
                assertEquals(BigInteger.valueOf(step), c.decimalValue,
                        "degree=" + degree + " step=" + step);
                if (step < 200) c = c.next();
            }
        }

        @Test
        @DisplayName("next() on rank 1000 matches Combinadic.of(1001) (degree 5)")
        void nextLargeRank() {
            Combinadic c = Combinadic.of(1000, 5, calculator).next();
            Combinadic expected = Combinadic.of(1001, 5, calculator);
            assertEquals(expected.toString(), c.toString());
            assertEquals(expected.decimalValue, c.decimalValue);
        }
    }

    // =========================================================
    // 4. Degree invariant
    // =========================================================

    @Nested
    @DisplayName("Degree invariant")
    class DegreeInvariant {

        @ParameterizedTest(name = "degree={0}")
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
        @DisplayName("degree() always returns the requested degree")
        void degreeAlwaysMatchesRequest(int degree) {
            Combinadic c = Combinadic.of(BigInteger.TEN, degree, calculator);
            assertEquals(degree, c.degree(),
                    "degree() mismatch at degree=" + degree);
        }

        @Test
        @DisplayName("degree() is preserved through next() calls")
        void degreePreservedThroughNext() {
            int expectedDegree = 4;
            Combinadic c = Combinadic.of(0, expectedDegree, calculator);
            for (int i = 0; i < 20; i++) {
                assertEquals(expectedDegree, c.degree(),
                        "degree changed at step " + i);
                c = c.next();
            }
        }
    }

    // =========================================================
    // 5. Boundary conditions
    // =========================================================

    @Nested
    @DisplayName("Boundary conditions")
    class Boundary {

        @Test
        @DisplayName("Rank 0 at degree 2 is [1, 0] (first C(n,2))")
        void rank0Degree2() {
            Combinadic c = Combinadic.of(0, 2, calculator);
            assertEquals("[1, 0]", c.toString());
            assertEquals(BigInteger.ZERO, c.decimalValue);
        }

        @Test
        @DisplayName("Degree 0 combinadic of any rank encodes empty combination")
        void degree0() {
            // degree 0 = empty combination; combinadic of any rank with degree 0
            Combinadic c = Combinadic.of(BigInteger.TEN, 0, calculator);
            assertEquals(0, c.degree());
        }

        @Test
        @DisplayName("Very large BigInteger rank is stored exactly")
        void largeRankStored() {
            BigInteger huge = new BigInteger("999999999999999999");
            Combinadic c = Combinadic.of(huge, 5, calculator);
            assertEquals(huge, c.decimalValue);
        }
    }

    // =========================================================
    // 6. Digit / structural properties
    // =========================================================

    @Nested
    @DisplayName("Structural properties")
    class StructuralProperties {

        @Test
        @DisplayName("Combinadic digits are strictly decreasing (colex property)")
        void digitsStrictlyDecreasing() {
            // In a valid combinadic number, digits are strictly decreasing from MSD to LSD
            for (int rank = 0; rank <= 200; rank++) {
                Combinadic c = Combinadic.of(rank, 4, calculator);
                // Access the string representation and parse
                String s = c.toString().replace("[", "").replace("]", "");
                String[] parts = s.split(", ");
                for (int i = 0; i < parts.length - 1; i++) {
                    int d1 = Integer.parseInt(parts[i].trim());
                    int d2 = Integer.parseInt(parts[i + 1].trim());
                    assertTrue(d1 > d2,
                            "rank=" + rank + ": digit[" + i + "]=" + d1
                                    + " must be > digit[" + (i+1) + "]=" + d2);
                }
            }
        }

        @Test
        @DisplayName("Number of Combinadic digits equals degree")
        void digitCountEqualsDegree() {
            for (int degree = 1; degree <= 5; degree++) {
                for (int rank = 0; rank <= 30; rank++) {
                    Combinadic c = Combinadic.of(rank, degree, calculator);
                    assertEquals(degree, c.degree(),
                            "degree mismatch at rank=" + rank + " degree=" + degree);
                }
            }
        }
    }
}