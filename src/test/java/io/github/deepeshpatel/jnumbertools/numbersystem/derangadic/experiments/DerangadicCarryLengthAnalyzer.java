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
public class DerangadicCarryLengthAnalyzer {

    //private static final int[]  N_VALUES   = {200, 500, 800, 1000, 2000};
    private static final int[]  N_VALUES   = {201, 501, 699 };//1001};,
    private static final long   ITERATIONS = 1_000_000L;
    private static final int    NUM_RANDOM_STARTS = 20;
    private static final long   SEED = 42L;  // for reproducibility
    private static final int MAX_CARRY = 10;  // max carry length to track in histogram

    @Test
    @EnabledIfSystemProperty(named = "performance.testing", matches = "true")
    public void carryLengthBenchmarkTest() {
        Calculator calc = new Calculator();

        System.out.println("=== Derangadic Carry Length Benchmark ===");
        System.out.printf("Iterations : %,d%n", ITERATIONS);
        System.out.printf("N values   : %s%n%n", Arrays.toString(N_VALUES));


        Random rng = new Random(SEED);
        int total = N_VALUES.length * NUM_RANDOM_STARTS;

        int completed = 0;

        for (int n : N_VALUES) {
            Set<BigInteger> usedRanks = new HashSet<>();
            BigInteger maxRank = calc.subFactorial(n);
            int bitLen = maxRank.bitLength();

            for (int k = 0; k < NUM_RANDOM_STARTS; k++) {
                BigInteger rank;
                do{
                    do {
                        rank = new BigInteger(bitLen, rng);
                    } while (rank.compareTo(maxRank) >= 0);
                }while (!usedRanks.add(rank)); // add() returns false if duplicate

                String label = "rand_" + k;
                runBenchmark(++completed, total, n, label, rank, calc);
            }
        }

        System.out.println("\n=== ALL CONFIGURATIONS COMPLETED ===");
    }

    private static void runBenchmark(int completed, int totalConfigs, int n, String rankLabel, BigInteger startRank, Calculator calc) {

        System.out.printf("%n=== [%d/%d] n=%-6d startRank=%-14s ===%n", completed, totalConfigs, n, rankLabel);
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, startRank, calc);
        System.out.println("actualN = " + machine.actualN());

        long[] carryCounts    = new long[MAX_CARRY + 1];
        long   actualIter     = 0;
        long   startTime      = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++) {
            int carryLen = machine.incrementAndGetCarryLength();
            if (carryLen == 0) break;
            actualIter++;
            if (carryLen <= MAX_CARRY) carryCounts[carryLen]++;
        }

        long duration = System.currentTimeMillis() - startTime;

        // --- statistics ---
        double weightedSum = 0, total = 0;
        for (int i = 1; i <= MAX_CARRY; i++) {
            weightedSum += (double) i * carryCounts[i];
            total       += carryCounts[i];
        }

        double mean          = total > 0 ? weightedSum / total : 0;
        double alpha         = total > 0 ? (double) carryCounts[2] / total : 0;
        double predictedMean = 2 * (Math.E - 1) - 2 * (Math.E - 2) * alpha;

        // --- print ---
        System.out.printf("iterations     : %,d%n",   actualIter);
        System.out.printf("time           : %,d ms%n", duration);
        System.out.printf("mean carry     : %.8f%n",   mean);
        System.out.printf("alpha=P(L=2)   : %.8f%n",   alpha);
        System.out.printf("predicted mean : %.8f%n%n", predictedMean);

        for (int i = 1; i <= MAX_CARRY; i++) {
            if (carryCounts[i] == 0) continue;
            System.out.printf("  L=%2d : %,12d  (%9.6f%%)%n",
                    i, carryCounts[i], 100.0 * carryCounts[i] / total);
        }

        System.out.println("\n  Ratios P(L=k)/P(L=k+1):");
        for (int k = 2; k < Math.min(MAX_CARRY, 11); k++) {
            if (carryCounts[k] == 0 || carryCounts[k + 1] == 0) continue;
            double empirical  = (double) carryCounts[k] / carryCounts[k + 1];
            double predicted  = (double) (k * k - 1) / k;
            System.out.printf("    k=%2d | empirical=%8.4f | predicted=%8.4f%n",
                    k, empirical, predicted);
        }

        System.out.printf("%n=== COMPLETED [%d/%d] ===%n", completed, totalConfigs);
    }
}