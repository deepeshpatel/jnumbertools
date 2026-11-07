package io.github.deepeshpatel.jnumbertools.datastructure;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BinomialCoefficientMap")
class BinomialCoefficientMapTest {

    /** Independent reference: multiplicative formula, no cache. */
    private static BigInteger reference(int n, int r) {
        BigInteger value = BigInteger.ONE;
        for (int i = 1; i <= r; i++) {
            value = value.multiply(BigInteger.valueOf(n - r + i)).divide(BigInteger.valueOf(i));
        }
        return value;
    }

    @Test
    @DisplayName("ascending, descending and random request order give the same values")
    void anyRequestOrderGivesCorrectValues() {
        int n = 200;
        BigInteger[] expected = new BigInteger[n / 2 + 1];
        for (int r = 0; r <= n / 2; r++) expected[r] = reference(n, r);

        BinomialCoefficientMap ascending = new BinomialCoefficientMap();
        for (int r = 0; r <= n / 2; r++) assertEquals(expected[r], ascending.computeIfAbsent(n, r), "ascending r=" + r);

        BinomialCoefficientMap descending = new BinomialCoefficientMap();
        for (int r = n / 2; r >= 0; r--) assertEquals(expected[r], descending.computeIfAbsent(n, r), "descending r=" + r);

        BinomialCoefficientMap shuffled = new BinomialCoefficientMap();
        Random random = new Random(11);
        for (int i = 0; i < 2000; i++) {
            int r = random.nextInt(n / 2 + 1);
            assertEquals(expected[r], shuffled.computeIfAbsent(n, r), "random r=" + r);
        }
    }

    @Test
    @DisplayName("rows for different n are independent")
    void rowsAreIndependent() {
        BinomialCoefficientMap map = new BinomialCoefficientMap();
        for (int n = 10; n <= 60; n++) {
            for (int r = 3; r <= n / 2; r++) {
                assertEquals(reference(n, r), map.computeIfAbsent(n, r), "n=" + n + " r=" + r);
            }
        }
    }

    @Test
    @DisplayName("clear() discards cached values and results stay correct afterwards")
    void clearThenRecompute() {
        BinomialCoefficientMap map = new BinomialCoefficientMap();
        BigInteger first = map.computeIfAbsent(300, 120);
        map.clear();
        assertEquals(first, map.computeIfAbsent(300, 120));
        assertEquals(reference(300, 40), map.computeIfAbsent(300, 40));
    }

    @Test
    @DisplayName("Calculator.nCr matches the reference for every (n, r), including r > n/2 and out-of-range r")
    void calculatorNCrMatchesReference() {
        Calculator calculator = new Calculator();
        for (int n = 0; n <= 120; n++) {
            for (int r = -2; r <= n + 2; r++) {
                BigInteger expected = (r < 0 || r > n) ? BigInteger.ZERO : reference(n, Math.min(r, n - r));
                assertEquals(expected, calculator.nCr(n, r), "n=" + n + " r=" + r);
            }
        }
    }

    @Test
    @DisplayName("concurrent requests for overlapping rows return correct values")
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
                    for (int i = 0; i < 5000; i++) {
                        int n = 3 + random.nextInt(i % 50 == 0 ? 500 : 60);
                        int r = random.nextInt(n + 1);
                        if (!shared.nCr(n, r).equals(reference(n, Math.min(r, n - r)))) mismatches++;
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
}
