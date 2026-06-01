/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.experiments;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Memory-optimized Derangadic Carry Counter using flattened legal table + bottom-up DP.
 */
public final class DerangadicCarryCounterDP {

    private static final Map<Integer, LegalTable> legalCache = new HashMap<>();

    // ===================================================================
    // Public API
    // ===================================================================

    public BigInteger carryCount(int n, int L) {
        if (n < 2 || L < 2 || L > n) return BigInteger.ZERO;

        int startLayer = (n % 2 == 0) ? 2 : 3;
        BigInteger total = BigInteger.ZERO;

        for (int m = startLayer; m <= n; m += 2) {
            if (m < L) continue;
            BigInteger raw = withinLayerCount(m, L);
            BigInteger shadows = (m >= startLayer + 2) ? withinLayerCount(m - 2, L) : BigInteger.ZERO;
            total = total.add(raw).subtract(shadows);
        }

        if (L >= startLayer + 2 && L <= n && (L - n) % 2 == 0)
            total = total.add(BigInteger.ONE);

        int candidateM = L + 1;
        if (candidateM >= startLayer + 2 && candidateM <= n && (candidateM - n) % 2 == 0)
            total = total.subtract(BigInteger.ONE);

        return total;
    }

    public BigInteger[] carryDistribution(int n) {
        BigInteger[] dist = new BigInteger[n + 1];
        Arrays.fill(dist, BigInteger.ZERO);
        for (int L = 2; L <= n; L++) {
            dist[L] = carryCount(n, L);
        }
        return dist;
    }

    // ===================================================================
    // Core Computation
    // ===================================================================

    private BigInteger withinLayerCount(int m, int L) {
        if (L < 2 || L > m) return BigInteger.ZERO;

        LegalTable legal = getLegalTable(m);
        int pivotIdx = L - 1;
        int maxMask = 1 << m;

        BigInteger[] memo = new BigInteger[maxMask];
        memo[maxMask - 1] = BigInteger.ONE;

        for (int mask = maxMask - 2; mask >= 0; mask--) {
            int step = Integer.bitCount(mask);
            int arrayIdx = m - 1 - step;

            int off = legal.offset[mask];
            int len = legal.length[mask];

            if (len == 0) {
                memo[mask] = BigInteger.ZERO;
                continue;
            }

            int maxD = len - 1;
            int dStart = 0, dEnd = maxD;

            if (arrayIdx < pivotIdx) {
                dStart = dEnd = maxD;
            } else if (arrayIdx == pivotIdx) {
                dEnd = maxD - 1;
            }

            BigInteger sum = BigInteger.ZERO;
            for (int d = dStart; d <= dEnd; d++) {
                int chosen = legal.candidates[off + d];
                int nextMask = mask | (1 << chosen);
                sum = sum.add(memo[nextMask]);
            }
            memo[mask] = sum;
        }

        return memo[0];
    }

    private LegalTable getLegalTable(int m) {
        return legalCache.computeIfAbsent(m, LegalTable::new);
    }

    // ===================================================================
    // Flattened Legal Table (Major Memory Saver)
    // ===================================================================

    private static class LegalTable {
        final int[] candidates;
        final int[] offset;
        final int[] length;

        LegalTable(int m) {
            System.out.println("Building legal table for m=" + m + " (2^" + m + " states)...");
            long start = System.currentTimeMillis();

            int states = 1 << m;
            offset = new int[states];
            length = new int[states];

            long totalCandidates = 0;
            for (int mask = 0; mask < states; mask++) {
                length[mask] = countLegal(mask, m);
                totalCandidates += length[mask];
            }

            candidates = new int[(int) totalCandidates];
            int pos = 0;

            for (int mask = 0; mask < states; mask++) {
                offset[mask] = pos;
                pos += fillLegal(mask, m, candidates, pos);
            }

            System.out.printf("Legal table m=%d built in %d ms (%,d candidates)%n",
                    m, System.currentTimeMillis() - start, totalCandidates);
        }

        private int countLegal(int mask, int m) {
            int step = Integer.bitCount(mask);
            int rem = m - step;
            int cnt = 0;
            if (rem == 2) {
                int e1 = -1, e2 = -1;
                for (int e = 0; e < m; e++) {
                    if ((mask >> e & 1) == 0) {
                        if (e1 < 0) e1 = e;
                        else { e2 = e; break; }
                    }
                }
                for (int e : new int[]{e1, e2}) {
                    if (e < 0 || e == step) continue;
                    int other = (e == e1) ? e2 : e1;
                    if (other != step + 1) cnt++;
                }
            } else {
                for (int e = 0; e < m; e++) {
                    if ((mask >> e & 1) == 0 && e != step) cnt++;
                }
            }
            return cnt;
        }

        private int fillLegal(int mask, int m, int[] buf, int start) {
            int step = Integer.bitCount(mask);
            int rem = m - step;
            int pos = start;
            if (rem == 2) {
                int e1 = -1, e2 = -1;
                for (int e = 0; e < m; e++) {
                    if ((mask >> e & 1) == 0) {
                        if (e1 < 0) e1 = e;
                        else { e2 = e; break; }
                    }
                }
                for (int e : new int[]{e1, e2}) {
                    if (e < 0 || e == step) continue;
                    int other = (e == e1) ? e2 : e1;
                    if (other != step + 1) buf[pos++] = e;
                }
            } else {
                for (int e = 0; e < m; e++) {
                    if ((mask >> e & 1) == 0 && e != step) buf[pos++] = e;
                }
            }
            return pos - start;
        }
    }

    // ===================================================================
    // Exact Main Method as requested
    // ===================================================================

    public static void main(String[] args) {
        var counter = new DerangadicCarryCounterDP();

        long[][] expected = {
                {4, 2, 2}, {4, 3, 3}, {4, 4, 3},
                {5, 2, 12}, {5, 3, 19}, {5, 4, 8}, {5, 5, 4},
                {6, 2, 84}, {6, 3, 109}, {6, 4, 51}, {6, 5, 15}, {6, 6, 5},
                {7, 2, 640}, {7, 3, 749}, {7, 4, 330}, {7, 5, 104}, {7, 6, 24}, {7, 7, 6},
                {8, 2, 5430}, {8, 3, 5863}, {8, 4, 2539}, {8, 5, 773},
                {8, 6, 185}, {8, 7, 35}, {8, 8, 7},
                {10, 2, 526568}, {10, 3, 511425}, {10, 4, 215067}, {10, 5, 63811},
                {10, 6, 14757}, {10, 7, 2805}, {10, 8, 455}, {10, 9, 63}, {10, 10, 9},
                {12, 2, 72755370L}, {12, 3, 66058315L}, {12, 4, 27254835L},
                {12, 11, 99}, {12, 12, 11},
                {14, 2, 13656650172L}, {14, 7, 57350105L}, {14, 12, 1595},
                {14, 13, 143}, {14, 14, 13},
        };

        long t0 = System.currentTimeMillis();
        boolean allOk = true;
        for (long[] row : expected) {
            int n = (int) row[0], L = (int) row[1];
            BigInteger exp = BigInteger.valueOf(row[2]);
            BigInteger got = counter.carryCount(n, L);
            if (!got.equals(exp)) {
                System.out.printf("FAIL n=%d L=%d: got=%s expected=%s%n", n, L, got, exp);
                allOk = false;
            }
        }
        System.out.printf("Self-test: %s in %d ms%n%n",
                allOk ? "all pass" : "FAILED", System.currentTimeMillis() - t0);

        // Extended table
        for (int n = 4; n <= 24; n++) {
            t0 = System.currentTimeMillis();
            BigInteger[] dist = counter.carryDistribution(n);
            long elapsed = System.currentTimeMillis() - t0;
            System.out.printf("=== n=%d (computed in %d ms) ===%n", n, elapsed);
            for (int L = 2; L <= n; L++)
                System.out.printf("  L=%2d : %,22d%n", L, dist[L]);
            System.out.println();
        }
    }
}