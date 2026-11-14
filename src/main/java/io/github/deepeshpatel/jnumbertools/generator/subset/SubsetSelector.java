/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.List;

/**
 * First step of building subsets: the elements are known, the size range is not.
 * <p>
 * Obtained from {@code JNumberTools.subsets().of(...)}. Choose the sizes with {@link #all()} or
 * {@link #inRange(int, int)}; either returns a {@link SubsetBuilder}, which offers {@code count()},
 * {@code lexOrder()} and the other operations. Because only a {@code SubsetBuilder} has those
 * operations, forgetting to choose a size range does not compile.
 * </p>
 *
 * <pre>{@code
 * JNumberTools.subsets().of("A", "B", "C").all().lexOrder();       // every subset
 * JNumberTools.subsets().of("A", "B", "C").inRange(1, 2).count();  // subsets of size 1 or 2
 * }</pre>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @param <T> the type of the elements
 * @see SubsetBuilder
 * @author Deepesh Patel
 */
public final class SubsetSelector<T> {

    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Creates a selector for the given elements.
     *
     * @param elements   the elements from which subsets will be generated; copied, so later changes to
     *                   the caller's list do not affect this selector. An empty list is allowed (see
     *                   ValidationRules.md)
     * @param calculator a calculator used for computing subset counts
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public SubsetSelector(List<T> elements, Calculator calculator) {
        // Null list is a programmer error (rule 1), not an empty set.
        Util.validateInput(elements);
        // Snapshot (rule 8): later changes to the caller's list must not alter this selector.
        // Null elements are rejected here, during the copy, rather than later during iteration (rule 7).
        this.elements = Util.copyOfNoNulls(elements);
        this.calculator = calculator;
    }

    /**
     * Configures the builder to generate all subsets of the given elements.
     *
     * @return a builder for all subsets
     */
    public SubsetBuilder<T> all() {
        return new SubsetBuilder<>(elements, calculator, 0, elements.size());
    }

    /**
     * Restricts subset generation to sizes within the specified range.
     * <p>
     * Only subsets whose size is between {@code from} and {@code to} (inclusive) will be generated.
     * Subsets are produced in lexicographical order, first by size, then by combination order within each size.
     * </p>
     *
     * <p>
     * For an empty set the rows of the SUBSETS table in ValidationRules.md apply: every range with
     * {@code 0 ≤ from ≤ to} is accepted ({@code from = 0} gives the single empty subset,
     * {@code from > 0} gives none), however large {@code to} is.
     * </p>
     *
     * @param from the minimum subset size (inclusive, must be ≥ 0)
     * @param to the maximum subset size (inclusive, must be ≥ from, and ≤ elements.size() for a non-empty list)
     * @return a builder for the specified size range
     * @throws IllegalArgumentException if from &lt; 0, to &lt; from, or (non-empty list) to &gt; elements.size()
     *         (which also covers from &gt; elements.size())
     */
    public SubsetBuilder<T> inRange(int from, int to) {
        if (from < 0 || to < from) {
            throw new IllegalArgumentException(
                    String.format("Invalid range: from=%d, to=%d - must satisfy 0 ≤ from ≤ to", from, to)
            );
        }

        // Empty list special cases
        if (elements.isEmpty()) {
            // All ranges with from ≥ 0 are valid, but will produce:
            // - from=0 → count=1, returns [[]]
            // - from>0 → count=0, returns []
            return new SubsetBuilder<>(elements, calculator, from, to);
        }

        // For non-empty list, validate bounds (to ≥ from, so to ≤ n also guarantees from ≤ n)
        if (to > elements.size()) {
            throw new IllegalArgumentException("Invalid range: to cannot exceed " + elements.size());
        }

        return new SubsetBuilder<>(elements, calculator, from, to);
    }

    @Override
    public String toString() {
        return "SubsetSelector{elements=" + elements + '}';
    }
}
