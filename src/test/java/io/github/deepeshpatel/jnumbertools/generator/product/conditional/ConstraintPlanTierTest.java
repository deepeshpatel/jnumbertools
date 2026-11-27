/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.base.Product;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the two lookahead tiers of {@link ConstraintPlan}: the feasibility tier answers iteration and
 * {@code isEmpty()} without building exact counts, and the counting tier is built only when asked for.
 */
class ConstraintPlanTierTest {

    private static ConstraintPlan planOf(Object builder) throws Exception {
        Field model = ConditionalProductBuilder.class.getDeclaredField("model");
        model.setAccessible(true);
        return ((ConstraintModel) model.get(builder)).plan();
    }

    private static List<Integer> values(int dimension, int size) {
        return IntStream.range(0, size).map(v -> dimension * 100 + v).boxed().toList();
    }

    private record Rule(int trigger, List<Integer> triggerValues, int target, List<Integer> allowed) {}

    private static ConditionalProductBuilder build(List<List<Integer>> dims, List<Rule> rules) {
        var builder = Product.conditional(dims.toArray(List<?>[]::new));
        for (int d = 1; d < dims.size(); d++) builder = builder.and(dims.get(d));
        for (Rule r : rules) {
            builder = builder.when(dims.get(r.trigger())).isOneOf(r.triggerValues())
                    .then(dims.get(r.target())).isOneOf(r.allowed());
        }
        return builder;
    }

    private static List<List<Object>> bruteForce(List<List<Integer>> dims, List<Rule> rules) {
        List<List<Object>> out = new ArrayList<>();
        int[] idx = new int[dims.size()];
        if (dims.stream().anyMatch(List::isEmpty)) return out;
        while (true) {
            List<Integer> tuple = new ArrayList<>();
            for (int d = 0; d < dims.size(); d++) tuple.add(dims.get(d).get(idx[d]));
            boolean ok = true;
            for (Rule r : rules) {
                if (r.triggerValues().contains(tuple.get(r.trigger())) && !r.allowed().contains(tuple.get(r.target()))) {
                    ok = false;
                    break;
                }
            }
            if (ok) out.add(new ArrayList<>(tuple));
            int d = dims.size() - 1;
            while (d >= 0 && ++idx[d] == dims.get(d).size()) idx[d--] = 0;
            if (d < 0) return out;
        }
    }

    private static List<Object> drain(Iterator<List<Object>> it) {
        List<Object> out = new ArrayList<>();
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    @Test
    void iterationAndIsEmptyBuildNoCountsAndCountDoes() throws Exception {
        List<List<Integer>> dims = List.of(values(0, 6), values(1, 6), values(2, 6), values(3, 6));
        List<Rule> rules = List.of(
                new Rule(0, List.of(0, 1, 2), 1, List.of(100, 101, 102)),
                new Rule(1, List.of(100, 101), 2, List.of(201, 203, 205)),
                new Rule(3, List.of(300, 303), 2, List.of(200, 202)));
        var builder = build(dims, rules);
        ConstraintPlan plan = planOf(builder);
        List<List<Object>> expected = bruteForce(dims, rules);
        assertFalse(expected.isEmpty());

        assertFalse(builder.isEmpty());
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.DP).stream().toList());
        assertEquals(expected, drain(builder.lexOrder(LexOrderStrategy.DP).iterator()));
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.PRUNED_BITSET).stream().toList());
        assertFalse(plan.countsBuilt(), "iteration and isEmpty() must not build the exact counts");

        assertEquals(BigInteger.valueOf(expected.size()), builder.count());
        assertTrue(plan.countsBuilt(), "count() builds the exact counts");
    }

    @Test
    void deadEndsAreDetectedWithoutCounts() throws Exception {
        // Every value of every dimension looks fine locally, but the last dimension can never agree with the
        // second to last one: the product is empty, and only the final dimension reveals it.
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < 6; d++) dims.add(values(d, 30));
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            rules.add(new Rule(d, dims.get(d).subList(0, 15), d + 1, dims.get(d + 1).subList(0, 25)));
        }
        rules.add(new Rule(4, dims.get(4), 5, List.of(dims.get(5).get(0))));
        rules.add(new Rule(3, dims.get(3), 5, List.of(dims.get(5).get(1))));   // disagrees with the rule above
        var builder = build(dims, rules);
        ConstraintPlan plan = planOf(builder);

        assertTrue(builder.isEmpty());
        assertTrue(builder.lexOrder(LexOrderStrategy.DP).stream().toList().isEmpty());
        assertTrue(drain(builder.lexOrder(LexOrderStrategy.DP).iterator()).isEmpty());
        assertTrue(builder.lexOrder(LexOrderStrategy.PRUNED_BITSET).stream().toList().isEmpty());
        assertFalse(plan.countsBuilt());
        assertEquals(BigInteger.ZERO, builder.count());
    }

    @Test
    void prunedStrategiesUseLoopLookaheadWithoutBuildingExactCounts() throws Exception {
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < 5; d++) dims.add(values(d, 10));
        List<Rule> rules = List.of(
                new Rule(0, dims.get(0), 1, dims.get(1).subList(0, 5)),
                new Rule(1, dims.get(1), 2, dims.get(2)),
                new Rule(2, dims.get(2), 3, dims.get(3).subList(0, 5)),
                new Rule(3, dims.get(3), 4, dims.get(4).subList(0, 5)),
                new Rule(2, dims.get(2).subList(0, 9), 4, dims.get(4).subList(5, 10)));
        var builder = build(dims, rules);
        ConstraintPlan plan = planOf(builder);
        List<List<Object>> expected = bruteForce(dims, rules);

        assertFalse(expected.isEmpty());
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.PRUNED_BITSET).stream().toList());
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.PRUNED_DFS).stream().toList());
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.ADAPTIVE, 100_000).stream().toList());
        assertFalse(plan.countsBuilt(), "feasibility lookahead must not build exact completion counts");
    }

    @Test
    void adaptiveAcyclicTraversalDoesNotConstructConstraintPlan() throws Exception {
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < 8; d++) dims.add(values(d, 3));
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            rules.add(new Rule(d, dims.get(d), d + 1, dims.get(d + 1).subList(0, 2)));
        }
        var builder = build(dims, rules);
        Field modelField = ConditionalProductBuilder.class.getDeclaredField("model");
        modelField.setAccessible(true);
        ConstraintModel model = (ConstraintModel) modelField.get(builder);

        assertEquals(1, builder.lexOrder(LexOrderStrategy.ADAPTIVE, 100_000).stream().limit(1).count());
        Field planField = ConstraintModel.class.getDeclaredField("plan");
        planField.setAccessible(true);
        assertEquals(null, planField.get(model),
                "acyclic adaptive traversal should not construct the feasibility/counting plan");
    }

    @Test
    void randomRuleSetsAgreeWithBruteForceOnEveryDpPath() throws Exception {
        Random random = new Random(20261117L);
        for (int round = 0; round < 400; round++) {
            int n = 2 + random.nextInt(4);
            List<List<Integer>> dims = new ArrayList<>();
            for (int d = 0; d < n; d++) dims.add(values(d, 1 + random.nextInt(6)));
            List<Rule> rules = new ArrayList<>();
            int ruleCount = random.nextInt(6);
            for (int i = 0; i < ruleCount; i++) {
                int trigger = random.nextInt(n);
                int target = random.nextInt(n - 1);
                if (target >= trigger) target++;
                rules.add(new Rule(trigger, subset(random, dims.get(trigger)), target, subset(random, dims.get(target))));
            }
            var builder = build(dims, rules);
            List<List<Object>> expected = bruteForce(dims, rules);
            String label = "round " + round;

            assertEquals(expected.isEmpty(), builder.isEmpty(), label);
            assertEquals(expected, builder.lexOrder(LexOrderStrategy.DP).stream().toList(), label);
            assertEquals(expected, drain(builder.lexOrder(LexOrderStrategy.DP).iterator()), label);
            assertEquals(expected, builder.lexOrder(LexOrderStrategy.DP).stream().parallel().toList(), label);
            assertEquals(expected, builder.lexOrder(LexOrderStrategy.PRUNED_BITSET).stream().toList(), label);
            assertEquals(BigInteger.valueOf(expected.size()), builder.count(), label);
            for (int rank = 0; rank < expected.size(); rank++) {
                assertEquals(expected.get(rank), builder.byRanks(List.of(BigInteger.valueOf(rank))).stream()
                        .findFirst().orElseThrow(), label + " rank " + rank);
            }
        }
    }

    @Test
    void parallelDpSplitsByRankAndKeepsEncounterOrder() throws Exception {
        List<List<Integer>> dims = List.of(values(0, 7), values(1, 7), values(2, 7), values(3, 7));
        List<Rule> rules = List.of(new Rule(0, List.of(0, 2, 4), 1, List.of(100, 101, 105)));
        var builder = build(dims, rules);
        ConstraintPlan plan = planOf(builder);
        List<List<Object>> expected = bruteForce(dims, rules);

        Spliterator<List<Object>> spliterator = builder.lexOrder(LexOrderStrategy.DP).spliterator();
        Spliterator<List<Object>> lower = spliterator.trySplit();
        assertNotNull(lower, "a large result must split");
        assertTrue(plan.countsBuilt(), "splitting by rank needs the counts");
        List<List<Object>> joined = new ArrayList<>();
        lower.forEachRemaining(joined::add);
        spliterator.forEachRemaining(joined::add);
        assertEquals(expected, joined, "lower range first, then the upper range");

        assertEquals(expected, builder.lexOrder(LexOrderStrategy.DP).stream().parallel().toList());
    }

    private static List<Integer> subset(Random random, List<Integer> from) {
        List<Integer> chosen = new ArrayList<>();
        for (Integer v : from) if (random.nextBoolean()) chosen.add(v);
        if (chosen.isEmpty()) chosen.add(from.get(random.nextInt(from.size())));
        return chosen;
    }

    @Test
    void countsAboveTheLongRangeUseTheBigIntegerTablesAndStayExact() throws Exception {
        // chain of 12 dimensions of 60 values; every rule lets the next dimension use only its first 50 values,
        // so the exact count 60 * 50^11 (about 1.5e20) is far above 2^62 and the long tables cannot be used
        int n = 12;
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < n; d++) dims.add(values(d, 60));
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d + 1 < n; d++) {
            rules.add(new Rule(d, dims.get(d), d + 1, dims.get(d + 1).subList(0, 50)));
        }
        var builder = build(dims, rules);
        BigInteger fifty11 = BigInteger.valueOf(50).pow(11);
        BigInteger expected = fifty11.multiply(BigInteger.valueOf(60));
        assertTrue(expected.bitLength() > 62);
        assertEquals(expected, builder.count());
        ConstraintCounts counts = planOf(builder).counts();
        assertTrue(counts.lsuffix == null && counts.suffix != null, "the exact tables are BigInteger");

        // rank = mixed radix number: dimension 0 has 60 digits, every later dimension 50
        Random random = new Random(5);
        List<BigInteger> ranks = new ArrayList<>(List.of(BigInteger.ZERO, expected.subtract(BigInteger.ONE)));
        for (int i = 0; i < 20; i++) ranks.add(new BigInteger(expected.bitLength(), random).mod(expected));
        for (BigInteger rank : ranks) {
            List<Object> want = new ArrayList<>();
            BigInteger rest = rank;
            int[] digit = new int[n];
            for (int d = n - 1; d >= 1; d--) {
                BigInteger[] qr = rest.divideAndRemainder(BigInteger.valueOf(50));
                digit[d] = qr[1].intValue();
                rest = qr[0];
            }
            digit[0] = rest.intValueExact();
            for (int d = 0; d < n; d++) want.add(d * 100 + digit[d]);
            assertEquals(want, builder.byRanks(List.of(rank)).stream().findFirst().orElseThrow(), "rank " + rank);
        }
    }

    @Test
    void countsJustBelowAndJustAboveTheLongLimitAgree() throws Exception {
        // 63^10 is about 9.8e17 (fits a long table) and 63^11 about 6.2e19 (does not)
        for (int dimensions : new int[]{10, 11, 12}) {
            List<List<Integer>> dims = new ArrayList<>();
            for (int d = 0; d < dimensions; d++) dims.add(values(d, 63));
            BigInteger expected = BigInteger.valueOf(63).pow(dimensions);   // 63^10 < 2^62 < 63^11
            var builder = build(dims, List.of());
            assertEquals(expected, builder.count(), dimensions + " dimensions");
            ConstraintCounts counts = planOf(builder).counts();
            assertEquals(expected.bitLength() <= 62, counts.lsuffix != null, dimensions + " dimensions");
        }
    }

    @Test
    void concurrentFirstUseOfTheCountingTierBuildsOneSharedInstance() throws Exception {
        List<List<Integer>> dims = List.of(values(0, 6), values(1, 6), values(2, 6), values(3, 6));
        List<Rule> rules = List.of(
                new Rule(0, List.of(0, 1, 2), 1, List.of(100, 101, 102)),
                new Rule(1, List.of(100, 101), 2, List.of(201, 203, 205)),
                new Rule(3, List.of(300, 303), 2, List.of(200, 202)));
        List<List<Object>> expected = bruteForce(dims, rules);
        BigInteger want = BigInteger.valueOf(expected.size());
        int threads = 8;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        try {
            for (int round = 0; round < 200; round++) {
                var builder = build(dims, rules);                 // a fresh model: counts not yet built
                ConstraintPlan plan = planOf(builder);
                assertFalse(plan.countsBuilt());
                java.util.concurrent.CyclicBarrier start = new java.util.concurrent.CyclicBarrier(threads);
                List<java.util.concurrent.Future<ConstraintCounts>> seen = new ArrayList<>();
                for (int t = 0; t < threads; t++) {
                    final int which = t;
                    seen.add(pool.submit(() -> {
                        start.await();
                        // half the threads count, the others unrank, so both entry points race on the first build
                        if (which % 2 == 0) assertEquals(want, builder.count());
                        else assertEquals(expected.get(0), builder.byRanks(List.of(BigInteger.ZERO)).stream()
                                .findFirst().orElseThrow());
                        return plan.counts();
                    }));
                }
                ConstraintCounts first = seen.get(0).get();
                for (var future : seen) assertTrue(future.get() == first, "every thread sees the same counting tier");
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
