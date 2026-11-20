/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Larger oracle shapes with structure, each checked against an independent depth-first oracle on every strategy,
 * {@code anyOrder()} (sequential and parallel), and the counting and rank operations: a star whose rules all end in the
 * last dimension, a star leaving the first, shuffled random trees, a cycle with a tail, duplicate values, very unequal
 * sizes, empty dimensions, and larger random graphs than the tier tests use.
 */
class ConditionalProductShapeOracleTest {

    private record Rule(int trigger, List<Integer> triggerValues, int target, List<Integer> allowed) { }

    private record Shape(String name, List<List<Integer>> dims, List<Rule> rules, boolean duplicateValues) { }

    // ------------------------------------------------------------------ shape builders

    private static List<Integer> values(int dimension, int size) {
        return IntStream.range(0, size).map(v -> dimension * 100 + v).boxed().toList();
    }

    private static List<List<Integer>> dims(int... sizes) {
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < sizes.length; d++) {
            // new ArrayList for empty ones: List.of() would hand out one shared instance for every empty dimension
            dims.add(sizes[d] == 0 ? new ArrayList<>() : values(d, sizes[d]));
        }
        return dims;
    }

    private static List<Integer> subset(Random random, List<Integer> from) {
        List<Integer> chosen = new ArrayList<>();
        for (Integer v : from) if (random.nextBoolean()) chosen.add(v);
        if (chosen.isEmpty() && !from.isEmpty()) chosen.add(from.get(random.nextInt(from.size())));
        return chosen;
    }

    private static Rule rule(Random random, List<List<Integer>> dims, int trigger, int target) {
        return new Rule(trigger, subset(random, dims.get(trigger)), target, subset(random, dims.get(target)));
    }

    private static Shape starLast(Random random) {
        var dims = dims(6, 5, 6, 5, 6, 7);
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < 5; d++) rules.add(rule(random, dims, d, 5));
        return new Shape("starLast", dims, rules, false);
    }

    private static Shape starFirst(Random random) {
        var dims = dims(6, 5, 5, 6, 5, 6);
        List<Rule> rules = new ArrayList<>();
        for (int d = 1; d < 6; d++) rules.add(rule(random, dims, 0, d));
        return new Shape("starFirst", dims, rules, false);
    }

    private static Shape shuffledTree(Random random, int seed) {
        int n = 7;
        int[] sizes = new int[n];
        for (int d = 0; d < n; d++) sizes[d] = 2 + random.nextInt(5);
        var dims = dims(sizes);
        List<Integer> order = new ArrayList<>(IntStream.range(0, n).boxed().toList());
        Collections.shuffle(order, random);
        List<Rule> rules = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            int child = order.get(i);
            int parent = order.get(random.nextInt(i));
            rules.add(random.nextBoolean() ? rule(random, dims, parent, child) : rule(random, dims, child, parent));
        }
        return new Shape("shuffledTree#" + seed, dims, rules, false);
    }

    private static Shape cyclePlusTail(Random random) {
        var dims = dims(5, 5, 5, 5, 5, 5);
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < 4; d++) rules.add(rule(random, dims, d, (d + 1) % 4));   // loop 0>1>2>3>0
        rules.add(rule(random, dims, 3, 4));
        rules.add(rule(random, dims, 4, 5));
        return new Shape("cyclePlusTail", dims, rules, false);
    }

    /** Rule values must be values of the named dimension, so choose from its distinct values. */
    private static Rule duplicateRule(Random random, List<List<Integer>> dims, int trigger, int target) {
        List<Integer> triggerChoices = new ArrayList<>(new java.util.LinkedHashSet<>(dims.get(trigger)));
        List<Integer> targetChoices = new ArrayList<>(new java.util.LinkedHashSet<>(dims.get(target)));
        return new Rule(trigger, subset(random, triggerChoices), target, subset(random, targetChoices));
    }

    private static Shape duplicateValues(Random random) {
        List<List<Integer>> dims = new ArrayList<>();
        dims.add(new ArrayList<>(List.of(1, 1, 2, 2, 3)));
        dims.add(new ArrayList<>(List.of(1, 2, 2, 3, 3, 3)));
        dims.add(new ArrayList<>(List.of(1, 1, 1, 2)));
        dims.add(new ArrayList<>(List.of(3, 3, 2, 1, 1)));
        List<Rule> rules = new ArrayList<>();
        rules.add(duplicateRule(random, dims, 0, 1));
        rules.add(duplicateRule(random, dims, 1, 2));
        rules.add(duplicateRule(random, dims, 0, 3));
        return new Shape("duplicateValues", dims, rules, true);
    }

    private static Shape unequalSizes(Random random) {
        var dims = dims(1, 2, 9, 3, 10, 1, 5);
        List<Rule> rules = new ArrayList<>();
        rules.add(rule(random, dims, 1, 2));
        rules.add(rule(random, dims, 2, 4));
        rules.add(rule(random, dims, 3, 4));
        rules.add(rule(random, dims, 4, 6));
        rules.add(rule(random, dims, 0, 3));
        return new Shape("unequalSizes", dims, rules, false);
    }

    private static List<Shape> emptyDimensions(Random random) {
        List<Shape> shapes = new ArrayList<>();
        for (int empty : new int[]{0, 2, 4}) {
            int[] sizes = {5, 6, 5, 4, 5};
            sizes[empty] = 0;
            var dims = dims(sizes);
            List<Rule> rules = new ArrayList<>();
            for (int d = 0; d < 4; d++) {
                if (!dims.get(d).isEmpty() && !dims.get(d + 1).isEmpty()) rules.add(rule(random, dims, d, d + 1));
            }
            shapes.add(new Shape("emptyDimension@" + empty, dims, rules, false));
        }
        return shapes;
    }

    private static Shape randomGraph(Random random, int seed) {
        int n = 4 + random.nextInt(4);
        int[] sizes = new int[n];
        for (int d = 0; d < n; d++) sizes[d] = 1 + random.nextInt(8);
        var dims = dims(sizes);
        List<Rule> rules = new ArrayList<>();
        int count = random.nextInt(9);
        for (int i = 0; i < count; i++) {
            int trigger = random.nextInt(n);
            int target = random.nextInt(n - 1);
            if (target >= trigger) target++;
            rules.add(rule(random, dims, trigger, target));
        }
        return new Shape("randomGraph#" + seed, dims, rules, false);
    }

    // ------------------------------------------------------------------ oracle

    private static ConditionalProductBuilder build(Shape shape) {
        var builder = cartesianProduct.conditional().ofList(shape.dims().get(0));
        for (int d = 1; d < shape.dims().size(); d++) builder = builder.and(shape.dims().get(d));
        for (Rule r : shape.rules()) {
            builder = builder.when(shape.dims().get(r.trigger())).isOneOf(r.triggerValues())
                    .then(shape.dims().get(r.target())).isOneOf(r.allowed());
        }
        return builder;
    }

    /** Independent depth-first oracle: rules are checked by value as soon as both dimensions are chosen. */
    private static List<List<Object>> oracle(Shape shape) {
        List<List<Object>> out = new ArrayList<>();
        if (shape.dims().stream().anyMatch(List::isEmpty)) return out;
        walk(shape, 0, new Object[shape.dims().size()], out);
        return out;
    }

    private static void walk(Shape shape, int depth, Object[] chosen, List<List<Object>> out) {
        if (depth == chosen.length) {
            out.add(new ArrayList<>(java.util.Arrays.asList(chosen)));
            return;
        }
        for (Integer candidate : shape.dims().get(depth)) {
            chosen[depth] = candidate;
            boolean ok = true;
            for (Rule r : shape.rules()) {
                if (Math.max(r.trigger(), r.target()) != depth) continue;
                if (r.triggerValues().contains(chosen[r.trigger()]) && !r.allowed().contains(chosen[r.target()])) {
                    ok = false;
                    break;
                }
            }
            if (ok) walk(shape, depth + 1, chosen, out);
        }
    }

    private static Map<List<Object>, Long> frequencies(Iterable<List<Object>> tuples) {
        Map<List<Object>, Long> m = new HashMap<>();
        for (List<Object> t : tuples) m.merge(t, 1L, Long::sum);
        return m;
    }

    // ------------------------------------------------------------------ the checks

    /** Checks every path against the oracle and returns the number of valid tuples. */
    private static int check(Shape shape) {
        String label = shape.name();
        var builder = build(shape);
        List<List<Object>> expected = oracle(shape);
        Map<List<Object>, Long> expectedFrequencies = frequencies(expected);

        assertEquals(expected.isEmpty(), builder.isEmpty(), label + " isEmpty");
        assertEquals(BigInteger.valueOf(expected.size()), builder.count(), label + " count");

        for (LexOrderStrategy s : List.of(LexOrderStrategy.PRUNED_BITSET, LexOrderStrategy.PRUNED_DFS, LexOrderStrategy.DP)) {
            assertEquals(expected, builder.lexOrder(s).stream().toList(), label + " " + s);
            assertEquals(expected, builder.lexOrder(s).stream().toList(), label + " " + s + " re-iteration");
            assertEquals(expectedFrequencies, frequencies(builder.lexOrder(s).stream().parallel().toList()),
                    label + " " + s + " parallel");
        }
        assertEquals(expected, builder.lexOrder().stream().toList(), label + " default");
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.ADAPTIVE, 1_000).stream().toList(), label + " ADAPTIVE");
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.DP).stream().parallel().toList(),
                label + " DP parallel keeps encounter order");

        assertEquals(expectedFrequencies, frequencies(builder.anyOrder().stream().toList()), label + " anyOrder");
        assertEquals(expectedFrequencies, frequencies(builder.anyOrder().stream().parallel().toList()),
                label + " anyOrder parallel");

        if (!expected.isEmpty()) checkRankOperations(shape, builder, expected);
        return expected.size();
    }

    private static void checkRankOperations(Shape shape, ConditionalProductBuilder builder, List<List<Object>> expected) {
        String label = shape.name();
        int size = expected.size();

        Random random = new Random(size);
        List<Integer> picks = new ArrayList<>();
        for (int i = 0; i < Math.min(size, 200); i++) picks.add(random.nextInt(size));
        List<BigInteger> ranks = picks.stream().map(BigInteger::valueOf).toList();
        List<List<Object>> byRanks = builder.byRanks(ranks).stream().toList();
        assertEquals(picks.stream().map(expected::get).toList(), byRanks, label + " byRanks");

        for (int m : new int[]{1, 3, Math.max(1, size / 2), size}) {
            for (int start : new int[]{0, Math.min(1, size - 1)}) {
                List<List<Object>> every = new ArrayList<>();
                for (int r = start; r < size; r += m) every.add(expected.get(r));
                assertEquals(every, builder.lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start)).stream().toList(),
                        label + " lexOrderMth m=" + m + " start=" + start);
            }
        }

        Set<List<Object>> valid = new HashSet<>(expected);
        int k = Math.min(size, 25);
        for (List<Object> t : builder.choice(k, new Random(7)).stream().toList()) {
            assertTrue(valid.contains(t), label + " choice returned an invalid tuple " + t);
        }
        List<List<Object>> sample = builder.sample(k, new Random(7)).stream().toList();
        assertEquals(k, sample.size(), label + " sample size");
        for (List<Object> t : sample) assertTrue(valid.contains(t), label + " sample returned an invalid tuple " + t);
        if (!shape.duplicateValues()) {
            assertEquals(k, new HashSet<>(sample).size(), label + " sample must be without replacement");
        }
    }

    // ------------------------------------------------------------------ tests

    @Test
    void starShapes() {
        Random random = new Random(20261118L);
        int nonEmpty = 0;
        for (int i = 0; i < 4; i++) {
            if (check(starLast(random)) > 0) nonEmpty++;
            if (check(starFirst(random)) > 0) nonEmpty++;
        }
        assertTrue(nonEmpty >= 4, "most star shapes should have valid tuples, had " + nonEmpty + " of 8");
    }

    @Test
    void shuffledTrees() {
        Random random = new Random(1118L);
        int nonEmpty = 0;
        for (int seed = 0; seed < 20; seed++) if (check(shuffledTree(random, seed)) > 0) nonEmpty++;
        assertTrue(nonEmpty >= 10, "most trees should have valid tuples, had " + nonEmpty + " of 20");
    }

    @Test
    void cycleWithTail() {
        Random random = new Random(42L);
        int nonEmpty = 0;
        for (int i = 0; i < 12; i++) if (check(cyclePlusTail(random)) > 0) nonEmpty++;
        assertTrue(nonEmpty >= 3, "some cycle shapes should have valid tuples, had " + nonEmpty + " of 12");
    }

    @Test
    void duplicateValuesCountEveryPosition() {
        Random random = new Random(99L);
        int nonEmpty = 0;
        for (int i = 0; i < 12; i++) if (check(duplicateValues(random)) > 0) nonEmpty++;
        assertTrue(nonEmpty >= 6, "most duplicate-value shapes should have valid tuples, had " + nonEmpty + " of 12");
    }

    @Test
    void veryUnequalSizes() {
        Random random = new Random(7L);
        int nonEmpty = 0;
        for (int i = 0; i < 12; i++) if (check(unequalSizes(random)) > 0) nonEmpty++;
        assertTrue(nonEmpty >= 6, "most unequal-size shapes should have valid tuples, had " + nonEmpty + " of 12");
    }

    @Test
    void emptyDimensionsYieldNothingOnEveryPath() {
        Random random = new Random(5L);
        for (int i = 0; i < 4; i++) {
            for (Shape shape : emptyDimensions(random)) assertEquals(0, check(shape), shape.name());
        }
    }

    @Test
    void largerRandomGraphs() {
        Random random = new Random(20261119L);
        int nonEmpty = 0;
        for (int seed = 0; seed < 120; seed++) if (check(randomGraph(random, seed)) > 0) nonEmpty++;
        assertTrue(nonEmpty >= 60, "most random graphs should have valid tuples, had " + nonEmpty + " of 120");
    }
}
