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
 * Computes exact grand-mean carry lengths E[L_n] for the Involutadic number
 * system and analyses convergence to the limiting constant (if one exists).
 */
public final class InvolutadicMeanConvergenceAnalysis {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    private final InvolutadicCarryCounter counter;
    private final InvolutadicAlgorithms alg;
    private final Calculator calc = new Calculator();

    public InvolutadicMeanConvergenceAnalysis() {

        this.counter = new InvolutadicCarryCounter();
        this.alg     = new InvolutadicAlgorithms(calc);
    }

    // =========================================================
    // Public API
    // =========================================================

    public BigDecimal exactMean(int n, BigInteger[] dist) {
        BigInteger numerator   = BigInteger.ZERO;
        BigInteger increments  = BigInteger.ZERO;
        for (int L = 1; L <= n; L++) {
            numerator  = numerator.add(BigInteger.valueOf(L).multiply(dist[L]));
            increments = increments.add(dist[L]);
        }
        if (increments.signum() == 0) return BigDecimal.ZERO;
        return new BigDecimal(numerator).divide(new BigDecimal(increments), MC);
    }

    public BigDecimal exactMean(int n) {
        return exactMean(n, counter.carryDistribution(n));
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        var analysis = new InvolutadicMeanConvergenceAnalysis();
        int maxN = 20;

        System.out.println("=== Involutadic: Exact Mean Carry Length E[L_n] ===\n");
        System.out.printf("%-4s  %-28s  %-22s  %s%n",
                "n", "T(n)", "E[L_n]", "ratio E[L_n]/E[L_{n-1}]");
        System.out.println("-".repeat(90));

        BigDecimal prevMean = null;
        double[] means = new double[maxN + 1];

        for (int n = 2; n <= maxN; n++) {
            long t0 = System.currentTimeMillis();
            BigInteger[] dist = analysis.counter.carryDistribution(n);
            long elapsed = System.currentTimeMillis() - t0;

            BigDecimal mean = analysis.exactMean(n, dist);
            means[n] = mean.doubleValue();

            BigInteger Tn = analysis.calc.telephoneNumber(n);
            String ratioStr = "--";
            if (prevMean != null && prevMean.compareTo(BigDecimal.ZERO) != 0) {
                String rs = mean.divide(prevMean, MC).toPlainString();
                ratioStr = rs.substring(0, Math.min(10, rs.length()));
            }
            System.out.printf("n=%-2d  %-28s  %-22.10f  %s  (%d ms)%n",
                    n, Tn, means[n], ratioStr, elapsed);
            prevMean = mean;
        }

        // ── Monotonicity ─────────────────────────────────────────────────────
        System.out.println();
        boolean monoInc = true, monoDec = true;
        for (int n = 3; n <= maxN; n++) {
            if (means[n] < means[n-1]) monoInc = false;
            if (means[n] > means[n-1]) monoDec = false;
        }
        System.out.printf("Monotone increasing: %b%n", monoInc);
        System.out.printf("Monotone decreasing: %b%n", monoDec);

        // ── Differences ──────────────────────────────────────────────────────
        System.out.printf("%n%-4s  %-22s  %-22s%n", "n", "E[L_n]", "E[L_n] - E[L_{n-1}]");
        System.out.println("-".repeat(52));
        for (int n = 2; n <= maxN; n++) {
            String diffStr = (n > 2) ? String.format("%+.10f", means[n] - means[n-1]) : "--";
            System.out.printf("%-4d  %-22.10f  %s%n", n, means[n], diffStr);
        }

        // ── Log-log regression ────────────────────────────────────────────────
        double proxyLimit = means[maxN];
        System.out.printf("%nProxy limit (E[L_%d]) = %.10f%n", maxN, proxyLimit);
        System.out.printf("%n%-4s  %-22s  %-22s%n", "n", "eps_n = |E[L_n]-limit|", "log(eps)/log(n)");
        System.out.println("-".repeat(55));
        for (int n = 3; n <= maxN - 1; n++) {
            double eps = Math.abs(means[n] - proxyLimit);
            if (eps > 1e-15)
                System.out.printf("%-4d  %-22.6e  %-22.6f%n", n, eps, Math.log(eps) / Math.log(n));
        }

        int fitStart = 4, fitEnd = maxN - 2;
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        int pts = 0;
        for (int n = fitStart; n <= fitEnd; n++) {
            double eps = Math.abs(means[n] - proxyLimit);
            if (eps > 1e-15) {
                double x = Math.log(n), y = Math.log(eps);
                sumX += x; sumY += y; sumXX += x*x; sumXY += x*y; pts++;
            }
        }
        if (pts >= 2) {
            double beta = -(pts * sumXY - sumX * sumY) / (pts * sumXX - sumX * sumX);
            double C = Math.exp((sumY - (-beta) * sumX) / pts);
            System.out.printf("%nLog-log regression (n=%d..%d): beta = %.6f, C = %.6f%n",
                    fitStart, fitEnd, beta, C);
        }
    }
}