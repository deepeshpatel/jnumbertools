package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.DerangadicAlgorithmsTest.isValidDerangement;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Disabled
public class DerangementAlgoPerformanceTest {

    private static final DerangadicAlgorithms DERANGADIC = new DerangadicAlgorithms();

    @Test
    @DisplayName("Performance test - should generate 10,000 derangements quickly")
    void testPerformance() {
        int n = 8;
        int iterations = 10000;
        BigInteger total = DERANGADIC.derangementCount(n);
        assertTrue(total.longValue() >= iterations,
                String.format("D_%d should be at least %d", n, iterations));

        long startTime = System.currentTimeMillis();

        for (long m = 0; m < iterations; m++) {
            int[] derangement = DERANGADIC.unrank(BigInteger.valueOf(m), n);
            assertTrue(isValidDerangement(derangement),
                    String.format("Invalid derangement at rank %d", m));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 500,
                String.format("Performance too slow: %d ms for %d derangements", duration, iterations));
        System.out.printf("Performance: %d ms for %d derangements (%.3f µs each)%n",
                duration, iterations, duration * 1000.0 / iterations);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    @DisplayName("Performance comparison with increasing n (fixed rank)")
    void testPerformanceFixedRank() {
        int rank = 7;  // Fixed small rank
        int[] nValues = {100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000};
        int iterations = 1000;

        System.out.println("\n=== Performance: Fixed Rank=" + rank + " ===");
        System.out.printf("%-10s %-15s %-15s %-10s%n", "n", "Array (ms)", "Fenwick (ms)", "Winner");
        System.out.println("-".repeat(55));

        for (int n : nValues) {

            int[] digits = DERANGADIC.toDerangadic(rank, n);

            // Warmup
            for (int i = 0; i < 100; i++) {
                DERANGADIC.toDerangementArray(digits, n);
                DERANGADIC.toDerangementFenwick(digits, n);
            }

            // Test Array
            long startArray = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementArray(digits, n);
            }
            long timeArray = System.currentTimeMillis() - startArray;

            // Test Fenwick
            long startFenwick = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementFenwick(digits, n);
            }
            long timeFenwick = System.currentTimeMillis() - startFenwick;

            String winner = (timeArray < timeFenwick) ? "Array" : "Fenwick";
            System.out.printf("%-10d %-15d %-15d %-10s%n", n, timeArray, timeFenwick, winner);
        }
    }


    @Test
    @DisplayName("Performance comparison: Finding the Crossover Point (n=10000)")
    void testPerformanceCrossover() {
        int n = 10000;

        List<NamedRank> testRanks = new ArrayList<>();
        testRanks.add(new NamedRank("Small (15 digits)", BigInteger.valueOf(1_000_000_000_000_000L)));
        testRanks.add(new NamedRank("Medium (100 digits)", BigInteger.TEN.pow(100)));
        testRanks.add(new NamedRank("Large (00 digits)", BigInteger.TEN.pow(600)));
        testRanks.add(new NamedRank("Large (700 digits)", BigInteger.TEN.pow(700)));
        testRanks.add(new NamedRank("Large (800 digits)", BigInteger.TEN.pow(800)));
        testRanks.add(new NamedRank("Heavy (900 digits)", BigInteger.TEN.pow(900)));
        testRanks.add(new NamedRank("Heavy (1000 digits)", BigInteger.TEN.pow(1000)));
        testRanks.add(new NamedRank("Heavy (1500 digits)", BigInteger.TEN.pow(1500)));
        testRanks.add(new NamedRank("Heavy (1600 digits)", BigInteger.TEN.pow(1600)));
        testRanks.add(new NamedRank("Heavy (1700 digits)", BigInteger.TEN.pow(1700)));

        int iterations = 100;

        System.out.println("\n=== Performance: Fixed n=" + n + " (Iterations: " + iterations + ") ===");
        System.out.printf("%-20s %-15s %-15s %-10s%n", "Rank Magnitude", "Array (ms)", "Fenwick (ms)", "Winner");
        System.out.println("-".repeat(70));

        for (NamedRank nr : testRanks) {
            int[] digits = DERANGADIC.toDerangadic(nr.value, n);

            // Warmup
            for (int i = 0; i < 5; i++) {
                DERANGADIC.toDerangementArray(digits, n);
                DERANGADIC.toDerangementFenwick(digits, n);
            }

            long startArray = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementArray(digits, n);
            }
            long timeArray = System.currentTimeMillis() - startArray;

            long startFenwick = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementFenwick(digits, n);
            }
            long timeFenwick = System.currentTimeMillis() - startFenwick;

            String winner = (timeArray < timeFenwick) ? "Array" : "Fenwick";
            System.out.printf("%-20s %-15d %-15d %-10s%n", nr.name, timeArray, timeFenwick, winner);
        }
    }

    @Test
    @DisplayName("Find N_THRESHOLD: Fixed High Rank vs Variable N")
    void testFindNThreshold() {
        // Constant high rank to ensure we are always testing against the 'digits' logic
        BigInteger highRank = BigInteger.TEN.pow(150);

        // Vary N from very small to the current threshold area
        int[] nValues = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000, 4000};
        int iterations = 1000;

        System.out.println("\n=== N_THRESHOLD Search (Rank fixed at 10^150) ===");
        System.out.printf("%-10s %-15s %-15s %-10s%n", "N", "Array (ms)", "Fenwick (ms)", "Winner");
        System.out.println("-".repeat(55));

        for (int n : nValues) {
            int[] digits = DERANGADIC.toDerangadic(highRank, n);

            // Warmup
            for (int i = 0; i < 100; i++) {
                DERANGADIC.toDerangementArray(digits, n);
                DERANGADIC.toDerangementFenwick(digits, n);
            }

            // Test Array
            long startArray = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementArray(digits, n);
            }
            long timeArray = System.currentTimeMillis() - startArray;

            // Test Fenwick
            long startFenwick = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementFenwick(digits, n);
            }
            long timeFenwick = System.currentTimeMillis() - startFenwick;

            String winner = (timeArray < timeFenwick) ? "Array" : "Fenwick";
            System.out.printf("%-10d %-15d %-15d %-10s%n", n, timeArray, timeFenwick, winner);
        }
    }


    @Test
    @DisplayName("Final Performance Benchmark: Hybrid logic across scenarios")
    void testToDerangementHybridScenarios() {
        int iterations = 100;

        // Scenarios designed to trigger different branches of your hybrid logic
        List<Scenario> scenarios = List.of(
                new Scenario("Small N / Low Rank", 50, BigInteger.valueOf(100)),
                new Scenario("Small N / High Rank", 80, BigInteger.TEN.pow(20)),
                new Scenario("Large N / Low Rank", 10000, BigInteger.valueOf(1000)),
                new Scenario("Large N / Medium Rank", 10000, BigInteger.TEN.pow(50)),
                new Scenario("Large N / High Rank (CROSSOVER)", 10000, BigInteger.TEN.pow(150)),
                new Scenario("Massive N / Low Rank", 50000, BigInteger.valueOf(500)),
                new Scenario("Massive N / High Rank", 50000, BigInteger.TEN.pow(500))
        );

        System.out.println("\n=== Final Performance Benchmark (Hybrid toDerangement) ===");
        System.out.printf("%-35s %-10s %-12s %-15s %-10s%n", "Scenario", "N", "Rank Digits", "Avg Time (ms)", "Logic");
        System.out.println("-".repeat(85));

        for (Scenario s : scenarios) {
            int[] digits = DERANGADIC.toDerangadic(s.rank, s.n);

            // Warmup
            for (int i = 0; i < 20; i++) {
                DERANGADIC.toDerangement(digits, s.n);
            }

            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangement(digits, s.n);
            }
            long end = System.nanoTime();

            double avgTimeMs = (double)(end - start) / (iterations * 1_000_000.0);

            // Logic check (matching your thresholds)
            String logic = (s.n < 100 || digits.length < 100) ? "Array" : "Fenwick";

            System.out.printf("%-35s %-10d %-12d %-15.4f %-10s%n",
                    s.name, s.n, digits.length, avgTimeMs, logic);
        }
    }

    /** Helper class for Scenario definitions */
    private static class Scenario {
        String name;
        int n;
        BigInteger rank;

        Scenario(String name, int n, BigInteger rank) {
            this.name = name;
            this.n = n;
            this.rank = rank;
        }
    }



    /**
     * Helper class for named test cases
     */
    private static class NamedRank {
        String name;
        BigInteger value;
        NamedRank(String name, BigInteger value) {
            this.name = name;
            this.value = value;
        }
    }
}
