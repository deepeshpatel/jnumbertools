/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.InvolutadicIncrementStateMachine;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Counts the number of increments with carry length exactly L,
 * across all T(n)-1 increments of the Involutadic number system of order n.
 *
 * <p><b>Carry-length definition.</b>
 * When the increment machine advances from one involution to the next,
 * it finds a pivot at LSD-first index {@code p} and resets digits at
 * indices {@code 0..p-1}.  The <em>carry length</em> of that increment is
 * {@code p + 1}.  This mirrors exactly the Derangadic convention.
 *
 * <p>Usage:
 * <pre>
 *   var counter = new InvolutadicCarryCounter();
 *   BigInteger[] dist = counter.carryDistribution(8);
 *   // dist[L] = number of increments with carry length L
 * </pre>
 */
public final class InvolutadicCarryCounter {

    private final Calculator calc;

    public InvolutadicCarryCounter() {
        this.calc = new Calculator();
    }

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Returns the carry-length distribution for order n.
     *
     * @param n order (n >= 2)
     * @return array {@code dist} of length {@code n+1} where
     *         {@code dist[L]} is the count of increments with carry length L.
     */
    public BigInteger[] carryDistribution(int n) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");

        BigInteger[] dist = new BigInteger[n + 1];
        Arrays.fill(dist, BigInteger.ZERO);

        var engine = new InvolutadicIncrementStateMachine(n, 0L, calc);

        int carryLen;
        while ((carryLen = engine.incrementAndGetCarryLength()) > 0) {
            if (carryLen <= n) {
                dist[carryLen] = dist[carryLen].add(BigInteger.ONE);
            }
        }

        return dist;
    }

    /**
     * Returns C_n(L): the number of increments with carry length exactly L.
     */
    public BigInteger carryCount(int n, int L) {
        return carryDistribution(n)[L];
    }

    // =========================================================
    // Main: distribution table for n=2..18
    // =========================================================

    public static void main(String[] args) {
        var counter = new InvolutadicCarryCounter();

        System.out.println("=== Involutadic Carry Distribution ===\n");

        for (int n = 2; n <= 18; n++) {
            long t0 = System.currentTimeMillis();
            BigInteger[] dist = counter.carryDistribution(n);
            long elapsed = System.currentTimeMillis() - t0;

            BigInteger Tn = BigInteger.ZERO;
            for (int L = 1; L <= n; L++) Tn = Tn.add(dist[L]);
            BigInteger TnFull = Tn.add(BigInteger.ONE);

            System.out.printf("=== n=%d  T(n)=%s  (computed in %d ms) ===%n", n, TnFull, elapsed);
            for (int L = 1; L <= n; L++) {
                if (dist[L].signum() > 0) {
                    System.out.printf("  L=%2d : %,22d%n", L, dist[L]);
                }
            }

            if (Tn.signum() > 0) {
                BigInteger numerator = BigInteger.ZERO;
                for (int L = 1; L <= n; L++) {
                    numerator = numerator.add(BigInteger.valueOf(L).multiply(dist[L]));
                }
                double mean = numerator.doubleValue() / Tn.doubleValue();
                System.out.printf("  E[L] = %.10f%n", mean);
            }
            System.out.println();
        }
    }
}