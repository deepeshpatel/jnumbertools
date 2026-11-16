/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

/**
 * Traversal strategies for {@link ConditionalProductBuilder#lexOrder()}.
 *
 * <p>{@link #PRUNED} is the default strategy. It performs lexicographic backtracking and checks
 * constraints against the selected prefix without constructing the exact counting plan.
 * {@link #DP} uses the exact counting plan to detect prefixes that have no valid completion.
 * {@link #ADAPTIVE} is a selector used with
 * {@link ConditionalProductBuilder#lexOrder(LexOrderStrategy, long)}; it chooses PRUNED or DP
 * from the caller's expected number of tuples to consume.</p>
 *
 * <p>This enum controls only ordinary lexicographic traversal. Exact counting and rank-based
 * operations continue to use the DP plan.</p>
 *
 * @see ConditionalProductBuilder#lexOrder()
 * @see ConditionalProductBuilder#lexOrder(LexOrderStrategy)
 * @see ConditionalProductBuilder#lexOrder(LexOrderStrategy, long)
 */
public enum LexOrderStrategy {
    /** Prefix-based backtracking; avoids building the counting plan. This is the default. */
    PRUNED,

    /** Uses exact DP completion counts to skip prefixes with no valid completion. */
    DP,

    /** Selects PRUNED or DP from an estimated traversal size. */
    ADAPTIVE
}
