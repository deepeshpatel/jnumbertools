/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Exact subtree and root completion-count tables for a {@link ConstraintPlan}.
 *
 * <p>The plan owns the constraint topology; this class owns the derived exact-count
 * tables and their primitive-long fast-path representation.</p>
 */
final class ConstraintCounts {
    /** Counts below this stay in primitive longs; a table value at or above it switches to BigInteger. */
    private static final long LIMIT = 1L << 62;

    private final ConstraintPlan plan;
    private final int[] dimSize;
    // BigInteger tables: built only when some count does not fit a long, otherwise null
    final BigInteger[][] free;
    final BigInteger[][] msg;
    final BigInteger[][] freePrefix;
    final BigInteger[] suffix;
    final BigInteger total;
    // primitive-long tables: built first, and the only ones when every count fits (the common case), otherwise null
    final long[][] lfree;
    final long[][] lmsg;
    final long[][] lfreePrefix;
    final long[] lsuffix;

    ConstraintCounts(ConstraintPlan plan, int[] order, int[] dimSize) {
        this.plan = plan;
        this.dimSize = dimSize;
        int nodes = plan.members.length;
        long[][] f = new long[nodes][];
        long[][] m = new long[nodes][];
        long[][] p = new long[nodes][];
        long[] suf = buildLong(plan, order, f, m, p);
        if (suf != null) {
            lfree = f;
            lmsg = m;
            lfreePrefix = p;
            lsuffix = suf;
            free = null;
            msg = null;
            freePrefix = null;
            suffix = null;
            total = BigInteger.valueOf(suf[0]);
        } else {
            BigInteger[][] bf = new BigInteger[nodes][];
            BigInteger[][] bm = new BigInteger[nodes][];
            BigInteger[][] bp = new BigInteger[nodes][];
            BigInteger[] bs = buildBig(plan, order, bf, bm, bp);
            free = bf;
            msg = bm;
            freePrefix = bp;
            suffix = bs;
            total = bs[0];
            lfree = null;
            lmsg = null;
            lfreePrefix = null;
            lsuffix = null;
        }
    }

    // ── table construction ──

    /**
     * Fills the long tables, or returns null when any stored count reaches {@link #LIMIT}. Rows of the relation to a
     * child that are equal share one sum, and a row that is a single run of values is summed from prefix sums.
     */
    private static long[] buildLong(ConstraintPlan plan, int[] order, long[][] free, long[][] msg, long[][] freePrefix) {
        for (int idx = order.length - 1; idx >= 0; idx--) {
            int v = order[idx];
            long[] f = new long[plan.size[v]];
            Arrays.fill(f, 1L);
            for (int c : plan.children[v]) {
                long[] message = messagesLong(plan.fromParent[c], plan.size[v], free[c]);
                if (message == null) return null;
                for (int a = 0; a < f.length; a++) {
                    f[a] = multiply(f[a], message[a]);
                    if (f[a] < 0) return null;
                }
                msg[c] = message;
            }
            free[v] = f;
        }
        for (int x = 0; x < free.length; x++) {
            if (plan.values[x] != null) {
                long[] prefix = new long[plan.size[x] + 1];
                for (int i = 0; i < plan.size[x]; i++) {
                    prefix[i + 1] = add(prefix[i], free[x][i]);
                    if (prefix[i + 1] < 0) return null;
                }
                freePrefix[x] = prefix;
            }
        }
        long[] suffix = new long[plan.roots.length + 1];
        suffix[plan.roots.length] = 1;
        for (int i = plan.roots.length - 1; i >= 0; i--) {
            long rootTotal = 0;
            for (long weight : free[plan.roots[i]]) {
                rootTotal = add(rootTotal, weight);
                if (rootTotal < 0) return null;
            }
            suffix[i] = multiply(suffix[i + 1], rootTotal);
            if (suffix[i] < 0) return null;
        }
        return suffix;
    }

    /** message[a] = sum of {@code childFree[b]} over the values b allowed by {@code rows[a]}; null on overflow. */
    private static long[] messagesLong(BitSet[] rows, int parentSize, long[] childFree) {
        long[] prefix = new long[childFree.length + 1];
        boolean prefixValid = true;
        for (int i = 0; i < childFree.length && prefixValid; i++) {
            prefix[i + 1] = add(prefix[i], childFree[i]);
            prefixValid = prefix[i + 1] >= 0;
        }
        long[] message = new long[parentSize];
        Map<BitSet, Long> sums = new HashMap<>();
        for (int a = 0; a < parentSize; a++) {
            BitSet allowed = rows[a];
            Long known = sums.get(allowed);
            if (known == null) {
                long sum = 0;
                int lo = allowed.nextSetBit(0);
                int hi = lo < 0 ? 0 : allowed.nextClearBit(lo);
                if (lo < 0) {
                    sum = 0;
                } else if (prefixValid && allowed.nextSetBit(hi) < 0) {
                    sum = prefix[hi] - prefix[lo];                      // one run of values
                } else {
                    for (int b = lo; b >= 0; b = allowed.nextSetBit(b + 1)) {
                        sum = add(sum, childFree[b]);
                        if (sum < 0) return null;
                    }
                }
                known = sum;
                sums.put(allowed, known);
            }
            message[a] = known;
        }
        return message;
    }

    private static BigInteger[] buildBig(ConstraintPlan plan, int[] order,
                                         BigInteger[][] free, BigInteger[][] msg, BigInteger[][] freePrefix) {
        for (int idx = order.length - 1; idx >= 0; idx--) {
            int v = order[idx];
            BigInteger[] f = new BigInteger[plan.size[v]];
            Arrays.fill(f, BigInteger.ONE);
            for (int c : plan.children[v]) {
                BigInteger[] message = messagesBig(plan.fromParent[c], plan.size[v], free[c]);
                for (int a = 0; a < f.length; a++) f[a] = f[a].multiply(message[a]);
                msg[c] = message;
            }
            free[v] = f;
        }
        for (int x = 0; x < free.length; x++) {
            if (plan.values[x] != null) {
                BigInteger[] prefix = new BigInteger[plan.size[x] + 1];
                prefix[0] = BigInteger.ZERO;
                for (int i = 0; i < plan.size[x]; i++) prefix[i + 1] = prefix[i].add(free[x][i]);
                freePrefix[x] = prefix;
            }
        }
        BigInteger[] suffix = new BigInteger[plan.roots.length + 1];
        suffix[plan.roots.length] = BigInteger.ONE;
        for (int i = plan.roots.length - 1; i >= 0; i--) {
            BigInteger rootTotal = BigInteger.ZERO;
            for (BigInteger weight : free[plan.roots[i]]) rootTotal = rootTotal.add(weight);
            suffix[i] = suffix[i + 1].multiply(rootTotal);
        }
        return suffix;
    }

    private static BigInteger[] messagesBig(BitSet[] rows, int parentSize, BigInteger[] childFree) {
        BigInteger[] prefix = new BigInteger[childFree.length + 1];
        prefix[0] = BigInteger.ZERO;
        for (int i = 0; i < childFree.length; i++) prefix[i + 1] = prefix[i].add(childFree[i]);
        BigInteger[] message = new BigInteger[parentSize];
        Map<BitSet, BigInteger> sums = new HashMap<>();
        for (int a = 0; a < parentSize; a++) {
            BitSet allowed = rows[a];
            BigInteger known = sums.get(allowed);
            if (known == null) {
                int lo = allowed.nextSetBit(0);
                int hi = lo < 0 ? 0 : allowed.nextClearBit(lo);
                if (lo < 0) {
                    known = BigInteger.ZERO;
                } else if (allowed.nextSetBit(hi) < 0) {
                    known = prefix[hi].subtract(prefix[lo]);            // one run of values
                } else {
                    known = BigInteger.ZERO;
                    for (int b = lo; b >= 0; b = allowed.nextSetBit(b + 1)) known = known.add(childFree[b]);
                }
                sums.put(allowed, known);
            }
            message[a] = known;
        }
        return message;
    }

    /** a + b for a, b in [0, LIMIT); -1 when the sum reaches LIMIT. */
    private static long add(long a, long b) {
        long sum = a + b;
        return sum >= LIMIT ? -1 : sum;
    }

    /** a * b for a, b in [0, LIMIT); -1 when the product reaches LIMIT. */
    private static long multiply(long a, long b) {
        if (a == 0 || b == 0) return 0;
        if (a > (LIMIT - 1) / b) return -1;
        return a * b;
    }

    /** Sum of count(c, b) over the values b in {@code allowed} (all values if null) that agree with the fixed dimensions. */
    private BigInteger sumConsistent(int c, BitSet allowed, int k, int[] val) {
        long r = plan.range(c, k, val);
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
        BigInteger fullProduct = BigInteger.ONE;
        for (int size : dimSize) fullProduct = fullProduct.multiply(BigInteger.valueOf(size));
        return this.total.shiftLeft(2).compareTo(fullProduct) >= 0;
    }

    // ── primitive-long versions of the counting queries (used when lsuffix != null) ──

    private long sumConsistentL(int c, BitSet allowed, int k, int[] val) {
        long r = plan.range(c, k, val);
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
        if (this.lsuffix != null) return BigInteger.valueOf(completionsL(k, val));
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
        int[] val = new int[plan.dimensions];
        BigInteger remaining = rank;
        for (int k = 0; k < plan.dimensions; k++) {
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
        int[] val = new int[plan.dimensions];
        long remaining = rank;
        for (int k = 0; k < plan.dimensions; k++) {
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
