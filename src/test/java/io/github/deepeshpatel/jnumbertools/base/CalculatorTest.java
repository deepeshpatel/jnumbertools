package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Calculator Number Theory Methods")
public class CalculatorTest {

    @Nested
    @DisplayName("Combination methods (nCr)")
    class CombinationTests {

        @Test
        @DisplayName("nCr: binomial coefficient")
        void testNCr() {
            assertEquals(BigInteger.ONE, calculator.nCr(5, 0));
            assertEquals(BigInteger.valueOf(10), calculator.nCr(5, 2));
            assertEquals(BigInteger.valueOf(1), calculator.nCr(5, 5));
        }

        @Test
        @DisplayName("nCrRepetitive: combinations with repetition C(n+r-1, r)")
        void testNCrRepetitive() {
            assertEquals(BigInteger.ONE, calculator.nCrRepetitive(5, 0));
            assertEquals(BigInteger.valueOf(5), calculator.nCrRepetitive(5, 1));
            assertEquals(BigInteger.valueOf(6), calculator.nCrRepetitive(3, 2));
            assertEquals(BigInteger.ZERO, calculator.nCrRepetitive(0, 1));
            assertEquals(BigInteger.ZERO, calculator.nCrRepetitive(5, -1));
            assertEquals(BigInteger.ZERO, calculator.nCrRepetitive(-5, 2));
            assertEquals(BigInteger.valueOf(10), calculator.nCrRepetitive(3, 3));
            assertEquals(BigInteger.valueOf(21), calculator.nCrRepetitive(3, 5));
        }

        @Test
        @DisplayName("nCrUpperBound: smallest n where nCr(n,r) > max")
        void testNCrUpperBound() {
            assertEquals(6, calculator.nCrUpperBound(2, BigInteger.valueOf(10)));
            assertEquals(6, calculator.nCrUpperBound(3, BigInteger.valueOf(10)));
            assertEquals(6, calculator.nCrUpperBound(1, BigInteger.valueOf(5)));
            assertEquals(0, calculator.nCrUpperBound(0, BigInteger.ZERO));
            assertEquals(2, calculator.nCrUpperBound(2, BigInteger.ZERO));
        }

        @Test
        @DisplayName("nCrUpperBound: exact boundaries around nCr(k, r)")
        void testNCrUpperBoundBoundaries() {
            for (int r = 1; r <= 30; r++) {
                for (int k = r; k < r + 40; k++) {
                    BigInteger c = calculator.nCr(k, r);
                    assertEquals(k, calculator.nCrUpperBound(r, c.subtract(BigInteger.ONE)),
                            "max = C(" + k + "," + r + ") - 1");
                    assertEquals(k + 1, calculator.nCrUpperBound(r, c),
                            "max = C(" + k + "," + r + ")");
                }
            }
        }

        @Test
        @DisplayName("nCrUpperBound: agrees with a linear scan")
        void testNCrUpperBoundMatchesLinearScan() {
            for (int r = 1; r <= 8; r++) {
                for (int max = -2; max <= 2000; max++) {
                    BigInteger bigMax = BigInteger.valueOf(max);
                    assertEquals(linearUpperBound(r, bigMax), calculator.nCrUpperBound(r, bigMax),
                            "r=" + r + ", max=" + max);
                }
            }
        }

        @Test
        @DisplayName("nCrUpperBound: large answers are found by bisection, not by scanning")
        void testNCrUpperBoundWithHugeAnswers() {
            // A linear scan would need ~1.0e9 and ~1.4e9 iterations here; bisection needs about 30.
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                assertEquals(1_000_000_001, calculator.nCrUpperBound(1, BigInteger.valueOf(1_000_000_000L)));

                BigInteger max = BigInteger.TEN.pow(18);
                int n = calculator.nCrUpperBound(2, max);
                assertTrue(calculator.nCr(n, 2).compareTo(max) > 0);
                assertTrue(calculator.nCr(n - 1, 2).compareTo(max) <= 0);

                BigInteger max3 = BigInteger.TEN.pow(27);
                int n3 = calculator.nCrUpperBound(3, max3);
                assertTrue(calculator.nCr(n3, 3).compareTo(max3) > 0);
                assertTrue(calculator.nCr(n3 - 1, 3).compareTo(max3) <= 0);
            });
        }

        @Test
        @DisplayName("nCrUpperBound: answer at the int limit is found, beyond it fails")
        void testNCrUpperBoundIntRangeLimit() {
            assertEquals(Integer.MAX_VALUE,
                    calculator.nCrUpperBound(1, BigInteger.valueOf(Integer.MAX_VALUE - 1L)));
            assertThrows(ArithmeticException.class,
                    () -> calculator.nCrUpperBound(1, BigInteger.valueOf(Integer.MAX_VALUE)));
        }

        @Test
        @DisplayName("nCrUpperBound: r = 0 and r < 0 cannot loop forever")
        void testNCrUpperBoundDegenerateR() {
            // nC0 = 1 for every n, so nothing can exceed max >= 1
            assertEquals(0, calculator.nCrUpperBound(0, BigInteger.ZERO));
            assertEquals(0, calculator.nCrUpperBound(0, BigInteger.valueOf(-5)));
            assertThrows(IllegalArgumentException.class, () -> calculator.nCrUpperBound(0, BigInteger.ONE));
            assertThrows(IllegalArgumentException.class, () -> calculator.nCrUpperBound(0, BigInteger.TEN));
            assertThrows(IllegalArgumentException.class, () -> calculator.nCrUpperBound(-1, BigInteger.TEN));
        }

        private int linearUpperBound(int r, BigInteger max) {
            int n = r;
            while (calculator.nCr(n, r).compareTo(max) <= 0) {
                n++;
            }
            return n;
        }

        @Test
        @DisplayName("totalSubsetsInRange: sum of nCr(n,k) for k in [from, to]")
        void testTotalSubsetsInRange() {
            assertEquals(BigInteger.valueOf(8), calculator.totalSubsetsInRange(0, 3, 3));
            assertEquals(BigInteger.valueOf(6), calculator.totalSubsetsInRange(1, 2, 3));
            assertEquals(BigInteger.ONE, calculator.totalSubsetsInRange(0, 0, 3));
            assertEquals(BigInteger.ONE, calculator.totalSubsetsInRange(0, 0, 0));
        }
    }

    @Nested
    @DisplayName("Permutation methods (nPr)")
    class PermutationTests {

        @Test
        @DisplayName("nPr: permutation coefficient")
        void testNPr() {
            assertEquals(BigInteger.valueOf(20), calculator.nPr(5, 2)); // 5P2 = 5!/(5-2)! = 20
            assertEquals(BigInteger.ONE, calculator.nPr(5, 0)); // nP0 = 1
            assertEquals(BigInteger.valueOf(120), calculator.nPr(5, 5)); // nPn = n!
        }
    }

    @Nested
    @DisplayName("Factorial and power methods")
    class FactorialTests {

        @Test
        @DisplayName("factorial: n!")
        void testFactorial() {
            assertEquals(BigInteger.ONE, calculator.factorial(0)); // 0! = 1
            assertEquals(BigInteger.ONE, calculator.factorial(1)); // 1! = 1
            assertEquals(BigInteger.valueOf(120), calculator.factorial(5)); // 5! = 120
            assertThrows(IllegalArgumentException.class, () -> calculator.factorial(-1)); // Negative input
        }

        @Test
        @DisplayName("power: a^b")
        void testPower() {
            assertEquals(BigInteger.valueOf(1), calculator.power(5, 0)); // Any number ^ 0 = 1
            assertEquals(BigInteger.valueOf(25), calculator.power(5, 2)); // 5^2 = 25
            assertEquals(BigInteger.valueOf(1), calculator.power(1, 100)); // 1^100 = 1
            assertEquals(BigInteger.valueOf(-27), calculator.power(-3, 3)); // -3^3 = -27
        }

        @Test
        @DisplayName("factorialUpperBound: smallest n where n! > threshold")
        void testFactorialUpperBound() {
            assertEquals(1, calculator.factorialUpperBound(BigInteger.ZERO));
            assertEquals(2, calculator.factorialUpperBound(BigInteger.ONE));
            assertEquals(3, calculator.factorialUpperBound(BigInteger.valueOf(2)));
            assertEquals(3, calculator.factorialUpperBound(BigInteger.valueOf(5)));
            assertEquals(4, calculator.factorialUpperBound(BigInteger.valueOf(6)));
            assertEquals(1, calculator.factorialUpperBound(BigInteger.valueOf(-10)));
        }

        @Test
        @DisplayName("subFactorial: !n (derangements)")
        void testSubFactorial() {
            assertEquals(BigInteger.ONE, calculator.subFactorial(0)); // !0 = 1
            assertEquals(BigInteger.ZERO, calculator.subFactorial(1)); // !1 = 0
            assertEquals(BigInteger.valueOf(1), calculator.subFactorial(2)); // !2 = 1
            assertEquals(BigInteger.valueOf(44), calculator.subFactorial(5)); // !5 = 44
        }
    }

    @Nested
    @DisplayName("Derangement-related methods")
    class DerangementTests {

        @Test
        @DisplayName("rencontresNumber: partial derangements")
        void testRencontresNumber() {
            assertEquals(BigInteger.ONE, calculator.rencontresNumber(0, 0));
            assertEquals(BigInteger.ZERO, calculator.rencontresNumber(1, 0));
            assertEquals(BigInteger.ONE, calculator.rencontresNumber(1, 1));
            assertEquals(BigInteger.valueOf(2), calculator.rencontresNumber(3, 0));
            assertEquals(BigInteger.valueOf(3), calculator.rencontresNumber(3, 1));
            assertEquals(BigInteger.ZERO, calculator.rencontresNumber(3, 2));
            assertEquals(BigInteger.ONE, calculator.rencontresNumber(3, 3));
            assertEquals(BigInteger.ZERO, calculator.rencontresNumber(3, -1));
            assertEquals(BigInteger.ZERO, calculator.rencontresNumber(3, 4));
        }
    }

    @Nested
    @DisplayName("Multiset combination methods")
    class MultisetCombinationTests {

        @Test
        @DisplayName("multisetCombinationsAll: all subset sizes")
        void testMultisetCombinationsAll() {
            int[] expected = {1, 3, 6, 8};
            int[] actual = Calculator.multisetCombinationsCountAll(2, 2, 3);
            assertArrayEquals(expected, actual);

            int[] freq = {1000, 1000, 1000};
            int total = Arrays.stream(freq).sum();
            int[] result = Calculator.multisetCombinationsCountAll(freq);
            assertEquals(501501, result[total-2000]);
            assertEquals(501501, result[1000]);
        }

        @Test
        @DisplayName("multisetCombinationsCount: exact count for specific size")
        void testMultisetCombinationsExact() {
            assertEquals(1, Calculator.multisetCombinationsCount(0, 2, 3, 2).intValue());
            assertEquals(6, Calculator.multisetCombinationsCount(2, 2, 3, 2).intValue());
            assertEquals(8, Calculator.multisetCombinationsCount(3, 2, 3, 2).intValue());
            assertEquals(1, Calculator.multisetCombinationsCount(7, 2, 3, 2).intValue());
            assertEquals(0, Calculator.multisetCombinationsCount(8, 2, 3, 2).intValue());

            assertEquals(501501, Calculator.multisetCombinationsCount(2000, 1000, 1000, 1000).intValue());
            assertEquals(501501, Calculator.multisetCombinationsCount(1000, 1000, 1000, 1000).intValue());
        }

        @Test
        @DisplayName("multisetCombinationsCount: invalid inputs throw exceptions")
        void testMultisetCombinationsExactInvalidInput() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.multisetCombinationsCount(-1, 2, 3, 2));
            assertThrows(IllegalArgumentException.class, () -> Calculator.multisetCombinationsCount(2, -1, 3, 2));
        }
    }

    @Nested
    @DisplayName("LCM (Least Common Multiple)")
    class LCMTest {

        @Test
        @DisplayName("Single element LCM")
        void testSingleElement() {
            assertEquals(BigInteger.valueOf(42), Calculator.lcm(BigInteger.valueOf(42)));
        }

        @Test
        @DisplayName("Two elements LCM")
        void testTwoElements() {
            assertEquals(BigInteger.valueOf(36), Calculator.lcm(BigInteger.valueOf(12), BigInteger.valueOf(18)));
        }

        @Test
        @DisplayName("Multiple elements LCM")
        void testMultipleElements() {
            assertEquals(BigInteger.valueOf(2520), Calculator.lcm(
                    BigInteger.valueOf(5), BigInteger.valueOf(7),
                    BigInteger.valueOf(8), BigInteger.valueOf(9)));
        }

        @Test
        @DisplayName("LCM with zero")
        void testWithZero() {
            assertEquals(BigInteger.ZERO, Calculator.lcm(BigInteger.valueOf(12), BigInteger.ZERO, BigInteger.valueOf(18)));
        }

        @Test
        @DisplayName("LCM with one")
        void testWithOne() {
            assertEquals(BigInteger.valueOf(18), Calculator.lcm(BigInteger.ONE, BigInteger.valueOf(18)));
        }

        @Test
        @DisplayName("LCM for large numbers")
        void testLargeNumbers() {
            BigInteger a = new BigInteger("12345678901234567890");
            BigInteger b = new BigInteger("98765432109876543210");
            BigInteger expected = a.multiply(b).divide(a.gcd(b));
            assertEquals(expected, Calculator.lcm(a, b));
        }

        @Test
        @DisplayName("LCM for large array")
        void testLargeArray() {
            BigInteger[] array = new BigInteger[100];
            for (int i = 0; i < array.length; i++) {
                array[i] = BigInteger.valueOf(i + 1);
            }
            BigInteger expected = BigInteger.valueOf(1);
            for (int i = 2; i <= 100; i++) {
                expected = expected.multiply(BigInteger.valueOf(i)).divide(expected.gcd(BigInteger.valueOf(i)));
            }
            assertEquals(expected, Calculator.lcm(array));
        }

        @Test
        @DisplayName("LCM with negative numbers")
        void testNegativeNumbers() {
            assertEquals(BigInteger.valueOf(36), Calculator.lcm(BigInteger.valueOf(-12), BigInteger.valueOf(18)));
        }

        @Test
        @DisplayName("LCM of all ones")
        void testAllOnes() {
            assertEquals(BigInteger.ONE, Calculator.lcm(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE));
        }

        @Test
        @DisplayName("LCM with empty array throws exception")
        void testInvalidInputEmptyArray() {
            var exception = assertThrows(IllegalArgumentException.class, () -> Calculator.lcm());
            assertEquals("At least one number required",exception.getMessage());
        }

        @Test
        @DisplayName("LCM performance test")
        void testPerformance() {
            BigInteger[] largeArray = new BigInteger[5000];
            for (int i = 0; i < largeArray.length; i++) {
                largeArray[i] = BigInteger.valueOf(i + 1);
            }
            assertDoesNotThrow(() -> Calculator.lcm(largeArray));
        }
    }

}
