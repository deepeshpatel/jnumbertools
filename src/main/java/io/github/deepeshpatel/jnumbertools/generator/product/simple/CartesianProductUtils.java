package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.List;

/**
 * Utility class for Cartesian product operations.
 */
public final class CartesianProductUtils {

    private CartesianProductUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Generates the next combination in the Cartesian product sequence.
     * @param current The current indices.
     * @param elements A list of lists containing elements.
     * @param <T> The type of elements in the lists.
     * @return True if there is a next combination, false otherwise.
     */
    public static <T> boolean createNext(int[] current, List<List<T>> elements) {
        for (int i = 0, j = current.length - 1; j >= 0; j--, i++) {
            if (current[j] == elements.get(j).size() - 1) {
                current[j] = 0;
            } else {
                current[j]++;
                return true;
            }
        }
        return false;
    }
}
