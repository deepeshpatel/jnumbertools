package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {
    private final Calculator calculator = new Calculator();

    @Test
    public void testNCrRepetitive() {

        // Case 1: r = 0. Choosing 0 objects should always result in 1 way.
        assertEquals(BigInteger.ONE, calculator.nCrRepetitive(5, 0));

        // Case 2: r = 1. With repetition, nCrRepetitive(5, 1) = nCr(5+1-1, 1) = nCr(5, 1) = 5.
        assertEquals(BigInteger.valueOf(5), calculator.nCrRepetitive(5, 1));

        // Case 3: Typical case. For n = 3 and r = 2:
        // nCrRepetitive(3, 2) = nCr(3+2-1, 2) = nCr(4, 2) = 6.
        assertEquals(BigInteger.valueOf(6), calculator.nCrRepetitive(3, 2));

        // Case 4: Edge condition with n = 0 and r > 0.
        // nCrRepetitive(0, 1) = nCr(0+1-1, 1) = nCr(0, 1).
        // Since there are 0 objects to choose from, the expected result is 0.
        assertEquals(BigInteger.ZERO, calculator.nCrRepetitive(0, 1));

        // Case 5: Negative r. According to the underlying nCr method, negative r should yield 0.
        assertEquals(BigInteger.ZERO, calculator.nCrRepetitive(5, -1));

        // Case 6: Negative n. Negative distinct objects result in 0 combinations.
        assertEquals(BigInteger.ZERO, calculator.nCrRepetitive(-5, 2));

        // Case 7: When r equals n.
        // Example: n = 3, r = 3 yields: nCrRepetitive(3, 3) = nCr(3+3-1, 3) = nCr(5, 3).
        // nCr(5, 3) equals nCr(5, 2) = 10.
        assertEquals(BigInteger.valueOf(10), calculator.nCrRepetitive(3, 3));

        // Case 8: When r > n.
        // Example: n = 3, r = 5 gives: nCrRepetitive(3, 5) = nCr(3+5-1, 5) = nCr(7, 5).
        // Since nCr(7, 5) = nCr(7, 2) = 21, we expect 21.
        assertEquals(BigInteger.valueOf(21), calculator.nCrRepetitive(3, 5));
    }


    @Test
    void testNCr() {
        assertEquals(BigInteger.ONE, calculator.nCr(5, 0));
        assertEquals(BigInteger.valueOf(10), calculator.nCr(5, 2));
        assertEquals(BigInteger.valueOf(1), calculator.nCr(5, 5));
        //assertThrows(IllegalArgumentException.class, () -> calculator.nCr(5, -1));
    }

    @Test
    public void testNCrUpperBound() {

        // Case 1:
        // For r = 2, nCr(5,2) = 10 which is equal to max, so the next n (n = 6) should satisfy: 6C2 = 15 > 10.
        int result = calculator.nCrUpperBound(2, BigInteger.valueOf(10));
        assertEquals(6, result);

        // Case 2:
        // For r = 3, nCr(5,3) = 10 equals max, so n = 6 (6C3 = 20 > 10) is the smallest valid n.
        result = calculator.nCrUpperBound(3, BigInteger.valueOf(10));
        assertEquals(6, result);

        // Case 3:
        // For r = 1, note that nCr(n, 1) = n. To have n > 5, the smallest n is 6.
        result = calculator.nCrUpperBound(1, BigInteger.valueOf(5));
        assertEquals(6, result);

        // Case 4:
        // For r = 0, nCr(n, 0) is always 1. If max is 0 then already 1 > 0. Since we initialize n = r = 0,
        // the smallest n is 0.
        result = calculator.nCrUpperBound(0, BigInteger.ZERO);
        assertEquals(0, result);

        // Case 5:
        // For r = 2 and max = 0, starting with n = 2 gives nCr(2,2) = 1 which is > 0, so the expected n is 2.
        result = calculator.nCrUpperBound(2, BigInteger.ZERO);
        assertEquals(2, result);
    }


    @Test
    void testNPr() {
        assertEquals(BigInteger.valueOf(20), calculator.nPr(5, 2)); // 5P2 = 5!/(5-2)! = 20
        assertEquals(BigInteger.ONE, calculator.nPr(5, 0)); // nP0 = 1
        assertEquals(BigInteger.valueOf(120), calculator.nPr(5, 5)); // nPn = n!
        //assertThrows(IllegalArgumentException.class, () -> calculator.nPr(5, -1)); // Negative r
    }

    @Test
    public void testFactorialUpperBound() {

        // Case 1: Threshold = 0
        // Since 1! = 1 > 0, the smallest n is 1.
        assertEquals(1, calculator.factorialUpperBound(BigInteger.ZERO),
                "factorialUpperBound(0) should return 1");

        // Case 2: Threshold = 1
        // 1! = 1 is not greater than 1, but 2! = 2 > 1, so the smallest n is 2.
        assertEquals(2, calculator.factorialUpperBound(BigInteger.ONE),
                "factorialUpperBound(1) should return 2");

        // Case 3: Threshold = 2
        // 2! = 2 is not > 2, but 3! = 6 > 2, so the smallest n is 3.
        assertEquals(3, calculator.factorialUpperBound(BigInteger.valueOf(2)),
                "factorialUpperBound(2) should return 3");

        // Case 4: Threshold = 5
        // 3! = 6 > 5, so the smallest n is 3.
        assertEquals(3, calculator.factorialUpperBound(BigInteger.valueOf(5)),
                "factorialUpperBound(5) should return 3");

        // Case 5: Threshold = 6
        // 3! = 6 is not greater than 6, but 4! = 24 > 6, so the smallest n is 4.
        assertEquals(4, calculator.factorialUpperBound(BigInteger.valueOf(6)),
                "factorialUpperBound(6) should return 4");

        // Case 6: Negative threshold
        // For any negative threshold, since 1! = 1 > negative number, the smallest n is 1.
        assertEquals(1, calculator.factorialUpperBound(BigInteger.valueOf(-10)),
                "factorialUpperBound(-10) should return 1");
    }


    @Test
    void testFactorial() {
        assertEquals(BigInteger.ONE, calculator.factorial(0)); // 0! = 1
        assertEquals(BigInteger.ONE, calculator.factorial(1)); // 1! = 1
        assertEquals(BigInteger.valueOf(120), calculator.factorial(5)); // 5! = 120
        assertThrows(IllegalArgumentException.class, () -> calculator.factorial(-1)); // Negative input
    }

    @Test
    void testPower() {
        assertEquals(BigInteger.valueOf(1), calculator.power(5, 0)); // Any number ^ 0 = 1
        assertEquals(BigInteger.valueOf(25), calculator.power(5, 2)); // 5^2 = 25
        assertEquals(BigInteger.valueOf(1), calculator.power(1, 100)); // 1^100 = 1
        assertEquals(BigInteger.valueOf(-27), calculator.power(-3, 3)); // -3^3 = -27
    }

    @Test
    public void testTotalSubsetsInRange() {
        // Case 1: from == 0 and to == noOfElements, should return 2^noOfElements.
        assertEquals(BigInteger.valueOf(8), calculator.totalSubsetsInRange(0, 3, 3));

        // Case 2: Partial range (from != 0 or to != noOfElements).
        // For noOfElements = 3, from = 1 and to = 2, expected result is nCr(3,1) + nCr(3,2) = 3 + 3 = 6.
        assertEquals(BigInteger.valueOf(6), calculator.totalSubsetsInRange(1, 2, 3));

        // Case 3: When only the empty set is considered (from = 0 and to = 0).
        // For noOfElements = 3, expected result is nCr(3,0) = 1.
        assertEquals(BigInteger.ONE, calculator.totalSubsetsInRange(0, 0, 3));

        // Case 4: When noOfElements is 0, there is only one subset (the empty set).
        // Since from = 0 and to = 0 equals noOfElements, it returns power(2, 0) = 1.
        assertEquals(BigInteger.ONE, calculator.totalSubsetsInRange(0, 0, 0));
    }


    @Test
    void testSubFactorial() {
        assertEquals(BigInteger.ONE, calculator.subFactorial(0)); // !0 = 1
        assertEquals(BigInteger.ZERO, calculator.subFactorial(1)); // !1 = 0
        assertEquals(BigInteger.valueOf(1), calculator.subFactorial(2)); // !2 = 1
        assertEquals(BigInteger.valueOf(44), calculator.subFactorial(5)); // !5 = 44
        //assertThrows(IllegalArgumentException.class, () -> calculator.subFactorial(-1)); // Negative case
    }

    @Test
    public void testRencontresNumber() {

        // Case 1: n = 0, k = 0.
        // Expected: nCr(0, 0) = 1 and subFactorial(0) = 1, so 1 * 1 = 1.
        assertEquals(BigInteger.ONE, calculator.rencontresNumber(0, 0));

        // Case 2: n = 1, k = 0.
        // Expected: nCr(1, 0) = 1 and subFactorial(1) = 0, so 1 * 0 = 0.
        assertEquals(BigInteger.ZERO, calculator.rencontresNumber(1, 0));

        // Case 3: n = 1, k = 1.
        // Expected: nCr(1, 1) = 1 and subFactorial(0) = 1, so 1 * 1 = 1.
        assertEquals(BigInteger.ONE, calculator.rencontresNumber(1, 1));

        // Case 4: n = 3, k = 0.
        // Expected: nCr(3, 0) = 1 and subFactorial(3) = 2, so 1 * 2 = 2.
        assertEquals(BigInteger.valueOf(2), calculator.rencontresNumber(3, 0));

        // Case 5: n = 3, k = 1.
        // Expected: nCr(3, 1) = 3 and subFactorial(2) = 1, so 3 * 1 = 3.
        assertEquals(BigInteger.valueOf(3), calculator.rencontresNumber(3, 1));

        // Case 6: n = 3, k = 2.
        // Expected: nCr(3, 2) = 3 and subFactorial(1) = 0, so 3 * 0 = 0.
        assertEquals(BigInteger.ZERO, calculator.rencontresNumber(3, 2));

        // Case 7: n = 3, k = 3.
        // Expected: nCr(3, 3) = 1 and subFactorial(0) = 1, so 1 * 1 = 1.
        assertEquals(BigInteger.ONE, calculator.rencontresNumber(3, 3));

        // Case 8: k < 0 (invalid input).
        // Expected: method returns BigInteger.ZERO.
        assertEquals(BigInteger.ZERO, calculator.rencontresNumber(3, -1));

        // Case 9: k > n (invalid input).
        // Expected: method returns BigInteger.ZERO.
        assertEquals(BigInteger.ZERO, calculator.rencontresNumber(3, 4));
    }

    @Test
    void testMultisetCombinationsAll() {
        // For frequencies {2, 2, 3}, total = 2+2+3 = 7.
        // The method returns coefficients for s = 0, 1, 2, 3.
        // Expected dp array: [1, 3, 6, 8]
        int[] expected = {1, 3, 6, 8};
        int[] actual = Calculator.multisetCombinationsCountAll(2, 2, 3);
        assertArrayEquals(expected, actual);

        //test for large values
        int[] freq = {1000, 1000, 1000};
        int total = Arrays.stream(freq).sum();
        int[] result = calculator.multisetCombinationsCountAll(freq);
        assertEquals(501501, result[total-2000]);
        assertEquals(501501, result[1000]);
    }

    @Test
    void testMultisetCombinationsExact() {
        // Using frequencies {2, 3, 2} (total = 7)
        // k = 0 → only one way (empty selection)
        assertEquals(1, calculator.multisetCombinationsCount(0, 2, 3, 2).intValue());
        // k = 2 → expected 6 ways (e.g., [A,A], [A,G], [A,B], [G,G], [G,B], [B,B])
        assertEquals(6, calculator.multisetCombinationsCount(2, 2, 3, 2).intValue());
        // k = 3 → expected 8 ways as described in the documentation
        assertEquals(8, calculator.multisetCombinationsCount(3, 2, 3, 2).intValue());
        // k = 7 (select all items) → 1 way
        assertEquals(1, calculator.multisetCombinationsCount(7, 2, 3, 2).intValue());
        // k = 8 (more than available items) → 0 ways
        assertEquals(0, calculator.multisetCombinationsCount(8, 2, 3, 2).intValue());

        //Test for large values
        assertEquals(501501, calculator.multisetCombinationsCount(2000, 1000, 1000, 1000).intValue());
        assertEquals(501501, calculator.multisetCombinationsCount(1000, 1000, 1000, 1000).intValue());
    }

    @Test
    void testMultisetCombinationsExactInvalidInput() {
        // Negative k should throw an exception.
        assertThrows(IllegalArgumentException.class, () -> calculator.multisetCombinationsCount(-1, 2, 3, 2));
        // Negative frequency should throw an exception.
        assertThrows(IllegalArgumentException.class, () -> calculator.multisetCombinationsCount(2, -1, 3, 2));
    }

}
