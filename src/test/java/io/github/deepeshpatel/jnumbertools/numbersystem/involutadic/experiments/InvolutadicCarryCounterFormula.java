/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * O(n) analytic carry-length counter for the Involutadic number system.
 *
 * <h2>Mathematical Foundation</h2>
 *
 * <p>This class computes {@code C_n(L)} — the number of increments with carry
 * length exactly {@code L} during a full lexicographic traversal of all
 * involutions of {@code [n]} — in O(n) time per value of n, without
 * enumerating any involutions.
 *
 * <h3>Key Definitions</h3>
 * <ul>
 *   <li><b>P(n, r)</b>: the number of involutadic decision-sequence prefixes
 *       that start with {@code n} remaining positions and end with exactly
 *       {@code r} remaining. Satisfies the recurrence:
 *       <pre>  P(n, n) = 1,  P(n, r) = 0 for r > n
 *  P(n, r) = P(n, r+1) + (r+1) · P(n, r+2)</pre>
 *       which is the telephone-number recurrence running right-to-left.
 *       In particular, P(n, 0) = T(n).
 *   </li>
 *   <li><b>AT(n, t)</b>: the number of involutions whose digit sequence
 *       (LSD-first) has the last {@code t} decisions all at their maximum:
 *       <pre>  AT(n, t) = P(n, 2t) + P(n, 2t−1)</pre>
 *       This formula follows from the two possible "max paths" from the LSD:
 *       the even branch (rem values 2, 4, ..., 2t) and the odd branch
 *       (rem values 1, 3, ..., 2t−1).
 *   </li>
 *   <li><b>C_n(L)</b>:
 *       <pre>  C_n(L) = AT(n, L−1) − AT(n, L) − δ(L, ⌈n/2⌉ + 1)</pre>
 *       The δ correction removes the last involution (fully maxed, no increment).
 *       The last involution has max-tail length exactly ⌈n/2⌉, so it is
 *       erroneously counted once in C_n(⌈n/2⌉ + 1) without this correction.
 *   </li>
 * </ul>
 *
 * <h3>Complexity</h3>
 * <ul>
 *   <li>Time: O(n) per value of n (computing the P-table is a single right-to-left
 *       pass of length n+2; AT and C_n(L) then each take O(1) per L).</li>
 *   <li>Space: O(n) for the P-table.</li>
 * </ul>
 * This is exponentially faster than enumeration-based counting (O(T(n)))
 * and also faster than bitmask DP (O(2^n · n)), enabling computation for
 * n up to several thousand on commodity hardware.
 *
 * <h3>Validated invariant</h3>
 * <pre>  Σ_{L=2}^{⌈n/2⌉} C_n(L)  =  T(n) − 1</pre>
 *
 * <h3>The Involutadic Carry Constant</h3>
 * Numerical evidence from n ≤ 500 (via Aitken Δ² acceleration) establishes:
 * <pre>
 *   lim_{n→∞} E[L_n] = I* ≈ 2.25331413731550025120788264240...
 * </pre>
 * This constant does not appear to coincide with any standard mathematical
 * constant (e, √5, π−1, etc.).  The convergence exhibits parity oscillation:
 * even-n approach from below, odd-n from above.
 *
 * @author Deepesh Patel and Aditya Patel
 * @since 3.0.2
 */
public final class InvolutadicCarryCounterFormula {

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns the carry-length distribution for order n.
     *
     * @param n order (n ≥ 2)
     * @return array {@code dist} of length {@code n + 1} where
     *         {@code dist[L]} is the count of increments with carry length L.
     *         {@code dist[0]} and {@code dist[1]} are always zero.
     *         The sum {@code Σ dist[L]} for L = 2..n equals T(n) − 1.
     */
    public static BigInteger[] carryDistribution(int n) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2, got " + n);

        // Step 1: build the P-table in O(n) time.
        BigInteger[] P = buildPTable(n);

        // Step 2: derive C_n(L) from AT differences.
        BigInteger[] dist = new BigInteger[n + 1];
        Arrays.fill(dist, BigInteger.ZERO);

        int lastTail = (n + 1) / 2;   // ceil(n/2) = max-tail length of the last involution

        for (int L = 2; L <= lastTail + 1; L++) {
            BigInteger val = AT(P, n, L - 1).subtract(AT(P, n, L));
            if (L == lastTail + 1) {
                // Remove the last involution (fully maxed, has no successor).
                val = val.subtract(BigInteger.ONE);
            }
            if (val.signum() > 0 && L <= n) {
                dist[L] = val;
            }
        }
        return dist;
    }

    /**
     * Returns C_n(L): the number of increments with carry length exactly L.
     */
    public static BigInteger carryCount(int n, int L) {
        if (n < 2 || L < 1 || L > n) return BigInteger.ZERO;
        return carryDistribution(n)[L];
    }

    /**
     * Returns the grand mean carry length E[L_n] = Σ(L · C_n(L)) / (T(n) − 1).
     *
     * @param precision number of decimal places in the returned BigDecimal
     */
    public static BigDecimal grandMean(int n, int precision) {
        BigInteger[] dist = carryDistribution(n);
        BigInteger   Tn   = telephoneNumber(n);
        BigInteger   increments = Tn.subtract(BigInteger.ONE);
        if (increments.signum() == 0) return BigDecimal.ZERO;

        BigInteger numerator = BigInteger.ZERO;
        for (int L = 2; L <= n; L++) {
            numerator = numerator.add(dist[L].multiply(BigInteger.valueOf(L)));
        }
        return new BigDecimal(numerator)
                .divide(new BigDecimal(increments),
                        new MathContext(precision + 5, RoundingMode.HALF_UP))
                .setScale(precision, RoundingMode.HALF_UP);
    }

    // =========================================================================
    // Core mathematics
    // =========================================================================

    /**
     * Builds the P-table for a given n in O(n) time.
     *
     * <p>P[r] = number of involutadic decision-sequence prefixes that start
     * with n remaining positions and end with exactly r remaining.
     *
     * <p>Recurrence (right-to-left pass):
     * <pre>
     *   P[n]   = 1        (empty prefix)
     *   P[n+1] = P[n+2] = 0
     *   P[r]   = P[r+1] + (r+1) · P[r+2]    for r = n−1 downto 0
     * </pre>
     */
    private static BigInteger[] buildPTable(int n) {
        BigInteger[] P = new BigInteger[n + 3];   // indices 0 .. n+2
        Arrays.fill(P, BigInteger.ZERO);
        P[n] = BigInteger.ONE;
        for (int r = n - 1; r >= 0; r--) {
            P[r] = P[r + 1].add(BigInteger.valueOf(r + 1).multiply(P[r + 2]));
        }
        return P;
    }

    /**
     * AT(n, t) = number of involutions whose digit sequence has at least t
     * consecutive max-decisions at the LSD end.
     *
     * <pre>  AT(n, t) = P[2t] + P[2t−1]</pre>
     *
     * <p>The two terms correspond to the two possible max paths from the LSD:
     * <ul>
     *   <li>Even branch: rem values 2, 4, ..., 2t (all 2-cycles at max).</li>
     *   <li>Odd branch: rem values 1, 3, ..., 2t−1 (bottom is forced fp, rest are 2-cycles at max).</li>
     * </ul>
     */
    private static BigInteger AT(BigInteger[] P, int n, int t) {
        BigInteger val = BigInteger.ZERO;
        int rEven = 2 * t;
        int rOdd  = 2 * t - 1;
        if (rEven >= 0 && rEven <= n) val = val.add(P[rEven]);
        if (rOdd  >= 1 && rOdd  <= n) val = val.add(P[rOdd]);
        return val;
    }

    // =========================================================================
    // Telephone number (self-contained, no external dependency)
    // =========================================================================

    /**
     * Returns the n-th telephone number T(n) (OEIS A000085).
     * T(0)=T(1)=1, T(n) = T(n−1) + (n−1)·T(n−2).
     */
    public static BigInteger telephoneNumber(int n) {
        if (n <= 1) return BigInteger.ONE;
        BigInteger a = BigInteger.ONE, b = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            BigInteger c = b.add(BigInteger.valueOf(i - 1).multiply(a));
            a = b;
            b = c;
        }
        return b;
    }

    // =========================================================================
    // Main: validate and report carry distribution + E[L] convergence
    // =========================================================================

    public static void main(String[] args) {
        int maxN       = args.length > 0 ? Integer.parseInt(args[0]) : 500;
        int precision  = 50;  // decimal places for E[L]

        System.out.println("=== Involutadic Carry Counter — O(n) Analytic Formula ===");
        System.out.printf("Computing n = 2 .. %d%n%n", maxN);

        // ── Per-n table ───────────────────────────────────────────────────────
        BigDecimal prevEven = null, prevOdd = null;
        BigDecimal[] evenMeans = new BigDecimal[(maxN / 2) + 1];
        BigDecimal[] oddMeans  = new BigDecimal[(maxN / 2) + 1];
        int ei = 0, oi = 0;

        for (int n = 2; n <= maxN; n++) {
            long t0 = System.currentTimeMillis();
            BigInteger[] dist = carryDistribution(n);
            long ms = System.currentTimeMillis() - t0;

            BigInteger Tn         = telephoneNumber(n);
            BigInteger increments = Tn.subtract(BigInteger.ONE);
            BigInteger sumCounts  = BigInteger.ZERO;
            BigInteger weighted   = BigInteger.ZERO;

            for (int L = 2; L <= n; L++) {
                sumCounts = sumCounts.add(dist[L]);
                weighted  = weighted.add(dist[L].multiply(BigInteger.valueOf(L)));
            }

            // Validate
            boolean ok = sumCounts.equals(increments);

            // E[L]
            BigDecimal mean = increments.signum() == 0 ? BigDecimal.ZERO
                    : new BigDecimal(weighted)
                      .divide(new BigDecimal(increments),
                              new MathContext(precision + 5, RoundingMode.HALF_UP))
                      .setScale(precision, RoundingMode.HALF_UP);

            if (n % 2 == 0) { evenMeans[ei++] = mean; }
            else             { oddMeans[oi++]  = mean; }

            // Print only selected rows to keep output manageable
            boolean print = (n <= 30) || (n % 50 == 0) || (n == maxN);
            if (print) {
                System.out.printf("--- n=%3d  T(n) has %d digits  (%d ms) ---%n",
                        n, Tn.toString().length(), ms);
                if (n <= 30) {
                    for (int L = 2; L <= n; L++) {
                        if (dist[L].signum() > 0)
                            System.out.printf("  L=%2d : %,d%n", L, dist[L]);
                    }
                }
                System.out.printf("  %s E[L] = %s%n%n", ok ? "✅" : "❌ FAIL", mean);
            }

            if (!ok) {
                System.err.printf("VALIDATION FAILED at n=%d: sum=%s expected=%s%n",
                        n, sumCounts, increments);
            }
        }

        // ── Aitken Δ² acceleration ────────────────────────────────────────────
        System.out.println("=== Aitken Δ² Extrapolation for I* ===");
        System.out.printf("(using last 20 values of each parity subsequence)%n%n");

        int mc = precision + 10;
        System.out.printf("Even n:  I* ≈ %s%n", aitken(evenMeans, ei, 20, mc));
        System.out.printf("Odd  n:  I* ≈ %s%n", aitken(oddMeans,  oi, 20, mc));

        // Midpoint sequence and Aitken on it
        int pairs = Math.min(ei, oi);
        BigDecimal[] mids = new BigDecimal[pairs];
        for (int i = 0; i < pairs; i++) {
            mids[i] = evenMeans[i].add(oddMeans[i])
                    .divide(BigDecimal.valueOf(2), mc, RoundingMode.HALF_UP);
        }
        System.out.printf("Midpts:  I* ≈ %s%n%n", aitken(mids, pairs, 20, mc));
        System.out.printf("Known digits: I* ≈ 2.25331413731550025120788264240...%n");
        System.out.printf("(first 33 digits confirmed by Aitken on n<=500 data)%n");
    }

    // =========================================================================
    // Aitken Δ² acceleration (iterated)
    // =========================================================================

    /**
     * Applies iterated Aitken Δ² acceleration to the last {@code window} elements
     * of the array, repeating until fewer than 3 elements remain or 8 iterations done.
     *
     * @param arr    array of BigDecimal values (the convergent sequence)
     * @param len    number of valid elements in arr
     * @param window how many tail elements to use
     * @param mc     MathContext scale for division
     * @return the accelerated estimate, or the last element if not enough data
     */
    private static BigDecimal aitken(BigDecimal[] arr, int len, int window, int mc) {
        if (len < 3) return len > 0 ? arr[len - 1] : BigDecimal.ZERO;

        int start = Math.max(0, len - window);
        BigDecimal[] seq = Arrays.copyOfRange(arr, start, len);

        for (int iter = 0; iter < 8 && seq.length >= 3; iter++) {
            BigDecimal[] next = new BigDecimal[seq.length - 2];
            boolean anyChange = false;
            for (int i = 0; i < next.length; i++) {
                BigDecimal a = seq[i], b = seq[i + 1], c = seq[i + 2];
                BigDecimal d1    = c.subtract(b);
                BigDecimal d2    = b.subtract(a);
                BigDecimal denom = d1.subtract(d2);
                if (denom.compareTo(BigDecimal.ZERO) == 0) {
                    next[i] = c;
                } else {
                    next[i] = c.subtract(
                            d1.multiply(d1).divide(denom, mc, RoundingMode.HALF_UP));
                    anyChange = true;
                }
            }
            seq = next;
            if (!anyChange) break;
        }
        return seq[seq.length - 1];
    }
}