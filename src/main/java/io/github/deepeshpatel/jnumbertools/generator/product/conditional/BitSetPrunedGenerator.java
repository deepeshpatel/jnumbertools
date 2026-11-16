/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

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
        int[] earlier = relationRows.belowDims[dimension];
        int size = model.dimensions.get(dimension).size();
        if (from >= size) return -1;
        if (earlier.length == 0) return from;

        BitSet[][] rows = relationRows.belowRows[dimension];
        BitSet firstAllowed = rows[0][prefix[earlier[0]]];
        candidateLoop:
        for (int candidate = firstAllowed.nextSetBit(from);
             candidate >= 0 && candidate < size;
             candidate = firstAllowed.nextSetBit(candidate + 1)) {
            for (int i = 1; i < earlier.length; i++) {
                if (!rows[i][prefix[earlier[i]]].get(candidate)) continue candidateLoop;
            }
            return candidate;
        }
        return -1;
    }

    @Override
    protected int nextValidCandidate(int dimension, int from) {
        int size = model.dimensions.get(dimension).size();
        if (from >= size) return -1;

        int[] earlier = belowDims[dimension];
        if (earlier.length == 0) return from;

        BitSet[][] rows = belowRows[dimension];
        BitSet firstAllowed = rows[0][val[earlier[0]]];
        candidateLoop:
        for (int candidate = firstAllowed.nextSetBit(from);
             candidate >= 0 && candidate < size;
             candidate = firstAllowed.nextSetBit(candidate + 1)) {
            for (int i = 1; i < earlier.length; i++) {
                if (!rows[i][val[earlier[i]]].get(candidate)) continue candidateLoop;
            }
            return candidate;
        }
        return -1;
    }
}
