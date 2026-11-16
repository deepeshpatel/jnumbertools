/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Immutable shared input model for a conditional Cartesian product.
 *
 * <p>The dimension snapshots and pairwise relations are shared by derived builders.
 * The compact rows used by prefix-pruned traversal are materialized lazily and then
 * safely published for reuse by every generator created from this model.</p>
 */
final class ConstraintModel {
    final List<List<?>> dimensions;
    final Map<Long, Relation> relations;
    private volatile Rows rows;

    ConstraintModel(List<List<?>> dimensions, Map<Long, Relation> relations) {
        this.dimensions = dimensions;
        this.relations = relations;
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
