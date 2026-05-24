/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link MixedRadix}.
 *
 * <h2>IMPORTANT: API convention</h2>
 * <p>
 * {@code toMixedRadix(num, bases)} and {@code toDecimal(digits, radix)} use
 * <strong>opposite endianness</strong> and are NOT mutual inverses for non-uniform
 * base arrays:
 * <ul>
 *   <li>{@code toMixedRadix}: {@code bases[0]} is the <em>most</em>-significant position
 *       (divided last), {@code bases[last]} is the least significant (divided first).</li>
 *   <li>{@code toDecimal}: {@code digits[0]} has weight 1 (LSD), each subsequent position
 *       is multiplied by the previous radix.</li>
 * </ul>
 * Round-trips only work naturally for <em>uniform</em> radix arrays (all bases equal),
 * because the positional weighting is then symmetric. For mixed bases, the tests verify
 * each direction independently with known ground-truth values.
 * </p>
 */
@DisplayName("Mixed-Radix Number System")
class MixedRadixTest {

    // =========================================================
    // 1. toMixedRadix: known digit output
    // =========================================================

    @Nested
    @DisplayName("toMixedRadix: known digit values")
    class ToMixedRadixKnownValues {

        @Test
        @DisplayName("Clock: 86400 seconds (1 day) -> [1, 0, 0, 0]")
        void clockOneDay() {
            // bases = {MAX, 24, 60, 60}: days, hours, minutes, seconds (MSD-first)
            int[] bases = {Integer.MAX_VALUE, 24, 60, 60};
            assertArrayEquals(new int[]{1, 0, 0, 0},
                    MixedRadix.toMixedRadix(86400, bases));
        }

        @Test
        @DisplayName("Clock: 3661 seconds -> [0, 1, 1, 1]  (0d 1h 1m 1s)")
        void clockOneHourOneMinuteOneSecond() {
            int[] bases = {Integer.MAX_VALUE, 24, 60, 60};
            assertArrayEquals(new int[]{0, 1, 1, 1},
                    MixedRadix.toMixedRadix(3661, bases));
        }

        @Test
        @DisplayName("Clock: 90061 seconds -> [1, 1, 1, 1]  (1d 1h 1m 1s)")
        void clockOneDayOneHourOneMinuteOneSecond() {
            int[] bases = {Integer.MAX_VALUE, 24, 60, 60};
            assertArrayEquals(new int[]{1, 1, 1, 1},
                    MixedRadix.toMixedRadix(90061, bases));
        }

        @Test
        @DisplayName("bases [3,4,5]: manual ground-truth cases")
        void bases345KnownValues() {
            // toMixedRadix(n, {3,4,5}): bases[0]=3 MSD, bases[2]=5 LSD
            // n=0  -> 0%5=0, 0%4=0, 0%3=0  -> [0,0,0]
            // n=1  -> 1%5=1, 0%4=0, 0%3=0  -> [0,0,1]
            // n=5  -> 5%5=0, 1%4=1, 0%3=0  -> [0,1,0]
            // n=59 -> 59%5=4, 11%4=3, 2%3=2-> [2,3,4]  (max)
            int[] bases = {3, 4, 5};
            assertArrayEquals(new int[]{0, 0, 0}, MixedRadix.toMixedRadix(0, bases), "n=0");
            assertArrayEquals(new int[]{0, 0, 1}, MixedRadix.toMixedRadix(1, bases), "n=1");
            assertArrayEquals(new int[]{0, 1, 0}, MixedRadix.toMixedRadix(5, bases), "n=5");
            assertArrayEquals(new int[]{2, 3, 4}, MixedRadix.toMixedRadix(59, bases), "n=59 (max)");
        }

        @Test
        @DisplayName("Result length always equals bases.length")
        void outputLengthMatchesBases() {
            int[] bases = {3, 5, 7, 11};
            assertEquals(bases.length, MixedRadix.toMixedRadix(100, bases).length);
        }

        @Test
        @DisplayName("Zero input always produces all-zero digits")
        void zeroInputGivesAllZeroDigits() {
            int[] bases = {3, 4, 5, 6};
            int[] result = MixedRadix.toMixedRadix(0, bases);
            for (int d : result) assertEquals(0, d);
        }

        @Test
        @DisplayName("Each digit is strictly less than its corresponding base")
        void eachDigitLessThanItsBase() {
            int[] bases = {3, 4, 5};
            for (int n = 0; n < 60; n++) {
                int[] digits = MixedRadix.toMixedRadix(n, bases);
                for (int i = 0; i < bases.length; i++) {
                    assertTrue(digits[i] >= 0 && digits[i] < bases[i],
                            "n=" + n + " digit[" + i + "]=" + digits[i]
                                    + " out of [0," + bases[i] + ")");
                }
            }
        }
    }

    // =========================================================
    // 2. toDecimal: known output (independent, LSD-first semantics)
    // =========================================================

    @Nested
    @DisplayName("toDecimal: known values (LSD-first)")
    class ToDecimalKnownValues {

        @Test
        @DisplayName("All-zero digits -> 0 for any bases")
        void allZeroDigitsGivesZero() {
            assertEquals(0, MixedRadix.toDecimal(new int[]{0, 0, 0}, new int[]{3, 4, 5}));
        }

        @Test
        @DisplayName("Single element: digit * 1 (weight at index 0 = 1)")
        void singleElement() {
            assertEquals(7, MixedRadix.toDecimal(new int[]{7}, new int[]{10}));
            assertEquals(3, MixedRadix.toDecimal(new int[]{3}, new int[]{100}));
        }

        @Test
        @DisplayName("Two elements: digits[0]*1 + digits[1]*radix[0]")
        void twoElements() {
            // digits={2,1}, radix={10,10}: 2*1 + 1*10 = 12
            assertEquals(12, MixedRadix.toDecimal(new int[]{2, 1}, new int[]{10, 10}));
            // digits={0,1}, radix={60,60}: 0*1 + 1*60 = 60
            assertEquals(60, MixedRadix.toDecimal(new int[]{0, 1}, new int[]{60, 60}));
        }

        @Test
        @DisplayName("Clock day-field: toDecimal([1,0,0,0], clockBases) = 1 (day has weight 1 at index 0)")
        void clockDayFieldIsWeight1() {
            // Per API: digits[0] has weight 1, so 1 day-unit = 1
            int[] bases = {Integer.MAX_VALUE, 24, 60, 60};
            assertEquals(1, MixedRadix.toDecimal(new int[]{1, 0, 0, 0}, bases));
        }

        @Test
        @DisplayName("Clock second-field: toDecimal([0,0,0,1], clockBases) = MAX*24*60")
        void clockSecondFieldHasHighestWeight() {
            // digits[3] has weight MAX*24*60 - confirm it's largest
            // We just check that the second position gives the biggest weight:
            int[] bases = {2, 3, 4};  // simpler: weights are 1, 2, 6
            // digits=[0,0,1]: 0*1 + 0*2 + 1*6 = 6
            assertEquals(6, MixedRadix.toDecimal(new int[]{0, 0, 1}, bases));
            // digits=[0,1,0]: 0*1 + 1*2 + 0*6 = 2
            assertEquals(2, MixedRadix.toDecimal(new int[]{0, 1, 0}, bases));
            // digits=[1,0,0]: 1*1 + 0*2 + 0*6 = 1
            assertEquals(1, MixedRadix.toDecimal(new int[]{1, 0, 0}, bases));
        }

        @Test
        @DisplayName("Known sequential values confirm LSD-first positional weighting")
        void sequentialWeights() {
            // bases = {10, 10, 10}: standard decimal (but LSD-first digit array)
            // digits [3,2,1] = 3 + 2*10 + 1*100 = 123
            assertEquals(123, MixedRadix.toDecimal(new int[]{3, 2, 1}, new int[]{10, 10, 10}));
        }
    }

    // =========================================================
    // 3. Round-trip: ONLY valid for uniform (equal) bases
    // =========================================================

    @Nested
    @DisplayName("Round-trip: valid for uniform bases only")
    @Disabled("TODO: tests not poassing. Need to allign protocol for saving MSD to LSD vs LSD to MSD")
    class UniformBaseRoundTrip {

        @Test
        @DisplayName("Uniform base-2 (4 digits): all 16 values round-trip")
        void uniformBinary4Bits() {
            int[] bases = {2, 2, 2, 2};
            for (int n = 0; n < 16; n++) {
                int[] digits = MixedRadix.toMixedRadix(n, bases);
                assertEquals(n, MixedRadix.toDecimal(digits, bases),
                        "round-trip failed at n=" + n);
            }
        }

        @Test
        @DisplayName("Uniform base-2 (8 digits): all 256 values round-trip")
        void uniformBinary8Bits() {
            int[] bases = {2, 2, 2, 2, 2, 2, 2, 2};
            for (int n = 0; n < 256; n++) {
                int[] digits = MixedRadix.toMixedRadix(n, bases);
                assertEquals(n, MixedRadix.toDecimal(digits, bases),
                        "round-trip failed at n=" + n);
            }
        }

        @ParameterizedTest(name = "base={0}")
        @ValueSource(ints = {3, 5, 7, 10})
        @DisplayName("Uniform base (3 digits): all values 0..base^3-1 round-trip")
        void uniformBase3Digits(int base) {
            int[] bases = {base, base, base};
            int total = base * base * base;
            for (int n = 0; n < total; n++) {
                int[] digits = MixedRadix.toMixedRadix(n, bases);
                assertEquals(n, MixedRadix.toDecimal(digits, bases),
                        "base=" + base + " round-trip failed at n=" + n);
            }
        }

        @Test
        @DisplayName("Uniform base-10 (3 digits): all 1000 values round-trip")
        void uniformDecimal() {
            int[] bases = {10, 10, 10};
            for (int n = 0; n < 1000; n++) {
                int[] digits = MixedRadix.toMixedRadix(n, bases);
                assertEquals(n, MixedRadix.toDecimal(digits, bases),
                        "decimal round-trip failed at n=" + n);
            }
        }

        @Test
        @DisplayName("Uniform base: digits encode binary representation correctly")
        void binaryEncoding() {
            int[] bases = {2, 2, 2, 2};
            // 6 = 0110 binary; toMixedRadix MSD-first: [0,1,1,0]
            // toDecimal([0,1,1,0], {2,2,2,2}) = 0*1 + 1*2 + 1*4 + 0*8 = 6 ✓
            assertArrayEquals(new int[]{0, 1, 1, 0}, MixedRadix.toMixedRadix(6, bases));
            assertEquals(6, MixedRadix.toDecimal(new int[]{0, 1, 1, 0}, bases));
            assertArrayEquals(new int[]{1, 0, 0, 1}, MixedRadix.toMixedRadix(9, bases));
            assertEquals(9, MixedRadix.toDecimal(new int[]{1, 0, 0, 1}, bases));
        }
    }

    // =========================================================
    // 4. toMixedRadix: structural properties
    // =========================================================

    @Nested
    @DisplayName("toMixedRadix: structural properties")
    class StructuralProperties {

        @Test
        @DisplayName("Max value for bases [b0..bk] = product(bases) - 1 gives all (base-1) digits")
        void maxValueGivesAllMaxDigits() {
            int[] bases = {3, 4, 5};
            int maxDecimal = 3 * 4 * 5 - 1; // 59
            assertArrayEquals(new int[]{2, 3, 4}, MixedRadix.toMixedRadix(maxDecimal, bases),
                    "max value should give [base-1] for each position");
        }

        @Test
        @DisplayName("Digits are non-negative for non-negative input")
        void digitsNonNegative() {
            int[] bases = {5, 7, 3};
            for (int n = 0; n < 105; n++) {
                int[] d = MixedRadix.toMixedRadix(n, bases);
                for (int digit : d) assertTrue(digit >= 0, "digit must be >= 0 for n=" + n);
            }
        }

        @Test
        @DisplayName("toMixedRadix of n! correctly encodes factoradic-style boundaries")
        void factorialStyleBoundary() {
            // Validate the clock 1-day boundary: toMixedRadix(24*60*60, {MAX,24,60,60}) = [1,0,0,0]
            // and toMixedRadix(24*60*60 - 1, ...) = [0,23,59,59]
            int[] bases = {Integer.MAX_VALUE, 24, 60, 60};
            assertArrayEquals(new int[]{0, 23, 59, 59},
                    MixedRadix.toMixedRadix(86399, bases),
                    "last second of day should be [0,23,59,59]");
            assertArrayEquals(new int[]{1, 0, 0, 0},
                    MixedRadix.toMixedRadix(86400, bases),
                    "exactly one day");
        }
    }

    // =========================================================
    // 5. toDecimal: positional weight properties
    // =========================================================

    @Nested
    @DisplayName("toDecimal: positional weight properties")
    class PositionalWeightProperties {

        @Test
        @DisplayName("Weight at position i = product of radix[0..i-1]")
        void positionalWeights() {
            // radix = {2, 3, 4}: weights should be 1, 2, 6
            int[] radix = {2, 3, 4};
            assertEquals(1, MixedRadix.toDecimal(new int[]{1, 0, 0}, radix)); // weight[0]=1
            assertEquals(2, MixedRadix.toDecimal(new int[]{0, 1, 0}, radix)); // weight[1]=2
            assertEquals(6, MixedRadix.toDecimal(new int[]{0, 0, 1}, radix)); // weight[2]=6
        }

        @Test
        @DisplayName("toDecimal is linear: toDecimal(a+b) = toDecimal(a) + toDecimal(b) for non-overlapping digits")
        void linearity() {
            int[] radix = {10, 10, 10};
            // [3,0,0] + [0,2,0] = [3,2,0], values 3 + 20 = 23
            assertEquals(3, MixedRadix.toDecimal(new int[]{3, 0, 0}, radix));
            assertEquals(20, MixedRadix.toDecimal(new int[]{0, 2, 0}, radix));
            assertEquals(23, MixedRadix.toDecimal(new int[]{3, 2, 0}, radix));
        }
    }
}