/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Exact subtree and root completion-count tables for a {@link ConstraintPlan}.
 *
 * <p>The plan owns the constraint topology; this class owns the derived exact-count
 * tables and their primitive-long fast-path representation.</p>
 */
final class ConstraintCounts {
    final BigInteger[][] free;
    final BigInteger[][] msg;
    final BigInteger[][] freePrefix;
    final BigInteger[] suffix;
    final BigInteger total;
    final BigInteger fullProduct;
    final long[][] lfree;
    final long[][] lmsg;
    final long[][] lfreePrefix;
    final long[] lsuffix;

    ConstraintCounts(ConstraintPlan plan, int[] order, int[] dimSize) {
        int nodes = plan.members.length;
        free = new BigInteger[nodes][];
        msg = new BigInteger[nodes][];

        for (int idx = order.length - 1; idx >= 0; idx--) {
            int v = order[idx];
            BigInteger[] f = new BigInteger[plan.size[v]];
            Arrays.fill(f, BigInteger.ONE);
            for (int c : plan.children[v]) {
                BigInteger[] msgToParent = new BigInteger[plan.size[v]];
                for (int a = 0; a < plan.size[v]; a++) {
                    BitSet allowed = plan.fromParent[c][a];
                    BigInteger sum = BigInteger.ZERO;
                    for (int b = allowed.nextSetBit(0); b >= 0; b = allowed.nextSetBit(b + 1)) {
                        sum = sum.add(free[c][b]);
                    }
                    msgToParent[a] = sum;
                    f[a] = f[a].multiply(sum);
                }
                msg[c] = msgToParent;
            }
            free[v] = f;
        }

        freePrefix = new BigInteger[nodes][];
        for (int x = 0; x < nodes; x++) {
            if (plan.values[x] != null) {
                BigInteger[] prefix = new BigInteger[plan.size[x] + 1];
                prefix[0] = BigInteger.ZERO;
                for (int i = 0; i < plan.size[x]; i++) {
                    prefix[i + 1] = prefix[i].add(free[x][i]);
                }
                freePrefix[x] = prefix;
            }
        }

        suffix = new BigInteger[plan.roots.length + 1];
        suffix[plan.roots.length] = BigInteger.ONE;
        for (int i = plan.roots.length - 1; i >= 0; i--) {
            BigInteger rootTotal = BigInteger.ZERO;
            for (BigInteger weight : free[plan.roots[i]]) rootTotal = rootTotal.add(weight);
            suffix[i] = suffix[i + 1].multiply(rootTotal);
        }
        total = suffix[0];

        BigInteger product = BigInteger.ONE;
        for (int size : dimSize) product = product.multiply(BigInteger.valueOf(size));
        fullProduct = product;

        boolean fits = fitsLong(free) && fitsLong(msg) && fitsLong(freePrefix) && fitsLong(suffix);
        lfree = fits ? toLong(free) : null;
        lmsg = fits ? toLong(msg) : null;
        lfreePrefix = fits ? toLong(freePrefix) : null;
        lsuffix = fits ? toLong(suffix) : null;
    }

    private static boolean fitsLong(BigInteger[] row) {
        if (row == null) return true;
        for (BigInteger value : row) if (value.bitLength() > 62) return false;
        return true;
    }

    private static boolean fitsLong(BigInteger[][] rows) {
        for (BigInteger[] row : rows) if (!fitsLong(row)) return false;
        return true;
    }

    private static long[] toLong(BigInteger[] row) {
        if (row == null) return null;
        long[] result = new long[row.length];
        for (int i = 0; i < result.length; i++) result[i] = row[i].longValue();
        return result;
    }

    private static long[][] toLong(BigInteger[][] rows) {
        long[][] result = new long[rows.length][];
        for (int i = 0; i < result.length; i++) result[i] = toLong(rows[i]);
        return result;
    }
}
