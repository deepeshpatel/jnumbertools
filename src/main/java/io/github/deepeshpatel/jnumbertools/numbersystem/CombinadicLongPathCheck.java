package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * Quick check (run its main): the long unranking path gives the same combinations as the BigInteger
 * search, and how much faster it is. Takes a few seconds.
 */
public class CombinadicLongPathCheck {

    public static void main(String[] args) {
        Calculator calc = new Calculator();
        CombinadicAlgorithms alg = new CombinadicAlgorithms(calc);
        Random random = new Random(7);

        // 1. Same answers: every rank for small spaces, 20,000 random ranks for large ones,
        //    including r close to n where the table saturates (C(69,34) overflows a long).
        int[][] cases = {{5, 3}, {10, 5}, {20, 8}, {20, 18}, {30, 15}, {70, 68}, {64, 32}, {100, 3}, {200, 2}, {1, 1}, {4, 0}, {6, 6}};
        long checked = 0;
        for (int[] c : cases) {
            int n = c[0], r = c[1];
            BigInteger total = calc.nCr(n, r);
            if (total.bitLength() > 62) { System.out.println("skip (too big for long path) n=" + n + " r=" + r); continue; }
            long t = total.longValue();
            long samples = t <= 200_000 ? t : 20_000;
            for (long i = 0; i < samples; i++) {
                long rank = t <= 200_000 ? i : (random.nextLong() & Long.MAX_VALUE) % t;
                BigInteger rk = BigInteger.valueOf(rank);
                int[] fast = alg.unRank(rk, total, n, r);
                int[] slow = alg.unRankBySearch(rk, total, n, r);
                if (!Arrays.equals(fast, slow)) {
                    throw new AssertionError("MISMATCH n=" + n + " r=" + r + " rank=" + rank
                            + " fast=" + Arrays.toString(fast) + " slow=" + Arrays.toString(slow));
                }
                checked++;
            }
        }
        System.out.println("OK: " + checked + " unranks identical to the BigInteger path");

        // 2. Speed, algorithm level: C(20,8), ranks spread over the space.
        int n = 20, r = 8;
        BigInteger total = calc.nCr(n, r);
        BigInteger[] ranks = new BigInteger[1000];
        for (int i = 0; i < ranks.length; i++) ranks[i] = total.multiply(BigInteger.valueOf(i)).divide(BigInteger.valueOf(ranks.length));
        long sink = 0;
        double bigNs = 0, longNs = 0;
        for (int round = 0; round < 12; round++) {            // first rounds are warm-up
            long a = System.nanoTime();
            for (BigInteger rk : ranks) sink += alg.unRankBySearch(rk, total, n, r)[0];
            long b = System.nanoTime();
            for (BigInteger rk : ranks) sink += alg.unRank(rk, total, n, r)[0];
            long c = System.nanoTime();
            if (round >= 6) { bigNs += (b - a); longNs += (c - b); }
        }
        double per = 6.0 * ranks.length;
        System.out.printf("unRank C(20,8): BigInteger %.0f ns, long %.0f ns, %.1fx faster%n", bigNs / per, longNs / per, bigNs / longNs);

        // 3. Speed, end to end: the same call the survey benchmark makes (100 random results).
        var builder = JNumberTools.combinations().unique(20, 8);
        double best = Double.MAX_VALUE;
        for (int round = 0; round < 40; round++) {
            long a = System.nanoTime();
            for (var result : builder.choice(100, new Random(1))) sink += result.get(0);
            long b = System.nanoTime();
            if (round >= 20) best = Math.min(best, (b - a) / 1000.0);
        }
        System.out.printf("choice(100) end to end: best %.1f us per 100 results (JMH before the change: 53.1 us)%n", best);
        if (sink == 42) System.out.println();                    // keep the JIT from removing the work
    }
}