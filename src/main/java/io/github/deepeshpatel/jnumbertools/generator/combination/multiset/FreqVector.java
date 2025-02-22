package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * A compact representation of a multiset combination where keys are integers from 0 to keyCount-1,
 * and values are their frequencies. Optimized for frequent updates and index-based lookups using
 * a cumulative frequency array.
 */
public class FreqVector {

    public final int[] frequencies; // Frequency of each key (index = key, value = freq)
    private final int size;         // Total size limit (sum of frequencies)
    private final int keyCount;     // Number of possible keys (0 to keyCount-1)
    private final int[] cumulativeFreq; // Cumulative frequency array for quick lookups
    private int total;              // Running total of frequencies (optimization)

    public FreqVector(int size, int keyCount) {
        this.size = size;
        this.keyCount = keyCount;
        this.frequencies = new int[keyCount];
        this.cumulativeFreq = new int[keyCount];
        this.total = 0; // Initialize total
    }

    public FreqVector(FreqVector freqVector) {
        this.size = freqVector.size;
        this.keyCount = freqVector.keyCount;
        this.frequencies = new int[keyCount];
        this.cumulativeFreq = new int[keyCount];
        System.arraycopy(freqVector.frequencies, 0, frequencies, 0, keyCount);
        System.arraycopy(freqVector.cumulativeFreq, 0, cumulativeFreq, 0, keyCount);
        this.total = freqVector.total;
    }

    public int size() {
        return size;
    }

    public void add(int k) {
        if (total >= size) {
            throw new IndexOutOfBoundsException("Cannot add more than " + size + " elements");
        }
        if (k >= keyCount || k < 0) {
            throw new IllegalArgumentException("Valid key range is 0 to " + (keyCount - 1));
        }
        frequencies[k]++;
        total++;
        updateCumulativeFreqFrom(k);
    }

    private void updateCumulativeFreqFrom(int startIndex) {
        // Incremental update: adjust from startIndex onward
        if (startIndex == 0) {
            cumulativeFreq[0] = frequencies[0];
            for (int i = 1; i < keyCount; i++) {
                cumulativeFreq[i] = cumulativeFreq[i - 1] + frequencies[i];
            }
        } else {
            for (int i = startIndex; i < keyCount; i++) {
                cumulativeFreq[i] = cumulativeFreq[i - 1] + frequencies[i];
            }
        }
    }

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
        // Only update from the smaller index to avoid redundant recalculations
        updateCumulativeFreqFrom(Math.min(oldValue, key));
    }

    public boolean isEmpty() {
        return total == 0; // Use total instead of array access
    }

    public boolean remove(int key) {
        if (key < 0 || key >= keyCount || frequencies[key] == 0) {
            return false;
        }
        frequencies[key]--;
        total--;
        updateCumulativeFreqFrom(key);
        return true;
    }

    public int findValueAtIndex(int index) {
        if (index < 0 || cumulativeFreq.length == 0 || index >= total) {
            return -1; // Beyond current elements or empty
        }
        int left = 0, right = keyCount - 1;
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

    // For debugging only
    public List<Integer> asList() {
        List<Integer> list = new ArrayList<>(size);
        for (int k = 0; k < keyCount; k++) {
            for (int i = 0; i < frequencies[k]; i++) {
                list.add(k);
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return Arrays.toString(frequencies) + " " + Arrays.toString(cumulativeFreq);
    }

    // Added for optimization verification
    public int getTotal() {
        return total;
    }
}