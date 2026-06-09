/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel
 *
 * DerangadicDataGenerator — JUnit data-generation harness for the Derangadic paper.
 *
 * NOT a correctness test suite: each @Test is an independently-runnable data/benchmark
 * generator (analogous to InvolutadicDataGenerator). Three methods:
 *
 *   A. benchmarkLexOrderGeneration()
 *        Sequential lex-order generation: Derangadic increment machine vs a
 *        brute-force "next permutation, accept if derangement" successor (the
 *        baseline used in the published paper). Brute force is COMPETITIVE here,
 *        not a strawman: ~37% of permutations are derangements (!n/n! -> 1/e), so
 *        it tries only ~2-3 permutations per accepted derangement. Both emit the
 *        identical lex-order stream. The increment machine is amortised O(1) per
 *        step (touches only the carry suffix, mean -> e); brute force is O(n) per
 *        step. So the machine wins per-step AND additionally provides rank state
 *        and random access (method C). Mikawa--Tanaka (2014) is the closest prior
 *        lex-order derangement ranking work, but their order is on CYCLE notation,
 *        not the one-line dictionary order used here, so it is discussed in prose
 *        rather than benchmarked head-to-head (different orders are not comparable).
 *
 *   B. analyzeCarryConvergence()
 *        Carry-length mean E[L_n] and its convergence to the conjectured constant
 *        e = 2.71828... . Exact distribution from DerangadicCarryCounterDP (the
 *        verified exact counter; exponential in n, so exact reach is ~n<=22), plus
 *        an empirical large-n cross-check via the state machine. Tests whether
 *        eps_n = E[L_n] - e follows a power law, a fixed-geometric law, or a
 *        stretched exponential (same model-discrimination used for Involutadic).
 *
 *   C. benchmarkRandomAccess()
 *        The distinguishing capability: jump to an arbitrary rank in time
 *        independent of the rank value. In the arithmetic-operation model
 *        (subfactorial and restricted-derangement counts precomputed/cached, as
 *        is standard in the combinatorial-generation literature), encode/unrank
 *        perform O(n^2) operations — independent of R. A pure successor needs
 *        Theta(R) steps to reach rank R. Measured times are shown for reference;
 *        complexity is reported as operation count, not wall-clock.
 *
 * Run individually, e.g.:  -Dperformance.testing=true and run one @Test.
 *
 * CACHE-WARM STRATEGY (why each @Test gets its own Calculator):
 *   Calculator memoises factorial, subFactorial, and restrictedDerangementCount.
 *   For large n, those BigInteger tables can consume significant heap. Sharing
 *   one Calculator across all three @Test methods would accumulate entries for
 *   every n ever used (200..4000 for A, up to 800 for B, 200..2000 for C) and
 *   risk OOM. Each method therefore creates its own Calculator instance so its
 *   cache is released when the method returns.
 *
 *   Within method C (random access), every timed call MUST see a fully populated
 *   cache so that no BigInteger computation leaks into the measured window.
 *   The strategy is deliberately simple: for each measured call, run it TWICE
 *   with the same rank and the same Calculator instance.
 *     Run 1 (untimed): the Calculator populates its memoised tables for every
 *                      (i, j) cell visited during unranking.
 *     Run 2 (timed):   every lookup is a cache hit; no BigInteger arithmetic
 *                      occurs during measurement.
 *   Run 1 output goes to the sink variable only and is never printed.
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.experiments;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicIncrementStateMachine;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Disabled("Data-generation / benchmark harness, not a correctness test")
public class DerangadicDataGenerator {

    private static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);
    private static final BigDecimal E_CONST = new BigDecimal(
            "2.71828182845904523536028747135266249775724709369996");

    // =====================================================================
    // A. Sequential lex-order generation benchmark
    // =====================================================================
    @Test
    @DisplayName("A: sequential lex-order generation — increment machine vs brute-force baseline")
    void benchmarkLexOrderGeneration() {
        System.out.println("=".repeat(72));
        System.out.println("A. SEQUENTIAL LEX-ORDER GENERATION");
        System.out.println("   (a) Derangadic increment machine   (b) brute-force next-perm + derangement filter");
        System.out.println("   Brute force is competitive here: ~37% of permutations are derangements");
        System.out.println("   (!n/n! -> 1/e), so it tries only ~2-3 permutations per accepted derangement.");
        System.out.println("=".repeat(72));

        // Fresh Calculator per @Test — isolates this method's cache from B and C
        // (see class-level CACHE-WARM STRATEGY note).
        Calculator calcA = new Calculator();
        int[] nValues = {200, 400, 800, 1200, 1600, 2000, 3000, 4000};
        int MEASURE = 200_000;

        // A deep, fixed start rank (well inside the range for all tested n).
        // 10^40 is far above !k thresholds for small carrier lengths, forcing the
        // full active window, and is reproducible.
        BigInteger startRank = BigInteger.TEN.pow(40);

        System.out.printf("%n%-6s  %-16s  %-18s  %s%n",
                "n", "increment_ns", "bruteforce_ns", "speedup(bf/inc)");

        long sink = 0;
        for (int n : nValues) {
            // ---- (a) increment machine ----
            // Run 1: populates Calculator cache + warms JIT. Time discarded.
            // Run 2: cache fully warm — this is the number we print.
            long incNs;
            {
                DerangadicIncrementStateMachine m1 =
                        new DerangadicIncrementStateMachine(n, startRank, calcA);
                for (int i = 0; i < MEASURE; i++) {
                    if (!m1.increment()) m1 = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calcA);
                    sink += m1.derangement()[0];
                }
                DerangadicIncrementStateMachine m2 =
                        new DerangadicIncrementStateMachine(n, startRank, calcA);
                long t0 = System.nanoTime();
                for (int i = 0; i < MEASURE; i++) {
                    if (!m2.increment()) m2 = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, calcA);
                    sink += m2.derangement()[0];
                }
                incNs = (System.nanoTime() - t0) / MEASURE;
            }

            // ---- (b) brute-force next-permutation + derangement filter ----
            // Same two-run pattern: Run 1 discarded, Run 2 printed.
            long btNs;
            {
                BruteForceDerangementSuccessor b1 = new BruteForceDerangementSuccessor(n);
                for (int i = 0; i < MEASURE; i++) {
                    if (!b1.increment()) b1 = new BruteForceDerangementSuccessor(n);
                    sink += b1.current()[0];
                }
                BruteForceDerangementSuccessor b2 = new BruteForceDerangementSuccessor(n);
                long t0 = System.nanoTime();
                for (int i = 0; i < MEASURE; i++) {
                    if (!b2.increment()) b2 = new BruteForceDerangementSuccessor(n);
                    sink += b2.current()[0];
                }
                btNs = (System.nanoTime() - t0) / MEASURE;
            }

            System.out.printf("%-6d  %-16d  %-18d  %.2fx%n",
                    n, incNs, btNs, (double) btNs / incNs);
        }

        System.out.println("\n--- LaTeX coordinates (increment machine) ---");
        // (regenerate by hand from the table above; left as a manual step to avoid
        //  storing two parallel arrays — the table is the source of truth)
        System.out.println("(sink=" + sink + ")");
        System.out.println("\nREADING: the increment machine is amortised O(1) per step (touches only the");
        System.out.println("carry suffix, mean length -> e), so it is roughly FLAT in n. Brute force is");
        System.out.println("O(n) per accepted derangement (each next-permutation and derangement check is");
        System.out.println("O(n), and ~2-3 are tried per acceptance). Hence the speedup grows with n. Both");
        System.out.println("produce the identical lex-order stream, but only the increment machine carries");
        System.out.println("rank state and supports random access (method C).");
    }

    // =====================================================================
    // B. Carry-length convergence to e
    // =====================================================================
    @Test
    @DisplayName("B: carry-length mean E[L_n] -> e, convergence shape")
    void analyzeCarryConvergence() {
        System.out.println("=".repeat(72));
        System.out.println("B. CARRY-LENGTH MEAN E[L_n] -> e = " + E_CONST.round(new MathContext(12)));
        System.out.println("=".repeat(72));

        // ---- Exact means from the verified DP counter (exact, but exponential: n<=22) ----
        DerangadicCarryCounterDP dp = new DerangadicCarryCounterDP();
        int maxExact = 22;

        System.out.printf("%n[exact, DerangadicCarryCounterDP]%n%-4s  %-22s  %-16s%n",
                "n", "E[L_n] (exact)", "eps_n = E - e");
        List<double[]> exactPts = new ArrayList<>(); // {n, eps}
        for (int n = 4; n <= maxExact; n++) {
            BigInteger[] dist = dp.carryDistribution(n);
            BigInteger total = BigInteger.ZERO, weighted = BigInteger.ZERO;
            for (int L = 2; L <= n; L++) {
                total = total.add(dist[L]);
                weighted = weighted.add(BigInteger.valueOf(L).multiply(dist[L]));
            }
            BigDecimal mean = new BigDecimal(weighted).divide(new BigDecimal(total), MC);
            double eps = mean.subtract(E_CONST, MC).doubleValue();
            System.out.printf("%-4d  %-22s  %+.9e%n",
                    n, mean.setScale(12, RoundingMode.HALF_UP).toPlainString(), eps);
            exactPts.add(new double[]{n, eps});
        }

        convergenceShape("EXACT (n<=" + maxExact + ")", exactPts);

        // ---- Empirical large-n cross-check via the state machine ----
        System.out.println("\n[empirical, state machine — large n, sampling]");
        // Fresh Calculator per @Test — isolates this method's subFactorial cache
        // from A and C.  See class-level CACHE-WARM STRATEGY note.
        Calculator calcB = new Calculator();
        int[] bigN = {100, 200, 400, 800};
        long ITERS = 5_000_000L;
        long SEED = 42L;
        Random rng = new Random(SEED);
        System.out.printf("%-6s  %-16s  %-16s  %s%n", "n", "E[L_n] (emp.)", "eps_n", "P(L=2)=alpha");
        for (int n : bigN) {
            BigInteger maxRank = calcB.subFactorial(n);
            int bitLen = maxRank.bitLength();
            BigInteger r;
            do { r = new BigInteger(bitLen, rng); } while (r.compareTo(maxRank) >= 0);
            DerangadicIncrementStateMachine m = new DerangadicIncrementStateMachine(n, r, calcB);
            long[] hist = new long[64];
            long count = 0;
            for (long i = 0; i < ITERS; i++) {
                int L = m.incrementAndGetCarryLength();
                if (L == 0) break;
                if (L < hist.length) hist[L]++;
                count++;
            }
            double wsum = 0, tot = 0;
            for (int L = 2; L < hist.length; L++) { wsum += (double) L * hist[L]; tot += hist[L]; }
            double mean = wsum / tot;
            double alpha = hist[2] / tot;
            System.out.printf("%-6d  %-16.9f  %-16.3e  %.6f%n",
                    n, mean, mean - Math.E, alpha);
        }
        System.out.println("\nNOTE: empirical means carry sampling noise; the exact block above is");
        System.out.println("authoritative for the convergence-shape fit. Conjectured P(L=k)=(k-1)/k!.");
    }

    /** Fit eps_n against power-law (ln|e| vs ln n), geometric (vs n), stretched (vs sqrt n). */
    private static void convergenceShape(String label, List<double[]> pts) {
        // use the tail (drop the first few pre-asymptotic points)
        int start = 0;
        while (start < pts.size() && pts.get(start)[0] < 8) start++;
        int k = pts.size() - start;
        if (k < 3) { System.out.println("  (too few points for " + label + ")"); return; }

        double[] lnAbs = new double[k], lnN = new double[k], nArr = new double[k], sqN = new double[k];
        int sign = 0;
        for (int i = 0; i < k; i++) {
            double[] p = pts.get(start + i);
            double eps = p[1];
            sign = (eps < 0) ? -1 : 1;
            lnAbs[i] = Math.log(Math.abs(eps));
            lnN[i] = Math.log(p[0]);
            nArr[i] = p[0];
            sqN[i] = Math.sqrt(p[0]);
        }
        double[] fp = ols(lnN, lnAbs);
        double[] fg = ols(nArr, lnAbs);
        double[] fs = ols(sqN, lnAbs);
        System.out.printf("%n  convergence-shape fit  [%s, %d tail pts, sign(eps)=%+d]:%n", label, k, sign);
        System.out.printf("    power-law  ln|e| vs ln n :  beta=%7.3f   R2=%.6f%n", -fp[0], fp[2]);
        System.out.printf("    geometric  ln|e| vs n    :  rho =%7.5f   R2=%.6f%n", Math.exp(fg[0]), fg[2]);
        System.out.printf("    stretched  ln|e| vs sqrt n:  b   =%7.4f   R2=%.6f%n", fs[0], fs[2]);
        System.out.println("    (the model whose R2 is highest AND stable across windows is the true form)");
    }

    /** OLS y = slope*x + intercept; returns {slope, intercept, R^2}. */
    private static double[] ols(double[] x, double[] y) {
        int k = x.length;
        double sX = 0, sY = 0, sXY = 0, sX2 = 0;
        for (int i = 0; i < k; i++) { sX += x[i]; sY += y[i]; sXY += x[i] * y[i]; sX2 += x[i] * x[i]; }
        double slope = (k * sXY - sX * sY) / (k * sX2 - sX * sX);
        double inter = (sY - slope * sX) / k;
        double yBar = sY / k, ssTot = 0, ssRes = 0;
        for (int i = 0; i < k; i++) {
            ssTot += (y[i] - yBar) * (y[i] - yBar);
            double r = y[i] - (slope * x[i] + inter);
            ssRes += r * r;
        }
        return new double[]{slope, inter, ssTot == 0 ? 1.0 : 1.0 - ssRes / ssTot};
    }

    // =====================================================================
    // C. Random-access benchmark
    // =====================================================================
    @Test
    @DisplayName("C: random access — jump to arbitrary rank, cost vs n and vs R")
    void benchmarkRandomAccess() {
        System.out.println("=".repeat(72));
        System.out.println("C. RANDOM ACCESS (rank-independent jump; the actual contribution)");
        System.out.println("=".repeat(72));

        // Fresh Calculator per @Test — isolates this method's large cache (n up to 2000)
        // from A and B.  See class-level CACHE-WARM STRATEGY note.
        Calculator calcC = new Calculator();
        Random rng = new Random(42);

        // ---- (A) jump-to-random-rank cost vs n ----
        System.out.println("\n(A) Time to jump to a RANDOM rank R in [0, !n), vs n");
        System.out.printf("    %-6s  %-16s  %-12s%n", "n", "jump_ns", "!n digits");
        int[] nVals = {200, 400, 600, 800, 1200, 1600, 2000};
        long sinkA = 0;
        for (int n : nVals) {
            BigInteger maxRank = calcC.subFactorial(n);
            int bitLen = maxRank.bitLength();
            BigInteger r = randomRank(rng, maxRank, bitLen);
            var algorithm = new DerangadicAlgorithms(calcC);

            // Run 1 (untimed): populates the restrictedDerangementCount(i,j) and
            // subFactorial caches for this n — every cell visited is now memoised.
            sinkA += algorithm.unrank(r, n)[0];

            // Run 2 (timed): cache fully warm, only lookup + int-array decode work.
            long t0 = System.nanoTime();
            sinkA += algorithm.unrank(r, n)[0];
            long ns = System.nanoTime() - t0;

            System.out.printf("    %-6d  %-16d  %-12d%n", n, ns, maxRank.toString().length());
        }
        System.out.println("    (sink=" + sinkA + ")");
        System.out.println("    NOTE: the jump cost is INDEPENDENT of the value of R (R may have thousands");
        System.out.println("    of digits). In the arithmetic-operation model (counts precomputed/cached),");
        System.out.println("    encode/unrank perform O(n^2) operations. The measured times below are");
        System.out.println("    shown for reference only; complexity is reported as operation count.");

        // ---- (B) reach rank R: flat (unrank) vs Theta(R) (pure successor) ----
        int nFixed = 30;
        BigInteger maxRank = calcC.subFactorial(nFixed);
        int[] Rvals = {10, 100, 1000, 10000, 100000};
        System.out.printf("%n(B) Cost to REACH rank R at fixed n=%d  (!%d has %d digits)%n",
                nFixed, nFixed, maxRank.toString().length());
        System.out.printf("    %-10s  %-18s  %-18s%n", "R", "derangadic_jump_ns", "bruteforce_steps_ns");
        // n=30 operations complete in microseconds — a single shot has high relative
        // noise. Run 1 warms the cache; then take min over REPS_B timed calls.
        int REPS_B = 200;
        var algFixed = new DerangadicAlgorithms(calcC);
        long sinkB = 0;
        for (int R : Rvals) {
            BigInteger Rbig = BigInteger.valueOf(R);

            // Run 1 (untimed): warms the full restrictedDerangementCount cache for nFixed.
            sinkB += algFixed.unrank(Rbig, nFixed)[0];

            // Timed: min over REPS_B calls — cache warm on every call.
            long bestJump = Long.MAX_VALUE;
            for (int rep = 0; rep < REPS_B; rep++) {
                long t0 = System.nanoTime();
                sinkB += algFixed.unrank(Rbig, nFixed)[0];
                long ns = System.nanoTime() - t0;
                if (ns < bestJump) bestJump = ns;
            }

            // Brute-force: same two-run pattern for JIT consistency.
            // Run 1 (untimed), then min over REPS_B timed calls.
            BruteForceDerangementSuccessor bWarm = new BruteForceDerangementSuccessor(nFixed);
            for (int s = 0; s < R; s++) bWarm.increment();
            sinkB += bWarm.current()[0];
            long bestStep = Long.MAX_VALUE;
            for (int rep = 0; rep < REPS_B; rep++) {
                BruteForceDerangementSuccessor b = new BruteForceDerangementSuccessor(nFixed);
                long t1 = System.nanoTime();
                for (int s = 0; s < R; s++) b.increment();
                long ns = System.nanoTime() - t1;
                sinkB += b.current()[0];
                if (ns < bestStep) bestStep = ns;
            }

            System.out.printf("    %-10d  %-18d  %-18d%n", R, bestJump, bestStep);
        }
        System.out.println("    (sink=" + sinkB + ")");
        System.out.println("\n    READING: the derangadic jump reaches rank R in O(n^2) operations,");
        System.out.println("    INDEPENDENT of R; brute force needs Theta(R) steps. For a deep rank such");
        System.out.println("    as R ~ !n/2 (R unbounded by n) the Theta(R) path is infeasible while the");
        System.out.println("    jump is not. This rank-independent random access — not per-step speed — is");
        System.out.println("    the capability brute force structurally cannot provide.");
    }

    private static BigInteger randomRank(Random rng, BigInteger max, int bitLen) {
        BigInteger r;
        do { r = new BigInteger(bitLen, rng); } while (r.compareTo(max) >= 0);
        return r;
    }

    // =====================================================================
    // Honest baseline: next-permutation-filter brute-force derangement successor
    //
    // Generates derangements of [0,n) in dictionary (one-line) lexicographic order
    // by repeatedly advancing to the next permutation (standard lexicographic
    // next-permutation) and accepting it iff it is a derangement. This is the
    // baseline used in the published Derangadic paper, and it is a GENUINELY
    // COMPETITIVE (not strawman) competitor: because !n / n! -> 1/e ~ 0.3679, about
    // 37% of permutations are derangements, so only ~2-3 permutations are tried per
    // accepted derangement. It produces exactly the SAME lex-order stream as the
    // Derangadic increment machine, but carries no rank state and cannot jump to an
    // arbitrary rank.
    //
    // Cost per emitted derangement: O(1) expected permutations tried, each
    // next-permutation step and derangement check being O(n) — so O(n) per step.
    // (The increment machine, by contrast, touches only the carry suffix, mean
    // length -> e, giving amortised O(1).)
    // =====================================================================
    static final class BruteForceDerangementSuccessor {
        private final int n;
        private final int[] a;

        BruteForceDerangementSuccessor(int n) {
            this.n = n;
            this.a = new int[n];
            buildFirstDerangement();
        }

        int[] current() { return a; }

        /**
         * Builds the lexicographically smallest derangement of [0,n) DIRECTLY
         * (greedy: smallest legal value per position, with a dead-end guard for the
         * final two positions). This avoids walking next-permutation from the
         * identity, which for large n would try an astronomical number of
         * permutations before reaching the first derangement.
         */
        private void buildFirstDerangement() {
            boolean[] used = new boolean[n];
            for (int pos = 0; pos < n; pos++) {
                int remaining = n - pos;
                for (int v = 0; v < n; v++) {
                    if (used[v] || v == pos) continue;
                    // Dead-end guard: if exactly two positions remain (pos, pos+1=n-1),
                    // choosing v must not force the last position to map to itself.
                    if (remaining == 2) {
                        int last = n - 1;
                        // the leftover value after taking v:
                        int leftover = -1;
                        for (int x = 0; x < n; x++) {
                            if (x == v || used[x]) continue;
                            leftover = x; break;
                        }
                        if (leftover == last) continue; // would fix the last position
                    }
                    a[pos] = v; used[v] = true;
                    break;
                }
            }
        }

        private boolean isDerangement() {
            for (int i = 0; i < n; i++) if (a[i] == i) return false;
            return true;
        }

        /** Standard in-place lexicographic next permutation; false if none. */
        private boolean nextPermutation() {
            int i = n - 2;
            while (i >= 0 && a[i] >= a[i + 1]) i--;
            if (i < 0) return false;
            int j = n - 1;
            while (a[j] <= a[i]) j--;
            int t = a[i]; a[i] = a[j]; a[j] = t;
            // reverse suffix [i+1, n)
            int lo = i + 1, hi = n - 1;
            while (lo < hi) { int tmp = a[lo]; a[lo] = a[hi]; a[hi] = tmp; lo++; hi--; }
            return true;
        }

        /** Advance to the next permutation that is a derangement; false if none. */
        private boolean advanceToNextDerangement() {
            while (nextPermutation()) {
                if (isDerangement()) return true;
            }
            return false;
        }

        /** Advance to next derangement in lex order; false if at last. */
        boolean increment() {
            return advanceToNextDerangement();
        }
    }
}