/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * Immutable shared input model for a conditional Cartesian product.
 *
 * <p>The dimension snapshots and pairwise relations are immutable inputs to the builder's generators.
 * The compact rows used by prefix-pruned traversal are materialized lazily and then
 * safely published for reuse by every generator created from this model.</p>
 */
final class ConstraintModel {
    final List<List<?>> dimensions;
    final PersistentRelationMap relations;
    private volatile Rows rows;

    ConstraintModel(List<List<?>> dimensions, PersistentRelationMap relations) {
        this.dimensions = dimensions;
        this.relations = relations;
    }

    ConstraintModel withDimension(List<?> dimension) {
        List<List<?>> updated = new ArrayList<>(dimensions);
        updated.add(dimension);
        return new ConstraintModel(Collections.unmodifiableList(updated), relations);
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
        return new ConstraintModel(dimensions, updatedRelations);
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
                rowLists.get(higher).add(relation.uToV);
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
}
