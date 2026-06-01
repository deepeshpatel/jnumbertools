package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic.experiments;

import java.util.*;

/**
 * Compare:
 *   1. General Involutadic encoding
 *   2. Classical I-code encoding
 *
 * IMPORTANT:
 * - I-code is defined only for fixed-point-free involutions (FPFI).
 * - For involutions containing fixed points, the table prints "N/A".
 *
 * Output columns:
 *   Rank | Involutadic | I-Code
 *
 * Main method runs for n = 5.
 */
public class InvolutadicVsICodeTable {

    public static void main(String[] args) {
        printTable(6);
    }

    // =========================================================
    // TABLE PRINTER
    // =========================================================

    static void printTable(int n) {

        List<int[]> involutions = generateAllInvolutions(n);

        System.out.println("n = " + n);
        System.out.println();

        System.out.printf(
                "%-5s %-25s %-25s %-20s%n",
                "Rank",
                "Permutation",
                "Involutadic",
                "I-Code"
        );

        System.out.println(
                "------------------------------------------------------------------------------------------"
        );

        int rank = 0;

        for (int[] inv : involutions) {

            int[] involutadic = toGeneralInvolutadic(inv);

            String iCodeString;

            if (hasFixedPoint(inv)) {
                iCodeString = "N/A";
            } else {
                iCodeString = Arrays.toString(toICodeFPFI(inv));
            }

            System.out.printf(
                    "%-5d %-25s %-25s %-20s%n",
                    rank++,
                    Arrays.toString(inv),
                    Arrays.toString(involutadic),
                    iCodeString
            );
        }
    }

    // =========================================================
    // GENERAL INVOLUTADIC
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

            // fixed point
            if (pi[pos] == pos) {

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
    // CLASSICAL I-CODE (FPFI ONLY)
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
    // CHECK FIXED POINT
    // =========================================================

    static boolean hasFixedPoint(int[] pi) {

        for (int i = 0; i < pi.length; i++) {
            if (pi[i] == i) {
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // GENERATE ALL INVOLUTIONS
    // =========================================================

    static List<int[]> generateAllInvolutions(int n) {

        List<int[]> result = new ArrayList<>();

        int[] pi = new int[n];

        Arrays.fill(pi, -1);

        backtrack(pi, 0, result);

        result.sort(InvolutadicVsICodeTable::lexCompare);

        return result;
    }

    static void backtrack(int[] pi, int start, List<int[]> result) {

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

        backtrack(pi, start + 1, result);

        pi[start] = -1;

        // pair with larger element
        for (int j = start + 1; j < n; j++) {

            if (pi[j] == -1) {

                pi[start] = j;
                pi[j] = start;

                backtrack(pi, start + 1, result);

                pi[start] = -1;
                pi[j] = -1;
            }
        }
    }

    // =========================================================
    // LEXICOGRAPHIC COMPARE
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