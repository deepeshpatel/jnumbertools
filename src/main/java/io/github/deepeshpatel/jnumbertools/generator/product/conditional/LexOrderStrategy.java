/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

/** Selects the implementation used to traverse a conditional Cartesian product in lexicographic order. */
public enum LexOrderStrategy {
    /** Prefix pruning using compact BitSet relation rows; the default. */
    PRUNED_BITSET,
    /** Prefix pruning using a direct indexed DFS iterator and dense boolean relation rows. */
    PRUNED_DFS,
    /** Exact completion-count dynamic programming. */
    DP,
    /** Selects PRUNED_BITSET or DP using the expected number of tuples to consume. */
    ADAPTIVE
}