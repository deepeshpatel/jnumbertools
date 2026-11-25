/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;
import java.util.Collections;

/**
 * Immutable shared input model for a conditional Cartesian product.
 *
 * <p>The dimension snapshots, pairwise relations and compound rules are immutable inputs to the builder's generators.
 * The compact rows used by prefix-pruned traversal are materialized lazily and then
 * safely published for reuse by every generator created from this model.</p>
 */
final class ConstraintModel {
    final List<List<?>> dimensions;
    final PersistentRelationMap relations;
    final DimensionIndexes indexes;
    final List<MultiTriggerRule> multiRules;
    private volatile Rows rows;
    private volatile DenseRows denseRows;
    private volatile boolean denseRowsInitialized;
    private volatile ConstraintPlan plan;

    ConstraintModel(List<List<?>> dimensions, PersistentRelationMap relations) {
        this(dimensions, relations, new DimensionIndexes(dimensions), List.of());
    }

    private ConstraintModel(List<List<?>> dimensions, PersistentRelationMap relations,
                            DimensionIndexes indexes, List<MultiTriggerRule> multiRules) {
        this.dimensions = dimensions;
        this.relations = relations;
        this.indexes = indexes;
        this.multiRules = multiRules;
    }

    ConstraintModel withDimension(List<?> dimension) {
        List<List<?>> updated = new ArrayList<>(dimensions);
        updated.add(dimension);
        List<List<?>> extended = Collections.unmodifiableList(updated);
        return new ConstraintModel(extended, relations, indexes.extendedTo(extended), multiRules);
    }

    ConstraintModel withRule(int triggerDim, BitSet triggerValues, int targetDim, BitSet allowedTargets) {
        int u = Math.min(triggerDim, targetDim);
        int v = Math.max(triggerDim, targetDim);
        long key = ConstraintGraph.key(u, v);
        Relation relation = relations.get(key);
        boolean newLinkedPair = relation == null;
        if (newLinkedPair) {
            long cells = (long) dimensions.get(u).size() * dimensions.get(v).size();
            if (cells > ConstraintGraph.MAX_PAIR_CELLS) {
                throw new IllegalArgumentException("The two dimensions have too many value pairs (" + cells
                        + ") for a rule; the limit is " + ConstraintGraph.MAX_PAIR_CELLS);
            }
            relation = Relation.full(dimensions.get(u).size(), dimensions.get(v).size());
        }
        Relation updated = relation.restrict(triggerValues, allowedTargets, triggerDim == u);
        PersistentRelationMap updatedRelations = relations.with(key, updated);
        if (newLinkedPair) {
            ConstraintGraph.checkLoopBlocks(dimensions, updatedRelations.keySet(), multiRules);
        }
        return new ConstraintModel(dimensions, updatedRelations, indexes, multiRules);
    }

    /**
     * Adds a non-binary implication. No pairwise relations are added for it: the dimensions of its scope are
     * enumerated together as one node of the counting plan and the rule is checked on that node's complete
     * assignments (see {@link ConstraintGraph#nodeOf}). Existing pairwise relations are left unchanged. Where
     * the scope's dimensions are already linked by a chain of pairwise rules, the chain becomes a second node
     * rather than being merged into the scope's node.
     */
    ConstraintModel withMultiRule(MultiTriggerRule rule) {
        List<MultiTriggerRule> rules = new ArrayList<>(multiRules);
        rules.add(rule);
        List<MultiTriggerRule> updated = Collections.unmodifiableList(rules);
        ConstraintGraph.checkLoopBlocks(dimensions, relations.keySet(), updated);
        return new ConstraintModel(dimensions, relations, indexes, updated);
    }

    /** Checks a compound implication once every dimension in its scope has been assigned. */
    boolean acceptsPrefix(int assignedThrough, int[] values) {
        if (multiRules.isEmpty()) return true;
        for (MultiTriggerRule rule : multiRules) {
            if (!rule.acceptsPrefix(assignedThrough, values)) return false;
        }
        return true;
    }

    ConstraintPlan plan() {
        ConstraintPlan result = plan;
        if (result == null) {
            synchronized (this) {
                result = plan;
                if (result == null) {
                    result = new ConstraintPlan(dimensions, relations, multiRules);
                    plan = result;
                }
            }
        }
        return result;
    }

    List<Object> tuple(int[] indices) {
        Object[] values = new Object[indices.length];
        for (int i = 0; i < indices.length; i++) {
            values[i] = dimensions.get(i).get(indices[i]);
        }
        return Tuple.wrap(values);
    }

    /**
     * Returns dense boolean relation rows when their total cell count is within the configured memory cap.
     * A null result means the dense representation is too large and callers should use the BitSet rows.
     */
    DenseRows denseRows() {
        if (denseRowsInitialized) return denseRows;
        synchronized (this) {
            if (!denseRowsInitialized) {
                long cells = 0;
                for (Map.Entry<Long, Relation> entry : relations.entrySet()) {
                    int u = (int) (entry.getKey() >>> 32);
                    int v = (int) (long) entry.getKey();
                    cells += (long) dimensions.get(u).size() * dimensions.get(v).size();
                    if (cells > DenseRows.MAX_CELLS) break;
                }
                if (cells <= DenseRows.MAX_CELLS) {
                    int dimensionCount = dimensions.size();
                    // Count relations per "higher" dimension, then fill arrays directly (no per-dimension Lists).
                    int[] count = new int[dimensionCount];
                    for (Long key : relations.keySet()) {
                        count[(int) (long) key]++;
                    }
                    int[][] belowDims = new int[dimensionCount][];
                    boolean[][][][] belowRows = new boolean[dimensionCount][][][];
                    for (int d = 0; d < dimensionCount; d++) {
                        belowDims[d] = new int[count[d]];
                        belowRows[d] = new boolean[count[d]][][];
                    }
                    int[] fill = new int[dimensionCount];
                    for (Map.Entry<Long, Relation> entry : relations.entrySet()) {
                        int lower = (int) (entry.getKey() >>> 32);
                        int higher = (int) (long) entry.getKey();
                        BitSet[] source = entry.getValue().uToV();
                        int width = dimensions.get(higher).size();
                        boolean[][] dense = new boolean[source.length][];
                        // The BitSet rows are shared between the values that have the same constraints, so each
                        // distinct row is converted once and the dense rows are shared in the same way (read-only).
                        IdentityHashMap<BitSet, boolean[]> converted = new IdentityHashMap<>();
                        for (int i = 0; i < source.length; i++) {
                            boolean[] row = converted.get(source[i]);
                            if (row == null) {
                                row = new boolean[width];
                                for (int bit = source[i].nextSetBit(0); bit >= 0;
                                     bit = source[i].nextSetBit(bit + 1)) {
                                    row[bit] = true;
                                }
                                converted.put(source[i], row);
                            }
                            dense[i] = row;
                        }
                        belowDims[higher][fill[higher]] = lower;
                        belowRows[higher][fill[higher]] = dense;
                        fill[higher]++;
                    }
                    denseRows = new DenseRows(belowDims, belowRows);
                }
                denseRowsInitialized = true;
            }
        }
        return denseRows;
    }

    Rows rows() {
        Rows result = rows;
        if (result == null) {
            synchronized (this) {
                result = rows;
                if (result == null) {
                    result = Rows.build(dimensions.size(), relations);
                    rows = result;
                }
            }
        }
        return result;
    }

    static final class Rows {
        final int[][] belowDims;
        final BitSet[][][] belowRows;

        private Rows(int[][] belowDims, BitSet[][][] belowRows) {
            this.belowDims = belowDims;
            this.belowRows = belowRows;
        }

        private static Rows build(int dimensionCount, Map<Long, Relation> relations) {
            // Two-pass: count relations per "higher" dimension, then fill primitive arrays directly.
            // Iteration order of relations.keySet() and relations.entrySet() is identical (ascending key
            // order), so each dimension's entries are appended in the same order as before.
            int[] count = new int[dimensionCount];
            for (Long key : relations.keySet()) {
                count[(int) (long) key]++;
            }
            int[][] belowDims = new int[dimensionCount][];
            BitSet[][][] belowRows = new BitSet[dimensionCount][][];
            for (int d = 0; d < dimensionCount; d++) {
                belowDims[d] = new int[count[d]];
                belowRows[d] = new BitSet[count[d]][];
            }
            int[] fill = new int[dimensionCount];
            for (Map.Entry<Long, Relation> entry : relations.entrySet()) {
                int lower = (int) (entry.getKey() >>> 32);
                int higher = (int) (long) entry.getKey();
                belowDims[higher][fill[higher]] = lower;
                belowRows[higher][fill[higher]] = entry.getValue().uToV();
                fill[higher]++;
            }
            return new Rows(belowDims, belowRows);
        }
    }

    static final class DenseRows {
        static final long MAX_CELLS = 4_000_000L;
        final int[][] belowDims;
        final boolean[][][][] belowRows;

        private DenseRows(int[][] belowDims, boolean[][][][] belowRows) {
            this.belowDims = belowDims;
            this.belowRows = belowRows;
        }
    }
}
