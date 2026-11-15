/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

/**
 * The set of still-available elements {@code 0..n-1}, with order-statistic queries, for the derangement
 * state machine.
 * <p>
 * For {@code n ≤ 64} the set is one {@code long} bit mask: counting the available elements below a position is a
 * single {@link Long#bitCount(long)}, and finding the k-th available element skips whole bytes by their bit
 * count. For small sets this beats the Fenwick tree, whose O(log n) bound brings no benefit at that size but
 * whose queries walk an array. Larger sets use the {@link FenwickTree}.
 * </p>
 * <p>
 * Positions in {@link #select(int)} and {@link #countBelow(int)} follow the Fenwick conventions the caller
 * already used: {@code select} takes a 1-based k and returns a 1-based position.
 * </p>
 */
final class AvailabilitySet {

    private static final int MASK_LIMIT = 64;

    private final int n;
    private final FenwickTree tree; // null when the mask is used
    private long mask;              // bit i set = element i available (only when tree == null)

    AvailabilitySet(int n) {
        this.n = n;
        this.tree = n > MASK_LIMIT ? new FenwickTree(n) : null;
    }

    /** Makes every element {@code 0..n-1} available. */
    void fillAll() {
        if (tree == null) {
            mask = n == 64 ? -1L : (1L << n) - 1;
        } else {
            for (int i = 1; i <= n; i++) {
                int v = tree.get(i);
                if (v != 1) tree.update(i, 1 - v);
            }
        }
    }

    /** Marks element {@code e} (0-based) as used. */
    void take(int e) {
        if (tree == null) mask &= ~(1L << e);
        else tree.update(e + 1, -1);
    }

    /** Marks element {@code e} (0-based) as available again. */
    void release(int e) {
        if (tree == null) mask |= 1L << e;
        else tree.update(e + 1, 1);
    }

    /** Number of available elements among {@code 0..pos-1}. */
    int countBelow(int pos) {
        if (tree != null) return tree.rsq(pos);
        return pos >= 64 ? Long.bitCount(mask) : Long.bitCount(mask & ((1L << pos) - 1));
    }

    /** 1-based position of the k-th (1-based) available element. */
    int select(int k) {
        if (tree != null) return tree.findKth(k);
        long m = mask;
        int base = 0;
        int inByte;
        while ((inByte = Long.bitCount(m & 0xFFL)) < k) { // skip whole bytes
            k -= inByte;
            m >>>= 8;
            base += 8;
        }
        for (int i = 1; i < k; i++) m &= m - 1;           // drop the lower set bits of this byte
        return base + Long.numberOfTrailingZeros(m) + 1;
    }
}
