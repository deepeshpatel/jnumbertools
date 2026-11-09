/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A compact representation of a multiset combination as a frequency vector.
 * <p>
 * This class represents a multiset combination of r elements from n distinct keys
 * (integers from 0 to n-1), where each key has a frequency.
 * </p>
 * <p>
 * For smaller numbers of distinct keys, cumulative frequencies are maintained
 * in an array. For larger numbers, a Fenwick tree is used to make updates and
 * index-based lookups more efficient.
 * </p>
 *
 * @author Deepesh Patel
 */
public class FreqVector {

    /**
     * Switch to Fenwick tree when the number of distinct keys reaches this value.
     */
    private static final int FENWICK_THRESHOLD = 128;

    /**
     * Frequency of each key (index = key, value = frequency).
     */
    public final int[] frequencies;

    /**
     * Total size limit of the combination.
     */
    private final int r;

    /**
     * Number of distinct keys.
     */
    private final int n;

    /**
     * Cumulative frequency array, used for smaller vectors.
     * Null when the Fenwick tree backend is used.
     */
    private final int[] cumulativeFreq;

    /**
     * Fenwick tree, used for larger vectors.
     * Null when the cumulative array backend is used.
     */
    private final FenwickTree fenwickTree;

    /**
     * Running total of frequencies.
     */
    private int total;

    /**
     * Constructs an empty frequency vector for a multiset combination.
     *
     * @param r the size of the combination
     * @param n the number of distinct keys
     */
    public FreqVector(int r, int n) {
        this.r = r;
        this.n = n;
        this.frequencies = new int[n];

        if (n >= FENWICK_THRESHOLD) {
            this.cumulativeFreq = null;
            this.fenwickTree = new FenwickTree(n);
        } else {
            this.cumulativeFreq = new int[n];
            this.fenwickTree = null;
        }

        this.total = 0;
    }

    /**
     * Constructs a frequency vector by copying an existing one.
     *
     * @param freqVector the FreqVector to copy
     */
    public FreqVector(FreqVector freqVector) {
        this.r = freqVector.r;
        this.n = freqVector.n;
        this.frequencies = Arrays.copyOf(freqVector.frequencies, n);

        if (freqVector.fenwickTree != null) {
            this.cumulativeFreq = null;
            this.fenwickTree = new FenwickTree(freqVector.fenwickTree);
        } else {
            this.cumulativeFreq = Arrays.copyOf(freqVector.cumulativeFreq, n);
            this.fenwickTree = null;
        }

        this.total = freqVector.total;
    }

    /**
     * Returns the size of the combination.
     *
     * @return the total size limit
     */
    public int size() {
        return r;
    }

    /**
     * Adds one occurrence of the specified key.
     *
     * @param k the key to add
     * @throws IndexOutOfBoundsException if adding exceeds the size limit
     * @throws IllegalArgumentException if the key is out of bounds
     */
    public void add(int k) {
        if (total >= r) {
            throw new IndexOutOfBoundsException(
                    "Cannot add more than r=" + r + " elements");
        }

        if (k >= n || k < 0) {
            throw new IllegalArgumentException(
                    "Valid key range is 0 to " + (n - 1));
        }

        frequencies[k]++;
        total++;
        updateIndex(k, 1);
    }

    /**
     * Updates the selected indexing backend after a frequency change.
     *
     * @param key the key whose frequency changed
     * @param delta the frequency change
     */
    private void updateIndex(int key, int delta) {
        if (fenwickTree != null) {
            fenwickTree.update(key + 1, delta);
        } else {
            updateCumulativeFreqFrom(key);
        }
    }

    /**
     * Updates the cumulative frequency array starting from the given index.
     *
     * @param startIndex the index from which to update
     */
    private void updateCumulativeFreqFrom(int startIndex) {
        if (startIndex == 0) {
            cumulativeFreq[0] = frequencies[0];

            for (int i = 1; i < n; i++) {
                cumulativeFreq[i] =
                        cumulativeFreq[i - 1] + frequencies[i];
            }
        } else {
            for (int i = startIndex; i < n; i++) {
                cumulativeFreq[i] =
                        cumulativeFreq[i - 1] + frequencies[i];
            }
        }
    }

    /**
     * Sets the element at the specified index to the given key.
     * <p>
     * If the index is beyond the current total, adds the key.
     * If the key at the index is already the same, no change is made.
     * </p>
     *
     * @param atIndex the index to set
     * @param key the key to set
     */
    public void set(int atIndex, int key) {
        int oldValue = findValueAtIndex(atIndex);

        if (oldValue == -1) {
            add(key);
            return;
        }

        if (key == oldValue) {
            return;
        }

        if (key < 0 || key >= n) {
            throw new IllegalArgumentException(
                    "Valid key range is 0 to " + (n - 1));
        }

        frequencies[oldValue]--;
        frequencies[key]++;

        if (fenwickTree != null) {
            fenwickTree.update(oldValue + 1, -1);
            fenwickTree.update(key + 1, 1);
        } else {
            updateCumulativeFreqFrom(Math.min(oldValue, key));
        }
    }

    /**
     * Checks if the frequency vector is empty.
     *
     * @return true if no elements are present
     */
    public boolean isEmpty() {
        return total == 0;
    }

    /**
     * Removes one occurrence of the specified key.
     *
     * @param key the key to remove
     * @return true if the key was removed; false otherwise
     */
    public boolean remove(int key) {
        if (key < 0 || key >= n || frequencies[key] == 0) {
            return false;
        }

        frequencies[key]--;
        total--;
        updateIndex(key, -1);

        return true;
    }

    /**
     * Finds the key at the specified index in the multiset combination.
     *
     * @param index the zero-based index to query
     * @return the key at the index, or -1 if the index is out of bounds
     */
    public int findValueAtIndex(int index) {
        if (index < 0 || index >= total || n == 0) {
            return -1;
        }

        if (fenwickTree != null) {
            return fenwickTree.findKth(index + 1) - 1;
        }

        int left = 0;
        int right = n - 1;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (cumulativeFreq[mid] > index) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Converts the frequency vector to a list of keys.
     *
     * @return a list containing each key according to its frequency
     */
    public List<Integer> asList() {
        List<Integer> list = new ArrayList<>(r);

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < frequencies[k]; i++) {
                list.add(k);
            }
        }

        return list;
    }

    /**
     * Returns a string representation of the frequency vector.
     *
     * @return the frequency and cumulative frequency representation
     */
    @Override
    public String toString() {
        if (fenwickTree != null) {
            return Arrays.toString(frequencies)
                    + " FenwickTree(total=" + total + ")";
        }

        return Arrays.toString(frequencies)
                + " " + Arrays.toString(cumulativeFreq);
    }

    /**
     * Returns the current total number of elements.
     *
     * @return the total sum of frequencies
     */
    public int getTotal() {
        return total;
    }
}
