/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link Factoradic}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Known values for small inputs (verified by hand)</li>
 *   <li>Round-trip: decimal → Factoradic → decimal</li>
 *   <li>Factoradic digit correctness (MSD-first display, LSD-first storage)</li>
 *   <li>Recurrence / structural properties</li>
 *   <li>Large-number correctness</li>
 *   <li>Boundary conditions (0, 1, factorial boundaries)</li>
 *   <li>{@code equals} and {@code hashCode} contracts</li>
 * </ul>
 */
@DisplayName("Factoradic Number System")
class FactoradicTest {

    // =========================================================
    // 1. Known small values
    // =========================================================

    @Nested
    @DisplayName("Known values for small inputs")
    class KnownValues {

        @Test
        @DisplayName("First 7 Factoradic representations (n = 0..6)")
        void first7Values() {
            // toString() prints digits MSD-first, trailing 0 is always present
            String[] expected = {
                    "[0]",
                    "[1, 0]",
                    "[1, 0, 0]",
                    "[1, 1, 0]",
                    "[2, 0, 0]",
                    "[2, 1, 0]",
                    "[1, 0, 0, 0]"
            };
            for (int i = 0; i <= 6; i++) {
                assertEquals(expected[i], Factoradic.of(i).toString(),
                        "Factoradic of " + i);
            }
        }

        @Test
        @DisplayName("Factoradic of 0 is [0]")
        void factoradicOfZero() {
            Factoradic f = Factoradic.of(0L);
            assertEquals("[0]", f.toString());
            assertEquals(BigInteger.ZERO, f.decimalValue);
        }

        @Test
        @DisplayName("Factoradic of 1 is [1, 0]")
        void factoradicOfOne() {
            Factoradic f = Factoradic.of(1L);
            assertEquals("[1, 0]", f.toString());
            assertEquals(BigInteger.ONE, f.decimalValue);
        }

        @Test
        @DisplayName("n! - 1 is the maximum n-digit Factoradic (all digits maxed)")
        void factorialBoundaries() {
            // 2! - 1 = 1 → [1, 0] — last 2-digit factoradic
            assertEquals("[1, 0]", Factoradic.of(1L).toString());
            // 3! - 1 = 5 → [2, 1, 0]
            assertEquals("[2, 1, 0]", Factoradic.of(5L).toString());
            // 4! - 1 = 23 → [3, 2, 1, 0]
            assertEquals("[3, 2, 1, 0]", Factoradic.of(23L).toString());
            // 5! - 1 = 119 → [4, 3, 2, 1, 0]
            assertEquals("[4, 3, 2, 1, 0]", Factoradic.of(119L).toString());
        }

        @Test
        @DisplayName("n! itself starts a new digit position")
        void factorialItself() {
            // 2! = 2 → [1, 0, 0]
            assertEquals("[1, 0, 0]", Factoradic.of(2L).toString());
            // 3! = 6 → [1, 0, 0, 0]
            assertEquals("[1, 0, 0, 0]", Factoradic.of(6L).toString());
            // 4! = 24 → [1, 0, 0, 0, 0]
            assertEquals("[1, 0, 0, 0, 0]", Factoradic.of(24L).toString());
        }
    }

    // =========================================================
    // 2. Round-trip: decimal → Factoradic → decimal
    // =========================================================

    @Nested
    @DisplayName("Round-trip: decimal ↔ Factoradic")
    class RoundTrip {

        @Test
        @DisplayName("Round-trip for 0..100")
        void roundTripSmall() {
            for (int i = 0; i <= 100; i++) {
                Factoradic f = Factoradic.of(i);
                assertEquals(i, f.decimalValue.intValue(),
                        "decimal round-trip failed at " + i);
            }
        }

        @ParameterizedTest(name = "n={0}")
        @ValueSource(longs = {1000L, 10_000L, 999_999L, Integer.MAX_VALUE, Long.MAX_VALUE / 2})
        @DisplayName("Round-trip for large values")
        void roundTripLarge(long n) {
            Factoradic f = Factoradic.of(n);
            assertEquals(BigInteger.valueOf(n), f.decimalValue,
                    "decimal round-trip failed at " + n);
        }

        @Test
        @DisplayName("Round-trip via BigInteger input")
        void roundTripBigInteger() {
            BigInteger huge = new BigInteger("4611686018427387904"); // 2^62
            Factoradic f = Factoradic.of(huge);
            assertEquals(huge, f.decimalValue);
        }
    }

    // =========================================================
    // 3. Digit structure / storage order
    // =========================================================

    @Nested
    @DisplayName("Digit structure")
    class DigitStructure {

        @Test
        @DisplayName("factoradicValues list is LSD-first (index 0 = digit 0 = always 0)")
        void lsdFirstStorage() {
            // In a factoradic number, position 0 always has value 0 (0! weight, max digit 0)
            for (int i = 0; i <= 50; i++) {
                Factoradic f = Factoradic.of(i);
                assertEquals(0, f.factoradicValues.get(0),
                        "LSD (index 0) of factoradic(" + i + ") must be 0");
            }
        }

        @Test
        @DisplayName("Digit at position k is in range [0, k]")
        void digitRangeConstraint() {
            for (int n = 0; n <= 120; n++) {
                Factoradic f = Factoradic.of(n);
                List<Integer> digits = f.factoradicValues;
                for (int k = 0; k < digits.size(); k++) {
                    int digit = digits.get(k);
                    assertTrue(digit >= 0 && digit <= k,
                            "n=" + n + ": digit at position " + k + " is " + digit
                                    + ", must be in [0," + k + "]");
                }
            }
        }

        @Test
        @DisplayName("factoradicValues list is unmodifiable")
        void listIsUnmodifiable() {
            Factoradic f = Factoradic.of(10L);
            assertThrows(UnsupportedOperationException.class,
                    () -> f.factoradicValues.add(99));
        }

        @Test
        @DisplayName("Known large values match expected Factoradic strings")
        void knownLargeValues() {
            // 4611686018427387904L = 2^62
            String[] expected = {
                    "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 0, 0]",
                    "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 1, 0]"
            };
            long base = 4611686018427387904L;
            assertEquals(expected[0], Factoradic.of(base).toString());
            assertEquals(expected[1], Factoradic.of(base + 1).toString());
        }
    }

    // =========================================================
    // 4. Relationship to permutations (factoradic encodes rank of permutation)
    // =========================================================

    @Nested
    @DisplayName("Permutation rank encoding")
    class PermutationRankEncoding {

        @Test
        @DisplayName("The n-th permutation of [0,1,2] is correctly encoded")
        void permutationsOf3Elements() {
            // The 6 permutations of {0,1,2} in lex order are ranked 0..5
            // Factoradic digits (MSD-to-LSD, excluding the trailing 0) encode the permutation
            // rank=0 → [0,1,2], factoradic=[0]  (leading 0, i.e. [0,0,0] but displayed [0])
            // rank=1 → [0,2,1], factoradic=[1,0] i.e. 1·1!=1
            // rank=2 → [1,0,2], factoradic=[1,0,0] i.e. 1·2!=2
            // rank=5 → [2,1,0], factoradic=[2,1,0]
            assertEquals("[2, 1, 0]", Factoradic.of(5L).toString()); // last permutation of 3
            assertEquals("[0]", Factoradic.of(0L).toString());        // first permutation
        }

        @Test
        @DisplayName("Consecutive Factoradic values differ by 1 in decimal")
        void consecutiveValues() {
            for (int i = 0; i < 50; i++) {
                Factoradic fi = Factoradic.of(i);
                Factoradic fi1 = Factoradic.of(i + 1);
                assertEquals(fi.decimalValue.add(BigInteger.ONE), fi1.decimalValue,
                        "gap at " + i);
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
        @DisplayName("Two Factoradic instances from the same decimal are equal")
        void equalInstances() {
            int n = 23456;
            Factoradic f1 = Factoradic.of(n);
            Factoradic f2 = Factoradic.of(n);
            assertEquals(f1, f2);
        }

        @Test
        @DisplayName("hashCode is consistent with equals")
        void hashCodeConsistency() {
            int n = 23456;
            Factoradic f1 = Factoradic.of(n);
            Factoradic f2 = Factoradic.of(n);
            assertEquals(f1.hashCode(), f2.hashCode());
        }

        @Test
        @DisplayName("hashCode equals the BigInteger hashCode of the decimal value")
        void hashCodeMatchesBigInteger() {
            int n = 23456;
            Factoradic f = Factoradic.of(n);
            // decimalValue.hashCode() == n for small ints (BigInteger(n).hashCode() == n)
            assertEquals(BigInteger.valueOf(n).hashCode(), f.hashCode());
        }

        @Test
        @DisplayName("Different decimal values produce unequal instances")
        void unequalInstances() {
            assertNotEquals(Factoradic.of(5L), Factoradic.of(6L));
        }

        @Test
        @DisplayName("Reflexivity: instance equals itself")
        void reflexive() {
            Factoradic f = Factoradic.of(42L);
            assertEquals(f, f);
        }

        @Test
        @DisplayName("Symmetry: equals is symmetric")
        void symmetric() {
            Factoradic f1 = Factoradic.of(42L);
            Factoradic f2 = Factoradic.of(42L);
            assertEquals(f1, f2);
            assertEquals(f2, f1);
        }

        @Test
        @DisplayName("Not equal to null or different type")
        void notEqualToNullOrOtherType() {
            Factoradic f = Factoradic.of(42L);
            assertNotEquals(null, f);
            assertNotEquals("42", f);
        }
    }

    // =========================================================
    // 6. Boundary / stress
    // =========================================================

    @Nested
    @DisplayName("Boundary conditions")
    class Boundary {

        @Test
        @DisplayName("Factoradic of Long.MAX_VALUE round-trips correctly")
        void longMaxValue() {
            Factoradic f = Factoradic.of(Long.MAX_VALUE);
            assertEquals(BigInteger.valueOf(Long.MAX_VALUE), f.decimalValue);
        }

        @Test
        @DisplayName("Sequence is dense: no gaps in [0, 999]")
        void denseSequence() {
            for (int i = 0; i < 1000; i++) {
                assertEquals(i, Factoradic.of(i).decimalValue.intValue());
            }
        }
    }
}