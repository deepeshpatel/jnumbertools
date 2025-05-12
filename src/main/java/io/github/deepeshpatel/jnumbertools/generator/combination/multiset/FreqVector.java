/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A compact representation of a multiset combination as a frequency vector.
 * <p>
 * This class represents a multiset combination of rᵣ elements from nₙ distinct keys (integers from 0 to nₙ−1),
 * where each key has a frequency (e.g., [2, 1, 0] means two of key 0, one of key 1, none of key 2).
 * It is optimized for frequent updates and index-based lookups using a cumulative frequency array,
 * enabling efficient operations in multiset combination algorithms.
 * </p>
 */
public class FreqVector {

    /**
     * Frequency of each key (index = key kₖ, value = frequency).
     */
    public final int[] frequencies;

    /**
     * Total size limit of the combination (rᵣ, sum of frequencies).
     */
    private final int r;

    /**
     * Number of distinct keys (nₙ, keys from 0 to nₙ−1).
     */
    private final int n;

    /**
     * Cumulative frequency array for fast index-based lookups.
     */
    private final int[] cumulativeFreq;

    /**
     * Running total of frequencies for optimization.
     */
    private int total;

    /**
     * Constructs an empty frequency vector for a multiset combination.
     *
     * @param r the size of the combination (rᵣ); must be non-negative
     * @param n the number of distinct keys (nₙ); must be positive
     * @return a new FreqVector instance
     * @throws IllegalArgumentException if rᵣ is negative or nₙ is non-positive
     */
    public FreqVector(int r, int n) {
        this.r = r;
        this.n = n;
        this.frequencies = new int[n];
        this.cumulativeFreq = new int[n];
        this.total = 0;
    }

    /**
     * Constructs a frequency vector by copying an existing one.
     *
     * @param freqVector the FreqVector to copy
     * @return a new FreqVector instance with the same state
     */
    public FreqVector(FreqVector freqVector) {
        this.r = freqVector.r;
        this.n = freqVector.n;
        this.frequencies = new int[n];
        this.cumulativeFreq = new int[n];
        System.arraycopy(freqVector.frequencies, 0, frequencies, 0, n);
        System.arraycopy(freqVector.cumulativeFreq, 0, cumulativeFreq, 0, n);
        this.total = freqVector.total;
    }

    /**
     * Returns the size of the combination (rᵣ).
     *
     * @return the total size limit
     */
    public int size() {
        return r;
    }

    /**
     * Adds one occurrence of the specified key to the frequency vector.
     *
     * @param k the key to add (0 ≤ kₖ < nₙ)
     * @throws IndexOutOfBoundsException if adding exceeds the size limit rᵣ
     * @throws IllegalArgumentException if kₖ is out of bounds
     */
    public void add(int k) {
        if (total >= r) {
            throw new IndexOutOfBoundsException("Cannot add more than rᵣ=" + r + " elements");
        }
        if (k >= n || k < 0) {
            throw new IllegalArgumentException("Valid key range is 0 to " + (n - 1));
        }
        frequencies[k]++;
        total++;
        updateCumulativeFreqFrom(k);
    }

    /**
     * Updates the cumulative frequency array starting from the given index.
     *
     * @param startIndex the index from which to update (0 ≤ startIndex < nₙ)
     */
    private void updateCumulativeFreqFrom(int startIndex) {
        if (startIndex == 0) {
            cumulativeFreq[0] = frequencies[0];
            for (int i = 1; i < n; i++) {
                cumulativeFreq[i] = cumulativeFreq[i - 1] + frequencies[i];
            }
        } else {
            for (int i = startIndex; i < n; i++) {
                cumulativeFreq[i] = cumulativeFreq[i - 1] + frequencies[i];
            }
        }
    }

    /**
     * Sets the element at the specified index to the given key.
     * <p>
     * If the index is beyond the current total, adds the key as if using {@code add(k)}.
     * If the key at the index is the same, no change is made.
     * </p>
     *
     * @param atIndex the index to set (0 ≤ atIndex < rᵣ)
     * @param key the key to set (0 ≤ key < nₙ)
     * @throws IllegalArgumentException if key is out of bounds
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
        frequencies[oldValue]--;
        frequencies[key]++;
        updateCumulativeFreqFrom(Math.min(oldValue, key));
    }

    /**
     * Checks if the frequency vector is empty.
     *
     * @return {@code true} if no elements are present; {@code false} otherwise
     */
    public boolean isEmpty() {
        return total == 0;
    }

    /**
     * Removes one occurrence of the specified key from the frequency vector.
     *
     * @param key the key to remove (0 ≤ key < nₙ)
     * @return {@code true} if the key was removed; {@code false} if the key was not present
     * @throws IllegalArgumentException if key is out of bounds
     */
    public boolean remove(int key) {
        if (key < 0 || key >= n || frequencies[key] == 0) {
            return false;
        }
        frequencies[key]--;
        total--;
        updateCumulativeFreqFrom(key);
        return true;
    }

    /**
     * Finds the key at the specified index in the multiset combination.
     * <p>
     * Uses binary search on the cumulative frequency array to locate the key.
     * </p>
     *
     * @param index the index to query (0 ≤ index < total)
     * @return the key at the index, or -1 if the index is out of bounds
     */
    public int findValueAtIndex(int index) {
        if (index < 0 || cumulativeFreq.length == 0 || index >= total) {
            return -1;
        }
        int left = 0, right = n - 1;
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
     * Converts the frequency vector to a list of keys for debugging.
     * <p>
     * Each key appears in the list as many times as its frequency.
     * </p>
     *
     * @return a list of keys representing the multiset combination
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
     * Returns a string representation of the frequency and cumulative frequency arrays.
     *
     * @return a string in the format "[frequencies] [cumulativeFreq]"
     */
    @Override
    public String toString() {
        return Arrays.toString(frequencies) + " " + Arrays.toString(cumulativeFreq);
    }

    /**
     * Returns the current total number of elements in the frequency vector.
     *
     * @return the total sum of frequencies
     */
    public int getTotal() {
        return total;
    }
}