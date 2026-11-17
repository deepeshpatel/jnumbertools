/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
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
        var builder = cartesianProduct.conditionalProductOf(dims.get(0));
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
}
