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
    private final ConstraintPlan plan;
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
        this.plan = plan;
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

    /**
     * Range [lo, hi) of the values of node c that agree with the dimensions already fixed (below k),
     * packed as lo in the high 32 bits and hi in the low 32 bits. The fixed dimensions of a node are the
     * first ones in its member list and its values are in lexicographic order, so the agreeing values are
     * contiguous and are found by binary search.
     */
    private long range(int c, int k, int[] val) {
        int[] mem = plan.members[c];
        if (mem[0] >= k) return (long) plan.size[c];                 // nothing fixed: [0, plan.size)
        if (plan.values[c] == null) {
            int b = val[mem[0]];
            return ((long) b << 32) | (b + 1);
        }
        int fixed = 0;
        while (fixed < mem.length && mem[fixed] < k) fixed++;
        int[][] list = values[c];
        int lo = lowerBound(list, mem, fixed, val, false);
        int hi = lowerBound(list, mem, fixed, val, true);
        return ((long) lo << 32) | hi;
    }

    /** First index whose fixed-prefix is >= the target prefix (or > it when {@code strict}). */
    private static int lowerBound(int[][] list, int[] mem, int fixed, int[] val, boolean strict) {
        int lo = 0;
        int hi = list.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            int cmp = 0;
            for (int pos = 0; pos < fixed && cmp == 0; pos++) {
                cmp = Integer.compare(list[mid][pos], val[mem[pos]]);
            }
            if (cmp < 0 || (strict && cmp == 0)) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    /** Sum of count(c, b) over the values b in {@code allowed} (all values if null) that agree with the fixed dimensions. */
    private BigInteger sumConsistent(int c, BitSet allowed, int k, int[] val) {
        long r = range(c, k, val);
        int lo;
        int hi;
        if (plan.members[c][0] >= k) {
            lo = 0;
            hi = plan.size[c];
        } else {
            lo = (int) (r >>> 32);
            hi = (int) r;
        }
        if (lo >= hi) return BigInteger.ZERO;
        if (allowed == null && plan.minChildSub[c] >= k && this.freePrefix[c] != null) {
            // no fixed dimension below c's children: every value counts exactly free[c][b]
            return this.freePrefix[c][hi].subtract(this.freePrefix[c][lo]);
        }
        BigInteger sum = BigInteger.ZERO;
        if (allowed == null) {
            for (int b = lo; b < hi; b++) sum = sum.add(count(c, b, k, val));
        } else {
            for (int b = allowed.nextSetBit(lo); b >= 0 && b < hi; b = allowed.nextSetBit(b + 1)) {
                sum = sum.add(count(c, b, k, val));
            }
        }
        return sum;
    }

    /** True when a uniformly random point of the full product is valid with probability at least 1/4. */
    boolean mostTuplesValid() {
        return this.total.shiftLeft(2).compareTo(this.fullProduct) >= 0;
    }

    // ── primitive-long versions of the counting queries (used when lsuffix != null) ──

    private long sumConsistentL(int c, BitSet allowed, int k, int[] val) {
        long r = range(c, k, val);
        int lo;
        int hi;
        if (plan.members[c][0] >= k) {
            lo = 0;
            hi = plan.size[c];
        } else {
            lo = (int) (r >>> 32);
            hi = (int) r;
        }
        if (lo >= hi) return 0;
        if (allowed == null && plan.minChildSub[c] >= k && this.lfreePrefix[c] != null) {
            return this.lfreePrefix[c][hi] - this.lfreePrefix[c][lo];
        }
        long sum = 0;
        if (allowed == null) {
            for (int b = lo; b < hi; b++) sum += countL(c, b, k, val);
        } else {
            for (int b = allowed.nextSetBit(lo); b >= 0 && b < hi; b = allowed.nextSetBit(b + 1)) {
                sum += countL(c, b, k, val);
            }
        }
        return sum;
    }

    private long completionsL(int k, int[] val) {
        int fixedRoots = plan.firstRoot[k];
        long result = this.lsuffix[fixedRoots];
        if (result == 0) return 0;
        for (int i = 0; i < fixedRoots; i++) {
            long c = sumConsistentL(plan.roots[i], null, k, val);
            if (c == 0) return 0;
            result *= c;
        }
        return result;
    }

    private long countL(int v, int a, int k, int[] val) {
        if (plan.minSub[v] >= k) return this.lfree[v][a];
        long result = 1;
        for (int c : plan.children[v]) {
            BitSet allowed = plan.fromParent[c][a];
            long s = plan.minSub[c] >= k ? this.lmsg[c][a] : sumConsistentL(c, allowed, k, val);
            if (s == 0) return 0;
            result *= s;
        }
        return result;
    }

    /** Number of valid ways to choose dimensions k..n-1 when dimensions 0..k-1 have values val[0..k-1]. */
    BigInteger completions(int k, int[] val) {
        int fixedRoots = plan.firstRoot[k];
        BigInteger result = this.suffix[fixedRoots];
        if (result.signum() == 0) return BigInteger.ZERO;
        for (int i = 0; i < fixedRoots; i++) {
            BigInteger c = sumConsistent(plan.roots[i], null, k, val);
            if (c.signum() == 0) return BigInteger.ZERO;
            result = result.multiply(c);
        }
        return result;
    }

    /** Ways to choose v's subtree when v has value a, with dimensions below k fixed. */
    private BigInteger count(int v, int a, int k, int[] val) {
        if (plan.minSub[v] >= k) return this.free[v][a];
        BigInteger result = BigInteger.ONE;
        for (int c : plan.children[v]) {
            BitSet allowed = plan.fromParent[c][a];
            BigInteger s = plan.minSub[c] >= k ? this.msg[c][a] : sumConsistent(c, allowed, k, val);
            if (s.signum() == 0) return BigInteger.ZERO;
            result = result.multiply(s);
        }
        return result;
    }

    boolean hasCompletion(int k, int[] val) {
        return this.lsuffix != null ? completionsL(k, val) > 0 : completions(k, val).signum() > 0;
    }

    /** Value indexes of the tuple with the given rank among the valid tuples; rank must be in range. */
    int[] unrank(BigInteger rank) {
        if (this.lsuffix != null) return unrankL(rank.longValueExact());
        int[] val = new int[dimensions];
        BigInteger remaining = rank;
        for (int k = 0; k < dimensions; k++) {
            boolean found = false;
            int dimSize = plan.dimensionSize(k);
            for (int idx = 0; idx < dimSize; idx++) {
                val[k] = idx;
                BigInteger c = k == plan.dimensions - 1
                        ? (plan.agreesWithBelow(k, idx, val) ? BigInteger.ONE : BigInteger.ZERO)
                        : completions(k + 1, val);
                if (c.signum() == 0) continue;
                if (remaining.compareTo(c) < 0) {
                    found = true;
                    break;
                }
                remaining = remaining.subtract(c);
            }
            if (!found) {
                throw new IllegalStateException("Rank " + rank + " could not be resolved");
            }
        }
        return val;
    }

    /** {@link #unrank(BigInteger)} in primitive arithmetic; only valid when the long tables exist. */
    private int[] unrankL(long rank) {
        int[] val = new int[dimensions];
        long remaining = rank;
        for (int k = 0; k < dimensions; k++) {
            boolean found = false;
            int size = plan.dimensionSize(k);
            for (int idx = 0; idx < size; idx++) {
                val[k] = idx;
                long c = k == plan.dimensions - 1
                        ? (plan.agreesWithBelow(k, idx, val) ? 1L : 0L)
                        : completionsL(k + 1, val);
                if (c == 0) continue;
                if (remaining < c) {
                    found = true;
                    break;
                }
                remaining -= c;
            }
            if (!found) {
                throw new IllegalStateException("Rank " + rank + " could not be resolved");
            }
        }
        return val;
    }
}
