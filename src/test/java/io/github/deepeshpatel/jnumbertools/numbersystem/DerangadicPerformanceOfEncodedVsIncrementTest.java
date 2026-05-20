package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance evaluation for Derangadic increment vs materialization.
 *
 * <p>Compares three approaches:</p>
 * <ul>
 *   <li><b>Full Increment</b> - DerangadicIncrement.increment() which maintains derangement</li>
 *   <li><b>Materialization Only</b> - Pre-generate encoded digits, then materialize via Algo.toDerangement()</li>
 *   <li><b>Unrank Baseline</b> - Direct rank-based conversion for comparison</li>
 * </ul>
 *
 * <p>This test is disabled by default. Enable with -Dstress.testing=true</p>
 */
@Disabled
public class DerangadicPerformanceOfEncodedVsIncrementTest {

    private static final Calculator CALC = new Calculator();
    private static final DerangadicAlgorithms ALG = new DerangadicAlgorithms(CALC);
    private static final DerangadicIncrement INC = new DerangadicIncrement(CALC);

    @Test
    @DisplayName("Performance: Compare increment vs materialization vs unrank")
    void testPerformanceComparison() {
        int n = 50000;
        long startRank = 9_000_000_000_000L;
        int iterations = 10000;

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║           Derangadic Performance: Increment vs Materialization vs Unrank       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
        System.out.printf("%n  n = %,d, ranks = %,d to %,d (%d iterations)%n%n",
                n, startRank + 1, startRank + iterations, iterations);

        // Verify correctness first
        verifyMethodsAgree(n, startRank, iterations);

        System.out.println();

        // Run benchmarks
        benchmarkFullIncrement(n, startRank, iterations);
        benchmarkMaterializationOnly(n, startRank, iterations);
        benchmarkUnrankBaseline(n, startRank, iterations);

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                              Analysis Complete                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Benchmark 1: Full Increment (maintains derangement incrementally)
     */
    private void benchmarkFullIncrement(int n, long startRank, int iterations) {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ Benchmark 1: DerangadicIncrement.increment() (full incremental)                 │");
        System.out.println("├─────────────────────────────────────────────────────────────────────────────────┤");

        DerangadicIncrement.DerangadicState state = INC.initialState(n, BigInteger.valueOf(startRank));
        long sink = 0;

        // Warmup
        for (int i = 0; i < 100; i++) {
            INC.increment(state);
            sink += state.currentDerangement()[0];
        }

        System.gc();

        // Timed run
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            INC.increment(state);
            int[] d = state.currentDerangement();
            sink += d[0];
        }
        long elapsed = System.nanoTime() - startTime;

        double ms = elapsed / 1_000_000.0;
        double nsPerIter = (double) elapsed / iterations;

        System.out.printf("│ Total time: %,10.2f ms%n", ms);
        System.out.printf("│ Time per iteration: %,8.1f ns%n", nsPerIter);
        System.out.printf("│ Sink (prevent optimization): %d%n", sink);
        System.out.println("└─────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    /**
     * Benchmark 2: Materialization Only (pre-generate encoded digits, then materialize)
     */
    private void benchmarkMaterializationOnly(int n, long startRank, int iterations) {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ Benchmark 2: Materialization Only (pre-generated encoded → Algo.toDerangement)  │");
        System.out.println("├─────────────────────────────────────────────────────────────────────────────────┤");

        // Step 1: Pre-generate encoded digits (NOT TIMED)
        DerangadicIncrement.DerangadicState state = INC.initialState(n, BigInteger.valueOf(startRank));
        List<int[]> encodedCache = new ArrayList<>(iterations);

        for (int i = 0; i < iterations; i++) {
            INC.incrementEncoded(state);
            encodedCache.add(state.getDigits());
        }

        // Step 2: Time materialization only
        long sink = 0;

        // Warmup
        for (int i = 0; i < 100; i++) {
            int[] d = ALG.toDerangement(encodedCache.get(i), n);
            sink += d[0];
        }

        System.gc();

        // Timed run
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int[] d = ALG.toDerangement(encodedCache.get(i), n);
            sink += d[0];
        }
        long elapsed = System.nanoTime() - startTime;

        double ms = elapsed / 1_000_000.0;
        double nsPerIter = (double) elapsed / iterations;

        System.out.printf("│ Pre-generation time: NOT TIMED (excluded from this benchmark)%n");
        System.out.printf("│ Total materialization time: %,10.2f ms%n", ms);
        System.out.printf("│ Materialization per iteration: %,8.1f ns%n", nsPerIter);
        System.out.printf("│ Sink (prevent optimization): %d%n", sink);
        System.out.println("└─────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    /**
     * Benchmark 3: Unrank Baseline (direct rank-based conversion)
     */
    private void benchmarkUnrankBaseline(int n, long startRank, int iterations) {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ Benchmark 3: Unrank Baseline (direct rank-based conversion)                     │");
        System.out.println("├─────────────────────────────────────────────────────────────────────────────────┤");

        long sink = 0;

        // Warmup
        for (int i = 0; i < 100; i++) {
            int[] d = ALG.unrank(startRank + i + 1, n);
            sink += d[0];
        }

        System.gc();

        // Timed run
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int[] d = ALG.unrank(startRank + i + 1, n);
            sink += d[0];
        }
        long elapsed = System.nanoTime() - startTime;

        double ms = elapsed / 1_000_000.0;
        double nsPerIter = (double) elapsed / iterations;

        System.out.printf("│ Total time: %,10.2f ms%n", ms);
        System.out.printf("│ Time per iteration: %,8.1f ns%n", nsPerIter);
        System.out.printf("│ Sink (prevent optimization): %d%n", sink);
        System.out.println("└─────────────────────────────────────────────────────────────────────────────────┘");
    }

    /**
     * Verify that all methods produce identical results
     */
    private void verifyMethodsAgree(int n, long startRank, int iterations) {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ Verification: All methods produce identical derangements                       │");
        System.out.println("├─────────────────────────────────────────────────────────────────────────────────┤");

        // Method 1: Full increment
        DerangadicIncrement.DerangadicState incState = INC.initialState(n, BigInteger.valueOf(startRank));

        // Method 2: Materialization from encoded
        DerangadicIncrement.DerangadicState encState = INC.initialState(n, BigInteger.valueOf(startRank));
        List<int[]> encodedCache = new ArrayList<>(iterations);
        for (int i = 0; i < iterations; i++) {
            INC.incrementEncoded(encState);
            encodedCache.add(encState.getDigits());
        }

        boolean allMatch = true;
        for (int i = 0; i < iterations && allMatch; i++) {
            INC.increment(incState);
            int[] fromIncrement = incState.currentDerangement();

            int[] fromMaterialize = ALG.toDerangement(encodedCache.get(i), n);
            int[] fromUnrank = ALG.unrank(startRank + i + 1, n);

            if (!java.util.Arrays.equals(fromIncrement, fromMaterialize) ||
                    !java.util.Arrays.equals(fromIncrement, fromUnrank)) {
                System.out.printf("│ ✗ MISMATCH at iteration %d%n", i);
                allMatch = false;
                break;
            }
        }

        if (allMatch) {
            System.out.println("│ ✓ All methods produce identical derangements for all iterations");
        }
        System.out.println("└─────────────────────────────────────────────────────────────────────────────────┘");
    }

    /**
     * Run with different n values for comprehensive comparison
     */
    @Test
    @DisplayName("Performance: Compare across different n values")
    void testPerformanceAcrossN() {
        int[] nValues = {100, 500, 1000, 5000, 10000, 50000};
        int iterations = 1000;
        long startRank = 9_000_000_000_000L;

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║           Performance Across Different n Values                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");

        System.out.println("\n┌─────────┬──────────────────┬──────────────────┬──────────────────┐");
        System.out.println("│    n    │  Increment (ns)   │ Materialize (ns) │   Unrank (ns)    │");
        System.out.println("├─────────┼──────────────────┼──────────────────┼──────────────────┤");

        for (int n : nValues) {
            if (n > 5000 && startRank > ALG.derangementCount(n).longValue()) {
                // Adjust startRank for smaller n
                continue;
            }

            long incTime = measureFullIncrement(n, startRank, iterations);
            long matTime = measureMaterializationOnly(n, startRank, iterations);
            long unrankTime = measureUnrankBaseline(n, startRank, iterations);

            System.out.printf("│ %7d │ %,14d │ %,14d │ %,14d │%n",
                    n, incTime, matTime, unrankTime);
        }

        System.out.println("└─────────┴──────────────────┴──────────────────┴──────────────────┘");
    }

    private long measureFullIncrement(int n, long startRank, int iterations) {
        DerangadicIncrement.DerangadicState state = INC.initialState(n, BigInteger.valueOf(startRank));

        // Warmup
        for (int i = 0; i < 100; i++) {
            INC.increment(state);
        }

        System.gc();

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            INC.increment(state);
        }
        return (System.nanoTime() - startTime) / iterations;
    }

    private long measureMaterializationOnly(int n, long startRank, int iterations) {
        // Pre-generate encoded digits
        DerangadicIncrement.DerangadicState state = INC.initialState(n, BigInteger.valueOf(startRank));
        List<int[]> encodedCache = new ArrayList<>(iterations);
        for (int i = 0; i < iterations; i++) {
            INC.incrementEncoded(state);
            encodedCache.add(state.getDigits());
        }

        // Warmup
        for (int i = 0; i < 100; i++) {
            ALG.toDerangement(encodedCache.get(i), n);
        }

        System.gc();

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ALG.toDerangement(encodedCache.get(i), n);
        }
        return (System.nanoTime() - startTime) / iterations;
    }

    private long measureUnrankBaseline(int n, long startRank, int iterations) {
        // Warmup
        for (int i = 0; i < 100; i++) {
            ALG.unrank(startRank + i + 1, n);
        }

        System.gc();

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ALG.unrank(startRank + i + 1, n);
        }
        return (System.nanoTime() - startTime) / iterations;
    }
}