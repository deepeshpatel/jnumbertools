/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.scenario;

import java.util.BitSet;

/** One directed conditional-product constraint used by the benchmark scenarios. */
public record Rule(int triggerDimension, BitSet triggerValues,
                   int targetDimension, BitSet allowedValues) {
}
