/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for Derangadic wrapper class.
 *
 * @author Deepesh Patel & Aditya Patel
 */
class DerangadicTest {

    private static final Calculator CALC = new Calculator();
    private static final DerangadicAlgorithms ALGO = new DerangadicAlgorithms(CALC);

    @Test
    @DisplayName("Derangadic.of() should create correct instances from rank")
    void testOf() {
        // n=4, D₄=9
        int[] expected0 = ALGO.toDerangadic(0, 4);
        Derangadic d0 = Derangadic.of(0, 4, CALC);
        assertEquals(BigInteger.ZERO, d0.decimalValue());
        assertArrayEquals(expected0, d0.derangadicValues());
        assertEquals(4, d0.order());

        int[] expected1 = ALGO.toDerangadic(1, 4);
        Derangadic d1 = Derangadic.of(1, 4, CALC);
        assertEquals(BigInteger.ONE, d1.decimalValue());
        assertArrayEquals(expected1, d1.derangadicValues());

        int[] expected8 = ALGO.toDerangadic(8, 4);
        Derangadic d8 = Derangadic.of(8, 4, CALC);
        assertEquals(BigInteger.valueOf(8), d8.decimalValue());
        assertArrayEquals(expected8, d8.derangadicValues());
    }

    @Test
    @DisplayName("Derangadic.fromDerangement() should create correct instances from derangement")
    void testFromDerangement() {
        // Rank 0 derangement for n=4: [1, 0, 3, 2]
        int[] derangement = {1, 0, 3, 2};
        Derangadic d = Derangadic.fromDerangement(derangement, 4, CALC);
        assertEquals(BigInteger.ZERO, d.decimalValue());
        assertTrue(arraysEqualIgnoringTrailingZeros(new int[]{0, 0}, d.derangadicValues()));

        // Rank 1 derangement for n=4: [1, 2, 3, 0]
        int[] derangement2 = {1, 2, 3, 0};
        Derangadic d2 = Derangadic.fromDerangement(derangement2, 4, CALC);
        assertEquals(BigInteger.ONE, d2.decimalValue());
        assertTrue(arraysEqualIgnoringTrailingZeros(new int[]{0, 1, 1}, d2.derangadicValues()));
    }

    @Test
    @DisplayName("toString() canonicalizes by trimming trailing zeros")
    void testToString() {
        // toString() now trims trailing zeros, so output is deterministic.
        Derangadic d0 = Derangadic.of(0, 4, CALC);
        assertEquals("[0](4)", d0.toString());

        Derangadic d1 = Derangadic.of(1, 4, CALC);
        assertEquals("[0, 1, 1](4)", d1.toString());
    }

    @Test
    @DisplayName("getMinimalSize() should return length of digit array")
    void testGetMinimalSize() {
        Derangadic d0 = Derangadic.of(0, 4, CALC);
        int size0 = d0.getMinimalSize();
        assertTrue(size0 == 2 || size0 == 1, "Size for rank 0 should be 1 or 2, got: " + size0);

        Derangadic d1 = Derangadic.of(1, 4, CALC);
        int size1 = d1.getMinimalSize();
        assertTrue(size1 == 3 || size1 == 4, "Size for rank 1 should be 3 or 4, got: " + size1);
    }

    @Test
    @DisplayName("toDerangement() should convert back to original derangement")
    void testToDerangement() {
        for (int n = 3; n <= 6; n++) {
            BigInteger total = ALGO.derangementCount(n);

            for (long rank = 0; rank < total.longValue() && rank < 50; rank++) {
                Derangadic d = Derangadic.of(rank, n, CALC);
                int[] derangement = d.toDerangement();

                assertTrue(isValidDerangement(derangement),
                        String.format("Invalid derangement at rank=%d, n=%d", rank, n));

                Derangadic d2 = Derangadic.fromDerangement(derangement, n, CALC);
                assertEquals(d.decimalValue(), d2.decimalValue(),
                        String.format("Round-trip failed at rank=%d, n=%d", rank, n));
            }
        }
    }

    @Test
    @DisplayName("equals()/hashCode() use (order, decimalValue) and ignore trailing-zero padding")
    void testEqualsAndHashCode() {
        Derangadic d1 = Derangadic.of(5, 4, CALC);
        Derangadic d2 = Derangadic.of(5, 4, CALC);
        Derangadic d3 = Derangadic.of(6, 4, CALC);
        Derangadic d4 = Derangadic.of(5, 5, CALC);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1, d4);
        assertNotEquals(d1, null);
        assertNotEquals(d1, "some string");

        // Cross-factory equality: of() and fromDerangement() must agree on equality
        // even if their digit arrays differ by trailing zeros.
        Derangadic viaRank = Derangadic.of(0, 4, CALC);
        Derangadic viaDer  = Derangadic.fromDerangement(new int[]{1, 0, 3, 2}, 4, CALC);
        assertEquals(viaRank, viaDer);
        assertEquals(viaRank.hashCode(), viaDer.hashCode());
    }

    @Test
    @DisplayName("Invalid inputs should throw appropriate exceptions")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> Derangadic.of(-1, 4, CALC));
        assertThrows(IllegalArgumentException.class, () -> Derangadic.of(BigInteger.valueOf(-1), 4, CALC));

        BigInteger total = ALGO.derangementCount(4);
        assertThrows(IllegalArgumentException.class, () -> Derangadic.of(total, 4, CALC));

        int[] invalidDerangement = {0, 2, 1};
        assertThrows(IllegalArgumentException.class, () ->
                Derangadic.fromDerangement(invalidDerangement, 3, CALC));

        assertThrows(NullPointerException.class, () -> Derangadic.of(0, 4, null));
        assertThrows(NullPointerException.class,
                () -> Derangadic.fromDerangement(new int[]{1, 0, 3, 2}, 4, null));
    }

    @Test
    @DisplayName("Round-trip through both APIs should be consistent")
    void testBothApisConsistent() {
        for (int n = 3; n <= 6; n++) {
            BigInteger total = ALGO.derangementCount(n);

            for (long rank = 0; rank < total.longValue() && rank < 50; rank++) {
                Derangadic d = Derangadic.of(rank, n, CALC);
                int[] derangementFromWrapper = d.toDerangement();
                int[] derangementFromAlgo = ALGO.unrank(BigInteger.valueOf(rank), n);

                assertArrayEquals(derangementFromAlgo, derangementFromWrapper,
                        String.format("Inconsistent derangement at rank=%d, n=%d", rank, n));
            }
        }
    }

    @Test
    @DisplayName("Static unrank()/rank()/count() shortcuts agree with the wrapper")
    void testStaticShortcuts() {
        for (int n = 3; n <= 7; n++) {
            BigInteger total = Derangadic.count(n, CALC);
            assertEquals(ALGO.derangementCount(n), total);

            for (long r = 0; r < total.longValue(); r++) {
                int[] viaStatic = Derangadic.unrank(r, n, CALC);
                int[] viaWrapper = Derangadic.of(r, n, CALC).toDerangement();
                assertArrayEquals(viaWrapper, viaStatic,
                        String.format("unrank mismatch at rank=%d, n=%d", r, n));
                assertTrue(isValidDerangement(viaStatic));

                BigInteger rankBack = Derangadic.rank(viaStatic, n, CALC);
                assertEquals(BigInteger.valueOf(r), rankBack,
                        String.format("rank round-trip failed at rank=%d, n=%d", r, n));
            }
        }

        // BigInteger overload
        int[] d = Derangadic.unrank(BigInteger.ZERO, 4, CALC);
        assertArrayEquals(new int[]{1, 0, 3, 2}, d);

        // Argument validation
        assertThrows(NullPointerException.class, () -> Derangadic.unrank(0L, 4, null));
        assertThrows(NullPointerException.class, () -> Derangadic.rank(new int[]{1, 0, 3, 2}, 4, null));
        assertThrows(IllegalArgumentException.class,
                () -> Derangadic.unrank(Derangadic.count(4, CALC), 4, CALC));
    }

    @Test
    @DisplayName("Instance next() walks rank+1 and rejects past-the-end")
    void testInstanceNext() {
        for (int n = 3; n <= 6; n++) {
            BigInteger total = Derangadic.count(n, CALC);
            Derangadic d = Derangadic.of(0, n, CALC);
            for (long r = 1; r < total.longValue(); r++) {
                Derangadic dn = d.next();
                assertEquals(BigInteger.valueOf(r), dn.decimalValue(),
                        String.format("next() rank mismatch at n=%d, r=%d", n, r));
                assertArrayEquals(Derangadic.unrank(r, n, CALC), dn.toDerangement(),
                        String.format("next() derangement mismatch at n=%d, r=%d", n, r));
                d = dn;
            }
            // Past the end: rank == D_n is out of range.
            Derangadic last = d; // currently at D_n - 1
            assertThrows(IllegalArgumentException.class, last::next);
        }
        // Null-calculator semantics inherited: not applicable (next() reuses
        // the stored engine), but a deserialized instance must throw ISE; we
        // cannot easily simulate that here.
    }

    @Test
    @DisplayName("Walker yields D_n distinct derangements in lexicographical order")
    void testWalker() {
        for (int n = 3; n <= 6; n++) {
            Derangadic.Walker w = Derangadic.walker(n, CALC);
            BigInteger total = Derangadic.count(n, CALC);

            // Sequence via advance() + current()
            int count = 0;
            int[] prev = null;
            do {
                int[] cur = w.currentCopy(); // we WILL retain → copy
                assertTrue(isValidDerangement(cur), "invalid at n=" + n + " count=" + count);
                if (prev != null) {
                    assertTrue(lexLess(prev, cur),
                            String.format("not in lex order at n=%d count=%d", n, count));
                }
                // Cross-check against static unrank
                assertArrayEquals(Derangadic.unrank(count, n, CALC), cur,
                        String.format("walker disagrees with unrank at n=%d rank=%d", n, count));
                prev = cur;
                count++;
            } while (w.advance());

            assertEquals(total.longValue(), count,
                    "walker yielded wrong number of derangements at n=" + n);
            // After exhaustion, further advance must return false.
            assertFalse(w.advance());
        }
    }

    @Test
    @DisplayName("Walker.iterator() returns clones (safe to retain)")
    void testWalkerIterator() {
        int n = 5;
        Derangadic.Walker w = Derangadic.walker(n, CALC);
        java.util.List<int[]> collected = new java.util.ArrayList<>();
        for (int[] d : w) collected.add(d);

        assertEquals(Derangadic.count(n, CALC).longValue(), collected.size());
        // The retained arrays must NOT alias each other (clones, not live).
        for (int i = 1; i < collected.size(); i++) {
            assertNotSame(collected.get(i - 1), collected.get(i));
        }
        // Each retained array must still equal the corresponding unrank.
        for (int r = 0; r < collected.size(); r++) {
            assertArrayEquals(Derangadic.unrank(r, n, CALC), collected.get(r));
        }
    }

    @Test
    @DisplayName("Walker.current() returns the same live array across calls")
    void testWalkerLiveArrayIdentity() {
        Derangadic.Walker w = Derangadic.walker(5, CALC);
        int[] a = w.current();
        w.advance();
        int[] b = w.current();
        // Live array — same backing storage, but contents updated.
        assertSame(a, b);
    }

    @Test
    @DisplayName("Walker rejects invalid construction")
    void testWalkerInvalid() {
        assertThrows(NullPointerException.class, () -> Derangadic.walker(5, null));
        assertThrows(IllegalArgumentException.class, () -> Derangadic.walker(1, CALC));
    }

    // ==================== Utility Methods ====================

    private static boolean lexLess(int[] a, int[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            if (a[i] < b[i]) return true;
            if (a[i] > b[i]) return false;
        }
        return a.length < b.length;
    }

    private static boolean isValidDerangement(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            if (arr[i] == i) return false;
        }
        boolean[] seen = new boolean[n];
        for (int val : arr) {
            if (val < 0 || val >= n) return false;
            if (seen[val]) return false;
            seen[val] = true;
        }
        return true;
    }

    private static boolean arraysEqualIgnoringTrailingZeros(int[] a, int[] b) {
        return Arrays.equals(trimTrailingZeros(a), trimTrailingZeros(b));
    }

    private static int[] trimTrailingZeros(int[] arr) {
        if (arr.length == 0) return arr;
        int lastNonZero = arr.length - 1;
        while (lastNonZero >= 0 && arr[lastNonZero] == 0) {
            lastNonZero--;
        }
        if (lastNonZero < 0) return new int[]{0};
        int[] result = new int[lastNonZero + 1];
        System.arraycopy(arr, 0, result, 0, lastNonZero + 1);
        return result;
    }
}
