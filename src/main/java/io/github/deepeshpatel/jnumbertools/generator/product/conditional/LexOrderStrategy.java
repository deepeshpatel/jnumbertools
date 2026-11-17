/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

/** Selects the implementation used to traverse a conditional Cartesian product in lexicographic order. */
public enum LexOrderStrategy {
    /** Prefix pruning using compact BitSet relation rows, with completion lookahead for looped constraint graphs; the default. */
    PRUNED_BITSET,
    /** Prefix pruning using dense boolean relation rows, with completion lookahead for looped constraint graphs. */
    PRUNED_DFS,
    /** Exact completion-count dynamic programming. */
    DP,
    /** Selects prefix pruning with topology-triggered completion lookahead; retained as the adaptive entry point. */
    ADAPTIVE
}