package io.github.deepeshpatel.jnumbertools.experiments.abstractalgebra;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Generates number partitions of a given total `r` using an infinite supply of elements.
 * Uses an iterator to generate partitions one by one.
 */
public class InfiniteSupplyPartition implements Iterable<Map<String, Integer>> {
    private final int r;
    private final boolean reverse;
    private final String[] elements;

    public InfiniteSupplyPartition(int r, boolean reverse, String[] elements) {
        this.r = r;
        this.reverse = reverse;
        this.elements = elements;
    }

    @Override
    public Iterator<Map<String, Integer>> iterator() {
        //return new PartitionIterator(r, elements);
        return reverse ? new PartitionIterator(r, elements) : new PartitionIterator(r, elements);
    }

    private static class PartitionIterator implements Iterator<Map<String, Integer>> {
        protected final int r;
        protected final String[] elements;
        protected final int[] partition;
        protected int length;

        public PartitionIterator(int r, String[] elements) {
            this.r = r;
            this.elements = elements;
            this.partition = new int[elements.length];
            this.partition[0] = r;
            this.length = 1;
        }

        @Override
        public boolean hasNext() {
            return length > 0;
        }

        @Override
        public Map<String, Integer> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Map<String, Integer> result = new LinkedHashMap<>();
            for (int i = 0; i < length; i++) {
                result.put(elements[i], partition[i]);
            }

            generateNextPartition();
            return result;
        }

        /**
         * Generates the next partition in lexicographical order.
         */
        protected void generateNextPartition() {
            int sum = 0;
            int i;

            for (i = length - 1; i >= 0 && partition[i] == 1; i--) {
                sum += partition[i];
            }

            if (i < 0) {
                length = 0;
                return;
            }

            partition[i]--;
            sum++;

            while (sum > 0 && i + 1 < elements.length) {
                partition[i + 1] = Math.min(partition[i], sum);
                sum -= partition[i + 1];
                i++;
            }

            length = i + 1;
        }
    }

    public static void main(String[] args) {
        int r = 5; // Example
        boolean reverse = false; // Change to true for reverse order
        String[] elements = {"Mango", "Guava", "Banana"};

        InfiniteSupplyPartition isp = new InfiniteSupplyPartition(r, reverse, elements);

        for (Map<String, Integer> partition : isp) {
            System.out.println(partition);
        }
    }
}
