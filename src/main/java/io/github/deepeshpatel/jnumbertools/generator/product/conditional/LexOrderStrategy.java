/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

/** Selects the implementation used to traverse a conditional Cartesian product in lexicographic order. */
public enum LexOrderStrategy {
    /** Prefix pruning using compact BitSet relation rows, with completion lookahead for looped constraint graphs; the default. */
    PRUNED_BITSET,
    /** Prefix pruning using dense boolean relation rows when available (otherwise BitSet fallback), with completion lookahead for looped constraint graphs. */
    PRUNED_DFS,
    /** Feasibility-guided sequential traversal with exact completion counts available for parallel rank-range splitting. */
    DP,
    /** Topology-aware BitSet prefix pruning with completion lookahead for looped constraint graphs; estimated-limit overload is retained for API compatibility. */
    ADAPTIVE
}