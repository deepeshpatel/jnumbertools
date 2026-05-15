/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

/**
 * Fenwick Tree (Binary Indexed Tree) for efficient prefix sum operations.
 * <p>
 * Supports point updates and prefix sum queries in O(log n) time.
 * Also provides order statistics (find k-th smallest element) in O(log n) time.
 * </p>
 * <p>
 * All indices are 1-based internally, matching standard Fenwick Tree convention.
 * </p>
 *
 * @author Deepesh Patel
 * @since 3.0.2
 */
public final class FenwickTree {
    private final int[] tree;
    private final int n;
    private final int maxPower;

    /**
     * Creates a Fenwick Tree of size n, initially all zeros.
     *
     * @param n size of the tree
     */
    public FenwickTree(int n) {
        this.n = n;
        this.tree = new int[n + 1];
        // Precompute highest power of two ≤ n
        this.maxPower = Integer.highestOneBit(n);
    }

    /**
     * Updates the value at index by adding delta.
     *
     * @param index 1-based index (1 ≤ index ≤ n)
     * @param delta value to add
     */
    public void update(int index, int delta) {
        while (index <= n) {
            tree[index] += delta;
            index += index & -index;
        }
    }

    /**
     * Returns the prefix sum from 1 to index (inclusive).
     *
     * @param index 1-based index (0 returns 0)
     * @return prefix sum
     */
    public int rsq(int index) {
        int sum = 0;
        while (index > 0) {
            sum += tree[index];
            index -= index & -index;
        }
        return sum;
    }

    /**
     * Returns the value at a specific index.
     *
     * @param index 1-based index
     * @return value at index
     */
    public int get(int index) {
        return rsq(index) - rsq(index - 1);
    }

    /**
     * Finds the smallest index such that prefix sum ≥ k.
     * <p>
     * Assumes 1 ≤ k ≤ total sum of all elements.
     * Uses binary lifting for O(log n) time.
     * </p>
     *
     * @param k target cumulative sum (1-indexed)
     * @return smallest index with prefix sum ≥ k
     */
    public int findKth(int k) {
        int idx = 0;
        int bitMask = maxPower;

        while (bitMask != 0) {
            int nextIdx = idx + bitMask;
            if (nextIdx <= n && tree[nextIdx] < k) {
                k -= tree[nextIdx];
                idx = nextIdx;
            }
            bitMask >>= 1;
        }
        return idx + 1;
    }

    /**
     * Returns the size of the tree.
     */
    public int size() {
        return n;
    }
}
