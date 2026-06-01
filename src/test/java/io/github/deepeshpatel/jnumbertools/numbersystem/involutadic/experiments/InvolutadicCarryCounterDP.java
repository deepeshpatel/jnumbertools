/*
JNumberTools Library v3.0.2
Copyright (c) 2025 Deepesh Patel
*/
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.*;

/**
 * Memory-optimized DP counter for Involutadic carry lengths.
 *
 * <p>Results are cached in a flat text file (one line per n):
 * <pre>
 *   n L=1_count L=2_count ... L=n_count
 * </pre>
 * On construction the file is read; only missing n-values are computed
 * and then appended. This avoids recomputing expensive large-n results.
 *
 * <p>Usage:
 * <pre>
 *   var counter = new InvolutadicCarryCounterDP();          // default file
 *   var counter = new InvolutadicCarryCounterDP(myPath);    // custom file
 *   BigInteger[] dist = counter.carryDistribution(18);      // dist[L] = count
 * </pre>
 *
 * <p>Command-line:
 * <pre>
 *   java InvolutadicCarryCounterDP [maxN] [cacheFile]
 * </pre>
 */
public final class InvolutadicCarryCounterDP {

    // ===================================================================
    // Constants
    // ===================================================================

    private static final String DEFAULT_FILE = "resources/cached/involutadic-carry-counts.txt";

    // ===================================================================
    // State
    // ===================================================================

    private final Path filePath;

    /** n -> dist array of length n+1; dist[0] unused; dist[L] = carry count for length L */
    private final TreeMap<Integer, BigInteger[]> cache = new TreeMap<>();

    // ===================================================================
    // Constructors
    // ===================================================================

    public InvolutadicCarryCounterDP() {
        this(Paths.get(DEFAULT_FILE));
    }

    public InvolutadicCarryCounterDP(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
        loadFromFile();
    }

    // ===================================================================
    // Public API
    // ===================================================================

    /**
     * Returns the carry-length distribution for order n.
     * Reads from cache if available; computes and saves otherwise.
     *
     * @param n order (n >= 2)
     * @return array of length n+1 where dist[L] = count of increments with carry length L
     */
    public BigInteger[] carryDistribution(int n) {
        if (!cache.containsKey(n)) computeAndSave(n);
        return cache.get(n);
    }

    /**
     * Returns C_n(L): the number of increments with carry length exactly L.
     */
    public BigInteger carryCount(int n, int L) {
        if (n < 1 || L < 1 || L > n) return BigInteger.ZERO;
        BigInteger[] dist = carryDistribution(n);
        return dist[L] != null ? dist[L] : BigInteger.ZERO;
    }

    /**
     * Returns the set of n-values currently in the cache.
     */
    public Set<Integer> cachedValues() {
        return Collections.unmodifiableSet(cache.keySet());
    }

    // ===================================================================
    // Cache I/O
    // ===================================================================

    /**
     * Loads all cached distributions from the file.
     * Format: one line per n, space-separated:
     *   n dist[1] dist[2] ... dist[n]
     */
    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            System.out.println("No cache file found at " + filePath.toAbsolutePath() + " — starting fresh.");
            return;
        }
        int loaded = 0;
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                int n = Integer.parseInt(parts[0]);
                if (parts.length != n + 1) {
                    System.err.printf("Warning: skipping malformed line for n=%d (expected %d tokens, got %d)%n",
                            n, n + 1, parts.length);
                    continue;
                }
                BigInteger[] dist = new BigInteger[n + 1];
                Arrays.fill(dist, BigInteger.ZERO);
                for (int L = 1; L <= n; L++) {
                    dist[L] = new BigInteger(parts[L]);
                }
                cache.put(n, dist);
                loaded++;
            }
        } catch (Exception e) {
            System.err.println("Warning: error reading cache file: " + e.getMessage());
        }
        if (loaded > 0) {
            System.out.printf("Loaded %d cached n-values from %s%n", loaded, filePath.toAbsolutePath());
            System.out.println("Cached n: " + cache.keySet());
        }
    }

    /**
     * Appends a single distribution line for n to the cache file.
     * Format: n dist[1] dist[2] ... dist[n]
     */
    private void appendToFile(int n, BigInteger[] dist) {
        try {
            Files.createDirectories(filePath.getParent() != null ? filePath.getParent() : Paths.get("."));
            try (BufferedWriter bw = Files.newBufferedWriter(filePath,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                StringBuilder sb = new StringBuilder();
                sb.append(n);
                for (int L = 1; L <= n; L++) {
                    sb.append(' ').append(dist[L] != null ? dist[L] : BigInteger.ZERO);
                }
                bw.write(sb.toString());
                System.out.println(sb);
                bw.newLine();
            }
            System.out.printf("  Saved n=%d to cache%n", n);
        } catch (IOException e) {
            System.err.println("Warning: could not write to cache file: " + e.getMessage());
        }
    }

    // ===================================================================
    // Computation
    // ===================================================================

    private void computeAndSave(int n) {
        System.out.printf("Computing n=%d ...%n", n);
        long t0 = System.currentTimeMillis();
        BigInteger[] dist = computeDistribution(n);
        long ms = System.currentTimeMillis() - t0;
        System.out.printf("  done in %,d ms%n", ms);
        cache.put(n, dist);
        appendToFile(n, dist);
    }

    private BigInteger[] computeDistribution(int n) {
        BigInteger[] dist = new BigInteger[n + 1];
        Arrays.fill(dist, BigInteger.ZERO);
        for (int L = 1; L <= n; L++) {
            dist[L] = computeCarryCount(n, L);
        }
        return dist;
    }

    private BigInteger computeCarryCount(int n, int L) {
        if (n < 1 || L < 1 || L > n) return BigInteger.ZERO;
        BigInteger total = BigInteger.ZERO;
        int minK = (n + 1) / 2;
        for (int K = minK; K <= n; K++) {
            total = total.add(countForLayer(n, L, K));
        }
        return total;
    }

    /**
     * Counts involutions with exactly K decisions whose digit array triggers
     * a carry of length L when incremented.
     *
     * Carry length L <=> pivot at LSD index p = L-1.
     * In MSD-first decision order (index s = 0..K-1):
     *   s > pivotS  => digit must be maximal (all-max suffix in LSD = leading in MSD)
     *   s == pivotS => digit must be strictly less than maximal
     *   s < pivotS  => digit unconstrained
     * where pivotS = K - 1 - p.
     */
    private BigInteger countForLayer(int n, int L, int K) {
        int p = L - 1;
        if (p >= K) return BigInteger.ZERO;

        int pivotS = K - 1 - p;

        BigInteger[] dp     = new BigInteger[1 << n];
        BigInteger[] nextDp = new BigInteger[1 << n];
        dp[0] = BigInteger.ONE;

        for (int s = 0; s < K; s++) {
            Arrays.fill(nextDp, null);
            boolean anyReachable = false;

            for (int mask = 0; mask < (1 << n); mask++) {
                BigInteger count = dp[mask];
                if (count == null || count.signum() == 0) continue;

                // First unplaced position
                int pos = Integer.numberOfTrailingZeros(~mask);
                if (pos >= n) continue;

                int remaining = n - Integer.bitCount(mask);
                int maxD = remaining - 1; // max digit at this step

                // Apply carry constraint
                int dStart = 0, dEnd = maxD;
                if (s > pivotS) {
                    dStart = dEnd = maxD;        // forced maximal
                } else if (s == pivotS) {
                    dEnd = maxD - 1;             // strictly less than max
                }
                // s < pivotS: unconstrained [0, maxD]

                if (dStart > dEnd) continue;

                // Branch: digit 0 = fixed point
                if (dStart == 0) {
                    int nm = mask | (1 << pos);
                    nextDp[nm] = (nextDp[nm] == null) ? count : nextDp[nm].add(count);
                    anyReachable = true;
                }

                // Branch: digits 1..dEnd = 2-cycles
                if (dEnd >= 1) {
                    int startD          = Math.max(dStart, 1);
                    int partnerIdxStart = startD - 1;   // 0-based partner index
                    int partnerIdxEnd   = dEnd - 1;

                    int idx = 0;
                    for (int partner = pos + 1; partner < n; partner++) {
                        if ((mask & (1 << partner)) == 0) {
                            if (idx >= partnerIdxStart && idx <= partnerIdxEnd) {
                                int nm = mask | (1 << pos) | (1 << partner);
                                nextDp[nm] = (nextDp[nm] == null) ? count : nextDp[nm].add(count);
                                anyReachable = true;
                            }
                            idx++;
                            if (idx > partnerIdxEnd) break; // early exit
                        }
                    }
                }
            }

            // Swap buffers
            BigInteger[] tmp = dp; dp = nextDp; nextDp = tmp;
            if (!anyReachable) break;
        }

        return dp[(1 << n) - 1] != null ? dp[(1 << n) - 1] : BigInteger.ZERO;
    }

    // ===================================================================
    // Telephone number (local, no external dependency)
    // ===================================================================

    private static BigInteger telephone(int n) {
        BigInteger[] t = new BigInteger[Math.max(n + 1, 2)];
        t[0] = BigInteger.ONE;
        t[1] = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            t[i] = t[i - 1].add(t[i - 2].multiply(BigInteger.valueOf(i - 1)));
        }
        return t[n];
    }

    // ===================================================================
    // Main
    // ===================================================================

    public static void main(String[] args) {
        int  maxN = args.length > 0 ? Integer.parseInt(args[0])  : 29;
        Path file = args.length > 1 ? Paths.get(args[1])         : Paths.get(DEFAULT_FILE);

        var counter = new InvolutadicCarryCounterDP(file);
        System.out.printf("%nTarget: n=2..%d | cache: %s%n%n", maxN, file.toAbsolutePath());

        // ── Phase 1: compute any missing n-values (even parity first, then odd) ──
        for (int parity = 0; parity <= 1; parity++) {
            int start = (parity == 0) ? 2 : 3;
            for (int n = start; n <= maxN; n += 2) {
                if (counter.cache.containsKey(n)) {
                    System.out.printf("n=%d already cached — skipping%n", n);
                } else {
                    try {
                        counter.computeAndSave(n);
                    } catch (OutOfMemoryError e) {
                        System.err.printf("OOM at n=%d. Try -Xmx28g or higher. Stopping.%n", n);
                        return;
                    }
                }
            }
        }

        // ── Phase 2: validate and report ──────────────────────────────────────────
        final int DECIMAL_SCALE = 40;
        boolean allOk = true;

        List<BigInteger> evenNums = new ArrayList<>(), evenDens = new ArrayList<>();
        List<BigInteger> oddNums  = new ArrayList<>(), oddDens  = new ArrayList<>();

        System.out.println("\n=== Involutadic Carry Counter DP — Validation & Summary ===\n");
        System.out.printf("Precision: %d decimal places%n", DECIMAL_SCALE);
        System.out.println("Validation: Σ C_n(L) == T(n) - 1\n");

        for (int n = 2; n <= maxN; n++) {
            BigInteger[] dist    = counter.carryDistribution(n);
            BigInteger   totalInv = telephone(n);
            BigInteger   sumCounts = BigInteger.ZERO;
            BigInteger   weightedSum = BigInteger.ZERO;

            System.out.printf("--- n=%2d (T(n)=%,d) ---%n", n, totalInv);

            for (int L = 1; L <= n; L++) {
                BigInteger cnt = (dist[L] != null) ? dist[L] : BigInteger.ZERO;
                sumCounts    = sumCounts.add(cnt);
                weightedSum  = weightedSum.add(cnt.multiply(BigInteger.valueOf(L)));
                if (cnt.signum() > 0) {
                    System.out.printf("  L=%2d : %,20d%n", L, cnt);
                }
            }

            BigInteger expected = totalInv.subtract(BigInteger.ONE);
            if (!sumCounts.equals(expected)) {
                System.err.printf("  ❌ FAIL: Σ counts=%s  expected=%s%n", sumCounts, expected);
                allOk = false;
            } else {
                BigDecimal mean = new BigDecimal(weightedSum)
                        .divide(new BigDecimal(totalInv), DECIMAL_SCALE, RoundingMode.HALF_UP);
                System.out.printf("  ✅ E[L] = %s%n", mean);
                if (n % 2 == 0) { evenNums.add(weightedSum); evenDens.add(totalInv); }
                else             { oddNums.add(weightedSum);  oddDens.add(totalInv);  }
            }
            System.out.println();
        }

        // ── Aitken Δ² extrapolation ───────────────────────────────────────────────
        if (evenNums.size() >= 3) {
            System.out.println("=== Aitken Δ² Extrapolation ===");
            extrapolate("Even", evenNums, evenDens, DECIMAL_SCALE);
            extrapolate("Odd ", oddNums,  oddDens,  DECIMAL_SCALE);
        }

        System.out.println(allOk ? "\n🟢 All validations passed." : "\n🔴 Some validations FAILED.");
    }

    private static void extrapolate(String label,
                                    List<BigInteger> nums,
                                    List<BigInteger> dens,
                                    int scale) {
        int sz = nums.size();
        if (sz < 3) return;

        BigDecimal a = ratio(nums.get(sz-3), dens.get(sz-3), scale);
        BigDecimal b = ratio(nums.get(sz-2), dens.get(sz-2), scale);
        BigDecimal c = ratio(nums.get(sz-1), dens.get(sz-1), scale);

        BigDecimal d1 = c.subtract(b);
        BigDecimal d2 = b.subtract(a);

        if (d2.compareTo(BigDecimal.ZERO) == 0) {
            System.out.printf("%s: Aitken Δ²E = 0, last value = %s%n",
                    label, c.setScale(scale, RoundingMode.HALF_UP));
            return;
        }
        BigDecimal acc = c.subtract(d1.multiply(d1).divide(d2, scale + 12, RoundingMode.HALF_UP));
        System.out.printf("%s: E_acc ≈ %s%n", label, acc.setScale(scale, RoundingMode.HALF_UP));
    }

    private static BigDecimal ratio(BigInteger num, BigInteger den, int scale) {
        return new BigDecimal(num).divide(new BigDecimal(den), scale + 12, RoundingMode.HALF_UP);
    }
}