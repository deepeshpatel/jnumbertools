/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

/** Selects the implementation used to traverse a conditional Cartesian product in lexicographic order. */
public enum LexOrderStrategy {
    /** Prefix pruning using compact BitSet relation rows and lazy work-triggered completion lookahead; the default. */
    PRUNED_BITSET,
    /** Prefix pruning using dense boolean relation rows when available (otherwise BitSet fallback), with lazy work-triggered completion lookahead. */
    PRUNED_DFS,
    /** Feasibility-guided sequential traversal with exact completion counts available for parallel rank-range splitting. */
    DP,
    /** Implementation policy whose internal algorithm selection is not an API guarantee and may change between versions. */
    ADAPTIVE
}