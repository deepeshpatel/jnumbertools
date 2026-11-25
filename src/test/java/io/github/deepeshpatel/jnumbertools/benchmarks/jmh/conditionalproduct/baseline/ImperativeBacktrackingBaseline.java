/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.baseline;

import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.scenario.Rule;
import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.scenario.Scenario.ScenarioData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * What a developer would write by hand: plain recursive backtracking with each rule checked as soon as both of its
 * dimensions are chosen. No streams, no lookahead.
 *
 * <p>Two result strategies bound the cost of the library's result objects from both sides:
 * {@link #allocating} builds one {@code Object[]} (wrapped as a list) per result like a library tuple, and
 * {@link #reuse} reuses one index array and allocates nothing per result.</p>
 *
 * <p>Checksums use the same scheme as the benchmark's verification ({@code hash * 31 + value} per element, then
 * {@code hash * 31 + 1} per tuple), so results can be compared with the library's output.</p>
 */
public final class ImperativeBacktrackingBaseline {
    private ImperativeBacktrackingBaseline() { }

    /** Receives each valid tuple as indexes; return {@code false} to stop the traversal. */
    public interface Visitor {
        boolean visit(int[] indexes);
    }

    public static final class Plan {
        private final int[] sizes;
        private final int[][] triggerDimension;
        private final int[][] targetDimension;
        private final BitSet[][] triggerValues;
        private final BitSet[][] allowedValues;

        private Plan(int[] sizes, int[][] triggerDimension, int[][] targetDimension,
                     BitSet[][] triggerValues, BitSet[][] allowedValues) {
            this.sizes = sizes;
            this.triggerDimension = triggerDimension;
            this.targetDimension = targetDimension;
            this.triggerValues = triggerValues;
            this.allowedValues = allowedValues;
        }

        public int dimensions() {
            return sizes.length;
        }
    }

    public static Plan compile(ScenarioData scenario) {
        int n = scenario.dimensions().size();
        int[] sizes = new int[n];
        List<List<Rule>> at = new ArrayList<>();
        for (int d = 0; d < n; d++) {
            sizes[d] = scenario.dimensions().get(d).size();
            at.add(new ArrayList<>());
        }
        for (Rule rule : scenario.rules()) {
            at.get(Math.max(rule.triggerDimension(), rule.targetDimension())).add(rule);
        }
        int[][] trigger = new int[n][];
        int[][] target = new int[n][];
        BitSet[][] triggerValues = new BitSet[n][];
        BitSet[][] allowed = new BitSet[n][];
        for (int d = 0; d < n; d++) {
            List<Rule> rules = at.get(d);
            trigger[d] = new int[rules.size()];
            target[d] = new int[rules.size()];
            triggerValues[d] = new BitSet[rules.size()];
            allowed[d] = new BitSet[rules.size()];
            for (int r = 0; r < rules.size(); r++) {
                trigger[d][r] = rules.get(r).triggerDimension();
                target[d][r] = rules.get(r).targetDimension();
                triggerValues[d][r] = (BitSet) rules.get(r).triggerValues().clone();
                allowed[d][r] = (BitSet) rules.get(r).allowedValues().clone();
            }
        }
        return new Plan(sizes, trigger, target, triggerValues, allowed);
    }

    /** One list per result; returns {count, checksum}. */
    public static long[] allocating(Plan plan) {
        long[] acc = {0, 1};
        int n = plan.sizes.length;
        walk(plan, new int[n], 0, indexes -> {
            Object[] tuple = new Object[n];
            for (int i = 0; i < n; i++) tuple[i] = indexes[i];
            long hash = acc[1];
            for (Object value : Arrays.asList(tuple)) hash = hash * 31 + (Integer) value;
            acc[1] = hash * 31 + 1;
            acc[0]++;
            return true;
        });
        return acc;
    }

    /** Nothing allocated per result; returns {count, checksum}. */
    public static long[] reuse(Plan plan) {
        int n = plan.sizes.length;
        long[] acc = {0, 1};
        long hash = tightWalk(plan, new int[n], 0, 1L, acc);
        acc[1] = hash;
        return acc;
    }

    private static long tightWalk(Plan plan, int[] indexes, int depth, long hash, long[] count) {
        if (depth == plan.sizes.length) {
            for (int value : indexes) hash = hash * 31 + value;
            count[0]++;
            return hash * 31 + 1;
        }
        for (int candidate = 0; candidate < plan.sizes[depth]; candidate++) {
            if (valid(plan, depth, candidate, indexes)) {
                indexes[depth] = candidate;
                hash = tightWalk(plan, indexes, depth + 1, hash, count);
            }
        }
        return hash;
    }

    /** Number of valid tuples by enumeration. */
    public static long count(Plan plan) {
        long[] count = {0};
        walk(plan, new int[plan.sizes.length], 0, indexes -> {
            count[0]++;
            return true;
        });
        return count[0];
    }

    /** Whether any tuple is valid; stops at the first one. */
    public static boolean isEmpty(Plan plan) {
        boolean[] found = {false};
        walk(plan, new int[plan.sizes.length], 0, indexes -> {
            found[0] = true;
            return false;
        });
        return !found[0];
    }

    /** All valid tuples as index arrays (for sampling baselines); only feasible for bounded workloads. */
    public static int[][] materialize(Plan plan) {
        List<int[]> all = new ArrayList<>();
        walk(plan, new int[plan.sizes.length], 0, indexes -> {
            all.add(indexes.clone());
            return true;
        });
        return all.toArray(new int[0][]);
    }

    /** Checksum of the tuples at ranks {@code start, start + m, ...}, by enumeration. */
    public static long everyMth(Plan plan, long m, long start) {
        long[] state = {0, 1};
        walk(plan, new int[plan.sizes.length], 0, indexes -> {
            long rank = state[0]++;
            if (rank >= start && (rank - start) % m == 0) state[1] = mix(state[1], indexes);
            return true;
        });
        return state[1];
    }

    /** Checksum of the tuples at the given ascending ranks, by enumeration. */
    public static long atRanks(Plan plan, long[] ascendingRanks) {
        long[] state = {0, 1, 0};
        walk(plan, new int[plan.sizes.length], 0, indexes -> {
            long rank = state[0]++;
            if ((int) state[2] < ascendingRanks.length && rank == ascendingRanks[(int) state[2]]) {
                state[1] = mix(state[1], indexes);
                state[2]++;
            }
            return state[2] < ascendingRanks.length;
        });
        return state[1];
    }

    public static long mix(long hash, int[] indexes) {
        for (int value : indexes) hash = hash * 31 + value;
        return hash * 31 + 1;
    }

    private static boolean walk(Plan plan, int[] indexes, int depth, Visitor visitor) {
        if (depth == plan.sizes.length) return visitor.visit(indexes);
        for (int candidate = 0; candidate < plan.sizes[depth]; candidate++) {
            if (valid(plan, depth, candidate, indexes)) {
                indexes[depth] = candidate;
                if (!walk(plan, indexes, depth + 1, visitor)) return false;
            }
        }
        return true;
    }

    private static boolean valid(Plan plan, int depth, int candidate, int[] indexes) {
        int[] trigger = plan.triggerDimension[depth];
        int[] target = plan.targetDimension[depth];
        for (int r = 0; r < trigger.length; r++) {
            int triggerIndex = trigger[r] == depth ? candidate : indexes[trigger[r]];
            int targetIndex = target[r] == depth ? candidate : indexes[target[r]];
            if (plan.triggerValues[depth][r].get(triggerIndex) && !plan.allowedValues[depth][r].get(targetIndex)) {
                return false;
            }
        }
        return true;
    }
}
