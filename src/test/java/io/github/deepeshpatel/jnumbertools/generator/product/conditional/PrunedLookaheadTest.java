/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Completion lookahead in the pruned traversals (loop or not): off at the start, switched on once the traversal has
 * descended far more often than it produced tuples. Each shape here is dead-end heavy enough
 * to cross that switch (far more than a thousand wasted descents) and is compared with a brute-force oracle.
 */
class PrunedLookaheadTest {
    private static final int N = 5;
    private static final int SIZE = 12;
    private static final List<LexOrderStrategy> EXPLICIT =
            List.of(LexOrderStrategy.PRUNED_BITSET, LexOrderStrategy.PRUNED_DFS, LexOrderStrategy.DP);

    private static List<Integer> range(int from, int to) {
        return IntStream.range(from, to).boxed().toList();
    }

    private static List<List<Integer>> dims(int n, int size) {
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < n; d++) dims.add(range(0, size)); // distinct instances: dimensions are identified by list
        return dims;
    }

    private static ConditionalProductBuilder base(List<List<Integer>> dims) {
        ConditionalProductBuilder b = cartesianProduct.conditional().ofList(dims.get(0));
        for (int d = 1; d < dims.size(); d++) b = b.and(dims.get(d));
        return b;
    }

    /**
     * Acyclic: dimension 0 and dimension 1 each constrain only the last dimension. Dimension 0 below {@code live}
     * wants the last value in [0,6), dimension 1 wants it equal to 11, so those prefixes are dead but each one
     * is only discovered at the last dimension after the free dimensions 2..3 have been enumerated.
     */
    private static ConditionalProductBuilder acyclicDeadEnd(int live) {
        var d = dims(N, SIZE);
        return base(d)
                .when(d.get(0)).isOneOf(range(0, live)).then(d.get(N - 1)).isOneOf(range(0, 6))
                .when(d.get(1)).isOneOf(range(0, SIZE)).then(d.get(N - 1)).isOneOf(range(11, 12));
    }

    private static List<List<Object>> acyclicOracle(int live) {
        List<List<Object>> result = new ArrayList<>();
        for (int a = live; a < SIZE; a++)
            for (int b = 0; b < SIZE; b++)
                for (int c = 0; c < SIZE; c++)
                    for (int e = 0; e < SIZE; e++) result.add(List.of(a, b, c, e, 11));
        return result;
    }

    /** Looped: a chain 0..4 with allowed [0,6) per step plus a rule 0 -> 4 that forces the last value up. */
    private static ConditionalProductBuilder loopedDeadEnd(int live) {
        var d = dims(N, SIZE);
        ConditionalProductBuilder b = base(d);
        for (int i = 0; i < N - 1; i++) {
            b = b.when(d.get(i)).isOneOf(range(0, SIZE)).then(d.get(i + 1)).isOneOf(range(0, 6));
        }
        return b.when(d.get(0)).isOneOf(range(0, live)).then(d.get(N - 1)).isOneOf(range(6, SIZE));
    }

    private static List<List<Object>> loopedOracle(int live) {
        List<List<Object>> result = new ArrayList<>();
        for (int a = live; a < SIZE; a++)
            for (int b = 0; b < 6; b++)
                for (int c = 0; c < 6; c++)
                    for (int e = 0; e < 6; e++)
                        for (int f = 0; f < 6; f++) result.add(List.of(a, b, c, e, f));
        return result;
    }

    private static void drain(java.util.Iterator<?> it) {
        while (it.hasNext()) it.next();
    }

    @Test
    void acyclicDeadEndSwitchesLookaheadOnAndMatchesBruteForce() {
        var b = acyclicDeadEnd(11);
        var expected = acyclicOracle(11);
        assertEquals(12 * 12 * 12, expected.size());

        for (LexOrderStrategy s : EXPLICIT) {
            assertEquals(expected, b.lexOrder(s).stream().toList(), s.name());
        }
        assertEquals(expected, b.lexOrder(LexOrderStrategy.ADAPTIVE, 1_000).stream().toList(), "ADAPTIVE");

        var it = b.lexOrder(LexOrderStrategy.PRUNED_BITSET).iterator();
        assertTrue(it instanceof PrunedLexGenerator);
        var generator = (PrunedLexGenerator) it;
        assertFalse(generator.lookaheadEnabled(), "lookahead starts off");
        drain(it);
        assertTrue(generator.lookaheadEnabled(), "wasted descents must switch lookahead on");

        var dense = b.lexOrder(LexOrderStrategy.PRUNED_DFS).iterator();
        drain(dense);
        assertTrue(((PrunedLexGenerator) dense).lookaheadEnabled());
    }

    @Test
    void loopedDeadEndSwitchesLookaheadOnAndMatchesBruteForce() {
        var b = loopedDeadEnd(11);
        var expected = loopedOracle(11);
        assertEquals(6 * 6 * 6 * 6, expected.size());
        for (LexOrderStrategy s : EXPLICIT) {
            assertEquals(expected, b.lexOrder(s).stream().toList(), s.name());
        }
        assertEquals(expected, b.lexOrder().stream().toList(), "re-iteration");
        var it = b.lexOrder(LexOrderStrategy.PRUNED_BITSET).iterator();
        assertFalse(((PrunedLexGenerator) it).lookaheadEnabled(), "lookahead starts off, loop or not");
        drain(it);
        assertTrue(((PrunedLexGenerator) it).lookaheadEnabled(), "wasted descents must switch lookahead on");
    }

    @Test
    void unorderedParallelTraversalMatchesBruteForce() {
        for (var shape : List.of(acyclicDeadEnd(11), loopedDeadEnd(11))) {
            var expected = new HashSet<>(shape == null ? List.of() : shape.lexOrder(LexOrderStrategy.DP).stream().toList());
            for (LexOrderStrategy s : List.of(LexOrderStrategy.PRUNED_BITSET, LexOrderStrategy.PRUNED_DFS)) {
                var actual = shape.lexOrder(s).stream().parallel().toList();
                assertEquals(expected.size(), actual.size(), s.name() + " count");
                assertEquals(expected, new HashSet<>(actual), s.name());
            }
            var any = shape.anyOrder().stream().parallel().toList();
            assertEquals(expected.size(), any.size(), "anyOrder count");
            assertEquals(expected, new HashSet<>(any), "anyOrder");
        }
    }

    /**
     * Live prefixes first, then a block of dead prefixes, then live prefixes again. The dead block wastes far more
     * than 1,024 + 4 x emitted descents, so the switch happens in the middle of the iteration, with tuples
     * already emitted before it and more emitted after it.
     */
    private static ConditionalProductBuilder liveDeadLive() {
        var d = dims(N, SIZE);
        return base(d)
                .when(d.get(0)).isOneOf(range(1, 9)).then(d.get(N - 1)).isOneOf(range(0, 6))
                .when(d.get(1)).isOneOf(range(0, SIZE)).then(d.get(N - 1)).isOneOf(range(11, 12));
    }

    private static List<List<Object>> liveDeadLiveOracle() {
        List<List<Object>> result = new ArrayList<>();
        for (int a : new int[]{0, 9, 10, 11})
            for (int b = 0; b < SIZE; b++)
                for (int c = 0; c < SIZE; c++)
                    for (int e = 0; e < SIZE; e++) result.add(List.of(a, b, c, e, 11));
        return result;
    }

    @Test
    void switchingLookaheadOnMidIterationKeepsOutputAndOrder() {
        var b = liveDeadLive();
        var expected = liveDeadLiveOracle();
        assertEquals(4 * 12 * 12 * 12, expected.size());

        for (LexOrderStrategy s : List.of(LexOrderStrategy.PRUNED_BITSET, LexOrderStrategy.PRUNED_DFS)) {
            var it = b.lexOrder(s).iterator();
            var generator = (PrunedLexGenerator) it;
            var seen = new ArrayList<Object>();
            int switchedAfter = -1;
            while (it.hasNext()) {
                if (switchedAfter < 0 && generator.lookaheadEnabled()) switchedAfter = seen.size();
                seen.add(it.next());
            }
            assertEquals(expected, seen, s.name() + ": same tuples in the same order");
            assertTrue(switchedAfter > 0, s.name() + ": tuples were emitted before the switch");
            assertTrue(switchedAfter < expected.size(), s.name() + ": tuples were emitted after the switch");
        }
    }

    @Test
    void anyOrderWithLookaheadMatchesOracleSequentiallyAndInParallel() {
        for (var shape : List.of(liveDeadLive(), acyclicDeadEnd(11), loopedDeadEnd(11))) {
            var expected = new HashSet<>(shape.lexOrder(LexOrderStrategy.DP).stream().toList());

            var sequential = shape.anyOrder().stream().toList();
            assertEquals(expected.size(), sequential.size(), "sequential count");
            assertEquals(expected, new HashSet<>(sequential), "sequential");

            var parallel = shape.anyOrder().stream().parallel().toList();
            assertEquals(expected.size(), parallel.size(), "parallel count");
            assertEquals(expected, new HashSet<>(parallel), "parallel");

            var it = shape.anyOrder().iterator();
            drain(it);
            assertTrue(it instanceof PrunedLexGenerator);
            assertTrue(((PrunedLexGenerator) it).lookaheadEnabled(), "anyOrder traversal switches lookahead on too");
        }
    }

    @Test
    void emptyProductsDiscoveredByLookaheadYieldNothing() {
        for (var shape : List.of(acyclicDeadEnd(SIZE), loopedDeadEnd(SIZE))) {
            assertTrue(shape.isEmpty());
            for (LexOrderStrategy s : EXPLICIT) {
                assertTrue(shape.lexOrder(s).stream().toList().isEmpty(), s.name());
                assertTrue(shape.lexOrder(s).stream().parallel().toList().isEmpty(), s.name() + " parallel");
            }
            assertTrue(shape.lexOrder(LexOrderStrategy.ADAPTIVE, 0).stream().toList().isEmpty());
        }
    }

    @Test
    void healthyAcyclicShapesNeverEnableLookahead() {
        var d = dims(4, 8);
        ConditionalProductBuilder b = base(d);
        for (int i = 0; i < 3; i++) {
            b = b.when(d.get(i)).isOneOf(range(0, 8)).then(d.get(i + 1)).isOneOf(range(0, 7));
        }
        var expected = b.lexOrder(LexOrderStrategy.DP).stream().toList();
        assertEquals(8 * 7 * 7 * 7, expected.size());
        for (LexOrderStrategy s : List.of(LexOrderStrategy.PRUNED_BITSET, LexOrderStrategy.PRUNED_DFS)) {
            var it = b.lexOrder(s).iterator();
            var seen = new ArrayList<Object>();
            while (it.hasNext()) seen.add(it.next());
            assertEquals(expected, seen, s.name());
            assertFalse(((PrunedLexGenerator) it).lookaheadEnabled(), s.name() + ": a chain has no waste");
        }
    }
}
