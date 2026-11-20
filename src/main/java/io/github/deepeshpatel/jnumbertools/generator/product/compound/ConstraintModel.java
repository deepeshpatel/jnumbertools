/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

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
            ConstraintGraph.checkLoopBlocks(dimensions, updatedRelations.keySet());
        }
        return new ConstraintModel(dimensions, updatedRelations, indexes, multiRules);
    }

    /**
     * Adds a non-binary implication. A full-relation clique joins every dimension in the rule's
     * scope into one enumerated constraint block; the rule itself is checked on complete block
     * assignments. Existing pairwise relations are left unchanged.
     */
    ConstraintModel withMultiRule(MultiTriggerRule rule) {
        PersistentRelationMap updated = relations;
        int[] scope = rule.scope;
        boolean addedEdge = false;
        for (int i = 0; i < scope.length; i++) {
            for (int j = i + 1; j < scope.length; j++) {
                int u = scope[i];
                int v = scope[j];
                long key = ConstraintGraph.key(u, v);
                if (updated.get(key) == null) {
                    long cells = (long) dimensions.get(u).size() * dimensions.get(v).size();
                    if (cells > ConstraintGraph.MAX_PAIR_CELLS) {
                        throw new IllegalArgumentException("The two dimensions in a multi-trigger rule have too many "
                                + "value pairs (" + cells + "); the limit is " + ConstraintGraph.MAX_PAIR_CELLS);
                    }
                    updated = updated.with(key, Relation.full(dimensions.get(u).size(), dimensions.get(v).size()));
                    addedEdge = true;
                }
            }
        }
        if (addedEdge) ConstraintGraph.checkLoopBlocks(dimensions, updated.keySet());
        List<MultiTriggerRule> rules = new ArrayList<>(multiRules);
        rules.add(rule);
        return new ConstraintModel(dimensions, updated, indexes, Collections.unmodifiableList(rules));
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
                    List<List<Integer>> dimensionLists = new ArrayList<>(dimensions.size());
                    List<List<boolean[][]>> rowLists = new ArrayList<>(dimensions.size());
                    for (int d = 0; d < dimensions.size(); d++) {
                        dimensionLists.add(new ArrayList<>());
                        rowLists.add(new ArrayList<>());
                    }
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
                        dimensionLists.get(higher).add(lower);
                        rowLists.get(higher).add(dense);
                    }
                    int[][] belowDims = new int[dimensions.size()][];
                    boolean[][][][] belowRows = new boolean[dimensions.size()][][][];
                    for (int d = 0; d < dimensions.size(); d++) {
                        belowDims[d] = dimensionLists.get(d).stream().mapToInt(Integer::intValue).toArray();
                        belowRows[d] = rowLists.get(d).toArray(new boolean[0][][]);
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
            List<List<Integer>> dimensionLists = new ArrayList<>(dimensionCount);
            List<List<BitSet[]>> rowLists = new ArrayList<>(dimensionCount);
            for (int d = 0; d < dimensionCount; d++) {
                dimensionLists.add(new ArrayList<>());
                rowLists.add(new ArrayList<>());
            }
            for (Map.Entry<Long, Relation> entry : relations.entrySet()) {
                int lower = (int) (entry.getKey() >>> 32);
                int higher = (int) (long) entry.getKey();
                Relation relation = entry.getValue();
                dimensionLists.get(higher).add(lower);
                rowLists.get(higher).add(relation.uToV());
            }
            int[][] belowDims = new int[dimensionCount][];
            BitSet[][][] belowRows = new BitSet[dimensionCount][][];
            for (int d = 0; d < dimensionCount; d++) {
                belowDims[d] = dimensionLists.get(d).stream().mapToInt(Integer::intValue).toArray();
                belowRows[d] = rowLists.get(d).toArray(new BitSet[0][]);
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
