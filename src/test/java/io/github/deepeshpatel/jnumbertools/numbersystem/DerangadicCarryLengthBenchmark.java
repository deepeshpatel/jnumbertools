package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import java.math.BigInteger;
import java.util.Arrays;

public class DerangadicCarryLengthBenchmark {

    // Configure benchmark parameters here
    private static final int[] N_VALUES = {20, 30, 40, 50, 100,200,300,400,1000,2000,5000,10000, 20000}; // Test multiple universe sizes
    private static final long ITERATIONS = 100_000; // Per n value
    private static final int MAX_CARRY = 15; // Track carries up to this length

    public static void main(String[] args) {
        Calculator calc = new Calculator();

        System.out.println("=== Derangadic Carry Length Benchmark ===");
        System.out.printf("Iterations per n: %,d%n", ITERATIONS);
        System.out.printf("Test n values: %s%n%n", Arrays.toString(N_VALUES));

        for (int n : N_VALUES) {
            runBenchmark(calc, n);
        }

        System.out.println("\n=== LaTeX Coordinates for Figure 2 ===");
        System.out.println("// Replace placeholder in Figure 2 with actual data below:");
        for (int n : N_VALUES) {
            System.out.printf("// n=%d: %s%n", n, getCoordinatesForN(calc, n));
        }
        System.out.println("==========================================");
    }

    private static void runBenchmark(Calculator calc, int n) {
        DerangadicIncrement inc = new DerangadicIncrement(calc);
        DerangadicIncrement.DerangadicState state = inc.initialState(n, BigInteger.ZERO);

        long[] carryCounts = new long[MAX_CARRY];
        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++) {
            int carryLen = inc.incrementAndGetCarryLength(state);
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
        System.out.printf("n=%3d | Mean: %.4f | Time: %4d ms | Distribution:%n", n, mean, duration);
        for (int i = 0; i < MAX_CARRY; i++) {
            if (carryCounts[i] > 0) {
                double pct = 100.0 * carryCounts[i] / ITERATIONS;
                System.out.printf("  Carry %2d: %,6d (%.2f%%)%n", i+1, carryCounts[i], pct);
            }
        }
        System.out.println();
    }

    private static String getCoordinatesForN(Calculator calc, int n) {
        DerangadicIncrement inc = new DerangadicIncrement(calc);
        DerangadicIncrement.DerangadicState state = inc.initialState(n, BigInteger.ZERO);

        long[] carryCounts = new long[MAX_CARRY];
        for (long i = 0; i < ITERATIONS; i++) {
            int carryLen = inc.incrementAndGetCarryLength(state);
            if (carryLen == 0) break;
            if (carryLen <= MAX_CARRY) {
                carryCounts[carryLen - 1]++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_CARRY; i++) {
            if (carryCounts[i] > 0) {
                sb.append(String.format("(%d, %d) ", i + 1, carryCounts[i]));
            }
        }
        return sb.toString().trim();
    }
}