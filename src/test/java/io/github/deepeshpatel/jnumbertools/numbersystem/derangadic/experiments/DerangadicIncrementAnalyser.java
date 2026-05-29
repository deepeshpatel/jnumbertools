package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.experiments;


import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicIncrementStateMachine;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R&D / Data Generation Harness (NOT a standard unit test).
 * This class is used for large-scale empirical analysis, statistics collection,
 * and pattern discovery (carry-length distribution, tail polynomials, convergence, etc.).
 * It is kept under src/test for convenience of execution, but it is not a
 * traditional JUnit test.
 */
@Disabled("Not a test but a data feneration harness")
public class DerangadicIncrementAnalyser {

    private final Calculator calculator = new Calculator();

    @Test
    void unrankPlacesMachineAtCorrectDerangement() {
        int n = 12;  // !12 = 176,214,841 — small enough to brute-force
        BigInteger maxRank = calculator.subFactorial(n);

        // Test ranks: 0, !12/10, !12/4, !12/2, 3!12/4, !12 - 1
        BigInteger[] testRanks = {
                BigInteger.ZERO,
                maxRank.divide(BigInteger.TEN),
                maxRank.divide(BigInteger.valueOf(4)),
                maxRank.divide(BigInteger.valueOf(2)),
                maxRank.multiply(BigInteger.valueOf(3)).divide(BigInteger.valueOf(4)),
                maxRank.subtract(BigInteger.ONE)
        };

        for (BigInteger targetRank : testRanks) {
            DerangadicIncrementStateMachine machine =
                    new DerangadicIncrementStateMachine(n, targetRank, calculator);
            int[] machineDerangement = machine.derangement().clone();

            // Brute-force: walk to targetRank-th derangement from identity
            int[] perm = new int[n];
            for (int i = 0; i < n; i++) perm[i] = i;
            BigInteger rank = BigInteger.ZERO.subtract(BigInteger.ONE);  // pre-increment
            while (rank.compareTo(targetRank) < 0) {
                if (!isDerangement(perm)) {
                    if (!nextPermutation(perm)) fail("ran out of permutations");
                    continue;
                }
                rank = rank.add(BigInteger.ONE);
                if (rank.equals(targetRank)) break;
                if (!nextPermutation(perm)) fail("ran out of permutations");
            }

            assertArrayEquals(perm, machineDerangement,
                    "Machine unrank at rank=" + targetRank + " (n=" + n +
                            ") differs from brute force");
        }
    }

    @Test
    void incrementSequenceMatchesBruteForce() {
        int n = 10;  // !10 = 1,334,961 — iterate them all
        DerangadicIncrementStateMachine machine =
                new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calculator);

        int[] bruteForce = machine.derangement().clone();
        BigInteger maxRank = calculator.subFactorial(n);

        for (BigInteger r = BigInteger.ZERO;
             r.compareTo(maxRank.subtract(BigInteger.ONE)) < 0;
             r = r.add(BigInteger.ONE)) {

            bruteForce = findNextDerangementByBruteForce(bruteForce);
            machine.increment();
            int[] machineDerangement = machine.derangement();

            assertArrayEquals(bruteForce, machineDerangement,
                    "Mismatch at rank=" + r.add(BigInteger.ONE));
        }
    }

    @Test
    void n699UnrankIsConsistent() {
        int n = 699;
        BigInteger maxRank = calculator.subFactorial(n);

        for (int i = 0; i < 10; i++) {
            BigInteger rank = maxRank.multiply(BigInteger.valueOf(i)).divide(BigInteger.TEN);
            DerangadicIncrementStateMachine machine =
                    new DerangadicIncrementStateMachine(n, rank, calculator);

            int[] derangement = machine.derangement().clone();
            int actualN = machine.actualN();
            int[] digits = machine.encoded().clone();

            // Sanity: must be a valid derangement of n elements
            assertEquals(n, derangement.length);
            boolean[] used = new boolean[n];
            for (int j = 0; j < n; j++) {
                assertNotEquals(j, derangement[j], "fixed point at " + j + " for i=" + i);
                assertFalse(used[derangement[j]], "duplicate " + derangement[j] + " for i=" + i);
                used[derangement[j]] = true;
            }

            // actualN expectations:
            //   i=0 → rank=0 → minimal encoding → actualN=3 (smallest odd)
            //   i>=1 → rank > !(n-2) → must use full window → actualN=n=699
            int expectedActualN = (i == 0) ? 3 : n;
            assertEquals(expectedActualN, actualN,
                    "actualN mismatch at i=" + i + ", rank=" + rank);

            // Diagnostic print: structure of first 16 digits & derangement entries
            System.out.printf("i=%d  actualN=%d%n", i, actualN);
            System.out.printf("   digits[0..15]      = %s%n",
                    Arrays.toString(Arrays.copyOf(digits, Math.min(16, digits.length))));
            System.out.printf("   derangement[0..15] = %s%n",
                    Arrays.toString(Arrays.copyOf(derangement, 16)));
        }
    }

    @Test
    @Disabled("Takes a very long time to run, but can be enabled for sanity checking")
    void assertNextDerangementSameForMachineAndBruteForce() {
        int n = 699;
        long iterations = 5_000_000_000L;
        List<BigInteger> ranks = new ArrayList<>();
        BigInteger maxRank = calculator.subFactorial(n);

        for (int i = 0; i < 10; i++) {
            BigInteger rank = maxRank.multiply(BigInteger.valueOf(i)).divide(BigInteger.TEN);
            ranks.add(rank);
        }

        for (BigInteger rank : ranks) {
            // Initialize machine at given start rank
            DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, rank, calculator);

            // Get current derangement from machine
            int[] current = machine.derangement().clone();

            for (long i = 0; i < iterations; i++) {
                // Get next via machine
                machine.increment();
                int[] nextViaMachine = machine.derangement().clone();

                // Get next via brute-force
                int[] nextViaBrute = findNextDerangementByBruteForce(current);

                // Assert equality
                assertNotNull(nextViaBrute, "Brute-force found no next derangement but machine claims there is one");
                assertArrayEquals(nextViaBrute, nextViaMachine,
                        String.format("Mismatch at n=%d, startRank=%s, iteration=%d\n" +
                                        "  Current: %s\n" +
                                        "  Expected next: %s\n" +
                                        "  Machine next:  %s",
                                n, rank, i + 1,
                                Arrays.toString(current),
                                Arrays.toString(nextViaBrute),
                                Arrays.toString(nextViaMachine)));

                // Update current for next iteration
                current = nextViaBrute;
            }
        }
    }

    // ==================== Brute-Force Helper Methods ====================

    private boolean isDerangement(int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            if (perm[i] == i) return false;
        }
        return true;
    }

    private boolean nextPermutation(int[] arr) {
        int i = arr.length - 2;
        while (i >= 0 && arr[i] >= arr[i + 1]) i--;
        if (i < 0) return false;

        int j = arr.length - 1;
        while (arr[j] <= arr[i]) j--;

        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;

        reverse(arr, i + 1, arr.length - 1);
        return true;
    }

    private void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }

    private int[] findNextDerangementByBruteForce(int[] current) {
        int[] next = current.clone();
        while (nextPermutation(next)) {
            if (isDerangement(next)) {
                return next;
            }
        }
        return null;
    }
}