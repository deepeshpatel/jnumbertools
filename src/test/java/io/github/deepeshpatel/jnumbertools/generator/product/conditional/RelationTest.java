/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.entrypoint.Product;

import io.github.deepeshpatel.jnumbertools.AllocationMeter;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The relation between two dimensions keeps the rules and builds its rows lazily, sharing one row between all values
 * that the same rules constrain. These tests check the rows against a pair-by-pair brute force, that rows really are
 * shared, that adding rules leaves earlier relations alone, and that building rules does not grow with the number of
 * value pairs.
 */
class RelationTest {

    private static final class SimpleRule {
        final BitSet trigger;
        final BitSet allowed;
        final boolean triggerIsU;

        SimpleRule(BitSet trigger, BitSet allowed, boolean triggerIsU) {
            this.trigger = trigger;
            this.allowed = allowed;
            this.triggerIsU = triggerIsU;
        }

        boolean allows(int a, int b) {
            int triggerValue = triggerIsU ? a : b;
            int targetValue = triggerIsU ? b : a;
            return !trigger.get(triggerValue) || allowed.get(targetValue);
        }
    }

    private static BitSet randomSet(int size, int percent, Random random) {
        BitSet set = new BitSet(size);
        for (int i = 0; i < size; i++) if (random.nextInt(100) < percent) set.set(i);
        return set;
    }

    private static Relation build(int du, int dv, List<SimpleRule> rules) {
        Relation relation = Relation.full(du, dv);
        for (SimpleRule rule : rules) relation = relation.restrict(rule.trigger, rule.allowed, rule.triggerIsU);
        return relation;
    }

    private static List<SimpleRule> randomRules(int du, int dv, int count, Random random) {
        List<SimpleRule> rules = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boolean triggerIsU = random.nextBoolean();
            int triggerSize = triggerIsU ? du : dv;
            int targetSize = triggerIsU ? dv : du;
            rules.add(new SimpleRule(randomSet(triggerSize, 30, random), randomSet(targetSize, 60, random), triggerIsU));
        }
        return rules;
    }

    private static void assertMatchesBruteForce(Relation relation, int du, int dv, List<SimpleRule> rules, String label) {
        BitSet[] uToV = relation.uToV();
        BitSet[] vToU = relation.vToU();
        assertEquals(du, uToV.length, label);
        assertEquals(dv, vToU.length, label);
        for (int a = 0; a < du; a++) {
            for (int b = 0; b < dv; b++) {
                boolean expected = true;
                for (SimpleRule rule : rules) expected &= rule.allows(a, b);
                assertEquals(expected, uToV[a].get(b), label + " uToV[" + a + "][" + b + "]");
                assertEquals(expected, vToU[b].get(a), label + " vToU[" + b + "][" + a + "]");
            }
            assertTrue(uToV[a].length() <= dv, label + " row has values beyond the other dimension");
        }
        for (int b = 0; b < dv; b++) assertTrue(vToU[b].length() <= du, label);
    }

    @Test
    void relationWithoutRulesAllowsEveryPair() {
        assertMatchesBruteForce(Relation.full(7, 5), 7, 5, List.of(), "no rules");
        assertMatchesBruteForce(Relation.full(1, 1), 1, 1, List.of(), "1x1");
    }

    @Test
    void rowsMatchABruteForcePairMatrixInBothDirections() {
        Random random = new Random(31);
        for (int n = 0; n < 400; n++) {
            int du = 1 + random.nextInt(40);
            int dv = 1 + random.nextInt(40);
            List<SimpleRule> rules = randomRules(du, dv, 1 + random.nextInt(8), random);
            assertMatchesBruteForce(build(du, dv, rules), du, dv, rules, "case " + n + " " + du + "x" + dv);
        }
    }

    @Test
    void manyRulesBeyondTheSharingLimitStillGiveTheSameRows() {
        // more than 62 rules cannot be tracked in a long, so rows are then built per value
        Random random = new Random(32);
        for (int n = 0; n < 10; n++) {
            int du = 5 + random.nextInt(20);
            int dv = 5 + random.nextInt(20);
            List<SimpleRule> rules = randomRules(du, dv, 63 + random.nextInt(20), random);
            assertMatchesBruteForce(build(du, dv, rules), du, dv, rules, "many rules, case " + n);
        }
    }

    @Test
    void valuesConstrainedByTheSameRulesShareOneRow() {
        int size = 1000;
        BitSet trigger = new BitSet(size);
        trigger.set(0, 100);                                 // the first tenth
        BitSet allowed = new BitSet(size);
        allowed.set(0);                                      // they need value 0
        Relation relation = Relation.full(size, size).restrict(trigger, allowed, true);
        BitSet[] rows = relation.uToV();
        IdentityHashMap<BitSet, Boolean> distinct = new IdentityHashMap<>();
        for (BitSet row : rows) distinct.put(row, true);
        assertEquals(2, distinct.size(), "one row for trigger values and one for all other values");
        assertSame(rows[0], rows[99]);
        assertSame(rows[100], rows[999]);
        assertEquals(1, rows[0].cardinality());
        assertEquals(size, rows[500].cardinality());
        // the reverse direction: every value of v except 0 is excluded for the trigger values of u
        BitSet[] reverse = relation.vToU();
        IdentityHashMap<BitSet, Boolean> distinctReverse = new IdentityHashMap<>();
        for (BitSet row : reverse) distinctReverse.put(row, true);
        assertEquals(2, distinctReverse.size());
        assertSame(reverse[1], reverse[999]);
        assertEquals(size - 100, reverse[1].cardinality());
        assertEquals(size, reverse[0].cardinality());
    }

    @Test
    void severalRulesMakeOneRowPerDistinctCombinationOnly() {
        int size = 600;
        List<SimpleRule> rules = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            BitSet trigger = new BitSet(size);
            trigger.set(r * 100, r * 100 + 100);             // three disjoint blocks of 100 trigger values each
            BitSet allowed = new BitSet(size);
            allowed.set(0, 300 + r * 100);
            rules.add(new SimpleRule(trigger, allowed, true));
        }
        Relation relation = build(size, size, rules);
        IdentityHashMap<BitSet, Boolean> distinct = new IdentityHashMap<>();
        for (BitSet row : relation.uToV()) distinct.put(row, true);
        assertEquals(4, distinct.size(), "three constrained groups plus the unconstrained rest");
        assertMatchesBruteForce(relation, size, size, rules, "three blocks");
    }

    @Test
    void addingARuleDoesNotChangeTheRelationItWasAddedTo() {
        Random random = new Random(33);
        int du = 12;
        int dv = 9;
        List<SimpleRule> first = randomRules(du, dv, 2, random);
        Relation base = build(du, dv, first);
        BitSet[] before = new BitSet[du];
        for (int a = 0; a < du; a++) before[a] = (BitSet) base.uToV()[a].clone();
        List<SimpleRule> more = new ArrayList<>(first);
        more.addAll(randomRules(du, dv, 3, random));
        Relation extended = build(du, dv, more);
        assertMatchesBruteForce(extended, du, dv, more, "extended");
        for (int a = 0; a < du; a++) assertEquals(before[a], base.uToV()[a], "row " + a + " of the base relation");
        assertMatchesBruteForce(base, du, dv, first, "base after extending");
    }

    @Test
    void changingTheSetsAfterwardsDoesNotChangeTheRelation() {
        BitSet trigger = new BitSet();
        trigger.set(1);
        BitSet allowed = new BitSet();
        allowed.set(2);
        Relation relation = Relation.full(4, 4).restrict(trigger, allowed, true);
        trigger.set(0);                                       // must have no effect
        allowed.set(3);
        assertTrue(relation.uToV()[0].get(3));                // 0 is not a trigger
        assertFalse(relation.uToV()[1].get(3));               // 1 is a trigger and 3 is not allowed
        assertTrue(relation.uToV()[1].get(2));
    }

    @Test
    void eachDirectionIsBuiltOnceEvenWhenAskedFromSeveralThreads() throws Exception {
        Random random = new Random(34);
        List<SimpleRule> rules = randomRules(60, 50, 4, random);
        for (int round = 0; round < 20; round++) {
            Relation relation = build(60, 50, rules);
            ExecutorService pool = Executors.newFixedThreadPool(6);
            try {
                List<Future<BitSet[][]>> results = new ArrayList<>();
                for (int t = 0; t < 12; t++) {
                    results.add(pool.submit(() -> new BitSet[][]{relation.uToV(), relation.vToU()}));
                }
                BitSet[][] first = results.get(0).get();
                for (Future<BitSet[][]> result : results) {
                    BitSet[][] pair = result.get();
                    assertSame(first[0], pair[0], "round " + round);
                    assertSame(first[1], pair[1], "round " + round);
                }
            } finally {
                pool.shutdownNow();
            }
            assertMatchesBruteForce(relation, 60, 50, rules, "round " + round);
        }
    }

    @Test
    void writingRulesDoesNotGrowWithTheNumberOfValuePairs() {
        Assumptions.assumeTrue(AllocationMeter.supported(), "per-thread allocation is not reported by this JVM");
        int size = 5000;
        // One dense table of value pairs for a single pair of dimensions is size*size bits. Building the rules used to
        // allocate two of them per pair plus a full copy of both for every rule added.
        double denseTableBytes = (double) size * size / 8;
        int[] sink = new int[1];
        double bytes = AllocationMeter.bytesPerCall(() -> {
            List<List<Integer>> dimensions = new ArrayList<>();
            for (int d = 0; d < 3; d++) {
                List<Integer> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) values.add(i);
                dimensions.add(values);
            }
            List<Integer> trigger = new ArrayList<>();
            for (int i = 0; i < size / 10; i++) trigger.add(i);
            ConditionalProductBuilder builder = Product.conditional(dimensions.get(0))
                    .and(dimensions.get(1)).and(dimensions.get(2));
            for (int d = 0; d + 1 < 3; d++) {
                builder = builder.when(dimensions.get(d)).isOneOf(trigger).then(dimensions.get(d + 1)).is(0);
            }
            int taken = 0;
            for (List<Object> tuple : builder.lexOrder()) {
                sink[0] += tuple.size();
                if (++taken == 1000) break;
            }
        }, 20, 5);
        assertTrue(bytes < denseTableBytes / 2,
                "building, adding 2 rules and taking 1000 tuples allocated " + bytes + " B; one dense pair table is "
                        + denseTableBytes + " B");
    }
}
