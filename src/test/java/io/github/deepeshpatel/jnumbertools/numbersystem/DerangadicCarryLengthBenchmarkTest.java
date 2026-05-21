package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Benchmark for analyzing carry length distribution in the Derangadic increment state machine.
 * <p>
 * This class measures the empirical distribution of carry lengths during lexicographic
 * traversal of derangements. The results validate the theoretical O(1) amortized complexity
 * and the $e^2 \approx 7.389$ expected carry length bound.
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @version 3.0.2
 */
public class DerangadicCarryLengthBenchmarkTest {

    // Configure benchmark parameters here
    private static final int[] N_VALUES = {20, 30, 40, 50, 100, 200, 300, 400, 1000, 2000, 5000, 10000, 20000};
    private static final long ITERATIONS = 100_000; // Per n value
    private static final int MAX_CARRY = 15; // Track carries up to this length

    @Test @EnabledIfSystemProperty(named = "performance.testing", matches = "true")
    public  void carryLengthBenchmarkTest() {

        Calculator calc = new Calculator();
        System.out.println("=== Derangadic Carry Length Benchmark ===");
        System.out.printf("Iterations per n: %,d%n", ITERATIONS);
        System.out.printf("Test n values: %s%n%n", Arrays.toString(N_VALUES));

        for (int n : N_VALUES) {
            runBenchmark(n, calc);
        }

        System.out.println("\n=== LaTeX Coordinates for Figure 2 ===");
        System.out.println("// Replace placeholder in Figure 2 with actual data below:");
        for (int n : N_VALUES) {
            System.out.printf("// n=%d: %s%n", n, getCoordinatesForN(calc, n));
        }
        System.out.println("==========================================");
    }

    //Runs the carry length benchmark for a specific n value.
    private static void runBenchmark(int n, Calculator calc) {
        // Create state machine starting from rank 0
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calc);

        long[] carryCounts = new long[MAX_CARRY];
        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++) {
            int carryLen = machine.incrementAndGetCarryLength();
            if (carryLen == 0) break; // End of enumeration
            if (carryLen <= MAX_CARRY) {
                carryCounts[carryLen - 1]++;
            }
        }

        long duration = System.currentTimeMillis() - start;

        // Calculate statistics
        double total = 0, weightedSum = 0;
        for (int i = 0; i < MAX_CARRY; i++) {
            if (carryCounts[i] > 0) {
                total += carryCounts[i];
                weightedSum += (i + 1) * carryCounts[i];
            }
        }
        double mean = total > 0 ? weightedSum / total : 0;

        // Print results
        System.out.printf("n=%4d | Mean: %.4f | Time: %4d ms | Distribution:%n", n, mean, duration);
        for (int i = 0; i < MAX_CARRY; i++) {
            if (carryCounts[i] > 0) {
                double pct = 100.0 * carryCounts[i] / ITERATIONS;
                System.out.printf("  Carry %2d: %,6d (%.2f%%)%n", i + 1, carryCounts[i], pct);
            }
        }
        System.out.println();
    }

    private static void runDetailedBenchmark(int n, long iterations, Calculator calc) {
        System.out.printf("%n=== Detailed Carry Length Benchmark for n=%d ===%n", n);
        System.out.printf("Iterations: %,d%n%n", iterations);

        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calc);

        long[] carryCounts = new long[MAX_CARRY];
        long start = System.nanoTime();

        for (long i = 0; i < iterations; i++) {
            int carryLen = machine.incrementAndGetCarryLength();
            if (carryLen == 0) {
                System.out.printf("Reached end of enumeration after %,d steps%n", i);
                break;
            }
            if (carryLen <= MAX_CARRY) {
                carryCounts[carryLen - 1]++;
            }
        }

        long duration = System.nanoTime() - start;
        double nsPerIter = (double) duration / iterations;

        // Calculate statistics
        double total = 0, weightedSum = 0;
        for (int i = 0; i < MAX_CARRY; i++) {
            total += carryCounts[i];
            weightedSum += (i + 1) * carryCounts[i];
        }
        double mean = total > 0 ? weightedSum / total : 0;
        double variance = 0;
        for (int i = 0; i < MAX_CARRY; i++) {
            double diff = (i + 1) - mean;
            variance += carryCounts[i] * diff * diff;
        }
        variance = total > 0 ? variance / total : 0;
        double stdDev = Math.sqrt(variance);

        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│                    Carry Length Distribution                    │");
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        for (int i = 0; i < MAX_CARRY; i++) {
            if (carryCounts[i] > 0) {
                double pct = 100.0 * carryCounts[i] / iterations;
                System.out.printf("│ Carry %2d: %,10d (%.2f%%)%n", i + 1, carryCounts[i], pct);
            }
        }
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        System.out.printf("│ Mean: %.4f%n", mean);
        System.out.printf("│ Std Dev: %.4f%n", stdDev);
        System.out.printf("│ Time per iteration: %,.1f ns%n", nsPerIter);
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
    }

    /**
     * Compares carry length distribution across different n values.
     * Demonstrates parity-locked stabilisation (distribution independent of n).
     */
    private static void compareAcrossN(Calculator calc) {
        int[] testValues = {20, 50, 100, 200};
        long iterations = 50_000;

        System.out.println("\n=== Cross-n Carry Length Comparison ===");
        System.out.println("Demonstrates parity-locked stabilisation:");
        System.out.println("Distribution should be identical across n values for same parity\n");

        for (int n : testValues) {
            DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calc);

            long[] carryCounts = new long[MAX_CARRY];
            for (long i = 0; i < iterations; i++) {
                int carryLen = machine.incrementAndGetCarryLength();
                if (carryLen == 0) break;
                if (carryLen <= MAX_CARRY) {
                    carryCounts[carryLen - 1]++;
                }
            }

            System.out.printf("n=%d distribution: ", n);
            for (int i = 0; i < Math.min(8, MAX_CARRY); i++) {
                System.out.printf("%d:%-4d ", i + 1, carryCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * Generates LaTeX coordinate pairs for plotting the carry length distribution.
     *
     * @param calc Calculator instance for subfactorial computations
     * @param n    universe size
     * @return String of coordinate pairs in format (x, y)
     */
    private static String getCoordinatesForN(Calculator calc, int n) {
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calc);

        long[] carryCounts = new long[MAX_CARRY];
        for (long i = 0; i < ITERATIONS; i++) {
            int carryLen = machine.incrementAndGetCarryLength();
            if (carryLen == 0) break;
            if (carryLen <= MAX_CARRY) {
                carryCounts[carryLen - 1]++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_CARRY; i++) {
            if (carryCounts[i] > 0) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(String.format("(%d, %d)", i + 1, carryCounts[i]));
            }
        }
        return sb.toString();
    }
}
