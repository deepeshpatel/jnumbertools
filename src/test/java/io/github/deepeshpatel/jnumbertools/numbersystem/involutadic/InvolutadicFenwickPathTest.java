/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the Fenwick-tree fast path of {@link InvolutadicAlgorithms} against the
 * linear-scan reference path, and exercises the Fenwick path directly for n above
 * the dispatch threshold (which the existing small-n suites never reach).
 *
 * <p>Strategy:
 * <ul>
 *   <li><b>Agreement (small n):</b> the public methods dispatch to the scan path for
 *       n &lt; N_THRESHOLD. To compare scan vs Fenwick on the same small n we cannot
 *       go through the public API (it would pick scan both times), so we verify the
 *       two paths indirectly: the scan path is already validated by the existing
 *       exhaustive suite, and here we confirm full round-trip consistency for n
 *       at and above the threshold, where Fenwick is live. A maximal-rank and a
 *       random-rank battery covers the order-statistic arithmetic that differs
 *       between the paths.</li>
 *   <li><b>Fenwick-live (large n):</b> for n in {100, 150, 256, 500} we check
 *       encode→decode = id, unrank→rank = id, toInvolution(encode) = unrank,
 *       fromInvolution(unrank) = encode, on random and boundary ranks.</li>
 * </ul>
 */
@DisplayName("Involutadic Fenwick fast-path")
class InvolutadicFenwickPathTest {

    private Calculator calculator;
    private InvolutadicAlgorithms alg;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
        alg = new InvolutadicAlgorithms(calculator);
    }

    private static boolean isInvolution(int[] pi) {
        for (int i = 0; i < pi.length; i++) {
            if (pi[i] < 0 || pi[i] >= pi.length) return false;
            if (pi[pi[i]] != i) return false;
        }
        return true;
    }

    /** All four round-trips for a single (rank, n), exercising whichever path n selects. */
    private void assertAllConsistent(BigInteger rank, int n) {
        int[] digits = alg.encode(rank, n);
        assertEquals(n, digits.length, "digit width");
        assertEquals(rank, alg.decode(digits), "encode->decode n=" + n + " rank=" + rank);

        int[] inv = alg.unrank(rank, n);
        assertTrue(isInvolution(inv), "valid involution n=" + n + " rank=" + rank);
        assertEquals(rank, alg.rank(inv), "unrank->rank n=" + n + " rank=" + rank);

        assertArrayEquals(inv, alg.toInvolution(digits),
                "toInvolution(encode)==unrank n=" + n + " rank=" + rank);
        assertArrayEquals(digits, alg.fromInvolution(inv),
                "fromInvolution(unrank)==encode n=" + n + " rank=" + rank);
    }

    @Nested
    @DisplayName("Fenwick-live round-trips (n >= threshold)")
    class FenwickLive {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {100, 150, 256, 500})
        void randomRanksRoundTrip(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            Random rng = new Random(42L + n);
            int bitLen = total.bitLength();
            for (int trial = 0; trial < 200; trial++) {
                BigInteger r;
                do { r = new BigInteger(bitLen, rng); } while (r.compareTo(total) >= 0);
                assertAllConsistent(r, n);
            }
        }

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {100, 101, 150, 256, 500})
        void boundaryRanksRoundTrip(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            assertAllConsistent(BigInteger.ZERO, n);                       // first
            assertAllConsistent(total.subtract(BigInteger.ONE), n);        // last
            assertAllConsistent(total.divide(BigInteger.TWO), n);          // middle
        }

        @Test
        @DisplayName("Rank 0 is the identity involution (Fenwick path)")
        void rank0Identity() {
            int n = 200;
            int[] inv = alg.unrank(BigInteger.ZERO, n);
            for (int i = 0; i < n; i++) assertEquals(i, inv[i], "fixed point at " + i);
        }
    }

    @Nested
    @DisplayName("Threshold continuity: behaviour matches across the n=99/100 boundary")
    class ThresholdContinuity {

        // For n just below and just above the threshold, the lexicographic stream
        // must be internally consistent regardless of which path is taken. We check
        // that consecutive ranks remain lexicographically increasing involutions.
        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {99, 100})
        void firstRanksAreLexIncreasing(int n) {
            int[] prev = alg.unrank(BigInteger.ZERO, n);
            for (long r = 1; r <= 500; r++) {
                int[] cur = alg.unrank(BigInteger.valueOf(r), n);
                assertTrue(lexLess(prev, cur),
                        "lex order broken n=" + n + " at rank " + r);
                assertEquals(BigInteger.valueOf(r), alg.rank(cur), "rank round-trip n=" + n);
                prev = cur;
            }
        }

        private boolean lexLess(int[] a, int[] b) {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) return a[i] < b[i];
            }
            return false;
        }
    }
}