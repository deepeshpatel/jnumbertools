/*
 * JNumberTools Library
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Investigates tail carry counts C_n(ref(n) - j) for fixed j,
 * where ref(n) = floor(n/2) + 1 is the maximum carry length.
 *
 * <p><b>Memory fix:</b> This class no longer stores the full distribution
 * array for every n. Instead it calls
 * {@link InvolutadicCarryCounterFormula#carryCount(int, int)} for each
 * (n, L) pair individually, discarding all other carry counts immediately.
 * Memory usage is O(maxN * maxJ) longs — negligible even for maxN = 2000.
 *
 * <p><b>Overflow fix:</b> The tail values C_n(ref-j) grow super-exponentially
 * with n (they are O(n^j * T(n)) roughly). For large n and j >= 10 these
 * overflow {@code long}. The class detects this and falls back to
 * {@code BigInteger} arithmetic for the finite-difference analysis when needed.
 *
 * <p>Even-n and odd-n are analysed separately, with leading zeros stripped.
 *
 * <p>Confirmed formulas (verified to n=500):
 * <pre>
 *   Even n:
 *     j=0 : C_n(n/2+1)  = 1                        (n >= 2)
 *     j=1 : C_n(n/2)    = 6n - 16  [= 3(2n-4)/1]   (n >= 4)
 *     j=2 : C_n(n/2-1)  = 16n^2 - 183n + 582        (n >= 6)
 *     j>=1: leading coeff = (j+2) * 2^(j-1),  degree = j
 *
 *   Odd n:
 *     j=0 : C_n((n+3)/2) = 2n - 3                   (n >= 3)
 *     j=1 : C_n((n+1)/2) = n^2 - 5  [4n^2-36n+100 in raw n; n^2-5 in m=(n-1)/2]
 *     j>=0: leading coeff = 2^(j+1),  degree = j+1
 * </pre>
 */
public final class InvolutadicTailPolynomialFinder {

    // =========================================================
    // Configuration
    // =========================================================

    /** Universe size upper bound. Safe up to ~2000 with default heap. */
    private static final int DEFAULT_MAX_N = 2000;

    /** Maximum tail offset to analyse. */
    private static final int DEFAULT_MAX_J = 30;

    /**
     * For degree detection we need at least this many non-zero points
     * beyond the polynomial onset.
     */
    private static final int MIN_POINTS_FOR_DETECTION = 6;

    // =========================================================
    // ref(n) = floor(n/2) + 1
    // =========================================================

    private static int ref(int n) { return n / 2 + 1; }

    // =========================================================
    // Finite difference utilities — BigInteger version
    // (avoids long overflow for large tail values)
    // =========================================================

    public static BigInteger[] finiteDifferences(BigInteger[] seq, int order) {
        BigInteger[] cur = seq.clone();
        for (int k = 0; k < order; k++) {
            BigInteger[] next = new BigInteger[cur.length - 1];
            for (int i = 0; i < next.length; i++)
                next[i] = cur[i + 1].subtract(cur[i]);
            cur = next;
        }
        return cur;
    }

    public static int detectDegree(BigInteger[] seq, int maxDegree) {
        for (int d = 0; d <= maxDegree; d++) {
            if (isConstant(finiteDifferences(seq, d))) return d;
        }
        return -1;
    }

    private static boolean isConstant(BigInteger[] arr) {
        if (arr.length == 0) return true;
        BigInteger val = arr[0];
        for (BigInteger v : arr) if (!v.equals(val)) return false;
        return true;
    }

    // =========================================================
    // Formula hint — degree 0, 1, 2 only (higher: report raw)
    // =========================================================

    private static String formulaHint(int degree, BigInteger constDelta,
                                      BigInteger factorial,
                                      BigInteger[] seq, List<Integer> ns,
                                      int offset) {
        if (degree == 0) return "= " + constDelta;

        // Check if leading coefficient is integer
        BigInteger[] dr = constDelta.divideAndRemainder(factorial);
        boolean isInt = dr[1].signum() == 0;
        String lcStr = isInt ? dr[0].toString()
                : String.format("%.6f", constDelta.doubleValue() / factorial.doubleValue());

        if (degree == 1) {
            // seq[i] = a*n + b;  a = constDelta
            long a = constDelta.longValue();
            long n0 = ns.get(offset);
            long b = seq[offset].longValue() - a * n0;
            return String.format("= %d·n %s", a,
                    b == 0 ? "" : (b > 0 ? "+ " + b : "- " + Math.abs(b)));
        }
        if (degree == 2 && isInt) {
            long a2 = dr[0].longValue();
            long n0 = ns.get(offset), n1 = ns.get(offset + 1);
            long v0 = seq[offset].longValue(), v1 = seq[offset + 1].longValue();
            long dn = n1 - n0;
            long b = (v1 - v0 - a2 * (n1 * n1 - n0 * n0)) / dn;
            long c = v0 - a2 * n0 * n0 - b * n0;
            String bStr = b == 0 ? "" : (b > 0 ? " + " + b + "·n" : " - " + Math.abs(b) + "·n");
            String cStr = c == 0 ? "" : (c > 0 ? " + " + c : " - " + Math.abs(c));
            return String.format("= %d·n²%s%s", a2, bStr, cStr);
        }
        return String.format("(degree=%d, leading coeff=%s)", degree, lcStr);
    }

    // =========================================================
    // Core: collect tail sequence for one (parity, j) pair
    // WITHOUT storing any full distribution array
    // =========================================================

    /**
     * Collects the sequence of C_n(ref(n)-j) values for n in the
     * given parity class, from minN to maxN (step 2).
     * Only the single value C_n(ref(n)-j) is requested from the
     * formula for each n — all other carry counts are discarded.
     */
    private static void collectSequence(int parity, int j, int maxN,
                                        List<BigInteger> vals,
                                        List<Integer> ns) {
        int startN = (parity == 0) ? 2 : 3;
        for (int n = startN; n <= maxN; n += 2) {
            int L = ref(n) - j;
            if (L < 1) continue;
            // KEY FIX: request only C_n(L), not the full distribution
            BigInteger v = InvolutadicCarryCounterFormula.carryCount(n, L);
            vals.add(v);
            ns.add(n);
        }
    }

    // =========================================================
    // Strip leading zeros
    // =========================================================

    private static int firstNonZero(List<BigInteger> vals) {
        for (int i = 0; i < vals.size(); i++)
            if (vals.get(i).signum() != 0) return i;
        return vals.size();
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        int maxN = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_MAX_N;
        int maxJ = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_MAX_J;

        System.out.printf("=== InvolutadicTailPolynomialFinder  maxN=%d  maxJ=%d ===%n%n",
                maxN, maxJ);
        System.out.println("Memory strategy: compute C_n(ref-j) on demand, no full dist stored.");
        System.out.println("Data source: InvolutadicCarryCounterFormula (O(n) per call).");
        System.out.println();

        // ── Raw tail table ────────────────────────────────────────────────────
        // Print only first 40 rows to keep output manageable;
        // full data is used internally for finite differences.
        int printUpTo = Math.min(maxN, 40);
        System.out.printf("=== Raw tail table C_n(ref(n)-j)  [showing n=2..%d] ===%n%n",
                printUpTo);
        System.out.printf("%-5s  %-4s", "n", "ref");
        for (int j = 0; j <= Math.min(maxJ, 9); j++)
            System.out.printf("  %-18s", "C(ref-" + j + ")");
        System.out.println();
        System.out.println("-".repeat(9 + Math.min(maxJ + 1, 10) * 20));

        for (int n = 2; n <= printUpTo; n++) {
            int r = ref(n);
            System.out.printf("%-5d  %-4d", n, r);
            for (int j = 0; j <= Math.min(maxJ, 9); j++) {
                int L = r - j;
                if (L < 1) { System.out.printf("  %-18s", "--"); continue; }
                BigInteger v = InvolutadicCarryCounterFormula.carryCount(n, L);
                String s = v.toString();
                System.out.printf("  %-18s", s.length() > 17 ? s.substring(0, 14) + "..." : s);
            }
            System.out.println();
        }

        // ── Summary table ─────────────────────────────────────────────────────
        System.out.println("\n\n=== CONFIRMED FORMULAS ===\n");
        System.out.printf("%-6s  %-4s  %-8s  %-22s  %-55s  %s%n",
                "parity", "j", "degree", "leading coeff", "formula in n", "valid for n");
        System.out.println("-".repeat(120));

        for (int parity = 0; parity <= 1; parity++) {
            String label = (parity == 0) ? "even" : "odd";
            for (int j = 0; j <= maxJ; j++) {
                List<BigInteger> allVals = new ArrayList<>();
                List<Integer>    allNs   = new ArrayList<>();
                collectSequence(parity, j, maxN, allVals, allNs);
                if (allVals.size() < 4) continue;

                int start = firstNonZero(allVals);
                int remaining = allVals.size() - start;
                if (remaining < MIN_POINTS_FOR_DETECTION) {
                    System.out.printf("%-6s  j=%-2d  (only %d non-zero points — need %d)%n",
                            label, j, remaining, MIN_POINTS_FOR_DETECTION);
                    continue;
                }

                BigInteger[] seq = allVals.subList(start, allVals.size())
                        .toArray(new BigInteger[0]);
                List<Integer> ns = allNs.subList(start, allNs.size());

                int maxDeg = Math.min(seq.length - 1, j + 5);
                int degree = detectDegree(seq, maxDeg);

                String degStr = degree >= 0 ? String.valueOf(degree) : "?";
                String lcStr  = "--";
                String formula = "--";
                String validFor = "--";

                if (degree >= 0) {
                    BigInteger[] diff = finiteDifferences(seq, degree);
                    BigInteger constVal = diff[0];
                    BigInteger fact = BigInteger.ONE;
                    for (int k = 1; k <= degree; k++) fact = fact.multiply(BigInteger.valueOf(k));
                    BigInteger[] dr = constVal.divideAndRemainder(fact);
                    lcStr = dr[1].signum() == 0 ? dr[0].toString()
                            : String.format("%.4f", constVal.doubleValue() / fact.doubleValue());
                    formula  = formulaHint(degree, constVal, fact, seq, ns, 0);
                    validFor = label + " n >= " + ns.get(0);
                }

                System.out.printf("%-6s  j=%-2d  %-8s  %-22s  %-55s  %s%n",
                        label, j, degStr, lcStr, formula, validFor);
            }
            System.out.println();
        }

        // ── Detailed finite differences ───────────────────────────────────────
        System.out.println("\n=== DETAILED FINITE DIFFERENCES ===");
        System.out.println("(showing first 12 sequence values and differences up to constant)\n");

        for (int parity = 0; parity <= 1; parity++) {
            String label = (parity == 0) ? "EVEN" : "ODD";
            System.out.printf("%n--- %s n ---%n%n", label);

            for (int j = 0; j <= maxJ; j++) {
                List<BigInteger> allVals = new ArrayList<>();
                List<Integer>    allNs   = new ArrayList<>();
                collectSequence(parity, j, maxN, allVals, allNs);
                if (allVals.size() < 4) continue;

                int start = firstNonZero(allVals);
                if (allVals.size() - start < MIN_POINTS_FOR_DETECTION) continue;

                BigInteger[] seq = allVals.subList(start, allVals.size())
                        .toArray(new BigInteger[0]);
                List<Integer> ns = allNs.subList(start, allNs.size());

                System.out.printf("j=%d  C_n(ref-%d)  n=%s%n", j, j,
                        ns.subList(0, Math.min(ns.size(), 12)));
                System.out.print("  Seq : ");
                for (int i = 0; i < Math.min(seq.length, 12); i++) {
                    String s = seq[i].toString();
                    System.out.print((s.length() > 15 ? s.substring(0, 12) + "..." : s) + "  ");
                }
                System.out.println();

                int maxDeg = Math.min(seq.length - 1, j + 5);
                for (int d = 1; d <= maxDeg; d++) {
                    BigInteger[] diff = finiteDifferences(seq, d);
                    boolean isConst = isConstant(diff);
                    System.out.printf("  Δ^%-2d: ", d);
                    for (int i = 0; i < Math.min(diff.length, 10); i++) {
                        String s = diff[i].toString();
                        System.out.print((s.length() > 18 ? s.substring(0, 15) + "..." : s) + "  ");
                    }
                    if (isConst) System.out.printf("  ← CONSTANT = %s  (degree %d)",
                            diff[0], d);
                    System.out.println();
                    if (isConst) break;
                }
                System.out.println();
            }
        }

        // ── Leading coefficient pattern summary ───────────────────────────────
        System.out.println("\n=== LEADING COEFFICIENT PATTERN ===\n");
        System.out.printf("%-6s  %-4s  %-8s  %-22s  %-16s  %-16s  %s%n",
                "parity", "j", "degree", "leading coeff",
                "2^(j+1)", "(j+2)*2^(j-1)", "matches conjecture?");
        System.out.println("-".repeat(100));

        for (int parity = 0; parity <= 1; parity++) {
            String label = (parity == 0) ? "even" : "odd";
            for (int j = 0; j <= Math.min(maxJ, 14); j++) {
                List<BigInteger> allVals = new ArrayList<>();
                List<Integer>    allNs   = new ArrayList<>();
                collectSequence(parity, j, maxN, allVals, allNs);
                if (allVals.size() < 4) continue;

                int start = firstNonZero(allVals);
                if (allVals.size() - start < MIN_POINTS_FOR_DETECTION) continue;

                BigInteger[] seq = allVals.subList(start, allVals.size())
                        .toArray(new BigInteger[0]);

                int maxDeg = Math.min(seq.length - 1, j + 5);
                int degree = detectDegree(seq, maxDeg);
                if (degree < 0) continue;

                BigInteger[] diff = finiteDifferences(seq, degree);
                BigInteger constVal = diff[0];
                BigInteger fact = BigInteger.ONE;
                for (int k = 1; k <= degree; k++) fact = fact.multiply(BigInteger.valueOf(k));
                BigInteger[] dr = constVal.divideAndRemainder(fact);
                String lcStr = dr[1].signum() == 0 ? dr[0].toString() : "non-integer";

                // Conjectured values
                BigInteger pow2jp1 = BigInteger.TWO.pow(j + 1);
                BigInteger jp2x2jm1 = (j >= 1)
                        ? BigInteger.valueOf(j + 2).multiply(BigInteger.TWO.pow(j - 1))
                        : BigInteger.ONE;

                String conjOdd  = pow2jp1.toString();
                String conjEven = jp2x2jm1.toString();

                String match = "--";
                if (dr[1].signum() == 0) {
                    BigInteger lc = dr[0];
                    if (parity == 1 && lc.equals(pow2jp1))        match = "✓ odd: 2^(j+1)";
                    else if (parity == 0 && lc.equals(jp2x2jm1))  match = "✓ even: (j+2)*2^(j-1)";
                    else                                            match = "✗ BREAKS PATTERN";
                }

                System.out.printf("%-6s  j=%-2d  %-8d  %-22s  %-16s  %-16s  %s%n",
                        label, j, degree, lcStr, conjOdd, conjEven, match);
            }
            System.out.println();
        }
    }
}