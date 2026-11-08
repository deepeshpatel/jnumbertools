/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

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
        this.maxPower = Integer.highestOneBit(n);
    }

    /**
     * Creates a Fenwick Tree of size n in which every position holds 1.
     * <p>
     * Built directly in O(n): the node at index i covers the {@code i & -i} positions ending at i, so it holds
     * exactly that many ones. This replaces n calls to {@link #update(int, int)} (O(n log n)).
     * </p>
     *
     * @param n size of the tree
     * @return a tree whose {@link #get(int)} is 1 at every index in [1, n]
     */
    public static FenwickTree ofOnes(int n) {
        FenwickTree tree = new FenwickTree(n);
        for (int i = 1; i <= n; i++) {
            tree.tree[i] = i & -i;
        }
        return tree;
    }

    /**
     * Creates a copy of an existing Fenwick Tree.
     *
     * @param other the tree to copy
     */
    public FenwickTree(FenwickTree other) {
        this.n = other.n;
        this.tree = other.tree.clone();
        this.maxPower = other.maxPower;
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
     * Finds the smallest index such that prefix sum is at least k.
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
     *
     * @return the number of positions in the tree
     */
    public int size() {
        return n;
    }
}
