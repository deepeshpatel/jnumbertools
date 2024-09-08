package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.List;

/**
 * A builder class for generating combinations from a multiset, where elements can be repeated.
 * This class helps in constructing a {@link RepetitiveCombinationMultiset} with the provided elements,
 * frequencies, and size.
 *
 * @param <T> The type of elements in the multiset.
 */
public final class MultisetCombinationBuilder<T> {

    private final List<T> elements;
    private final int[] frequencies;
    private final int size;

    /**
     * Constructs a new {@code MultisetCombinationBuilder} with the specified elements, frequencies, and combination size.
     *
     * @param elements    the list of elements from which combinations will be generated.
     * @param frequencies an array of integers representing the frequency of each element in the multiset.
     * @param size        the size of each combination to be generated.
     */
    public MultisetCombinationBuilder(List<T> elements, int[] frequencies, int size) {
        this.elements = elements;
        this.frequencies = frequencies;
        this.size = size;
    }

    /**
     * Builds a {@link RepetitiveCombinationMultiset} that generates combinations in lexicographical order.
     *
     * @return a {@code RepetitiveCombinationMultiset} instance that can generate combinations with the specified elements and frequencies.
     */
    public RepetitiveCombinationMultiset<T> lexOrder() {
        //TODO: change the signature and name.
        return new RepetitiveCombinationMultiset<>(elements, size,frequencies);
    }
}
