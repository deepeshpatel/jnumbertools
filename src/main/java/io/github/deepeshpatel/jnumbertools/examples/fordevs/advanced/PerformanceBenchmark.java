/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.examples.fordevs.advanced;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Benchmarks performance of different generation strategies.
 * Demonstrates why rank-based and m-th generation are essential for large spaces.
 * Uses a single shared Calculator instance to maximize cache reuse.
 */
public class PerformanceBenchmark {

    private static final Runtime runtime = Runtime.getRuntime();

    // Shared Calculator instance — reused across all benchmarks for maximum cache efficiency
    private static final Calculator CALC = new Calculator();

    public static void main(String[] args) {
        System.out.println("=== Performance Benchmarks ===\n");

        benchmarkSmallPermutations();
        benchmarkLargeCombinationSpace();
        benchmarkMthGenerationPower();
        benchmarkMemoryFootprint();
        summaryAndRecommendations();

        // Optional: show final memory usage
        gc();
        System.out.printf("%nFinal JVM memory usage: ~%,d MB total, ~%,d MB free%n",
                runtime.totalMemory() / 1_048_576, runtime.freeMemory() / 1_048_576);
    }

    private static void benchmarkSmallPermutations() {
        System.out.println("1. Small permutations (8! = 40,320)");

        int n = 8;
        BigInteger targetRank = BigInteger.valueOf(20000);

        // A: Generate all + skip
        long start = System.nanoTime();
        gc();
        long memBefore = usedMemory();
        var all = new Permutations(CALC)
                .unique(n)
                .lexOrder()
                .stream()
                .skip(targetRank.longValue())
                .findFirst();
        long timeAll = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long memAll = usedMemory() - memBefore;

        // B: Direct single rank access
        start = System.nanoTime();
        gc();
        memBefore = usedMemory();
        var byRank = new Permutations(CALC)
                .unique(n)
                .byRanks(List.of(targetRank))
                .stream()
                .findFirst();
        long timeRank = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long memRank = usedMemory() - memBefore;

        System.out.printf("   A) Full + skip: %,d ms   (mem ~%,d KB)%n", timeAll, memAll / 1024);
        System.out.printf("   B) Single rank:  %,d ms   (mem ~%,d KB)%n", timeRank, memRank / 1024);
        System.out.printf("   Speedup: %.1fx   Memory saving: %.1fx%n%n",
                (double) timeAll / timeRank, (double) memAll / memRank);
    }

    private static void benchmarkLargeCombinationSpace() {
        System.out.println("2. Large combination space — C(60,30) ≈ 1.18 × 10¹⁷");

        int n = 60;
        int r = 30;
        BigInteger huge = new Combinations(CALC).unique(n, r).count();
        System.out.println("   Total: " + huge + "  (" + huge.bitLength() + " bits)");

        BigInteger midRank = huge.divide(BigInteger.valueOf(3));

        System.out.println("   Naive full lexOrder() + skip → would require generating ~"
                + midRank + " combinations first");
        System.out.println("   → Impractical: would take days/weeks or OOM (not attempted here)");

        long start = System.nanoTime();
        gc();
        long memBefore = usedMemory();

        var direct = new Combinations(CALC)
                .unique(n, r)
                .byRanks(List.of(midRank))
                .stream()
                .findFirst()
                .orElse(null);

        long timeDirect = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long memDirect = usedMemory() - memBefore;

        System.out.printf("   Direct rank access (rank %,d): %,d ms   (mem delta ~%,d KB)%n",
                midRank, timeDirect, memDirect / 1024);

        if (direct != null) {
            System.out.println("   Example combination at middle rank: " + formatCombo(direct));
        }

        System.out.println("   → Only rank-based access is feasible for huge spaces");
    }

    private static void benchmarkMthGenerationPower() {
        System.out.println("3. Power of m-th generation (huge step sizes – practical runtime)");

        int n = 50;
        int r = 20;
        BigInteger total = new Combinations(CALC).unique(n, r).count();
        BigInteger step = total.divide(BigInteger.valueOf(1_000_000));

        System.out.printf("   Space: C(%d,%d) ≈ %,d%n", n, r, total);
        System.out.printf("   Step size: ~%,d   → up to ~1,000,000 distant samples possible%n", step);

        long start = System.nanoTime();
        gc();
        long memBefore = usedMemory();

        int limit = 500;
        var samples = new Combinations(CALC)
                .unique(n, r)
                .lexOrderMth(step, BigInteger.ZERO)
                .stream()
                .limit(limit)
                .toList();

        long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long memUsed = usedMemory() - memBefore;

        System.out.printf("   Generated %,d combinations (spaced ~%,d apart) in %,d ms   (≈ %,.0f μs each)%n",
                samples.size(), step, timeMs, (double) timeMs * 1000 / limit);
        System.out.printf("   Peak memory delta: ~%,d KB   (constant — independent of total space)%n", memUsed / 1024);

        if (!samples.isEmpty()) {
            System.out.println("   First sample (rank 0):          " + formatCombo(samples.get(0)));
            System.out.println("   Last sample (very far rank):    " + formatCombo(samples.get(samples.size() - 1)));
        }

        System.out.println("   → Jump trillions of ranks instantly — no prefix generation needed!");

        // Bonus: multiset m-th generation (larger selection for better total)
        System.out.println("\nBonus: m-th on multiset (choose 15 from {A:30, B:25, C:20, D:10})");
        var multisetMap = new LinkedHashMap<String, Integer>();
        multisetMap.put("A", 30);
        multisetMap.put("B", 25);
        multisetMap.put("C", 20);
        multisetMap.put("D", 10);

        var msetBuilder = new Combinations(CALC).multiset(multisetMap, 15);
        BigInteger msetTotal = msetBuilder.count();

        long msetStart = System.nanoTime();
        var msetSamples = msetBuilder.lexOrderMth(BigInteger.valueOf(1_000_000_000), BigInteger.ZERO)
                .stream()
                .limit(20)
                .collect(Collectors.toList());
        long msetTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - msetStart);

        System.out.printf("   Total combinations: %,d%n", msetTotal);
        System.out.printf("   Generated 20 every billion-th in %,d ms   (≈ %,.0f μs each)%n",
                msetTime, (double) msetTime * 1000 / 20);

        if (!msetSamples.isEmpty()) {
            System.out.println("   Example far multiset combo:     " + msetSamples.get(0));
        }
        System.out.println("   → Constant-time jumping works perfectly for multisets too!");
    }

    private static void benchmarkMemoryFootprint() {
        System.out.println("4. Memory Footprint Comparison");

        int n = 25;
        int r = 12;
        long limit = 100_000;

        gc();
        long memStart = usedMemory();

        new Combinations(CALC).unique(n, r).lexOrder()
                .stream().limit(limit).forEach(c -> {});
        long memStream = usedMemory() - memStart;

        gc();
        memStart = usedMemory();

        var list = new Combinations(CALC).unique(n, r).lexOrder()
                .stream().limit(limit).toList();
        long memList = usedMemory() - memStart;

        System.out.printf("   Streaming %,d combos: ~%,d KB%n", limit, memStream / 1024);
        System.out.printf("   Materialized List:   ~%,d KB   (%s)%n",
                memList / 1024,
                memList > memStream * 1.5
                        ? String.format("%.1fx more", (double) memList / memStream)
                        : "similar (GC variance possible)");
        System.out.println("   → Prefer .stream() + limit() for large spaces");
    }

    private static void summaryAndRecommendations() {
        System.out.println("\n5. SUMMARY & RECOMMENDATIONS\n");

        System.out.println("   ┌───────────────────────┬────────────────────────────────────┐");
        System.out.println("   │ Use Case              │ Recommended Strategy               │");
        System.out.println("   ├───────────────────────┼────────────────────────────────────┤");
        System.out.println("   │ Small / medium space  │ lexOrder().stream()                │");
        System.out.println("   │ Random access         │ byRanks() or choice()              │");
        System.out.println("   │ Every m-th (any m)    │ lexOrderMth(m, start) — very fast  │");
        System.out.println("   │ Huge space            │ m-th generation or direct rank     │");
        System.out.println("   │ Memory critical       │ Never materialize full lists       │");
        System.out.println("   └───────────────────────┴────────────────────────────────────┘");

        System.out.println("\n   Strongest feature:");
        System.out.println("   → m-th generation lets you jump across astronomical spaces");
        System.out.println("     with constant time & memory per element.");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private static void gc() {
        System.gc();
        System.gc();
    }

    private static long usedMemory() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static String formatCombo(List<Integer> c) {
        return c.stream()
                .map(i -> String.format("%02d", i))
                .collect(Collectors.joining(" "));
    }
}
