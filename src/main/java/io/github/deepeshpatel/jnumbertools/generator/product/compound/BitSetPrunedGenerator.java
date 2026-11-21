/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

import java.util.BitSet;

/** Prefix-pruned lexicographic traversal backed by compact BitSet relation rows. */
final class BitSetPrunedGenerator extends PrunedLexGenerator {
    private final int[][] belowDims;
    private final BitSet[][][] belowRows;

    BitSetPrunedGenerator(ConstraintModel model) {
        super(model);
        ConstraintModel.Rows rows = model.rows();
        belowDims = rows.belowDims;
        belowRows = rows.belowRows;
    }

    BitSetPrunedGenerator(ConstraintModel model, int firstDimensionFrom, int firstDimensionTo) {
        super(model, firstDimensionFrom, firstDimensionTo);
        ConstraintModel.Rows rows = model.rows();
        belowDims = rows.belowDims;
        belowRows = rows.belowRows;
    }

    BitSetPrunedGenerator(ConstraintModel model, int[] prefix, int candidateFrom, int candidateTo) {
        super(model, prefix, candidateFrom, candidateTo);
        ConstraintModel.Rows rows = model.rows();
        belowDims = rows.belowDims;
        belowRows = rows.belowRows;
    }

    static int nextValidCandidate(ConstraintModel model, int[] prefix, int dimension, int from) {
        ConstraintModel.Rows relationRows = model.rows();
        return nextValidCandidate(
                relationRows.belowDims[dimension],
                relationRows.belowRows[dimension],
                prefix,
                model.dimensions.get(dimension).size(),
                from);
    }

    @Override
    protected int nextValidCandidate(int dimension, int from) {
        return nextValidCandidate(
                belowDims[dimension],
                belowRows[dimension],
                val,
                model.dimensions.get(dimension).size(),
                from);
    }

    /**
     * Iterates candidates from the most selective relation row.
     * Intersecting the constraints is order-independent; nextSetBit keeps
     * candidates in ascending order.
     */
    private static int nextValidCandidate(
            int[] earlier, BitSet[][] rows, int[] prefix, int size, int from) {
        if (from >= size) return -1;
        if (earlier.length == 0) return from;

        BitSet pivot = rows[0][prefix[earlier[0]]];
        if (earlier.length == 1) {
            int candidate = pivot.nextSetBit(from);
            return candidate >= 0 && candidate < size ? candidate : -1;
        }

        int pivotIndex = 0;
        int smallestCardinality = pivot.cardinality();
        for (int i = 1; i < earlier.length; i++) {
            BitSet allowed = rows[i][prefix[earlier[i]]];
            int cardinality = allowed.cardinality();
            if (cardinality < smallestCardinality) {
                pivot = allowed;
                pivotIndex = i;
                smallestCardinality = cardinality;
                if (cardinality == 0) return -1;
            }
        }

        if (smallestCardinality == 0) return -1;

        candidateLoop:
        for (int candidate = pivot.nextSetBit(from);
             candidate >= 0 && candidate < size;
             candidate = pivot.nextSetBit(candidate + 1)) {
            for (int i = 0; i < earlier.length; i++) {
                if (i == pivotIndex) continue;
                if (!rows[i][prefix[earlier[i]]].get(candidate)) {
                    continue candidateLoop;
                }
            }
            return candidate;
        }
        return -1;
    }
}
