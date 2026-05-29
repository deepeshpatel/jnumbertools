/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.experiments;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Fast bitmask-DP counter for Derangadic carry-length frequencies over a
 * full lexicographic enumeration of universe-{@code n} derangements.
 *
 * <p>Produces the same counts as the increment machine iterated from rank
 * 0 to rank {@code !n - 1}. Where the simpler enumeration-based counter
 * runs in {@code O(!n)}, this one uses memoisation on a bitmask of placed
 * elements, reducing complexity to {@code O(2^n * n^3)}.</p>
 *
 * <h2>Algorithm</h2>
 * <p>The carry-length count {@code C_n(L)} decomposes as a sum over the
 * parity-matched layers visited during the full enumeration:</p>
 * <pre>
 *   C_n(L) = sum over m in {startLayer, startLayer+2, ..., n} of
 *              [ W(m, L) - shadows(m, L) ]
 *            + (1 if L is an expansion target)
 *            - (1 if L+1 is an expansion target)
 * </pre>
 * <p>where {@code W(m, L)} is the within-layer-{@code m} count of digit
 * tuples whose pivot lands at LSD-first array index {@code L - 1}.</p>
 *
 * <p>The crucial speedup is in computing {@code W(m, L)}. Instead of
 * enumerating each of the {@code m!} orderings, the DP memoises on
 * {@code (usedMask, step)}: many orderings reach the same partial
 * configuration and have identical completion counts. State space is
 * {@code O(2^m * m)} per target {@code L}, transitions are {@code O(m)}.</p>
 *
 * <h2>Complexity</h2>
 * <ul>
 *   <li><b>Time:</b> {@code O(2^n * n^3)} total to build the full
 *       distribution for universe {@code n}.</li>
 *   <li><b>Space:</b> {@code O(2^n * n)} for the memoisation table per
 *       target {@code L}; cleared between {@code L} iterations to keep
 *       peak memory bounded.</li>
 * </ul>
 *
 * <h2>Recommended use</h2>
 * <p>For tail values ({@code L = n, n-1, n-2, n-3, n-4, n-5}) the known
 * polynomial formulas {@code P_j(n)} from the paper are faster
 * (constant-time) and equally exact; use them in preference. This DP is
 * the right tool for the bulk values {@code L = 2, 3, ..., n-6} where no
 * closed-form polynomial is known.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * DerangadicCarryCounterDP c = new DerangadicCarryCounterDP(new Calculator());
 * c.carryCount(15, 9);     // returns 17,762,792 in well under a second
 * c.carryDistribution(15); // full distribution for n=15
 * }</pre>
 *
 * @author Deepesh Patel &amp; Aditya Patel
 */
public final class DerangadicCarryCounterDP {


    /**
     * Exact count of carry-length-{@code L} events during the full
     * lexicographic enumeration of derangements of {@code n} elements.
     *
     * @param n universe size, {@code n >= 2}
     * @param L carry length, {@code 2 <= L <= n}
     * @return count of such events; zero outside the valid range
     */
    public BigInteger carryCount(int n, int L) {
        if (n < 2 || L < 2 || L > n) return BigInteger.ZERO;

        int startLayer = (n % 2 == 0) ? 2 : 3;
        BigInteger total = BigInteger.ZERO;

        // Sum (within-layer minus shadow) contributions per parity-matched layer.
        for (int m = startLayer; m <= n; m += 2) {
            if (m < L) continue;
            BigInteger raw = withinLayerCount(m, L);
            BigInteger shadows = (m >= startLayer + 2)
                    ? withinLayerCount(m - 2, L)
                    : BigInteger.ZERO;
            total = total.add(raw).subtract(shadows);
        }

        // Layer-expansion events: each expansion (m-2 -> m) contributes
        // one carry of length exactly m, for m of the right parity, m >= startLayer + 2.
        if (L >= startLayer + 2 && L <= n && (L - n) % 2 == 0) {
            total = total.add(BigInteger.ONE);
        }

        // All-maxed shadow correction: subtract one for L = m - 1 where m
        // is a parity-matched layer >= startLayer + 2.
        int candidateM = L + 1;
        if (candidateM >= startLayer + 2 && candidateM <= n
                && (candidateM - n) % 2 == 0) {
            total = total.subtract(BigInteger.ONE);
        }

        return total;
    }

    /**
     * Full carry-length distribution for universe {@code n}.
     *
     * @return array of length {@code n + 1} where {@code dist[L]} is the
     *         count for that carry length; indices 0 and 1 are zero.
     */
    public BigInteger[] carryDistribution(int n) {
        BigInteger[] dist = new BigInteger[n + 1];
        Arrays.fill(dist, BigInteger.ZERO);
        for (int L = 2; L <= n; L++) {
            dist[L] = carryCount(n, L);
        }
        return dist;
    }

    // ----------------------------------------------------------------
    // Within-layer DP
    // ----------------------------------------------------------------

    /**
     * Counts the layer-{@code m} digit tuples whose pivot would land at
     * LSD-first array index {@code L - 1}. Uses bitmask DP keyed on
     * {@code (usedMask, step)}.
     */
    private BigInteger withinLayerCount(int m, int L) {
        if (L < 2 || L > m) return BigInteger.ZERO;

        // Cache cleared per (m, L) call to bound peak memory.
        Map<Long, BigInteger> memo = new HashMap<>();
        return countCompletions(0, 0, m, L - 1, memo);
    }

    /**
     * DP node value: number of ways to complete the layer-m enumeration
     * from the given state, respecting the pivot constraint at
     * {@code pivotArrayIdx} (LSD-first).
     */
    private BigInteger countCompletions(int usedMask, int step, int m,
                                        int pivotArrayIdx,
                                        Map<Long, BigInteger> memo) {
        if (step == m) return BigInteger.ONE;

        long key = ((long) usedMask << 6) | step;  // step fits in 6 bits for m <= 63
        BigInteger cached = memo.get(key);
        if (cached != null) return cached;

        int position = step;
        int arrayIdx = m - 1 - step;
        int remaining = m - step;

        // Build legal-candidate list (sorted ascending by element value),
        // applying dead-end avoidance when remaining == 2.
        int[] legal = new int[m];
        int legalCount = 0;

        if (remaining == 2) {
            int e1 = -1, e2 = -1;
            for (int e = 0; e < m; e++) {
                if ((usedMask & (1 << e)) == 0) {
                    if (e1 < 0) e1 = e;
                    else { e2 = e; break; }
                }
            }
            for (int e : new int[] {e1, e2}) {
                if (e == position) continue;
                int other = (e == e1) ? e2 : e1;
                if (other == position + 1) continue;  // dead end
                legal[legalCount++] = e;
            }
        } else {
            for (int e = 0; e < m; e++) {
                if ((usedMask & (1 << e)) != 0) continue;
                if (e == position) continue;
                legal[legalCount++] = e;
            }
        }

        BigInteger result;
        if (legalCount == 0) {
            result = BigInteger.ZERO;
        } else {
            int maxDigit = legalCount - 1;

            int dStart, dEnd;
            if (arrayIdx < pivotArrayIdx) {
                dStart = maxDigit;        // below pivot: forced max
                dEnd = maxDigit;
            } else if (arrayIdx == pivotArrayIdx) {
                dStart = 0;               // at pivot: non-max
                dEnd = maxDigit - 1;
            } else {
                dStart = 0;               // above pivot: free
                dEnd = maxDigit;
            }

            result = BigInteger.ZERO;
            for (int d = dStart; d <= dEnd; d++) {
                int chosen = legal[d];
                result = result.add(countCompletions(
                        usedMask | (1 << chosen), step + 1, m, pivotArrayIdx, memo));
            }
        }

        memo.put(key, result);
        return result;
    }

    // ----------------------------------------------------------------
    // Self-test
    // ----------------------------------------------------------------

    public static void main(String[] args) {
        var counter = new DerangadicCarryCounterDP();

        // Ground-truth data from your full-enumeration runs (n = 4..14)
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

        // Extend the table: compute full distributions for n = 15, 16
        for (int n : new int[] {21,22,23,24}) {
            t0 = System.currentTimeMillis();
            BigInteger[] dist = counter.carryDistribution(n);
            long elapsed = System.currentTimeMillis() - t0;
            System.out.printf("=== n=%d   (computed in %d ms) ===%n", n, elapsed);
            for (int L = 2; L <= n; L++) {
                System.out.printf("  L=%2d : %,18d%n", L, dist[L]);
            }
            System.out.println();
        }
    }
}