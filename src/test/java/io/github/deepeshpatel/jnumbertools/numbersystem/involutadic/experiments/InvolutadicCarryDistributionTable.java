/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.InvolutadicAlgorithms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Produces a publication-ready carry-distribution table for the Involutadic
 * number system, analogous to the tables in the Derangadic paper.
 *
 * <p>For each n, prints {@code C_n(L)} for all L, T(n), E[L_n], and
 * the probability distribution {@code C_n(L)/T(n)}.
 */
public final class InvolutadicCarryDistributionTable {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private final InvolutadicCarryCounter counter;
    private final InvolutadicAlgorithms alg;

    public InvolutadicCarryDistributionTable() {
        Calculator calc = new Calculator();
        this.counter = new InvolutadicCarryCounter();
        this.alg     = new InvolutadicAlgorithms(calc);
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        var table = new InvolutadicCarryDistributionTable();
        int maxN = 18;

        System.out.println("=== Involutadic Carry-Length Distribution C_n(L) ===\n");

        for (int n = 2; n <= maxN; n++) {
            long t0 = System.currentTimeMillis();
            BigInteger[] dist = table.counter.carryDistribution(n);
            long elapsed = System.currentTimeMillis() - t0;

            BigInteger increments = BigInteger.ZERO;
            for (int L = 1; L <= n; L++) increments = increments.add(dist[L]);
            BigInteger TnFull = increments.add(BigInteger.ONE);

            BigInteger numerator = BigInteger.ZERO;
            for (int L = 1; L <= n; L++)
                numerator = numerator.add(BigInteger.valueOf(L).multiply(dist[L]));
            double mean = increments.signum() > 0
                    ? numerator.doubleValue() / increments.doubleValue() : 0.0;

            System.out.printf("=== n=%d  T(n)=%s  (computed in %d ms) ===%n", n, TnFull, elapsed);
            System.out.printf("  %-5s  %22s  %12s%n", "L", "C_n(L)", "C_n(L)/T(n)");
            System.out.println("  " + "-".repeat(43));

            for (int L = 1; L <= n; L++) {
                if (dist[L].signum() > 0) {
                    double prob = new BigDecimal(dist[L])
                            .divide(new BigDecimal(TnFull), MC).doubleValue();
                    System.out.printf("  L=%-3d  %,22d  %12.8f%n", L, dist[L], prob);
                }
            }
            System.out.printf("  E[L_n] = %.10f%n%n", mean);
        }

        // ── Probability comparison table ──────────────────────────────────────
        System.out.println("=== Probability table p_n(L) = C_n(L) / T(n) ===\n");

        BigInteger[][] allDist = new BigInteger[maxN + 1][];
        for (int n = 2; n <= maxN; n++)
            allDist[n] = table.counter.carryDistribution(n);

        int maxL = 10;
        System.out.printf("%-4s", "n\\L");
        for (int L = 1; L <= maxL; L++) System.out.printf("  p(L=%-2d) ", L);
        System.out.printf("  %-12s%n", "E[L_n]");
        System.out.println("-".repeat(4 + maxL * 11 + 14));

        for (int n = 2; n <= maxN; n++) {
            BigInteger[] dist = allDist[n];
            BigInteger increments = BigInteger.ZERO;
            for (int L = 1; L <= n; L++) increments = increments.add(dist[L]);
            BigInteger TnFull = increments.add(BigInteger.ONE);

            System.out.printf("%-4d", n);
            for (int L = 1; L <= maxL; L++) {
                if (L <= n && dist[L].signum() > 0) {
                    double prob = new BigDecimal(dist[L])
                            .divide(new BigDecimal(TnFull), MC).doubleValue();
                    System.out.printf("  %-9.6f", prob);
                } else {
                    System.out.printf("  %-9s", "--");
                }
            }
            BigInteger num = BigInteger.ZERO;
            for (int L = 1; L <= n; L++) num = num.add(BigInteger.valueOf(L).multiply(dist[L]));
            double mean = increments.signum() > 0 ? num.doubleValue() / increments.doubleValue() : 0;
            System.out.printf("  %-12.8f%n", mean);
        }

        // ── Derangadic limit comparison ───────────────────────────────────────
        System.out.println("\n=== Limiting probabilities p_n(L) for n=" + maxN
                + " vs Derangadic limit e^{-1}/(L-1)! ===\n");
        System.out.printf("%-6s  %-12s  %-12s  %-12s%n", "L", "p_" + maxN + "(L)", "Derangadic", "ratio");
        System.out.println("-".repeat(50));

        BigInteger[] distMax = allDist[maxN];
        BigInteger incMax = BigInteger.ZERO;
        for (int L = 1; L <= maxN; L++) incMax = incMax.add(distMax[L]);
        BigInteger TnMax = incMax.add(BigInteger.ONE);

        long factorial = 1;
        for (int L = 1; L <= Math.min(maxN, 10); L++) {
            if (L > 1) factorial *= (L - 1);
            if (distMax[L].signum() > 0) {
                double pActual = new BigDecimal(distMax[L])
                        .divide(new BigDecimal(TnMax), MC).doubleValue();
                double pDerangadic = Math.exp(-1.0) / factorial;
                System.out.printf("  L=%-3d  %-12.8f  %-12.8f  %-12.6f%n",
                        L, pActual, pDerangadic, pActual / pDerangadic);
            }
        }
    }
}