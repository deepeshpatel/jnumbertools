/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.base;

import java.util.List;

/**
 * Utility class providing helper methods for combinatorial generators.
 *
 * @author Deepesh Patel
 */
public class Util {

    public static void validateInput(List<?> elements) {
        if(elements == null) {
            String error = "elements should not be null for combinatorics generators to work." +
                    "\nEmpty list is allowed and will be treated as empty-set(∅)";
            throw new NullPointerException(error);
        }
    }

    public static void validateNK(int n, int k) {
        if (n < 0 || k < 0) {
            throw new IllegalArgumentException(
                    String.format("n and k must be ≥ 0 for ⁿPₖ/ⁿC. Found [n=%d, k=%d]", n, k)
            );
        }
    }
}
