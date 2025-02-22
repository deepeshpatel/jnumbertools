package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.*;

public class Util {
    /**
     * Returns an iterator over a collection that contains a single element: an empty list.
     * <p>
     * This iterator is designed to mimic the null-set (φ) by returning a collection with
     * a size of one (a single empty list), which is necessary for correct count assertions.
     * </p>
     *
     * @param <E> the type of elements contained in the empty list.
     * @return an iterator over a collection with a single empty list.
     */
    public static <E> Iterator<List<E>> emptyListIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty list (size is 1),
        // which is required to mimic the null-set (φ) for correct count assertions.
        return Collections.singletonList(Collections.<E>emptyList()).iterator();
    }

    public static <K,V> Iterator<Map<K,V>> emptyMapIterator() {
        // Note: Do not replace this with Collections.emptyIterator().
        // This iterator returns a list containing an empty map (size is 1),
        // which is required to mimic the null-set (φ) for correct count assertions.
        return Collections.singletonList(Collections.<K,V>emptyMap()).iterator();
    }

    public static <T> Map<T, Integer> createFrequencyMap(Map.Entry<T, Integer>[] sortedEntries, int[] sortedArray) {
        // Initialize an empty map with generic type T as keys
        Map<T, Integer> freqMap = new LinkedHashMap<>();

        if (sortedArray.length == 0) {
            return freqMap;
        }

        // Count frequencies in one pass through sorted intArray
        int currentNum = sortedArray[0];
        int currentCount = 1;

        for (int i = 1; i < sortedArray.length; i++) {
            if (sortedArray[i] == currentNum) {
                currentCount++;
            } else {
                freqMap.put(sortedEntries[currentNum].getKey(), currentCount); // Use List.get() instead of array access
                currentNum = sortedArray[i];
                currentCount = 1;
            }
        }

        // Handle the last group
        freqMap.put(sortedEntries[currentNum].getKey(), currentCount); // Use List.get() for last group

        return freqMap;
    }

    /**
     * Creates an array of {@code Map.Entry} objects from the given map with entries sorted by keys in lexicographical order.
     * <p>
     * The resulting array contains entries sorted based on the natural ordering of the keys
     * (lexicographical order for strings, or comparable ordering for other types). If the input map is
     * {@code null} or empty, an empty array is returned.
     * </p>
     *
     * @param <T> the type of keys in the map, must implement {@code Comparable<T>}
     * @param input the input map whose entries are to be sorted by keys
     * @return an array of {@code Map.Entry<T, Integer>} sorted by keys in lexicographical order
     * @throws ClassCastException if the keys in the input map do not implement {@code Comparable}
     */
    public static <T extends Comparable<T>> Map.Entry<T, Integer>[] toLexOrderedMap(Map<T, Integer> input) {
        if (input == null || input.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map.Entry<T, Integer>[] emptyArray = (Map.Entry<T, Integer>[]) new Map.Entry[0];
            return emptyArray;
        }
        return input.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toArray(Map.Entry[]::new);
    }
}
