/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Investigates whether the carry-length ratios
 * {@code R_n(L) = C_n(L+1) / C_n(L)} converge as n → ∞ for each fixed L,
 * analogous to Conjecture 6.3 in the Derangadic paper.
 */
public final class InvolutadicRatioLawChecker {

    private static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);

    private final InvolutadicCarryCounter counter;
    private final InvolutadicCarryCounterFormula formula = new InvolutadicCarryCounterFormula();
    private final InvolutadicCarryCounterDP dpCounter = new InvolutadicCarryCounterDP();

    public InvolutadicRatioLawChecker() {
        this.counter = new InvolutadicCarryCounter();
    }

    // =========================================================
    // Public API
    // =========================================================

    public Map<Integer, Double> ratios(int n, BigInteger[] dist) {
        Map<Integer, Double> result = new HashMap<>();
        for (int L = 1; L < n; L++) {
            if (dist[L].signum() > 0 && dist[L + 1].signum() > 0) {
                result.put(L, new BigDecimal(dist[L + 1])
                        .divide(new BigDecimal(dist[L]), MC).doubleValue());
            }
        }
        return result;
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        var checker = new InvolutadicRatioLawChecker();
        int maxN = 18;

        BigInteger[][] allDist = new BigInteger[maxN + 1][];
        System.out.println("Computing distributions...");
        for (int n = 2; n <= maxN; n++) {
            long t0 = System.currentTimeMillis();
            //System.out.println("Should match:");
//            System.out.println("\nCurrent=" + Arrays.toString(checker.counter.carryDistribution(n)));
//            System.out.println("Old    =" + Arrays.toString(checker.formula.carryDistribution(n)));
            //System.out.println(Arrays.toString(checker.dpCounter.carryDistribution(n)));


            allDist[n] = checker.counter.carryDistribution(n);
            System.out.printf("  n=%d done (%d ms)%n", n, System.currentTimeMillis() - t0);
        }
        System.out.println();

        // ── Full ratio table ──────────────────────────────────────────────────
        System.out.println("=== Ratio Table R_n(L) = C_n(L+1) / C_n(L) ===\n");
        System.out.printf("%-4s", "n\\L");
        for (int L = 1; L <= maxN - 1; L++) System.out.printf("  L=%-6d", L);
        System.out.println();
        System.out.println("-".repeat(4 + (maxN - 1) * 10));

        for (int n = 2; n <= maxN; n++) {
            System.out.printf("%-4d", n);
            BigInteger[] dist = allDist[n];
            for (int L = 1; L < n; L++) {
                if (dist[L].signum() > 0 && dist[L + 1].signum() > 0) {
                    double ratio = new BigDecimal(dist[L + 1])
                            .divide(new BigDecimal(dist[L]), MC).doubleValue();
                    System.out.printf("  %-8.5f", ratio);
                } else {
                    System.out.printf("  %-8s", dist[L].signum() > 0 ? "0" : "--");
                }
            }
            System.out.println();
        }

        // ── Derangadic limit comparison ───────────────────────────────────────
        System.out.println("\n=== Derangadic limit 1/(L+1) for comparison ===");
        System.out.printf("%-8s", "L");
        for (int L = 1; L <= Math.min(maxN - 1, 10); L++) System.out.printf("  %-8d", L);
        System.out.println();
        System.out.printf("%-8s", "1/(L+1)");
        for (int L = 1; L <= Math.min(maxN - 1, 10); L++) System.out.printf("  %-8.5f", 1.0 / (L + 1));
        System.out.println();

        // ── Column-wise convergence ───────────────────────────────────────────
        System.out.println("\n=== Column-wise convergence R_n(L) for fixed L ===\n");
        for (int L = 1; L <= Math.min(8, maxN - 1); L++) {
            System.out.printf("L=%d:%n", L);
            System.out.printf("  %-5s  %-12s  %-12s  %-12s%n", "n", "R_n(L)", "delta", "vs 1/(L+1)");
            double prev = Double.NaN;
            for (int n = L + 1; n <= maxN; n++) {
                BigInteger[] dist = allDist[n];
                if (dist[L].signum() > 0 && L + 1 <= n && dist[L + 1].signum() > 0) {
                    double ratio = new BigDecimal(dist[L + 1])
                            .divide(new BigDecimal(dist[L]), MC).doubleValue();
                    String deltaStr = Double.isNaN(prev) ? "--" : String.format("%+.6e", ratio - prev);
                    System.out.printf("  n=%-3d  %-12.8f  %-12s  %+.6e%n",
                            n, ratio, deltaStr, ratio - 1.0 / (L + 1));
                    prev = ratio;
                }
            }
            System.out.println();
        }

        // ── Row sums ─────────────────────────────────────────────────────────
        System.out.println("=== Row sums Σ_L R_n(L) ===");
        for (int n = 4; n <= maxN; n++) {
            double rowSum = 0; int terms = 0;
            BigInteger[] dist = allDist[n];
            for (int L = 1; L < n; L++) {
                if (dist[L].signum() > 0 && dist[L + 1].signum() > 0) {
                    rowSum += new BigDecimal(dist[L + 1]).divide(new BigDecimal(dist[L]), MC).doubleValue();
                    terms++;
                }
            }
            System.out.printf("  n=%2d: Σ R_n(L) = %.8f  (%d terms)%n", n, rowSum, terms);
        }
    }
}