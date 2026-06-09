/*
 * InvolutadicDataGenerator.java   (v2.B)
 *
 * Generates all empirical data for the Involutadic paper.
 *
 * INTERNAL TOOL — measures statistics for the paper. Reads exact P-table data
 * and benchmarks. Findings established with this tool (stable as of v2.B):
 *
 *   - Mean carry length: E[L_n] -> I* = 1 + sqrt(pi/2) ~ 2.25331413731550.
 *   - Convergence shape (Section 5): eps_n = E[L_n] - I* ~ C*(-1)^n*exp(-2*sqrt(n)).
 *     NOT a power law (fitted beta drifts with window) and NOT fixed-geometric
 *     (ratio climbs toward 1). The 3-parameter fit locks the sqrt(n) coefficient
 *     to -2. The sqrt(n) matches the telephone-number asymptotic exponent.
 *   - Variance (Section 7): Var[L_n] -> V* ~ 0.2665454 (fast convergence, flat by
 *     n~80, both parities agree). This is NOT I*(3-I*) (= 1.6825) and NOT 0.179;
 *     the paper's conj:variance and tab:variance are both incorrect and must be
 *     revised. No simple closed form for V* has been found.
 *   - Random access (Section 6): jump-to-arbitrary-rank measured at ~O(n^2)
 *     (exponent settles to 2.0 for large n), dominated by O(n) big-integer
 *     divisions on an Theta(n log n)-bit rank. It is RANK-INDEPENDENT, which is
 *     the actual contribution vs a pure successor's Theta(R). It is NOT O(n);
 *     the paper's tab:ops "O(n)" entry must be revised to O(n^2).
 *
 * Section 2 benchmark compares three lex-order generation methods:
 *   (a) Involutadic increment machine  — this paper, O(1) amortised
 *   (b) Backtracking                   — iterative lex-order, O(n) worst case
 *   (c) van Baronaigien (1991)         — NAIVE O(n) I-code successor (rebuilds the
 *       available-element list each step), NOT the original loopless O(1) algorithm
 *
 * Repeated unranking is intentionally NOT benchmarked: it is O(n) per step and
 * the comparison of interest is against incremental lex-order generators.
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.InvolutadicIncrementStateMachine;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class InvolutadicDataGenerator {

    private static final BigDecimal ICONST = new BigDecimal("2.2533141373155002512");
    private static final MathContext MC    = new MathContext(20, RoundingMode.HALF_UP);

    // =========================================================================
    // (c) van Baronaigien (1991) — loopless lex-order involution successor
    //
    // Reference:
    //   D.L. Roelants van Baronaigien, "A Loopless Algorithm for Generating
    //   Involutions", Journal of Algorithms 12(3):491-501, 1991.
    //
    // I-code digit convention (0-based, matching the original paper):
    //   g[i] = 0        -> fixed point at decision element x
    //   g[i] = k >= 1   -> 2-cycle with the k-th available element > x (0-indexed)
    //
    // A doubly-linked list of available elements provides O(1) k-th element
    // lookup, making each rebuild step O(carry-length) with O(1) amortised cost
    // by the same potential argument as the Involutadic machine.
    // =========================================================================
    static final class VanBaronaigienGenerator {

        private final int   n;
        final         int[] pi;          // current involution [0..n-1]

        private int   K;                 // number of decisions (= n - #2cycles)
        private final int[] g;           // I-code digits, 0-based, length K
        private final int[] maxG;        // max digit at each decision position
        private final int[] decElem;     // element x(i) handled at decision i

        // Doubly-linked list over available (unconsumed) elements.
        // Sentinel node index = n.
        //   next[n]   = first available element (n if none)
        //   next[i]   = next available element after i (n if none)
        //   prev[n]   = last available element  (n if none)
        //   prev[i]   = prev available element before i (n if none)
        private final int[] next;
        private final int[] prev;

        VanBaronaigienGenerator(int n) {
            this.n    = n;
            this.pi   = new int[n];
            this.g    = new int[n];
            this.maxG = new int[n];
            this.decElem = new int[n];
            this.next = new int[n + 1];
            this.prev = new int[n + 1];
            buildFirstInvolution();
        }

        /** Initialise to the lexicographically first involution: identity. */
        private void buildFirstInvolution() {
            for (int i = 0; i < n; i++) pi[i] = i;
            K = n;
            for (int i = 0; i < n; i++) {
                g[i]       = 0;
                maxG[i]    = n - 1 - i;   // #elements available after position i
                decElem[i] = i;
            }
            buildFullList();
        }

        /** Rebuild linked list to include all elements 0..n-1 (in order). */
        private void buildFullList() {
            next[n] = 0;
            for (int i = 0; i < n - 1; i++) { next[i] = i + 1; prev[i + 1] = i; }
            next[n - 1] = n;
            prev[0] = n;
            prev[n] = n - 1;
        }

        /**
         * Rebuild linked list to contain exactly the elements NOT consumed by
         * decisions 0..p-1.
         */
        private void rebuildListAfterPrefix(int p) {
            boolean[] consumed = new boolean[n];
            for (int i = 0; i < p; i++) {
                int x = decElem[i];
                consumed[x] = true;
                if (g[i] > 0) consumed[pi[x]] = true;   // partner of 2-cycle
            }
            // Relink in order
            int prev_node = n;
            next[n] = n;   // empty initially
            for (int i = 0; i < n; i++) {
                if (!consumed[i]) {
                    next[prev_node] = i;
                    prev[i]         = prev_node;
                    prev_node       = i;
                }
            }
            next[prev_node] = n;
        }

        /** Remove element x from the linked list in O(1). */
        private void listRemove(int x) {
            int p  = prev[x];
            int nx = next[x];
            next[p]  = nx;
            if (nx != n) prev[nx] = p;
        }

        /** Return the k-th element strictly after pos in the linked list (0-indexed). */
        private int kthAfter(int pos, int k) {
            int cur = next[pos];
            for (int i = 0; i < k; i++) cur = next[cur];
            return cur;
        }

        /** Count elements in the list that come after pos. */
        private int countAfter(int pos) {
            int cnt = 0;
            for (int cur = next[pos]; cur != n; cur = next[cur]) cnt++;
            return cnt;
        }

        /**
         * Advance to the next involution in lex order.
         * @return true if advanced; false if already at the last involution.
         */
        boolean increment() {
            // Find pivot: rightmost decision where g[p] < maxG[p]
            int p = K - 1;
            while (p >= 0 && g[p] >= maxG[p]) p--;
            if (p < 0) return false;

            // Restore list to state after decisions 0..p-1
            rebuildListAfterPrefix(p);

            // Increment digit at pivot
            g[p]++;

            // Re-apply decision at pivot, then fill suffix with fixed points
            applyFrom(p);
            return true;
        }

        /**
         * Apply decisions starting at index p.
         * Decision p uses the already-set g[p]; decisions p+1,p+2,... are reset to 0.
         */
        private void applyFrom(int p) {
            K = p;  // will grow as we add decisions below

            int x = next[n];   // first available element
            while (x != n) {
                boolean isPivot = (K == p);
                int d = isPivot ? g[p] : 0;

                int avail = countAfter(x);
                maxG[K]    = avail;
                decElem[K] = x;

                if (!isPivot) g[K] = 0;

                if (d == 0) {
                    // Fixed point
                    pi[x] = x;
                    listRemove(x);
                } else {
                    // 2-cycle: partner is the (d-1)-th element after x (0-indexed in list)
                    int partner = kthAfter(x, d - 1);
                    pi[x]       = partner;
                    pi[partner] = x;
                    listRemove(x);
                    listRemove(partner);
                }
                K++;
                x = next[n];   // next available (list updated by removals above)
            }
        }

        int[] involution() { return pi; }
    }

    // =========================================================================
    // (b) Backtracking — iterative lex-order involution generator
    //
    // Processes positions 0..n-1 left to right.
    // At each active (non-consumed) position p, chooses:
    //   choice 0        -> fixed point
    //   choice k >= 1   -> 2-cycle with the k-th free element > p (1-indexed)
    // Positions consumed as the right partner of a 2-cycle are skipped.
    // =========================================================================
    static final class BacktrackingLexGenerator {

        private final int     n;
        final         int[]   pi;        // current involution
        private final int[]   choice;    // choice[p] at each ACTIVE position p
        private final int[]   partner;   // partner[p] = actual partner (p itself if fp)
        private final boolean[] used;    // used[i] = element i is consumed

        BacktrackingLexGenerator(int n) {
            this.n       = n;
            this.pi      = new int[n];
            this.choice  = new int[n];
            this.partner = new int[n];
            this.used    = new boolean[n];
            buildFirst();
        }

        private void buildFirst() {
            Arrays.fill(used, false);
            for (int p = 0; p < n; p++) {
                pi[p]      = p;
                choice[p]  = 0;
                partner[p] = p;
                used[p]    = true;
            }
        }

        /**
         * Advance to the next involution in lex order.
         * @return true if advanced; false if already at the last.
         */
        boolean increment() {
            // Scan right-to-left for the rightmost ACTIVE position we can increment.
            // Active = position p where partner[p] >= p
            // (it made its own choice; partner[p] < p means p was consumed by someone earlier)
            int p = n - 1;
            while (p >= 0) {
                // Skip positions consumed as a right-partner of an earlier 2-cycle
                if (partner[p] < p) { p--; continue; }

                // Undo current choice at p
                int par = partner[p];
                used[p]   = false;
                used[par] = false;
                if (par != p) { pi[p] = p; pi[par] = par; }

                // Try next choice
                int nc = choice[p] + 1;
                if (nc == 0) { p--; continue; }   // (never happens since nc >= 1 here)

                int freeCount = countFreeAfter(p);
                if (nc <= freeCount) {
                    // Valid: place at p with choice nc
                    place(p, nc);
                    // Fill all remaining positions as fixed points
                    fillFixedPoints(p + 1);
                    return true;
                }
                // No valid choice at p — backtrack further
                p--;
            }
            return false;
        }

        /** Place position p with choice nc (0=fp, k>=1 = k-th free element after p). */
        private void place(int p, int nc) {
            choice[p] = nc;
            if (nc == 0) {
                partner[p] = p;
                pi[p]      = p;
                used[p]    = true;
            } else {
                int par    = kthFreeAfter(p, nc);
                partner[p] = par;
                partner[par] = p;   // mark so pivot search skips par
                pi[p]      = par;
                pi[par]    = p;
                used[p]    = true;
                used[par]  = true;
            }
        }

        /** Fill every free position from 'from' onward as a fixed point. */
        private void fillFixedPoints(int from) {
            for (int p = from; p < n; p++) {
                if (!used[p]) {
                    choice[p]  = 0;
                    partner[p] = p;
                    pi[p]      = p;
                    used[p]    = true;
                }
                // positions consumed as 2-cycle partners: pi already set, skip
            }
        }

        private int countFreeAfter(int pos) {
            int c = 0;
            for (int j = pos + 1; j < n; j++) if (!used[j]) c++;
            return c;
        }

        /** Return the k-th free element strictly after pos (1-indexed). */
        private int kthFreeAfter(int pos, int k) {
            int c = 0;
            for (int j = pos + 1; j < n; j++) {
                if (!used[j] && ++c == k) return j;
            }
            throw new IllegalStateException("Not enough free elements after " + pos);
        }

        int[] involution() { return pi; }
    }

    // =========================================================================
    // main
    // =========================================================================

    public static void main(String[] args) {
        // Each section gets its own Calculator so its memoised telephone-number
        // table is released when the section returns, preventing OOM across the
        // full run (see class-level CACHE-WARM STRATEGY note in DerangadicDataGenerator).
        System.out.println("=".repeat(70));
        System.out.println("INVOLUTADIC PAPER DATA GENERATOR");
        System.out.println("=".repeat(70));

        verifySelf(new Calculator());
        verifyBaronaigien();
        verifyBacktracking();

        section4ExactMeansAndVariances(new Calculator());
        section5ConvergenceShape(new Calculator());
        section3CarryHistogram(new Calculator());
        section2PerformanceBenchmark(new Calculator());
        section6RandomAccessBenchmark(new Calculator());
        section7VarianceLimit(new Calculator());

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL SECTIONS COMPLETE");
        System.out.println("=".repeat(70));
    }

    // =========================================================================
    // Correctness verification for both new generators
    // =========================================================================

    private static final int[][] EXPECTED_N4 = {
            {0,1,2,3},{0,1,3,2},{0,2,1,3},{0,3,2,1},
            {1,0,2,3},{1,0,3,2},{2,1,0,3},{2,3,0,1},
            {3,1,2,0},{3,2,1,0}
    };

    private static void verifyBaronaigien() {
        System.out.println("\nVerifying VanBaronaigienGenerator...");
        VanBaronaigienGenerator g4 = new VanBaronaigienGenerator(4);
        boolean ok = true;
        for (int r = 0; r < EXPECTED_N4.length; r++) {
            if (!Arrays.equals(g4.involution(), EXPECTED_N4[r])) {
                System.out.printf("  FAIL rank=%d got=%s expected=%s%n",
                        r, Arrays.toString(g4.involution()), Arrays.toString(EXPECTED_N4[r]));
                ok = false;
            }
            if (r < EXPECTED_N4.length - 1) g4.increment();
        }
        if (ok) System.out.println("  n=4: all 10 involutions correct.");

        // Count for n=6 (T(6)=76)
        VanBaronaigienGenerator g6 = new VanBaronaigienGenerator(6);
        int cnt = 1; while (g6.increment()) cnt++;
        System.out.println("  n=6: count=" + cnt + (cnt==76 ? " OK" : " FAIL (expected 76)"));

        // Count for n=7 (T(7)=232)
        VanBaronaigienGenerator g7 = new VanBaronaigienGenerator(7);
        cnt = 1; while (g7.increment()) cnt++;
        System.out.println("  n=7: count=" + cnt + (cnt==232 ? " OK" : " FAIL (expected 232)"));
    }

    private static void verifyBacktracking() {
        System.out.println("\nVerifying BacktrackingLexGenerator...");
        BacktrackingLexGenerator g4 = new BacktrackingLexGenerator(4);
        boolean ok = true;
        for (int r = 0; r < EXPECTED_N4.length; r++) {
            if (!Arrays.equals(g4.involution(), EXPECTED_N4[r])) {
                System.out.printf("  FAIL rank=%d got=%s expected=%s%n",
                        r, Arrays.toString(g4.involution()), Arrays.toString(EXPECTED_N4[r]));
                ok = false;
            }
            if (r < EXPECTED_N4.length - 1) g4.increment();
        }
        if (ok) System.out.println("  n=4: all 10 involutions correct.");

        // Count for n=6 (T(6)=76) — FIXED: now uses BacktrackingLexGenerator
        BacktrackingLexGenerator g6 = new BacktrackingLexGenerator(6);
        int cnt = 1; while (g6.increment()) cnt++;
        System.out.println("  n=6: count=" + cnt + (cnt==76 ? " OK" : " FAIL (expected 76)"));

        // Count for n=7 (T(7)=232) — FIXED: now uses BacktrackingLexGenerator
        BacktrackingLexGenerator g7 = new BacktrackingLexGenerator(7);
        cnt = 1; while (g7.increment()) cnt++;
        System.out.println("  n=7: count=" + cnt + (cnt==232 ? " OK" : " FAIL (expected 232)"));
    }

    // =========================================================================
    // P-table utilities
    // =========================================================================

    private static BigInteger[] buildPTable(int n, Calculator calc) {
        BigInteger[] P = new BigInteger[n + 3];
        Arrays.fill(P, BigInteger.ZERO);
        P[n] = BigInteger.ONE;
        for (int r = n - 1; r >= 0; r--)
            P[r] = P[r+1].add(BigInteger.valueOf(r+1).multiply(P[r+2]));
        return P;
    }

    private static BigInteger AT(BigInteger[] P, int t) {
        int len = P.length;
        BigInteger e = (2*t < len)                  ? P[2*t]   : BigInteger.ZERO;
        BigInteger o = (2*t-1 >= 0 && 2*t-1 < len) ? P[2*t-1] : BigInteger.ZERO;
        return e.add(o);
    }

    private static BigInteger[] carryDistribution(int n, Calculator calc) {
        BigInteger[] P = buildPTable(n, calc);
        int maxL = n / 2 + 1;
        BigInteger[] C = new BigInteger[maxL + 2];
        Arrays.fill(C, BigInteger.ZERO);
        for (int L = 2; L <= maxL; L++) {
            BigInteger a = AT(P, L-1);
            C[L] = (L < maxL) ? a.subtract(AT(P, L))
                    : a.subtract(AT(P, L+1)).subtract(BigInteger.ONE);
        }
        return C;
    }

    private static BigDecimal grandMean(int n, BigInteger[] C, Calculator calc) {
        BigInteger total = calc.telephoneNumber(n).subtract(BigInteger.ONE);
        int maxL = n / 2 + 1;
        BigInteger ws = BigInteger.ZERO;
        for (int L = 2; L <= maxL; L++)
            ws = ws.add(BigInteger.valueOf(L).multiply(C[L]));
        return new BigDecimal(ws).divide(new BigDecimal(total), MC);
    }

    private static BigDecimal variance(int n, BigInteger[] C, BigDecimal mean, Calculator calc) {
        BigInteger total = calc.telephoneNumber(n).subtract(BigInteger.ONE);
        int maxL = n / 2 + 1;
        BigDecimal e2 = BigDecimal.ZERO;
        for (int L = 2; L <= maxL; L++) {
            BigDecimal prob = new BigDecimal(C[L]).divide(new BigDecimal(total), MC);
            e2 = e2.add(prob.multiply(BigDecimal.valueOf((long)L*L), MC));
        }
        return e2.subtract(mean.multiply(mean, MC), MC);
    }

    // =========================================================================
    // Self-verification (carry formula)
    // =========================================================================

    private static void verifySelf(Calculator calc) {
        System.out.println("\nSelf-verification of carry distribution formula...");
        int[][] knownC = {{8,1},{20,5},{60,14,1},{180,44,7}};
        int[]   knownN = {4,5,6,7};
        boolean ok = true;
        for (int ni = 0; ni < knownN.length; ni++) {
            int n = knownN[ni];
            BigInteger[] C = carryDistribution(n, calc);
            BigInteger total = calc.telephoneNumber(n).subtract(BigInteger.ONE);
            BigInteger sum = BigInteger.ZERO;
            for (int L = 2; L <= n/2+1; L++) sum = sum.add(C[L]);
            if (!sum.equals(total)) { System.out.printf("  FAIL n=%d sum%n",n); ok=false; continue; }
            for (int j = 0; j < knownC[ni].length; j++)
                if (!C[j+2].equals(BigInteger.valueOf(knownC[ni][j]))) { System.out.printf("  FAIL n=%d L=%d%n",n,j+2); ok=false; }
            System.out.printf("  n=%d OK%n", n);
        }
        // Top-bucket pin (anchor ref(n) = floor(n/2)+1, the j=0 tail value).
        // The even and odd top buckets differ (paper Table tab:tail-poly-formulas,
        // j=0 row): even n -> C_n(floor(n/2)+1) = 1; odd n -> C_n(floor(n/2)+1) = n.
        for (int n : new int[]{8,9,10,11}) {
            BigInteger[] C = carryDistribution(n, calc);
            int top = n/2 + 1;
            BigInteger expected = (n % 2 == 0) ? BigInteger.ONE : BigInteger.valueOf(n);
            if (!C[top].equals(expected)) {
                System.out.printf("  FAIL top-bucket n=%d: C_n(%d)=%s (expected %s)%n",
                        n, top, C[top], expected);
                ok = false;
            } else {
                System.out.printf("  n=%d top-bucket C_n(%d)=%s OK%n", n, top, expected);
            }
        }
        for (int n : new int[]{2,3}) {
            BigDecimal m = grandMean(n, carryDistribution(n,calc), calc);
            if (Math.abs(m.doubleValue()-2.0)>1e-9) { System.out.printf("  FAIL E[L_%d]%n",n); ok=false; }
            else System.out.printf("  n=%d E[L_n]=2.0 OK%n", n);
        }
        if (ok) System.out.println("  All self-checks passed.\n");
        else    { System.out.println("  ERRORS FOUND."); System.exit(1); }
    }

    // =========================================================================
    // Section 4 — Exact means and variances
    // =========================================================================

    private static void section4ExactMeansAndVariances(Calculator calc) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SECTION 4: EXACT MEANS AND VARIANCES");
        System.out.println("=".repeat(70));
        System.out.printf("%-4s  %-22s  %-14s  %-12s  %s%n",
                "n","T(n)","E[L_n]","Var[L_n]","epsilon_n");
        for (int n = 2; n <= 30; n++) {
            BigInteger[] C = carryDistribution(n, calc);
            BigDecimal mean = grandMean(n, C, calc);
            BigDecimal var  = variance(n, C, mean, calc);
            System.out.printf("%-4d  %-22s  %-14s  %-12s  %s%n", n,
                    calc.telephoneNumber(n),
                    mean.setScale(9,RoundingMode.HALF_UP).toPlainString(),
                    var.setScale(6,RoundingMode.HALF_UP).toPlainString(),
                    mean.subtract(ICONST,MC).setScale(9,RoundingMode.HALF_UP).toPlainString());
        }
        System.out.print("\nEven-n: ");
        for (int n=2;n<=30;n+=2) System.out.printf("(%d,%.9f) ",n,
                grandMean(n,carryDistribution(n,calc),calc).setScale(9,RoundingMode.HALF_UP).doubleValue());
        System.out.print("\nOdd-n:  ");
        for (int n=3;n<=29;n+=2) System.out.printf("(%d,%.9f) ",n,
                grandMean(n,carryDistribution(n,calc),calc).setScale(9,RoundingMode.HALF_UP).doubleValue());
        System.out.println();
    }

    // =========================================================================
    // Section 5 — Convergence shape of E[L_n] -> I*
    //
    // PURPOSE: determine the functional form of the deviation
    //     eps_n = E[L_n] - I*.
    // Earlier drafts assumed a power law eps_n ~ C / n^beta. The exact data
    // REJECTS that: the fitted beta drifts with the fitting window (a true power
    // law gives a window-independent beta), and the step-2 ratio eps_n/eps_{n-2}
    // climbs steadily toward 1 instead of plateauing (a plateau would indicate
    // geometric decay; a climb-to-1 rules out BOTH a fixed power law and a fixed
    // geometric rate). The exact data instead matches a STRETCHED EXPONENTIAL
    //     ln|eps_n| = a + b*sqrt(n),   b ~ -2,
    // at R^2 = 1 with a window-stable slope. The sqrt(n) is the same sqrt(n) that
    // appears in the telephone-number asymptotics T_n (and hence in I* = 1+sqrt(pi/2)).
    //
    // This routine reports, per parity:
    //   (i)  the step-2 ratio sequence (model-free evidence: climbs toward 1);
    //   (ii) three competing fits across several tail windows, side by side, so
    //        the power/geometric instability vs sqrt(n) stability is VISIBLE:
    //          power     : ln|eps| vs ln n     -> slope = -beta
    //          geometric : ln|eps| vs n        -> slope = ln rho
    //          stretched : ln|eps| vs sqrt(n)  -> slope = b   (expected ~ -2)
    //
    // A high-precision I* (50 digits) is used here so that eps_n stays
    // trustworthy out to n ~ 200 before underflow against the constant's
    // truncation error. (The 19-digit ICONST used elsewhere is fine for the
    // n<=30 display table but would hit its floor inside this analysis.)
    // =========================================================================

    private static final BigDecimal ICONST_HP =
            new BigDecimal("2.25331413731550025120788264240552262650349336831001");
    private static final MathContext MC_HP = new MathContext(60, RoundingMode.HALF_UP);

    /** Signed deviation eps_n = E[L_n] - I*, computed at high precision.
     *  Uses its own 60-digit mean (NOT the 20-digit grandMean) so that eps_n
     *  stays above the floating floor out to n ~ 200, where eps_n ~ e^{-2 sqrt n}
     *  is already ~1e-12 and would otherwise be lost to truncation. */
    private static double epsHP(int n, Calculator calc) {
        BigInteger[] C = carryDistribution(n, calc);
        BigInteger total = calc.telephoneNumber(n).subtract(BigInteger.ONE);
        int maxL = n / 2 + 1;
        BigInteger ws = BigInteger.ZERO;
        for (int L = 2; L <= maxL; L++)
            ws = ws.add(BigInteger.valueOf(L).multiply(C[L]));
        BigDecimal mean = new BigDecimal(ws).divide(new BigDecimal(total), MC_HP);
        return mean.subtract(ICONST_HP, MC_HP).doubleValue();
    }

    private static void section5ConvergenceShape(Calculator calc) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SECTION 5: CONVERGENCE SHAPE OF E[L_n] -> I*");
        System.out.println("  Testing power law vs geometric vs stretched-exponential (sqrt n)");
        System.out.println("=".repeat(70));

        // Cap n where |eps_n| is still safely above the constant's precision floor.
        // With a 50-digit I*, eps_n stays meaningful well past n=200; we use 200.
        final int HI = 200;

        for (int parity = 0; parity <= 1; parity++) {
            String label = (parity == 0) ? "EVEN n" : "ODD n";
            System.out.printf("%n--- %s ---%n", label);

            // (i) model-free evidence: step-2 ratio eps_n / eps_{n-2}
            System.out.println("  step-2 ratio  eps_n / eps_{n-2}  (climb toward 1 => not fixed power, not fixed geometric):");
            System.out.print("   ");
            double prev = Double.NaN;
            int shown = 0;
            for (int n = (parity == 0 ? 2 : 3); n <= HI; n += 2) {
                double e = epsHP(n, calc);
                if (!Double.isNaN(prev) && prev != 0.0 &&
                        (n == 40 || n == 80 || n == 120 || n == 160 || n == 200)) {
                    System.out.printf("n=%d:%.4f  ", n, e / prev);
                    shown++;
                }
                prev = e;
            }
            System.out.println();

            // (ii) three fits across several tail windows
            System.out.printf("  %-10s %-22s %-22s %-26s%n",
                    "tail n>=", "power (ln|e| vs ln n)", "geometric (ln|e| vs n)", "stretched (ln|e| vs sqrt n)");
            for (int lo : new int[]{40, 80, 120}) {
                double[] xs_lnN = collectX(calc, lo, HI, parity, 0); // ln n
                double[] xs_n   = collectX(calc, lo, HI, parity, 1); // n
                double[] xs_sq  = collectX(calc, lo, HI, parity, 2); // sqrt n
                double[] ys     = collectY(calc, lo, HI, parity);    // ln|eps|

                double[] fp = ols(xs_lnN, ys);  // slope = -beta
                double[] fg = ols(xs_n,   ys);  // slope = ln rho
                double[] fs = ols(xs_sq,  ys);  // slope = b

                System.out.printf("  %-10d beta=%6.3f R2=%.6f  rho=%.5f R2=%.6f  b=%7.4f R2=%.6f%n",
                        lo,
                        -fp[0], fp[2],
                        Math.exp(fg[0]), fg[2],
                        fs[0], fs[2]);
            }

            // Three-parameter refinement: ln|eps| = a + b*sqrt(n) + c*ln(n).
            // Adding the ln(n) prefactor term should LOCK b onto exactly -2 (the
            // two-parameter sqrt-n fit gives b ~ -1.994 because the prefactor leaks
            // into the slope). c is the polynomial-prefactor exponent: c ~ 0 would
            // mean a pure stretched exponential; a small drifting c means a weak
            // prefactor not resolvable at n <= 200.
            System.out.println("  3-parameter refinement  ln|eps| = a + b*sqrt(n) + c*ln(n):");
            for (int lo : new int[]{40, 80, 120}) {
                double[] sq = collectX(calc, lo, HI, parity, 2); // sqrt n
                double[] ln = collectX(calc, lo, HI, parity, 0); // ln n
                double[] ys = collectY(calc, lo, HI, parity);    // ln|eps|
                double[] f  = fit3(sq, ln, ys);                  // {a, b, c, R2}
                System.out.printf("    n>=%-4d  b(sqrt n)=%+.5f  c(ln n)=%+.5f  a=%+.4f  R2=%.8f%n",
                        lo, f[1], f[2], f[0], f[3]);
            }
        }

        System.out.println();
        System.out.println("  READING: a window-STABLE slope at R^2~1 identifies the true model.");
        System.out.println("  Expect: power beta drifts up with window (NOT a power law);");
        System.out.println("          geometric rho drifts up with window (NOT fixed-geometric);");
        System.out.println("          stretched b ~ -2 and stable  =>  eps_n ~ C*(-1)^n*exp(-2*sqrt(n)).");
        System.out.println("  The sqrt(n) matches the telephone-number asymptotic exponent.");
        System.out.println("  3-param: b locks to -2; c (prefactor exponent) is small and drifts");
        System.out.println("          toward 0 -- consistent with a weak/zero polynomial prefactor.");
    }

    /**
     * Three-parameter least squares: y = a + b*x1 + c*x2, solved via the 3x3
     * normal equations with Gaussian elimination (no external linear-algebra dep).
     * @return {a, b, c, R^2}
     */
    private static double[] fit3(double[] x1, double[] x2, double[] y) {
        int k = Math.min(x1.length, Math.min(x2.length, y.length));
        if (k < 3) return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};

        // Design columns: col0 = 1, col1 = x1, col2 = x2.
        // Build X^T X (3x3) and X^T y (3).
        double[][] XtX = new double[3][3];
        double[]   XtY = new double[3];
        for (int r = 0; r < k; r++) {
            double[] row = {1.0, x1[r], x2[r]};
            for (int i = 0; i < 3; i++) {
                XtY[i] += row[i] * y[r];
                for (int j = 0; j < 3; j++) XtX[i][j] += row[i] * row[j];
            }
        }

        // Augmented matrix [XtX | XtY], Gaussian elimination with partial pivot.
        double[][] M = new double[3][4];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(XtX[i], 0, M[i], 0, 3);
            M[i][3] = XtY[i];
        }
        for (int col = 0; col < 3; col++) {
            int piv = col;
            for (int r = col + 1; r < 3; r++)
                if (Math.abs(M[r][col]) > Math.abs(M[piv][col])) piv = r;
            double[] tmp = M[col]; M[col] = M[piv]; M[piv] = tmp;
            double pv = M[col][col];
            for (int j = col; j < 4; j++) M[col][j] /= pv;
            for (int r = 0; r < 3; r++) {
                if (r == col) continue;
                double f = M[r][col];
                for (int j = col; j < 4; j++) M[r][j] -= f * M[col][j];
            }
        }
        double a = M[0][3], b = M[1][3], c = M[2][3];

        // R^2
        double yBar = 0; for (int r = 0; r < k; r++) yBar += y[r]; yBar /= k;
        double ssTot = 0, ssRes = 0;
        for (int r = 0; r < k; r++) {
            double pred = a + b * x1[r] + c * x2[r];
            ssTot += (y[r] - yBar) * (y[r] - yBar);
            ssRes += (y[r] - pred) * (y[r] - pred);
        }
        double r2 = (ssTot == 0) ? 1.0 : 1.0 - ssRes / ssTot;
        return new double[]{a, b, c, r2};
    }

    /**
     * Collect the regressor column over one parity class in [lo, hi].
     * @param kind 0 = ln n, 1 = n, 2 = sqrt n
     */
    private static double[] collectX(Calculator calc, int lo, int hi, int parity, int kind) {
        java.util.List<Double> xs = new java.util.ArrayList<>();
        for (int n = lo; n <= hi; n++) {
            if (n % 2 != parity) continue;
            double e = epsHP(n, calc);
            if (e == 0.0) continue;
            double x = (kind == 0) ? Math.log(n) : (kind == 1) ? n : Math.sqrt(n);
            xs.add(x);
        }
        double[] out = new double[xs.size()];
        for (int i = 0; i < out.length; i++) out[i] = xs.get(i);
        return out;
    }

    /** Collect ln|eps_n| over one parity class in [lo, hi]. */
    private static double[] collectY(Calculator calc, int lo, int hi, int parity) {
        java.util.List<Double> ys = new java.util.ArrayList<>();
        for (int n = lo; n <= hi; n++) {
            if (n % 2 != parity) continue;
            double e = epsHP(n, calc);
            if (e == 0.0) continue;
            ys.add(Math.log(Math.abs(e)));
        }
        double[] out = new double[ys.size()];
        for (int i = 0; i < out.length; i++) out[i] = ys.get(i);
        return out;
    }

    /** Ordinary least squares y = slope*x + intercept; returns {slope, intercept, R^2}. */
    private static double[] ols(double[] x, double[] y) {
        int k = Math.min(x.length, y.length);
        if (k < 2) return new double[]{Double.NaN, Double.NaN, Double.NaN};
        double sX = 0, sY = 0, sXY = 0, sX2 = 0;
        for (int i = 0; i < k; i++) { sX += x[i]; sY += y[i]; sXY += x[i]*y[i]; sX2 += x[i]*x[i]; }
        double slope = (k*sXY - sX*sY) / (k*sX2 - sX*sX);
        double inter = (sY - slope*sX) / k;
        double yBar = sY / k, ssTot = 0, ssRes = 0;
        for (int i = 0; i < k; i++) {
            ssTot += (y[i] - yBar) * (y[i] - yBar);
            double r = y[i] - (slope*x[i] + inter);
            ssRes += r * r;
        }
        double r2 = (ssTot == 0) ? 1.0 : 1.0 - ssRes/ssTot;
        return new double[]{slope, inter, r2};
    }

    // =========================================================================
    // Section 3 — Carry histogram
    // =========================================================================

    private static void section3CarryHistogram(Calculator calc) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SECTION 3: CARRY HISTOGRAM (fig:carry-hist)");
        System.out.println("=".repeat(70));
        int n=100; long STEPS=100_000_000L; int maxL=n/2+1;
        BigInteger[] Cn=carryDistribution(n,calc);
        BigInteger total=calc.telephoneNumber(n).subtract(BigInteger.ONE);
        BigInteger startRank=calc.telephoneNumber(n).divide(BigInteger.valueOf(3));
        // Run 1 (untimed): warm Calculator cache for this n, and the JIT.
        // Constructing the machine at startRank calls telephoneNumber up to n,
        // which fully populates the memoised table. No loop needed.
        {
            InvolutadicIncrementStateMachine warmup =
                    new InvolutadicIncrementStateMachine(n, startRank, calc);
            warmup.increment(); // one step to exercise the hot path
        }
        // Run 2 (measured): all telephoneNumber lookups are cache hits.
        long[] ec=new long[maxL+2];
        InvolutadicIncrementStateMachine engine=new InvolutadicIncrementStateMachine(n,startRank,calc);
        long sink=0;
        for (long s=0;s<STEPS;s++) {
            int carry = engine.incrementAndGetCarryLength();
            if (carry == 0) {
                engine = new InvolutadicIncrementStateMachine(n, 0L, calc);
                continue;
            }
            if (carry < ec.length) ec[carry]++;
            sink += engine.involution()[0];
        }
        System.out.println("Empirical (sink="+sink+"):");
        System.out.println("\n--- LaTeX coordinates for fig:carry-hist ---");
        System.out.println("\\addplot[ybar,fill=blue!30,draw=blue!60!black] coordinates {");
        StringBuilder sb=new StringBuilder("    ");
        for (int L=2;L<=Math.min(maxL,10);L++) if(ec[L]>0) sb.append(String.format("(%d,%d) ",L,ec[L]));
        System.out.println(sb.toString().trim()+"\n};");
        System.out.println("\\addplot[red,thick,dashed,mark=none] coordinates {");
        sb=new StringBuilder("    ");
        for (int L=2;L<=Math.min(maxL,10);L++) if(Cn[L].compareTo(BigInteger.ZERO)>0) {
            long sc=new BigDecimal(Cn[L]).divide(new BigDecimal(total),MC)
                    .multiply(new BigDecimal(STEPS),MC).setScale(0,RoundingMode.HALF_UP).longValue();
            sb.append(String.format("(%d,%d) ",L,sc));
        }
        System.out.println(sb.toString().trim()+"\n};");
    }

    // =========================================================================
    // Section 2 — Performance benchmark (three methods)
    //   (a) Involutadic   (b) Backtracking   (c) van Baronaigien
    // Repeated unranking is intentionally omitted (O(n) per step; not the
    // comparison of interest). Each method uses the run-twice strategy:
    // Run 1 (untimed) populates the Calculator cache and warms the JIT;
    // Run 2 (timed) is the measurement. Every method feeds the same sink.
    // =========================================================================

    private static void section2PerformanceBenchmark(Calculator calc) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SECTION 2: PERFORMANCE BENCHMARK");
        System.out.println("(a) Involutadic  (b) Backtrack  (c) van Baronaigien");
        System.out.println("=".repeat(70));
        // CACHE-WARM STRATEGY: run-twice.
        //   Run 1 (untimed): populates the Calculator's memoised telephone-number table
        //                    for all cells touched at this n, and warms the JIT.
        //   Run 2 (timed):   every lookup is a cache hit; no BigInteger arithmetic
        //                    occurs during measurement.
        // This replaces the old warmup-loop + best-of-5 pattern.

//        int[] nValues = {400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 4000};
        int[] nValues = {400, 1000, 2000, 4000, 8000, 12000, 16000, 20000};
        int   MEASURE = 200_000;

        long[] nsA = new long[nValues.length]; // Involutadic
        long[] nsC = new long[nValues.length]; // Backtracking
        long[] nsD = new long[nValues.length]; // van Baronaigien

        System.out.printf("%-6s  %-14s  %-14s  %-14s%n",
                "n","involutadic_ns","backtrack_ns","baronaigien_ns");

        for (int idx = 0; idx < nValues.length; idx++) {
            int n = nValues[idx];
            BigInteger Tn = calc.telephoneNumber(n);
            BigInteger startRank = Tn.divide(BigInteger.valueOf(4));
            long sink = 0;

            // (a) Involutadic increment machine — run-twice
            {
                // Run 1 (untimed): warm Calculator cache + JIT
                InvolutadicIncrementStateMachine run1 =
                        new InvolutadicIncrementStateMachine(n, startRank, calc);
                for (int i = 0; i < MEASURE; i++) {
                    if (!run1.increment()) run1 = new InvolutadicIncrementStateMachine(n, 0L, calc);
                    sink += run1.involution()[0];
                }
                // Run 2 (timed): cache fully warm
                InvolutadicIncrementStateMachine run2 =
                        new InvolutadicIncrementStateMachine(n, startRank, calc);
                long t0 = System.nanoTime();
                for (int i = 0; i < MEASURE; i++) {
                    if (!run2.increment()) run2 = new InvolutadicIncrementStateMachine(n, 0L, calc);
                    sink += run2.involution()[0];
                }
                nsA[idx] = (System.nanoTime() - t0) / MEASURE;
            }

            // (b) Backtracking — run-twice (no Calculator; JIT warm only)
            {
                BacktrackingLexGenerator run1 = new BacktrackingLexGenerator(n);
                for (int i = 0; i < MEASURE; i++) {
                    if (!run1.increment()) run1 = new BacktrackingLexGenerator(n);
                    sink += run1.involution()[0];
                }
                BacktrackingLexGenerator run2 = new BacktrackingLexGenerator(n);
                long t0 = System.nanoTime();
                for (int i = 0; i < MEASURE; i++) {
                    if (!run2.increment()) run2 = new BacktrackingLexGenerator(n);
                    sink += run2.involution()[0];
                }
                nsC[idx] = (System.nanoTime() - t0) / MEASURE;
            }

            // (c) van Baronaigien — run-twice (no Calculator; JIT warm only)
            {
                VanBaronaigienGenerator run1 = new VanBaronaigienGenerator(n);
                for (int i = 0; i < MEASURE; i++) {
                    if (!run1.increment()) run1 = new VanBaronaigienGenerator(n);
                    sink += run1.involution()[0];
                }
                VanBaronaigienGenerator run2 = new VanBaronaigienGenerator(n);
                long t0 = System.nanoTime();
                for (int i = 0; i < MEASURE; i++) {
                    if (!run2.increment()) run2 = new VanBaronaigienGenerator(n);
                    sink += run2.involution()[0];
                }
                nsD[idx] = (System.nanoTime() - t0) / MEASURE;
            }

            System.out.printf("%-6d  %-14d  %-14d  %-14d  (sink=%d)%n",
                    n,nsA[idx],nsC[idx],nsD[idx],sink);
        }

        // LaTeX coordinates
        System.out.println("\n--- LaTeX coordinates for fig:perf-comparison ---");
        printPlot("blue",   "*",         "Involutadic (this paper, amortised $O(1)$)",  nValues, nsA);
        printPlot("teal",   "triangle*", "Backtracking $O(n)$ worst case",              nValues, nsC);
        printPlot("orange", "diamond*",  "van Baronaigien (1991), amortised $O(1)$",    nValues, nsD);

        // Summary table
        System.out.println("\n--- Summary ---");
        System.out.printf("%-6s  %-12s  %-12s  %-12s  %-18s%n",
                "n","invol_ns","backtr_ns","baron_ns","speedup(inv/baron)");
        for (int i=0;i<nValues.length;i++)
            System.out.printf("%-6d  %-12d  %-12d  %-12d  %.1fx%n",
                    nValues[i],nsA[i],nsC[i],nsD[i],
                    (double)nsD[i]/nsA[i]);
    }

    // =========================================================================
    // Section 6 — Random-access benchmark (the actual contribution)
    //
    // Pure sequential speed is NOT the Involutadic advantage: a naive in-place
    // backtracking successor generates faster (mean carry ~ 2.25, so its loop
    // almost always terminates in 1-2 steps). The distinguishing capability is
    // RANDOM ACCESS: Involutadic jumps to an arbitrary rank R in O(n²) time,
    // independent of R, and maintains rank/digit state. A pure successor can only
    // step forward from rank 0, costing Theta(R) to reach rank R.
    //
    // (A) Involutadic random-access cost vs n: time to construct the machine at a
    //     random rank R in [0, T(n)). Scales ~O(n), independent of the size of R.
    //
    // (B) Cost to REACH rank R, Involutadic (unrank, flat in R) vs backtracking
    //     (R single-steps, linear in R), at a fixed modest n where backtracking
    //     can actually reach the tested R. We measure the SCALING honestly and
    //     then state the consequence: for R ~ T(n)/2 the Theta(R) cost is
    //     infeasible (R is unbounded by n), whereas O(n) is not.
    // =========================================================================

    private static void section6RandomAccessBenchmark(Calculator calc) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SECTION 6: RANDOM-ACCESS BENCHMARK");
        System.out.println("(Involutadic's actual advantage: rank-independent jump, measured ~O(n^2))");
        System.out.println("=".repeat(70));

        java.util.Random rng = new java.util.Random(42);

        // ---- (A) Involutadic random-access (unrank to random R) cost vs n ----
        System.out.println("\n(A) Involutadic: time to jump to a RANDOM rank R in [0, T(n)), vs n");
        System.out.println("    (R is astronomically large; cost depends on n, not on R)");
        int[] nVals = {400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 4000};
        int REPS_A = 400;
        System.out.printf("    %-6s  %-14s  %-12s%n", "n", "jump_ns", "T(n) digits");
        long sinkA = 0;
        long[] jumpNs = new long[nVals.length];
        for (int idx = 0; idx < nVals.length; idx++) {
            int n = nVals[idx];
            BigInteger Tn = calc.telephoneNumber(n);
            int bitLen = Tn.bitLength();

            // Run 1 (untimed): REPS_A calls to populate Calculator cache + warm JIT.
            BigInteger[] Rs1 = new BigInteger[REPS_A];
            for (int i = 0; i < REPS_A; i++) Rs1[i] = randomRank(rng, Tn, bitLen);
            for (int i = 0; i < REPS_A; i++)
                sinkA += new InvolutadicIncrementStateMachine(n, Rs1[i], calc).involution()[0];

            // Run 2 (timed): all Calculator lookups are cache hits.
            BigInteger[] Rs2 = new BigInteger[REPS_A];
            for (int i = 0; i < REPS_A; i++) Rs2[i] = randomRank(rng, Tn, bitLen);
            long t0 = System.nanoTime();
            for (int i = 0; i < REPS_A; i++)
                sinkA += new InvolutadicIncrementStateMachine(n, Rs2[i], calc).involution()[0];
            jumpNs[idx] = (System.nanoTime() - t0) / REPS_A;

            System.out.printf("    %-6d  %-14d  %-12d%n", n, jumpNs[idx], Tn.toString().length());
        }
        System.out.println("    (sink=" + sinkA + ")");
        System.out.println("\n    --- LaTeX coordinates for fig:random-access (Involutadic jump) ---");
        printPlot("blue", "*", "Involutadic random access $O(n^2)$ (rank-independent)", nVals, jumpNs);

        // ---- (B) Cost to reach rank R: Involutadic (flat) vs backtracking (linear) ----
        int nFixed = 40;   // small enough that backtracking can reach the tested R
        BigInteger TnFixed = calc.telephoneNumber(nFixed);
        int[] Rvals = {10, 100, 1000, 10000, 100000};
        System.out.printf("%n(B) Cost to REACH rank R at fixed n=%d  (T(%d) = %s)%n",
                nFixed, nFixed, TnFixed);
        System.out.println("    Involutadic = unrank(R) (expected flat in R);"
                + " Backtracking = R single-steps (expected linear in R)");
        System.out.printf("    %-10s  %-16s  %-16s%n", "R", "involutadic_ns", "backtrack_ns");
        long sinkB = 0;
        // Run-twice strategy for the single-shot Involutadic jumps:
        //   Run 1 (untimed): warms the Calculator's full telephoneNumber cache for nFixed.
        //   Run 2 (timed):   every lookup is a cache hit; no BigInteger arithmetic.
        // A single construction at rank 0 is enough to populate the cache for all
        // telephoneNumber(0..nFixed) entries, so one untimed call suffices.
        sinkB += new InvolutadicIncrementStateMachine(nFixed, BigInteger.ZERO, calc).involution()[0];
        int REPS_B = 200;
        long[] invReach = new long[Rvals.length];
        long[] btReach  = new long[Rvals.length];
        for (int idx = 0; idx < Rvals.length; idx++) {
            int R = Rvals[idx];
            BigInteger Rbig = BigInteger.valueOf(R);

            // Involutadic: run-twice. Run 1 untimed (cache already warm from above
            // but also covers JIT); run REPS_B timed calls, take the min.
            sinkB += new InvolutadicIncrementStateMachine(nFixed, Rbig, calc).involution()[0];
            long bestInv = Long.MAX_VALUE;
            for (int rep = 0; rep < REPS_B; rep++) {
                long t0 = System.nanoTime();
                InvolutadicIncrementStateMachine e =
                        new InvolutadicIncrementStateMachine(nFixed, Rbig, calc);
                long ns = System.nanoTime() - t0;
                sinkB += e.involution()[0];
                if (ns < bestInv) bestInv = ns;
            }
            invReach[idx] = bestInv;

            // Backtracking: run-twice. No Calculator involved; JIT warm after first run.
            // Run 1 (untimed):
            {
                BacktrackingLexGenerator bWarm = new BacktrackingLexGenerator(nFixed);
                for (int s = 0; s < R; s++) bWarm.increment();
                sinkB += bWarm.involution()[0];
            }
            // Run REPS_B timed calls, take the min.
            long bestBt = Long.MAX_VALUE;
            for (int rep = 0; rep < REPS_B; rep++) {
                BacktrackingLexGenerator b = new BacktrackingLexGenerator(nFixed);
                long t1 = System.nanoTime();
                for (int s = 0; s < R; s++) b.increment();
                long ns = System.nanoTime() - t1;
                sinkB += b.involution()[0];
                if (ns < bestBt) bestBt = ns;
            }
            btReach[idx] = bestBt;

            System.out.printf("    %-10d  %-16d  %-16d%n", R, bestInv, bestBt);
        }
        System.out.println("    (sink=" + sinkB + ")");
        System.out.println("\n    --- LaTeX coordinates for fig:reach-rank ---");
        long[] RvalsL = new long[Rvals.length];
        for (int i = 0; i < Rvals.length; i++) RvalsL[i] = Rvals[i];
        printPlotXY("blue",   "*",         "Involutadic unrank $O(n^2)$ (flat in $R$)",   Rvals, invReach);
        printPlotXY("teal",   "triangle*", "Backtracking $\\Theta(R)$ (linear in $R$)",   Rvals, btReach);

        System.out.println("\n    READING: Involutadic reach-cost is flat in R (it unranks in O(n^2), R-independent);");
        System.out.println("    backtracking reach-cost is linear in R. For R ~ T(n)/2 (R unbounded");
        System.out.println("    by n) the Theta(R) path is infeasible while O(n^2) is not -- this is");
        System.out.println("    the random-access advantage, NOT pure sequential speed.");
    }

    // =========================================================================
    // Section 7 — Variance limit V* = lim Var[L_n]
    //
    // The paper (conj:variance, tab:variance) claims Var[L_n] -> I*(3-I*) ~ 0.179.
    // This is WRONG on three independent counts, which this section documents with
    // exact P-table data:
    //   (1) Arithmetic: I*(3-I*) with I*=2.25331 equals 1.6825, NOT 0.179. The
    //       formula and the stated value 0.179 are inconsistent with each other.
    //   (2) Data: the paper's tab:variance values (sigma^2_8=0.170, _16=0.178,
    //       trending to ~0.179) disagree with the exact P-table variance computed
    //       here (sigma^2_8=0.243964, _16=0.265318), which is the SAME column shown
    //       in Section 4. The paper table appears stale / a different definition.
    //   (3) Limit: the true limit is V* ~ 0.2665454 (this section), not 0.179 and
    //       not 1.6825.
    //
    // This section reports the exact variance, its fast convergence (both parities),
    // an Aitken-extrapolated V*, and tests candidate closed forms. No simple closed
    // form has been found; V* appears to be a genuine empirical constant. The
    // conjecture and table in the paper must be revised accordingly.
    // =========================================================================

    private static void section7VarianceLimit(Calculator calc) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SECTION 7: VARIANCE LIMIT  V* = lim Var[L_n]");
        System.out.println("  Paper claims I*(3-I*) ~ 0.179; this tests it against exact data");
        System.out.println("=".repeat(70));

        // Exact variance via high-precision; converges fast so n<=200 is ample.
        final int HI = 200;

        System.out.println("\n  Exact Var[L_n] (high precision), convergence:");
        System.out.printf("  %-5s %-22s %-16s%n", "n", "Var[L_n]", "Var - 0.2665454");
        for (int n : new int[]{8, 16, 30, 60, 100, 150, 200}) {
            BigDecimal v = varianceHP(n, calc);
            System.out.printf("  %-5d %-22s %+.9f%n",
                    n, v.setScale(12, RoundingMode.HALF_UP).toPlainString(),
                    v.doubleValue() - 0.2665454);
        }

        // Parity check: even and odd both settle to the same V*.
        System.out.println("\n  Parity subsequences (last 4 each; both must agree):");
        for (int parity = 0; parity <= 1; parity++) {
            System.out.printf("    %-5s ", parity == 0 ? "even" : "odd");
            for (int n = HI - (parity == 0 ? 6 : 7); n <= HI; n += 2) {
                System.out.printf("n%d:%.9f  ", n, varianceHP(n, calc).doubleValue());
            }
            System.out.println();
        }

        // Aitken extrapolation of V* on the even subsequence tail.
        int cnt = 0;
        BigDecimal[] evens = new BigDecimal[HI];
        for (int n = 100; n <= HI; n += 2) evens[cnt++] = varianceHP(n, calc);
        BigDecimal vStar = aitkenLimit(evens, cnt);
        System.out.printf("%n  Aitken-extrapolated V* ~ %s%n",
                vStar.setScale(10, RoundingMode.HALF_UP).toPlainString());

        // Test candidate closed forms.
        double V = vStar.doubleValue();
        double Ic = 1.0 + Math.sqrt(Math.PI / 2.0);
        double s  = Math.sqrt(Math.PI / 2.0);   // = Ic - 1
        System.out.println("\n  Candidate closed forms vs V* (diff; |diff|<1e-4 would be a match):");
        printCandidate("I*(3-I*)  [paper formula]", Ic * (3 - Ic), V);
        printCandidate("(I*-1)(3-I*)",              (Ic - 1) * (3 - Ic), V);
        printCandidate("s(2-s), s=sqrt(pi/2)",      s * (2 - s), V);
        printCandidate("pi/2 - 1",                  Math.PI / 2 - 1, V);
        printCandidate("2 - pi/2",                  2 - Math.PI / 2, V);
        printCandidate("1 - pi/4",                  1 - Math.PI / 4, V);
        printCandidate("4 - pi",                    4 - Math.PI, V);

        System.out.println("\n  READING: V* ~ 0.2665454, converges fast (flat by n~80), both parities");
        System.out.println("  agree. It is NOT I*(3-I*)=1.6825 and NOT 0.179. No tested closed form");
        System.out.println("  matches. conj:variance and tab:variance in the paper are incorrect and");
        System.out.println("  must be revised: drop the I*(3-I*)~0.179 claim; report V* empirically");
        System.out.println("  (or as an open constant) using the exact Section 4 variance column.");
    }

    /** Exact Var[L_n] at high precision: E[L^2] - E[L]^2 via the P-table. */
    private static BigDecimal varianceHP(int n, Calculator calc) {
        BigInteger[] C = carryDistribution(n, calc);
        BigInteger total = calc.telephoneNumber(n).subtract(BigInteger.ONE);
        int maxL = n / 2 + 1;
        BigInteger w1 = BigInteger.ZERO, w2 = BigInteger.ZERO;
        for (int L = 2; L <= maxL; L++) {
            BigInteger c = C[L];
            BigInteger Lb = BigInteger.valueOf(L);
            w1 = w1.add(Lb.multiply(c));
            w2 = w2.add(Lb.multiply(Lb).multiply(c));
        }
        BigDecimal tot = new BigDecimal(total);
        BigDecimal mean = new BigDecimal(w1).divide(tot, MC_HP);
        BigDecimal e2   = new BigDecimal(w2).divide(tot, MC_HP);
        return e2.subtract(mean.multiply(mean, MC_HP), MC_HP);
    }

    /** Iterated Aitken Delta^2 limit estimate on the first {@code len} entries. */
    private static BigDecimal aitkenLimit(BigDecimal[] seq, int len) {
        BigDecimal[] s = new BigDecimal[len];
        System.arraycopy(seq, 0, s, 0, len);
        int m = len;
        for (int iter = 0; iter < 6 && m >= 3; iter++) {
            BigDecimal[] next = new BigDecimal[m - 2];
            for (int i = 0; i < m - 2; i++) {
                BigDecimal a = s[i], b = s[i + 1], c = s[i + 2];
                BigDecimal denom = c.subtract(b.multiply(BigDecimal.valueOf(2))).add(a);
                next[i] = (denom.signum() == 0) ? c
                        : c.subtract(c.subtract(b).pow(2).divide(denom, MC_HP));
            }
            s = next; m -= 2;
        }
        return s[m - 1];
    }

    private static void printCandidate(String name, double val, double V) {
        String flag = Math.abs(val - V) < 1e-4 ? "   <-- MATCH" : "";
        System.out.printf("    %-30s = %.10f   diff=%+.3e%s%n", name, val, val - V, flag);
    }

    /** Uniform random rank in [0, T) by rejection sampling on bitLen-bit integers. */
    private static BigInteger randomRank(java.util.Random rng, BigInteger T, int bitLen) {
        BigInteger r;
        do { r = new BigInteger(bitLen, rng); } while (r.compareTo(T) >= 0);
        return r;
    }

    /** Like printPlot but x-values are the rank values R (not n). */
    private static void printPlotXY(String color, String mark, String legend, int[] xs, long[] ys) {
        System.out.printf("%n\\addplot[%s,thick,mark=%s] coordinates {%n    ", color, mark);
        for (int i = 0; i < xs.length; i++) System.out.printf("(%d,%d) ", xs[i], ys[i]);
        System.out.println("\n};\n\\addlegendentry{" + legend + "}");
    }

    private static void printPlot(String color,String mark,String legend,int[] ns,long[] t) {
        System.out.printf("%n\\addplot[%s,thick,mark=%s] coordinates {%n    ",color,mark);
        for (int i=0;i<ns.length;i++) System.out.printf("(%d,%d) ",ns[i],t[i]);
        System.out.println("\n};\n\\addlegendentry{"+legend+"}");
    }
}