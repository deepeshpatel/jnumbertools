/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FenwickTree")
class FenwickTreeTest {

    private static FenwickTree builtByUpdates(int n) {
        FenwickTree tree = new FenwickTree(n);
        for (int i = 1; i <= n; i++) {
            tree.update(i, 1);
        }
        return tree;
    }

    @Test
    @DisplayName("ofOnes is indistinguishable from n single updates, for every query")
    void ofOnesMatchesRepeatedUpdates() {
        for (int n : new int[]{0, 1, 2, 3, 7, 8, 9, 63, 64, 65, 127, 128, 129, 1000, 1023, 1024, 1025}) {
            FenwickTree direct = FenwickTree.ofOnes(n);
            FenwickTree reference = builtByUpdates(n);

            assertEquals(n, direct.size());
            for (int i = 0; i <= n; i++) {
                assertEquals(reference.rsq(i), direct.rsq(i), "rsq n=" + n + " i=" + i);
                assertEquals(i, direct.rsq(i), "prefix of ones n=" + n);
            }
            for (int i = 1; i <= n; i++) {
                assertEquals(1, direct.get(i), "get n=" + n + " i=" + i);
                assertEquals(i, direct.findKth(i), "findKth n=" + n + " k=" + i);
            }
        }
    }

    @Test
    @DisplayName("ofOnes keeps behaving like the update-built tree after removals and re-additions")
    void ofOnesSurvivesFurtherUpdates() {
        Random random = new Random(8);
        for (int n : new int[]{5, 100, 129, 1000}) {
            FenwickTree direct = FenwickTree.ofOnes(n);
            FenwickTree reference = builtByUpdates(n);
            boolean[] present = new boolean[n + 1];
            java.util.Arrays.fill(present, true);
            int remaining = n;

            for (int step = 0; step < 500; step++) {
                int i = 1 + random.nextInt(n);
                int delta = present[i] ? -1 : 1;
                present[i] = !present[i];
                remaining += delta;
                direct.update(i, delta);
                reference.update(i, delta);

                int probe = 1 + random.nextInt(n);
                assertEquals(reference.rsq(probe), direct.rsq(probe), "rsq n=" + n);
                if (remaining > 0) {
                    int k = 1 + random.nextInt(remaining);
                    assertEquals(reference.findKth(k), direct.findKth(k), "findKth n=" + n + " k=" + k);
                }
            }
        }
    }
}
