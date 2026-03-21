/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import io.github.deepeshpatel.jnumbertools.api.Calculator;

import java.math.BigInteger;
import java.util.*;

/**
 * Utility class providing helper methods for combinatorial generators.
 *
 * @author Deepesh Patel
 */
public class Util {
    /**
     * Returns an iterator over a collection that contains a single element: an empty list.
     * <p>
     * This iterator is designed to mimic the empty-set(∅)by returning a collection with
     * a size of one (a single empty list), which is necessary for correct count assertions.
     * </p>
     *
     * @param <E> the type of elements contained in the empty list.
     * @return an iterator over a collection with a single empty list.
     */
    public static <E> Iterator<List<E>> emptyListIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty list (size is 1),
        // which is required to mimic the empty-set(∅)for correct count assertions.
        return Collections.singletonList(Collections.<E>emptyList()).iterator();
    }

    public static <K,V> Iterator<Map<K,V>> emptyMapIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty map (size is 1),
        // which is required to mimic the empty-set(∅)for correct count assertions.
        return Collections.singletonList(Collections.<K,V>emptyMap()).iterator();
    }

    public static void validateInput(List<?> elements) {
        if(elements == null) {
            String error = "elements should not be null for combinatorics generators to work." +
                    "\nEmpty list is allowed and will be treated as empty-set(∅)";
            throw new NullPointerException(error);
        }
    }

    public static void validateNotNegative(int n, String varName) {
        if(n < 0) {
            throw new IllegalArgumentException(varName + " must be ≥ 0");
        }
    }

    public static void validateMapOptions(LinkedHashMap<?, Integer> options) {
        String message = "Options must be non-null, and contain frequencies ≥ 0";
        if(options == null) {
            throw new NullPointerException(message);
        }
        if (options.values().stream().anyMatch(f -> f < 0)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateMapOptions(LinkedHashMap<?, Integer> options, int r, Calculator calculator) {
        validateMapOptions(options);
        int[] frequencies = options.values().stream().mapToInt(Integer::intValue).toArray();
        calculator.multisetCombinationsCount(r, frequencies);
    }

    public static boolean isEmptyList(List<List<Object>> list) {
        return list.size() == 1 && list.get(0).isEmpty();
    }

    public static void validateNK(int n, int k) {
        if (n < 0 || k < 0) {
            throw new IllegalArgumentException(
                    String.format("n and k must be ≥ 0 for ⁿPₖ/ⁿC. Found [n=%d, k=%d]", n, k)
            );
        }
    }

    /**
     * Filters out zero-frequency entries from a multiset map, keeping only positive frequencies.
     * Zero frequencies mean "element not available" and should be excluded from processing.
     *
     * @param <T> the type of elements
     * @param multiset map of elements to their frequencies
     * @return a new LinkedHashMap containing only elements with positive frequencies
     * @throws NullPointerException if multiset is null
     */
    public static <T> LinkedHashMap<T, Integer> filterZeroFrequencies(LinkedHashMap<T, Integer> multiset) {
        if (multiset == null) {
            throw new NullPointerException("Multiset map cannot be null");
        }
        
        LinkedHashMap<T, Integer> filtered = new LinkedHashMap<>();
        for (Map.Entry<T, Integer> entry : multiset.entrySet()) {
            Integer freq = entry.getValue();
            if (freq != null && freq > 0) {  // Only keep positive frequencies
                filtered.put(entry.getKey(), freq);
            }
        }
        return filtered;
    }

    /**
     * Validates parameters commonly used in lexOrderMth(m, start) calls, including upper bound check.
     * <p>
     * Builders should call this method before creating a generator to ensure fail-fast validation.
     * This enhanced version also validates that the starting rank is within the valid range
     * of available elements.
     * </p>
     *
     * @param m     increment/step size (must be positive)
     * @param start starting rank (must be non-negative and less than count)
     * @param count total number of elements available (must be non-negative)
     * @throws IllegalArgumentException if:
     *         <ul>
     *           <li>m is null or ≤ 0</li>
     *           <li>start is null or < 0</li>
     *           <li>start is ≥ count (out of valid range)</li>
     *         </ul>
     */

    public static void validateLexOrderMthParams(BigInteger m, BigInteger start, BigInteger count) {
        if (m == null || m.signum() <= 0) {
            throw new IllegalArgumentException(String.format("Increment 'm' must be positive (m > 0). Found " + m));
        }

        if (start == null ||
                start.signum() < 0 ||
                (start.compareTo(count) >= 0 && count.signum()> 0 )) {
            var msg = String.format("Element should be in range [0,%s). Found %s", count, start);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Validates parameters commonly used in byRanks(ranks) calls.
     * <p>
     * Builders should call this method before creating a generator
     * to ensure fail-fast validation for null parameters only.
     * Invalid rank values (negative, out of bounds) will be caught
     * naturally during iteration without performance overhead.
     * </p>
     * @param ranks iterable of ranks to validate
     * @throws IllegalArgumentException if ranks is null
     */
    public static void validateByRanksParams(Iterable<BigInteger> ranks) {
        if (ranks == null) {
            throw new IllegalArgumentException("Ranks sequence cannot be null");
        }
    }

}
