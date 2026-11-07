package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Factorial and subfactorial are cached densely up to a limit and at checkpoints above it. These tests check that
 * every value is correct whatever order it is requested in, including across the dense/checkpoint boundary.
 */
@DisplayName("Calculator factorial / subFactorial caching")
class CalculatorFactorialCacheTest {

    /** Large enough to cover the dense range (2000) and well over a hundred checkpoints above it. */
    private static final int MAX = 3000;
    private static final BigInteger[] FACTORIAL = new BigInteger[MAX + 1];
    private static final BigInteger[] SUB_FACTORIAL = new BigInteger[MAX + 1];

    static {
        FACTORIAL[0] = BigInteger.ONE;
        for (int i = 1; i <= MAX; i++) {
            FACTORIAL[i] = FACTORIAL[i - 1].multiply(BigInteger.valueOf(i));
        }
        // Independent of the implementation's recurrence: !k = (k-1)(!(k-1) + !(k-2)).
        SUB_FACTORIAL[0] = BigInteger.ONE;
        SUB_FACTORIAL[1] = BigInteger.ZERO;
        for (int k = 2; k <= MAX; k++) {
            SUB_FACTORIAL[k] = BigInteger.valueOf(k - 1).multiply(SUB_FACTORIAL[k - 1].add(SUB_FACTORIAL[k - 2]));
        }
    }

    private static void assertValue(Calculator calculator, int n) {
        assertEquals(FACTORIAL[n], calculator.factorial(n), "factorial(" + n + ")");
        assertEquals(SUB_FACTORIAL[n], calculator.subFactorial(n), "subFactorial(" + n + ")");
    }

    @Test
    @DisplayName("small values and the dense/checkpoint boundary are correct")
    void smallValuesAndBoundary() {
        Calculator calculator = new Calculator();
        for (int n = 0; n <= 300; n++) assertValue(calculator, n);
        for (int n = 1950; n <= 2100; n++) assertValue(calculator, n);
    }

    @Test
    @DisplayName("ascending, descending and random request orders give the same values")
    void requestOrderDoesNotMatter() {
        Calculator ascending = new Calculator();
        for (int n = 0; n <= MAX; n++) assertValue(ascending, n);

        Calculator descending = new Calculator();
        for (int n = MAX; n >= 0; n--) assertValue(descending, n);

        Calculator everyOther = new Calculator();
        for (int n = MAX; n >= 2; n -= 2) assertValue(everyOther, n);

        Calculator shuffled = new Calculator();
        Random random = new Random(17);
        for (int i = 0; i < 3000; i++) assertValue(shuffled, random.nextInt(MAX + 1));
    }

    @Test
    @DisplayName("a single large request on a fresh calculator is correct (no neighbours cached)")
    void singleRequestOnFreshCalculator() {
        for (int n : new int[]{2001, 2019, 2020, 2021, 2500, 2999, 3000}) {
            assertValue(new Calculator(), n);
        }
    }

    @Test
    @DisplayName("checkpoints are derived correctly from both the next lower and the next higher checkpoint")
    void checkpointsFromEitherNeighbour() {
        Calculator calculator = new Calculator();
        // Cache two distant checkpoints, then request values nearer to one or the other.
        assertValue(calculator, 2200);
        assertValue(calculator, 2800);
        for (int n : new int[]{2210, 2399, 2400, 2401, 2599, 2600, 2601, 2790, 2230, 2780, 2500}) {
            assertValue(calculator, n);
        }
    }

    @Test
    @DisplayName("factorialUpperBound returns the smallest n with n! > value, including across the dense limit")
    void factorialUpperBound() {
        Calculator calculator = new Calculator();
        for (int n : new int[]{1, 2, 3, 4, 5, 10, 25, 100, 1999, 2000, 2001, 2020, 2500}) {
            assertEquals(n, calculator.factorialUpperBound(FACTORIAL[n].subtract(BigInteger.ONE)), "just below " + n + "!");
            assertEquals(n + 1, calculator.factorialUpperBound(FACTORIAL[n]), "equal to " + n + "!");
        }
        assertEquals(1, calculator.factorialUpperBound(BigInteger.ZERO));
    }

    @Test
    @DisplayName("clearCaches() discards cached values and results stay correct afterwards")
    void clearCaches() {
        Calculator calculator = new Calculator();
        for (int n : new int[]{150, 2100, 2750}) assertValue(calculator, n);
        calculator.clearCaches();
        for (int n : new int[]{2750, 150, 2100, 2101}) assertValue(calculator, n);
    }

    @Test
    @DisplayName("concurrent requests return correct values")
    void concurrentRequests() throws Exception {
        Calculator shared = new Calculator();
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int seed = t;
                results.add(pool.submit(() -> {
                    Random random = new Random(seed);
                    int mismatches = 0;
                    for (int i = 0; i < 1500; i++) {
                        int n = random.nextInt(MAX + 1);
                        if (!shared.factorial(n).equals(FACTORIAL[n])) mismatches++;
                        if (!shared.subFactorial(n).equals(SUB_FACTORIAL[n])) mismatches++;
                    }
                    return mismatches;
                }));
            }
            int total = 0;
            for (Future<Integer> f : results) total += f.get();
            assertEquals(0, total);
        } finally {
            pool.shutdown();
        }
    }

    @Test
    @DisplayName("Derangadic encoding of a small rank does not depend on a large universe of the same parity")
    void smallRankInLargeUniverse() {
        DerangadicAlgorithms algorithms = new DerangadicAlgorithms(new Calculator());
        for (long rank : new long[]{0, 1, 2, 3, 12345}) {
            BigInteger m = BigInteger.valueOf(rank);
            assertArrayEquals(algorithms.encode(m, 10), algorithms.encode(m, 4000), "even universe, rank " + rank);
            assertArrayEquals(algorithms.encode(m, 11), algorithms.encode(m, 3999), "odd universe, rank " + rank);
        }
    }
}
