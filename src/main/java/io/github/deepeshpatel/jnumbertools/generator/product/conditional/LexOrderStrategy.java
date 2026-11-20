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
    /**
     * Lets the library choose the algorithm. There is no guarantee about which algorithm runs for which workload or
     * condition, and the choice may change between versions; each version's actual choice is documented in the
     * changelog and the maintainer decision log. Use an explicit strategy when a specific algorithm is required.
     */
    ADAPTIVE
}