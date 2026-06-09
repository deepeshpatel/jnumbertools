/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.InvolutadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.InvolutadicIncrementStateMachine;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Correctness verifier for {@link InvolutadicIncrementStateMachine}.
 *
 * <p>For each order n in a configurable range, exhaustively checks that:
 * <ol>
 *   <li>The machine produces exactly T(n) distinct involutions.</li>
 *   <li>Each involution matches {@link InvolutadicAlgorithms#unrank} at the corresponding rank.</li>
 *   <li>Each produced array is a valid involution (π(π(i)) = i for all i).</li>
 *   <li>Consecutive involutions are in strict lexicographic order.</li>
 *   <li>The digit array length equals the number of decisions in the current involution.</li>
 *   <li>The carry-length distribution sums to T(n) - 1.</li>
 * </ol>
 */
public final class InvolutadicIncrementVerifier {

    private final InvolutadicAlgorithms alg;
    private final Calculator calc;

    public InvolutadicIncrementVerifier() {
        this.calc = new Calculator();
        this.alg  = new InvolutadicAlgorithms(calc);
    }

    // =========================================================
    // Public API
    // =========================================================

    public boolean verify(int n) {
        System.out.printf("Verifying n=%d (T(%d)=%s)...%n", n, n, calc.telephoneNumber(n));
        long t0 = System.currentTimeMillis();
        boolean passed = true;

        var engine = new InvolutadicIncrementStateMachine(n, 0L, calc);
        int[] prev = null;
        long rank = 0;
        long[] carryDist = new long[n + 1];

        do {
            int[] inv = engine.involution();

            // Check 1: valid involution
            if (!isValidInvolution(inv, n)) {
                System.out.printf("  FAIL rank=%d: not a valid involution: %s%n",
                        rank, Arrays.toString(inv));
                passed = false;
            }

            // Check 2: matches brute-force unrank
            int[] expected = alg.unrank(rank, n);
            if (!Arrays.equals(expected, inv)) {
                System.out.printf("  FAIL rank=%d: machine gives %s, unrank gives %s%n",
                        rank, Arrays.toString(inv), Arrays.toString(expected));
                passed = false;
            }

            // Check 3: lexicographic order
            if (prev != null && !lexLessThan(prev, inv)) {
                System.out.printf("  FAIL rank=%d: lex order violated: %s >= %s%n",
                        rank, Arrays.toString(prev), Arrays.toString(inv));
                passed = false;
            }

            // Check 4: digit array length consistency
            int[] digits = engine.getDigits();
            int expectedDecisions = countDecisions(inv, n);
            if (digits.length != expectedDecisions) {
                System.out.printf("  FAIL rank=%d: digit array length %d != expected %d for %s%n",
                        rank, digits.length, expectedDecisions, Arrays.toString(inv));
                passed = false;
            }

            prev = Arrays.copyOf(inv, n);
            rank++;

        } while (recordCarryAndIncrement(engine, carryDist, n));

        // Check 5: total count
        long expectedTotal = calc.telephoneNumber(n).longValueExact();
        if (rank != expectedTotal) {
            System.out.printf("  FAIL: produced %d involutions, expected T(%d)=%d%n",
                    rank, n, expectedTotal);
            passed = false;
        }

        // Check 6: carry distribution sums to T(n)-1
        long carrySum = 0;
        for (long c : carryDist) carrySum += c;
        if (carrySum != expectedTotal - 1) {
            System.out.printf("  FAIL: carry distribution sum %d != T(%d)-1=%d%n",
                    carrySum, n, expectedTotal - 1);
            passed = false;
        }

        long elapsed = System.currentTimeMillis() - t0;
        System.out.printf("  %s in %d ms (%d involutions)%n",
                passed ? "PASS" : "FAIL", elapsed, rank);

        if (passed) {
            System.out.printf("  Carry distribution:%n");
            for (int L = 1; L <= n; L++) {
                if (carryDist[L] > 0) System.out.printf("    L=%2d: %,d%n", L, carryDist[L]);
            }
        }
        System.out.println();
        return passed;
    }

    // =========================================================
    // Helpers
    // =========================================================

    /**
     * Calls incrementAndGetCarryLength(), records the result in carryDist,
     * and returns true if there was a next involution.
     */
    private static boolean recordCarryAndIncrement(
            InvolutadicIncrementStateMachine engine, long[] carryDist, int n) {
        int carryLen = engine.incrementAndGetCarryLength();
        if (carryLen <= 0) return false;
        if (carryLen <= n) carryDist[carryLen]++;
        return true;
    }

    private static boolean isValidInvolution(int[] pi, int n) {
        if (pi.length != n) return false;
        for (int i = 0; i < n; i++) {
            if (pi[i] < 0 || pi[i] >= n) return false;
            if (pi[pi[i]] != i) return false;
        }
        return true;
    }

    private static boolean lexLessThan(int[] a, int[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            if (a[i] < b[i]) return true;
            if (a[i] > b[i]) return false;
        }
        return a.length < b.length;
    }

    private static int countDecisions(int[] pi, int n) {
        boolean[] placed = new boolean[n];
        int count = 0;
        for (int pos = 0; pos < n; pos++) {
            if (placed[pos]) continue;
            count++;
            placed[pos] = true;
            if (pi[pos] != pos) placed[pi[pos]] = true;
        }
        return count;
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        var verifier = new InvolutadicIncrementVerifier();
        System.out.println("=== InvolutadicIncrementStateMachine Verifier ===\n");

        boolean allPassed = true;
        for (int n = 2; n <= 12; n++) {
            boolean passed = verifier.verify(n);
            allPassed &= passed;
            if (!passed) {
                System.out.println("STOPPING: failure detected at n=" + n);
                break;
            }
        }
        System.out.println(allPassed ? "All verifications PASSED." : "Some verifications FAILED.");
    }
}