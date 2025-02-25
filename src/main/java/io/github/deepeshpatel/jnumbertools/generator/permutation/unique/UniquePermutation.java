/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermItrForElements;

import java.util.Iterator;
import java.util.List;

/**
 * Utility to generate all n! unique permutations (with no repeated values).
 * <p>
 * Permutations are generated in lexicographic order of indices of input values,
 * treating the value at each index as unique.
 * </p>
 *
 * <p>
 * For more information on permutations, see
 * <a href="https://en.wikipedia.org/wiki/Permutation">Wikipedia Permutation</a>.
 * </p>
 *
 * <p>
 * Instance of this class is intended to be created via a builder and hence does not have a public constructor.
 * Example:
 *         <pre><code class="language-java">
 * int size = 3;
 * new Permutations().unique(size)
 *         .lexOrder()
 *         .stream().toList();
 *         </code></pre>
 * This will generate the following output (all possible permutations of "A", "B", and "C" in lexicographic order):
 * <pre>
 * [A, B, C]
 * [A, C, B]
 * [B, A, C]
 * [B, C, A]
 * [C, A, B]
 * [C, B, A]
 * </pre>
 * </p>
 *
 * @param <T> the type of elements in the permutation
 * @see <a href="https://en.wikipedia.org/wiki/Permutation">Wikipedia Permutation</a>
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermutation<T> extends AbstractGenerator<T> {

    /**
     * Constructs a UniquePermutation generator for the specified list of elements.
     *
     * @param elements the list of elements to generate permutations from
     */
    UniquePermutation(List<T> elements) {
        super(elements);
    }

    /**
     * Returns an iterator over the unique permutations of the elements.
     *
     * @return an iterator of lists, each representing a unique permutation
     */
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? Util.emptyListIterator() :
                new UniquePermItrForElements<>(elements.size(), this::indicesToValues);
    }

}
