package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.experiments;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicIncrementStateMachine;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * R&D / Data Generation Harness (NOT a standard unit test).
 * This class is used for large-scale empirical analysis, statistics collection,
 * and pattern discovery (carry-length distribution, tail polynomials, convergence, etc.).
 * It is kept under src/test for convenience of execution, but it is not a
 * traditional JUnit test.
 */
@Disabled("Not a test but a data generation harness")
public class CarryLengthMeanOfAllAnalyser {


    private static final int[] N_VALUES = {200, 400, 600, 800, 1000, 1500, 2000, 3000, 5000, 8000, 10000};
    private static final long ITERATIONS = 10_000_000L;
    private static final int NUM_RANDOM_STARTS = 100;
    private static final int MAX_CARRY = 15;



//    private static final int[] N_VALUES = {200, 1000, 10000};
//    private static final int[] N_VALUES = {201, 501, 699};
//    private static final long ITERATIONS = 5_000_000L;
//    private static final int NUM_RANDOM_STARTS = 100;
    private static final long SEED = 42L;
//    private static final int MAX_CARRY = 15;

    @Test
    @EnabledIfSystemProperty(named = "performance.testing", matches = "true")
    public void carryLengthBenchmarkMeanOfAllTest() {

        Calculator calc = new Calculator();

        System.out.println("=== Derangadic Carry Length Benchmark (Mean Of All) ===");
        System.out.printf("Iterations per run : %,d%n", ITERATIONS);
        System.out.printf("Random starts      : %,d%n", NUM_RANDOM_STARTS);
        System.out.printf("N values           : %s%n", Arrays.toString(N_VALUES));
        Random rng = new Random(SEED);

        for (int n : N_VALUES) {
            System.out.printf("%n=== n=%d ===%n", n);
            BigInteger maxRank = calc.subFactorial(n);
            int bitLen = maxRank.bitLength();
            Set<BigInteger> usedRanks = new HashSet<>();
            long[] totalCarryCounts = new long[MAX_CARRY + 1];
            long totalIterations = 0;
            long totalTime = 0;
            for (int k = 0; k < NUM_RANDOM_STARTS; k++) {
                BigInteger rank;
                do {
                    do {
                        rank = new BigInteger(bitLen, rng);
                    } while (rank.compareTo(maxRank) >= 0);
                }
                while (!usedRanks.add(rank));

                DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, rank, calc);
                long startTime = System.currentTimeMillis();

                for (long i = 0; i < ITERATIONS; i++) {
                    int carryLen = machine.incrementAndGetCarryLength();
                    if (carryLen == 0) { break; }

                    totalIterations++;

                    if (carryLen <= MAX_CARRY) {
                        totalCarryCounts[carryLen]++;
                    }
                }

                totalTime += (System.currentTimeMillis() - startTime);
            }

            double weightedSum = 0;
            double total = 0;

            for (int i = 1; i <= MAX_CARRY; i++) {
                weightedSum += (double) i * totalCarryCounts[i];
                total += totalCarryCounts[i];
            }

            double mean = weightedSum / total;
            double alpha = (double) totalCarryCounts[2] / total;
            double predictedMean = 2 * (Math.E - 1) - 2 * (Math.E - 2) * alpha;

            System.out.printf("actualN         : %d%n", n);
            System.out.printf("total iterations: %,d%n", totalIterations);
            System.out.printf("total time      : %,d ms%n", totalTime);
            System.out.printf("mean carry      : %.8f%n", mean);
            System.out.printf("alpha=P(L=2)    : %.8f%n", alpha);
            System.out.printf("predicted mean  : %.8f%n", predictedMean);
            System.out.println("\nCarry distribution:");

            for (int i = 1; i <= MAX_CARRY; i++) {

                long count = totalCarryCounts[i];
                if (count == 0) { continue; }
                System.out.printf("  L=%2d : %,14d  (%10.6f%%)%n", i, count, 100.0 * count / total);
            }

            System.out.println("\nRatios P(L=k)/P(L=k+1):");

            for (int k = 2; k < MAX_CARRY; k++) {
                long a = totalCarryCounts[k];
                long b = totalCarryCounts[k + 1];
                if (a == 0 || b == 0) {continue;}
                double empirical = (double) a / b;
                double predicted = (double) (k * k - 1) / k;
                System.out.printf("  k=%2d | empirical=%8.4f | predicted=%8.4f%n", k, empirical, predicted);
            }
        }

        System.out.println("\n=== ALL BENCHMARKS COMPLETED ===");
    }
}