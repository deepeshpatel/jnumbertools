/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

/**
 * A shared table of binomial coefficients in primitive {@code long} arithmetic, for unranking when the whole
 * result space fits in 62 bits.
 * <p>
 * Entries are saturated at {@link Long#MAX_VALUE}. That is safe whenever the ranks involved are below 2<sup>62</sup>:
 * a saturated entry can only ever be compared with a smaller remaining rank (and so is never subtracted), which
 * gives the same decisions as exact arithmetic. Binomials depend on nothing but their arguments, so one table
 * serves every caller. It only grows and is replaced, never mutated, so it is safe to share between threads.
 * </p>
 *
 * @author Deepesh Patel
 */
public final class LongBinomials {

    /** Largest table (rows x columns) that is built; bigger requests fall back to {@code BigInteger} arithmetic. */
    private static final int MAX_CELLS = 1 << 16;

    private static volatile long[][] table = new long[0][];

    private LongBinomials() {
    }

    /**
     * Returns a table {@code t} with {@code t[c][k] = C(c, k)} for {@code 0 <= c < rows} and
     * {@code 0 <= k < cols}, saturated at {@link Long#MAX_VALUE}; entries with {@code k > c} are 0. The array may
     * be larger than requested and must not be modified.
     *
     * @param rows the number of rows needed (largest n plus one)
     * @param cols the number of columns needed (largest k plus one)
     * @return the table, or {@code null} when it would be too large (or either size is not positive)
     */
    public static long[][] table(int rows, int cols) {
        long[][] t = table;
        if (rows > 0 && cols > 0 && t.length >= rows && t[0].length >= cols) return t;
        if (rows < 1 || cols < 1) return null;
        // Grow to cover everything seen so far, so alternating requests do not rebuild the table each time.
        int r = Math.max(rows, t.length);
        int c = Math.max(cols, t.length > 0 ? t[0].length : 0);
        if ((long) r * c > MAX_CELLS) return null;
        t = new long[r][c];
        for (int n = 0; n < r; n++) {
            t[n][0] = 1;
            for (int k = 1; k < c && k <= n; k++) {
                long sum = t[n - 1][k - 1] + t[n - 1][k];
                t[n][k] = sum < 0 ? Long.MAX_VALUE : sum;
            }
        }
        table = t;
        return t;
    }
}
