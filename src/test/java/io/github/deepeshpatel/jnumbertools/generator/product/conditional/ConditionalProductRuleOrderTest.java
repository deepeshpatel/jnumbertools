/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.base.Product;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rules of a conditional product can be added in any order, in either direction between two dimensions, and
 * more than once for the same pair of dimensions. Whatever the order, the builder must give exactly the tuples a
 * brute-force filter gives, and its internal relation table must stay in ascending key order, which is what makes
 * the plan, and so the performance, the same on every run.
 */
class ConditionalProductRuleOrderTest {

    private static final int DIMENSIONS = 6;
    private static final int VALUES = 3;

    /** If the tuple's value in {@code triggerDim} is one of {@code trigger}, its value in {@code targetDim} must not be forbidden. */
    private record Rule(int triggerDim, Set<Integer> trigger, int targetDim, Set<Integer> forbidden) {
        boolean holds(int[] tuple) {
            return !trigger.contains(tuple[triggerDim]) || !forbidden.contains(tuple[targetDim]);
        }
    }

    // pairs (0,1) twice, a reversed pair (5,2) and (3,1), a cycle 0-1-2-0, and pairs that land before, between and after others
    private static final List<Rule> RULES = List.of(
            new Rule(0, Set.of(1), 1, Set.of(2)),
            new Rule(1, Set.of(0, 2), 2, Set.of(1)),
            new Rule(0, Set.of(2), 2, Set.of(0)),
            new Rule(3, Set.of(1), 5, Set.of(0, 2)),
            new Rule(2, Set.of(1), 3, Set.of(1)),
            new Rule(4, Set.of(0), 5, Set.of(1)),
            new Rule(1, Set.of(1), 4, Set.of(2)),
            new Rule(0, Set.of(0, 1), 1, Set.of(0)),         // second rule on the pair (0,1)
            new Rule(5, Set.of(2), 2, Set.of(2)),            // trigger in the higher dimension
            new Rule(3, Set.of(0), 1, Set.of(1, 2)));       // trigger in the higher dimension

    private static List<List<Integer>> newDimensions() {
        List<List<Integer>> dimensions = new ArrayList<>();
        for (int d = 0; d < DIMENSIONS; d++) {
            List<Integer> values = new ArrayList<>();
            for (int v = 0; v < VALUES; v++) values.add(v);
            dimensions.add(values);                           // each dimension is its own list instance
        }
        return dimensions;
    }

    private static ConditionalProductBuilder build(List<Rule> rules, List<List<Integer>> dimensions) {
        ConditionalProductBuilder builder = JNumberTools.cartesianProduct().conditionalProductOf(dimensions.get(0));
        for (int d = 1; d < DIMENSIONS; d++) builder = builder.and(dimensions.get(d));
        for (Rule rule : rules) {
            builder = builder.when(dimensions.get(rule.triggerDim())).isOneOf(rule.trigger().toArray())
                    .then(dimensions.get(rule.targetDim())).isNoneOf(rule.forbidden().toArray());
        }
        return builder;
    }

    /** Brute-force oracle: every tuple in lexicographic order that satisfies every rule. */
    private static List<List<Object>> expected(List<Rule> rules) {
        List<List<Object>> result = new ArrayList<>();
        int total = (int) Math.pow(VALUES, DIMENSIONS);
        for (int code = 0; code < total; code++) {
            int[] tuple = new int[DIMENSIONS];
            int rest = code;
            for (int d = DIMENSIONS - 1; d >= 0; d--) {        // dimension 0 is the most significant
                tuple[d] = rest % VALUES;
                rest /= VALUES;
            }
            boolean ok = true;
            for (Rule rule : rules) ok &= rule.holds(tuple);
            if (ok) {
                List<Object> list = new ArrayList<>();
                for (int v : tuple) list.add(v);
                result.add(list);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<Long, ?> relationsOf(ConditionalProductBuilder builder) throws ReflectiveOperationException {
        Field modelField = ConditionalProductBuilder.class.getDeclaredField("model");
        modelField.setAccessible(true);
        Object model = modelField.get(builder);

        Field relationsField = ConstraintModel.class.getDeclaredField("relations");
        relationsField.setAccessible(true);
        return (Map<Long, ?>) relationsField.get(model);
    }

    private static Set<Integer> pairsOf(List<Rule> rules) {
        Set<Integer> pairs = new TreeSet<>();
        for (Rule rule : rules) {
            pairs.add(Math.min(rule.triggerDim(), rule.targetDim()) * 100 + Math.max(rule.triggerDim(), rule.targetDim()));
        }
        return pairs;
    }

    @Test
    void anyInsertionOrderGivesTheBruteForceResult() {
        List<List<Object>> oracle = expected(RULES);
        assertTrue(oracle.size() > 10 && oracle.size() < 729, "the rules should leave some, not all, tuples: " + oracle.size());
        Random random = new Random(17);
        for (int attempt = 0; attempt < 40; attempt++) {
            List<Rule> shuffled = new ArrayList<>(RULES);
            Collections.shuffle(shuffled, random);
            ConditionalProductBuilder builder = build(shuffled, newDimensions());
            assertEquals(BigInteger.valueOf(oracle.size()), builder.count(), "attempt " + attempt);
            List<List<Object>> actual = new ArrayList<>();
            builder.lexOrder().forEach(actual::add);
            assertEquals(oracle, actual, "attempt " + attempt);
        }
    }

    @Test
    void ranksAndSamplesFollowTheSameOrderForAnyInsertionOrder() {
        List<List<Object>> oracle = expected(RULES);
        Random random = new Random(3);
        for (int attempt = 0; attempt < 10; attempt++) {
            List<Rule> shuffled = new ArrayList<>(RULES);
            Collections.shuffle(shuffled, random);
            ConditionalProductBuilder builder = build(shuffled, newDimensions());
            for (int rank = 0; rank < oracle.size(); rank += 7) {
                assertEquals(oracle.get(rank), builder.byRanks(List.of(BigInteger.valueOf(rank))).iterator().next(),
                        "attempt " + attempt + " rank " + rank);
            }
            for (List<Object> tuple : builder.choice(30, new Random(attempt))) {
                assertTrue(oracle.contains(tuple), "choice returned an invalid tuple " + tuple);
            }
        }
    }

    @Test
    void relationTableStaysInAscendingKeyOrderWhateverTheInsertionOrder() throws ReflectiveOperationException {
        Random random = new Random(29);
        for (int attempt = 0; attempt < 40; attempt++) {
            List<Rule> shuffled = new ArrayList<>(RULES);
            Collections.shuffle(shuffled, random);
            ConditionalProductBuilder builder = build(shuffled, newDimensions());
            List<Long> keys = new ArrayList<>(relationsOf(builder).keySet());
            List<Long> sorted = new ArrayList<>(keys);
            Collections.sort(sorted);
            assertEquals(sorted, keys, "attempt " + attempt);
            assertEquals(pairsOf(RULES).size(), keys.size(), "one relation per linked pair of dimensions");
        }
    }

    @Test
    void everyIntermediateBuilderKeepsTheOrderToo() throws ReflectiveOperationException {
        List<List<Integer>> dimensions = newDimensions();
        ConditionalProductBuilder builder = JNumberTools.cartesianProduct().conditionalProductOf(dimensions.get(0));
        for (int d = 1; d < DIMENSIONS; d++) builder = builder.and(dimensions.get(d));
        List<Rule> shuffled = new ArrayList<>(RULES);
        Collections.shuffle(shuffled, new Random(5));
        for (Rule rule : shuffled) {
            builder = builder.when(dimensions.get(rule.triggerDim())).isOneOf(rule.trigger().toArray())
                    .then(dimensions.get(rule.targetDim())).isNoneOf(rule.forbidden().toArray());
            List<Long> keys = new ArrayList<>(relationsOf(builder).keySet());
            List<Long> sorted = new ArrayList<>(keys);
            Collections.sort(sorted);
            assertEquals(sorted, keys);
        }
    }

    @Test
    void addingARuleDoesNotChangeTheBuilderItWasAddedTo() throws ReflectiveOperationException {
        List<List<Integer>> dimensions = newDimensions();
        ConditionalProductBuilder base = build(RULES.subList(0, 3), dimensions);
        Map<Long, ?> before = Map.copyOf(relationsOf(base));
        BigInteger countBefore = base.count();
        base.when(dimensions.get(4)).is(1).then(dimensions.get(5)).isNoneOf(0, 2);
        assertEquals(before, relationsOf(base));
        assertEquals(countBefore, base.count());
    }
}
