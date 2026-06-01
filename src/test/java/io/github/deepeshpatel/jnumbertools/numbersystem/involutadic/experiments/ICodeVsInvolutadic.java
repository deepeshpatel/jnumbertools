package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.util.*;

/**
 * Standalone demo comparing:
 *
 * 1. Classical I-code for fixed-point-free involutions (FPFIs)
 * 2. JNumberTools-style Involutadic encoding
 *
 * This program prints the first few encodings so you can visually compare them.
 *
 * Conclusion:
 * - For FPFIs only -> encodings are IDENTICAL.
 * - For general involutions with fixed points -> encodings DIFFER.
 */
public class ICodeVsInvolutadic {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("Fixed-Point-Free Involutions (n = 4)");
        System.out.println("I-code vs Involutadic");
        System.out.println("==============================================");

        List<int[]> fpfis = generateFPFIs(4);

        int rank = 0;
        for (int[] inv : fpfis) {

            int[] icode = toICodeFPFI(inv);
            int[] involutadic = toInvolutadicFPFI(inv);

            System.out.printf(
                    "%2d  %-15s  I-code=%-10s  Involutadic=%-10s  SAME=%s%n",
                    rank++,
                    Arrays.toString(inv),
                    Arrays.toString(icode),
                    Arrays.toString(involutadic),
                    Arrays.equals(icode, involutadic)
            );
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("General involutions (n = 4)");
        System.out.println("Now they diverge because fixed points exist");
        System.out.println("==============================================");

        List<int[]> involutions = generateAllInvolutions(4);

        rank = 0;
        for (int[] inv : involutions) {

            int[] involutadic = toGeneralInvolutadic(inv);

            System.out.printf(
                    "%2d  %-15s  Involutadic=%s%n",
                    rank++,
                    Arrays.toString(inv),
                    Arrays.toString(involutadic)
            );
        }
    }

    // =========================================================
    // Classical I-code for FIXED-POINT-FREE involutions
    // =========================================================

    static int[] toICodeFPFI(int[] pi) {

        int n = pi.length;

        TreeSet<Integer> unpaired = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            unpaired.add(i);
        }

        List<Integer> digits = new ArrayList<>();

        while (!unpaired.isEmpty()) {

            int x = unpaired.first();
            int y = pi[x];

            List<Integer> candidates = new ArrayList<>();
            for (int v : unpaired) {
                if (v != x) {
                    candidates.add(v);
                }
            }

            int digit = candidates.indexOf(y);
            digits.add(digit);

            unpaired.remove(x);
            unpaired.remove(y);
        }

        return digits.stream().mapToInt(i -> i).toArray();
    }

    // =========================================================
    // Involutadic restricted to FPFIs
    // =========================================================

    static int[] toInvolutadicFPFI(int[] pi) {

        int n = pi.length;

        TreeSet<Integer> unused = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            unused.add(i);
        }

        List<Integer> digits = new ArrayList<>();

        while (!unused.isEmpty()) {

            int pos = unused.first();
            int partner = pi[pos];

            List<Integer> candidates = new ArrayList<>();
            for (int v : unused) {
                if (v != pos) {
                    candidates.add(v);
                }
            }

            int digit = candidates.indexOf(partner);
            digits.add(digit);

            unused.remove(pos);
            unused.remove(partner);
        }

        return digits.stream().mapToInt(i -> i).toArray();
    }

    // =========================================================
    // General involutadic (supports fixed points)
    // =========================================================

    static int[] toGeneralInvolutadic(int[] pi) {

        int n = pi.length;

        TreeSet<Integer> unused = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            unused.add(i);
        }

        List<Integer> digits = new ArrayList<>();

        while (!unused.isEmpty()) {

            int pos = unused.first();

            if (pi[pos] == pos) {

                // fixed point encoded as digit 0
                digits.add(0);
                unused.remove(pos);

            } else {

                int partner = pi[pos];

                List<Integer> candidates = new ArrayList<>();
                for (int v : unused) {
                    if (v > pos) {
                        candidates.add(v);
                    }
                }

                int digit = candidates.indexOf(partner) + 1;
                digits.add(digit);

                unused.remove(pos);
                unused.remove(partner);
            }
        }

        return digits.stream().mapToInt(i -> i).toArray();
    }

    // =========================================================
    // Generate all FPFIs
    // =========================================================

    static List<int[]> generateFPFIs(int n) {

        List<int[]> result = new ArrayList<>();

        int[] pi = new int[n];
        Arrays.fill(pi, -1);

        backtrackFPFI(pi, 0, result);

        result.sort(ICodeVsInvolutadic::lexCompare);

        return result;
    }

    static void backtrackFPFI(int[] pi, int start, List<int[]> result) {

        int n = pi.length;

        while (start < n && pi[start] != -1) {
            start++;
        }

        if (start == n) {
            result.add(pi.clone());
            return;
        }

        for (int j = start + 1; j < n; j++) {

            if (pi[j] == -1) {

                pi[start] = j;
                pi[j] = start;

                backtrackFPFI(pi, start + 1, result);

                pi[start] = -1;
                pi[j] = -1;
            }
        }
    }

    // =========================================================
    // Generate all involutions
    // =========================================================

    static List<int[]> generateAllInvolutions(int n) {

        List<int[]> result = new ArrayList<>();

        int[] pi = new int[n];
        Arrays.fill(pi, -1);

        backtrackAll(pi, 0, result);

        result.sort(ICodeVsInvolutadic::lexCompare);

        return result;
    }

    static void backtrackAll(int[] pi, int start, List<int[]> result) {

        int n = pi.length;

        while (start < n && pi[start] != -1) {
            start++;
        }

        if (start == n) {
            result.add(pi.clone());
            return;
        }

        // fixed point
        pi[start] = start;
        backtrackAll(pi, start + 1, result);
        pi[start] = -1;

        // pair with j
        for (int j = start + 1; j < n; j++) {

            if (pi[j] == -1) {

                pi[start] = j;
                pi[j] = start;

                backtrackAll(pi, start + 1, result);

                pi[start] = -1;
                pi[j] = -1;
            }
        }
    }

    // =========================================================
    // Lexicographic compare
    // =========================================================

    static int lexCompare(int[] a, int[] b) {

        for (int i = 0; i < a.length; i++) {

            if (a[i] != b[i]) {
                return Integer.compare(a[i], b[i]);
            }
        }

        return 0;
    }
}