package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.List;

/**
 * Utility class for Cartesian product operations.
 * <p>
 * This class provides helper methods for generating the next tuple (combination of indices)
 * in a Cartesian product sequence of a list of lists. It is intended for internal usage only.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */

@SuppressWarnings({"rawtypes"})


public final class CartesianProductUtils {

    private CartesianProductUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Updates the {@code current} indices array to the next combination in the Cartesian product sequence.
     * <p>
     * The method treats the {@code current} array as a multi-digit number where each "digit" has a
     * base equal to the size of the corresponding list in {@code elements}. It increments the number
     * by one in lexicographical order. If a "digit" reaches its maximum (i.e. the last element of the corresponding list),
     * it is reset to 0 and the next more significant digit is incremented.
     * </p>
     *
     * @param current  the current indices representing the current combination in the Cartesian product; this array is updated in place.
     * @param elements a list of lists, where each inner list represents the set of possible values for one dimension of the product.
     * @return {@code true} if the indices were successfully updated to a valid next combination; {@code false} if the last combination has been reached.
     */
    public static boolean createNext(int[] current, List elements) {
        for (int i = 0, j = current.length - 1; j >= 0; j--, i++) {
            List innerList = (List) elements.get(j);
            if (current[j] == innerList.size() - 1) {
                current[j] = 0;
            } else {
                current[j]++;
                return true;
            }
        }
        return false;
    }
}
