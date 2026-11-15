/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

/**
 * Shared table of natural logarithms of factorials, {@code ln(i!)}, used to estimate where a binomial coefficient
 * crosses a given value before the exact {@code BigInteger} comparison confirms it.
 * <p>
 * The table only grows, and every entry is computed the same way whichever thread extends it, so a published
 * array is always correct and can be read without locking.
 * </p>
 */
final class LogFactorials {

    /** Largest n the table serves (32 KB at the limit); callers fall back to exact search above it. */
    static final int MAX_N = 4096;

    private static volatile double[] table = {0.0, 0.0};

    private LogFactorials() {
    }

    /** Returns a table with at least {@code n + 1} entries, where entry i is ln(i!). Requires n â‰¤ {@link #MAX_N}. */
    static double[] upTo(int n) {
        double[] current = table;
        if (current.length > n) {
            return current;
        }
        return grow(n);
    }

    private static synchronized double[] grow(int n) {
        double[] current = table;
        if (current.length > n) {
            return current;
        }
        int size = Math.min(Math.max(n + 1, current.length * 2), MAX_N + 1);
        double[] bigger = new double[size];
        System.arraycopy(current, 0, bigger, 0, current.length);
        for (int i = current.length; i < size; i++) {
            bigger[i] = bigger[i - 1] + Math.log(i);
        }
        table = bigger;
        return bigger;
    }
}
