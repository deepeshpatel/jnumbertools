/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

/**
 * Prefix-pruned lexicographic traversal using dense boolean relation rows.
 *
 * <p>The dense rows are shared by the immutable constraint model and are created only when the
 * aggregate relation-cell count is within {@link ConstraintModel.DenseRows#MAX_CELLS}.</p>
 */
final class DenseDfsGenerator extends PrunedLexGenerator {
    private final int[][] belowDims;
    private final boolean[][][][] belowRows;

    DenseDfsGenerator(ConstraintModel model, ConstraintModel.DenseRows rows) {
        super(model);
        this.belowDims = rows.belowDims;
        this.belowRows = rows.belowRows;
    }

    DenseDfsGenerator(ConstraintModel model, ConstraintModel.DenseRows rows,
                      int firstDimensionFrom, int firstDimensionTo) {
        super(model, firstDimensionFrom, firstDimensionTo);
        this.belowDims = rows.belowDims;
        this.belowRows = rows.belowRows;
    }

    @Override
    protected int nextValidCandidate(int dimension, int from) {
        int size = model.dimensions.get(dimension).size();
        if (from >= size) return -1;

        int[] earlier = belowDims[dimension];
        if (earlier.length == 0) return from;

        boolean[][][] rows = belowRows[dimension];
        candidateLoop:
        for (int candidate = from; candidate < size; candidate++) {
            for (int i = 0; i < earlier.length; i++) {
                if (!rows[i][val[earlier[i]]][candidate]) continue candidateLoop;
            }
            return candidate;
        }
        return -1;
    }
}
