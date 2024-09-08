/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.complex;

import io.github.deepeshpatel.jnumbertools.generator.product.simple.CartesianProductUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a complex product of multiple sets of combinations or subsets.
 * <p>
 * This class provides an iterator for generating the Cartesian product of the lists contained within the complex product.
 * It also provides a stream for functional-style operations on the product.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * ComplexProduct complexProduct = new ComplexProduct(List.of(
 *     List.of(List.of("A", "B"), List.of("C", "D")),
 *     List.of(List.of("E", "F"), List.of("G", "H"))
 * ));
 * complexProduct.stream().forEach(System.out::println);
 * </pre>
 * This example creates a complex product of Cartesian products and prints each combination.
 *
 * @since 1.0.3
 * @author Deepesh Patel
 */
public class ComplexProduct implements Iterable<List<?>> {

    private final List<List<List<?>>> elements;

    /**
     * Constructs a ComplexProduct with the specified list of combinations or subsets.
     *
     * @param elements a list of lists where each inner list contains lists of elements
     */
    public ComplexProduct(List<List<List<?>>> elements) {
        this.elements = elements;
    }

    /**
     * Computes the total number of possible combinations in the complex product.
     *
     * @return the total number of possible combinations
     */
    public long count() {
        long count = 1;
        for (var e : elements) {
            count *= e.size();
        }
        return count;
    }

    @Override
    public Iterator<List<?>> iterator() {
        return new Itr();
    }

    /**
     * Returns a stream of the Cartesian product combinations.
     *
     * @return a stream of combinations
     */
    public Stream<List<?>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    private class Itr implements Iterator<List<?>> {

        final int[] current = new int[elements.size()];
        boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<?> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            var result = indicesToList();
            hasNext = CartesianProductUtils.createNext(current, elements);
            return result;
        }

        private List<Object> indicesToList() {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < elements.size(); i++) {
                list.addAll(elements.get(i).get(current[i]));
            }
            return list;
        }
    }
}
