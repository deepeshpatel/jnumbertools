/*
JNumberTools Library v3.0.2
Copyright (c) 2025 Deepesh Patel
*/
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * High-precision analysis engine for the Involutadic carry constant ℐ.
 * <p>
 * Features:
 * <ul>
 *   <li>Exact E[L] computation with 50 decimal places</li>
 *   <li>Parity-separated Aitken Δ² extrapolation</li>
 *   <li>Midpoint averaging (even/odd cancellation)</li>
 *   <li>Tail polynomial extraction (t = n - L)</li>
 *   <li>Robust rational Lagrange interpolation</li>
 *   <li>PSLQ-ready output</li>
 * </ul>
 */
public final class InvolutadicCarryAnalysis {

    private static final int DECIMAL_SCALE = 50;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public static void main(String[] args) {
        InvolutadicCarryCounterDP counter = new InvolutadicCarryCounterDP();
        int maxN = 30;

        System.out.println("=== Involutadic Carry Constant ℐ Analysis ===");
        System.out.printf("Precision: %d decimal places%n", DECIMAL_SCALE);
        System.out.println("Validation: Σ counts == T(n) - 1\n");

        List<ExactMean> evenMeans = new ArrayList<>();
        List<ExactMean> oddMeans = new ArrayList<>();

        // tailCounts[t][n] = count of carry length L = n - t
        Map<Integer, Map<Integer, BigInteger>> tailCounts = new HashMap<>();

        for (int n = 2; n <= maxN; n++) {
            long t0 = System.currentTimeMillis();
            BigInteger[] dist = counter.carryDistribution(n);
            long elapsed = System.currentTimeMillis() - t0;

            BigInteger totalInv = telephone(n);
            BigInteger sumCounts = BigInteger.ZERO;
            BigInteger weightedSum = BigInteger.ZERO;

            for (int L = 1; L <= n; L++) {
                BigInteger cnt = dist[L];
                sumCounts = sumCounts.add(cnt);
                weightedSum = weightedSum.add(cnt.multiply(BigInteger.valueOf(L)));

                int t = n - L;
                if (t <= 5) {  // Collect up to t=5
                    tailCounts.computeIfAbsent(t, k -> new HashMap<>()).put(n, cnt);
                }
            }

            BigInteger expected = totalInv.subtract(BigInteger.ONE);
            boolean valid = sumCounts.equals(expected);

            BigDecimal meanBD = new BigDecimal(weightedSum)
                    .divide(new BigDecimal(totalInv), DECIMAL_SCALE + 10, RM)
                    .setScale(DECIMAL_SCALE, RM);

            System.out.printf("--- n=%2d (T(n)=%,d) | %d ms %s ---%n",
                    n, totalInv, elapsed, valid ? "✓" : "✗");
            System.out.printf("  E[L] = %s%n", meanBD);

            ExactMean em = new ExactMean(n, weightedSum, totalInv, meanBD);
            if (n % 2 == 0) evenMeans.add(em);
            else oddMeans.add(em);

            // Print small tail counts
            for (int t = 0; t <= 5; t++) {
                var map = tailCounts.get(t);
                if (map != null && map.containsKey(n)) {
                    System.out.printf("  t=%d → C(n=%d, L=%d) = %,d%n", t, n, n - t, map.get(n));
                }
            }
            System.out.println();
        }

        // === Extrapolations ===
        System.out.println("=== Parity-Separated Aitken Δ² Extrapolation ===");
        extrapolateAndPrint("Even", evenMeans);
        extrapolateAndPrint("Odd ", oddMeans);

        System.out.println("\n=== Midpoint Averaging (Even/Odd) ===");
        midpointExtrapolation(evenMeans, oddMeans);

        // === Tail Polynomials ===
        System.out.println("\n=== Tail Polynomial Extraction (t = n - L) ===");
        for (int t = 0; t <= 5; t++) {
            fitAndPrintPolynomial(t, tailCounts.get(t));
        }

        // === Final Estimate ===
        System.out.println("\n=== PSLQ-Ready Final Estimate ===");
        BigDecimal finalEst = computeFinalEstimate(evenMeans, oddMeans);
        System.out.printf("I ≈ %s%n", finalEst);
        System.out.println("Suggested PSLQ basis: [1, E, E², log(2), π, √2, Catalan, EulerGamma]");
    }

    // ===================================================================
    // Data Holder
    // ===================================================================
    private static class ExactMean {
        final int n;
        final BigInteger num;
        final BigInteger den;
        final BigDecimal decimal;

        ExactMean(int n, BigInteger num, BigInteger den, BigDecimal dec) {
            this.n = n;
            this.num = num;
            this.den = den;
            this.decimal = dec;
        }
    }

    // ===================================================================
    // Extrapolation Methods
    // ===================================================================
    private static void extrapolateAndPrint(String label, List<ExactMean> means) {
        if (means.size() < 3) {
            System.out.printf("%s: Not enough data for Aitken%n", label);
            return;
        }

        ExactMean en = means.get(means.size() - 1);
        ExactMean enm1 = means.get(means.size() - 2);
        ExactMean enm2 = means.get(means.size() - 3);

        BigDecimal En   = en.decimal;
        BigDecimal Enm1 = enm1.decimal;
        BigDecimal Enm2 = enm2.decimal;

        BigDecimal d1 = En.subtract(Enm1);
        BigDecimal d2 = Enm1.subtract(Enm2);

        if (d2.compareTo(BigDecimal.ZERO) == 0) {
            System.out.printf("%s (n=%d): Aitken Δ² = %s (Δ²E=0)%n", label, en.n, En);
            return;
        }

        BigDecimal acc = En.subtract(d1.multiply(d1).divide(d2, DECIMAL_SCALE + 15, RM));
        System.out.printf("%s (n=%d): Aitken Δ² ≈ %s%n", label, en.n, acc.setScale(DECIMAL_SCALE, RM));
    }

    private static void midpointExtrapolation(List<ExactMean> even, List<ExactMean> odd) {
        if (even.size() < 2 || odd.size() < 2) return;

        System.out.println("Last few midpoints:");
        List<BigDecimal> mids = new ArrayList<>();

        for (int i = 0; i < Math.min(even.size(), odd.size()); i++) {
            ExactMean em = even.get(even.size() - 1 - i);
            ExactMean om = odd.get(odd.size() - 1 - i);
            if (om.n == em.n + 1) {
                BigDecimal mid = em.decimal.add(om.decimal)
                        .divide(BigDecimal.valueOf(2), DECIMAL_SCALE + 10, RM);
                mids.add(mid);
                System.out.printf("  n=%d/%d midpoint = %s%n", em.n, om.n, mid.setScale(DECIMAL_SCALE, RM));
            }
        }

        if (mids.size() >= 3) {
            BigDecimal m2 = mids.get(mids.size() - 1);
            BigDecimal m1 = mids.get(mids.size() - 2);
            BigDecimal m0 = mids.get(mids.size() - 3);

            BigDecimal d1 = m2.subtract(m1);
            BigDecimal d2 = m1.subtract(m0);

            if (d2.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal acc = m2.subtract(d1.multiply(d1).divide(d2, DECIMAL_SCALE + 15, RM));
                System.out.printf("  Midpoint Aitken Δ² ≈ %s%n", acc.setScale(DECIMAL_SCALE, RM));
            }
        }
    }

    private static BigDecimal computeFinalEstimate(List<ExactMean> even, List<ExactMean> odd) {
        if (even.size() >= 3 && odd.size() >= 3) {
            List<BigDecimal> mids = new ArrayList<>();
            for (int i = even.size() - 3; i < even.size(); i++) {
                ExactMean em = even.get(i);
                for (ExactMean om : odd) {
                    if (om.n == em.n + 1) {
                        mids.add(em.decimal.add(om.decimal)
                                .divide(BigDecimal.valueOf(2), DECIMAL_SCALE + 20, RM));
                        break;
                    }
                }
            }
            if (mids.size() == 3) {
                BigDecimal d1 = mids.get(2).subtract(mids.get(1));
                BigDecimal d2 = mids.get(1).subtract(mids.get(0));
                if (d2.compareTo(BigDecimal.ZERO) != 0) {
                    return mids.get(2).subtract(d1.multiply(d1).divide(d2, DECIMAL_SCALE + 20, RM))
                            .setScale(DECIMAL_SCALE, RM);
                }
            }
        }
        // Fallback
        if (!even.isEmpty() && !odd.isEmpty()) {
            return even.get(even.size() - 1).decimal.add(odd.get(odd.size() - 1).decimal)
                    .divide(BigDecimal.valueOf(2), DECIMAL_SCALE, RM);
        }
        return BigDecimal.ZERO;
    }

    // ===================================================================
    // Tail Polynomial Fitting
    // ===================================================================
    private static void fitAndPrintPolynomial(int t, Map<Integer, BigInteger> data) {
        if (data == null || data.size() < 3) {
            System.out.printf("t=%d: Insufficient data%n", t);
            return;
        }

        List<Point> evenPts = new ArrayList<>();
        List<Point> oddPts = new ArrayList<>();

        for (var e : data.entrySet()) {
            int n = e.getKey();
            BigInteger cnt = e.getValue();
            int m = n / 2;
            if (n % 2 == 0)
                evenPts.add(new Point(m, cnt));
            else
                oddPts.add(new Point(m, cnt));
        }

        System.out.printf("t=%d (Expected deg: even≈%d, odd≈%d):%n", t, t, t + 1);

        if (evenPts.size() >= t + 1) {
            Polynomial p = interpolate(evenPts.subList(0, t + 1));
            System.out.printf("  Even (n=2m): %s%n", p);
        }
        if (oddPts.size() >= t + 2) {
            Polynomial p = interpolate(oddPts.subList(0, t + 2));
            System.out.printf("  Odd  (n=2m+1): %s%n", p);
        }
        System.out.println();
    }

    private static class Point {
        final int x;
        final BigInteger y;
        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    /** Robust Lagrange interpolation returning integer coefficient polynomial */
    private static Polynomial interpolate(List<Point> points) {
        int deg = points.size() - 1;
        BigInteger[] coeffs = new BigInteger[deg + 1];
        Arrays.fill(coeffs, BigInteger.ZERO);

        for (int i = 0; i <= deg; i++) {
            Point pi = points.get(i);
            BigInteger[] basis = {BigInteger.ONE};
            BigInteger denom = BigInteger.ONE;

            for (int j = 0; j <= deg; j++) {
                if (i == j) continue;
                Point pj = points.get(j);

                // Multiply by (x - pj.x)
                BigInteger[] temp = new BigInteger[basis.length + 1];
                Arrays.fill(temp, BigInteger.ZERO);

                for (int k = 0; k < basis.length; k++) {
                    // x term
                    temp[k] = temp[k].add(basis[k].multiply(BigInteger.valueOf(-pj.x)));
                    // constant term (shift)
                    temp[k + 1] = temp[k + 1].add(basis[k]);
                }
                basis = temp;
                denom = denom.multiply(BigInteger.valueOf(pi.x - pj.x));
            }

            // Multiply by y_i / denom
            if (!denom.equals(BigInteger.ZERO)) {
                BigInteger factor = pi.y.divide(denom);   // Must be exact
                for (int k = 0; k < basis.length; k++) {
                    coeffs[k] = coeffs[k].add(basis[k].multiply(factor));
                }
            }
        }

        return new Polynomial(coeffs);
    }

    private static class Polynomial {
        final BigInteger[] coeffs;

        Polynomial(BigInteger[] c) {
            int deg = c.length - 1;
            while (deg > 0 && c[deg].equals(BigInteger.ZERO)) deg--;
            this.coeffs = Arrays.copyOf(c, deg + 1);
        }

        @Override
        public String toString() {
            if (coeffs.length == 0) return "0";
            StringBuilder sb = new StringBuilder();
            for (int i = coeffs.length - 1; i >= 0; i--) {
                BigInteger c = coeffs[i];
                if (c.equals(BigInteger.ZERO)) continue;

                if (sb.length() > 0) {
                    sb.append(c.signum() > 0 ? " + " : " - ");
                } else if (c.signum() < 0) {
                    sb.append("-");
                }

                BigInteger ac = c.abs();
                if (!ac.equals(BigInteger.ONE) || i == 0) {
                    sb.append(ac);
                }
                if (i > 0) {
                    if (ac.equals(BigInteger.ONE) && i != 0) {
                        // do nothing extra
                    } else {
                        sb.append("*");
                    }
                    sb.append("m");
                    if (i > 1) sb.append("^").append(i);
                }
            }
            return sb.toString();
        }
    }

    // ===================================================================
    // Telephone Cache
    // ===================================================================
    private static BigInteger[] TELEPHONE_CACHE = new BigInteger[64];
    static {
        TELEPHONE_CACHE[0] = BigInteger.ONE;
        TELEPHONE_CACHE[1] = BigInteger.ONE;
        for (int i = 2; i < TELEPHONE_CACHE.length; i++) {
            TELEPHONE_CACHE[i] = TELEPHONE_CACHE[i - 1]
                    .add(TELEPHONE_CACHE[i - 2].multiply(BigInteger.valueOf(i - 1)));
        }
    }

    private static BigInteger telephone(int n) {
        if (n >= TELEPHONE_CACHE.length) {
            int old = TELEPHONE_CACHE.length;
            TELEPHONE_CACHE = Arrays.copyOf(TELEPHONE_CACHE, n + 10);
            for (int i = old; i < TELEPHONE_CACHE.length; i++) {
                TELEPHONE_CACHE[i] = TELEPHONE_CACHE[i - 1]
                        .add(TELEPHONE_CACHE[i - 2].multiply(BigInteger.valueOf(i - 1)));
            }
        }
        return TELEPHONE_CACHE[n];
    }
}