/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombination;
import io.github.deepeshpatel.jnumbertools.generator.IteratorSequence;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Implements the iterable of unique permutations of size k.
 * Permutations are generated in lex order of indices of input values, considering value at each indices as unique.
 * For example all permutations of size 2 for [1,2,3] are -
 * <pre>
 *     [1, 2], [2, 1], [1, 3], [3, 1], [2, 3] and [3, 2]
 *
 * Code example -
 *
 *  new KPermutation&lt;&gt;(Arrays.asList("A","B","C"),2)
 *      .forEach(System.out::println);
 *
 *  or
 *
 *  JJNumberTools.permutationsOf("A","B","C")
 *      .k(2)
 *      .forEach(System.out::println);
 *
 * will generate -
 * [A, B]
 * [B, A]
 * [A, C]
 * [C, A]
 * [B, C]
 * [C, B]
 * </pre>
 * @author Deepesh Patel
 */
public class KPermutation<T> extends AbstractGenerator<T> {

    final int k;

    /**
     * @param seed Input of size n from which unique permutations of size k will be generated.
     * @param k size of permutations. k must be &lt;=n
     */
    public KPermutation(Collection<T> seed, int k) {
        super(seed);
        if(k<0) {
            throw new IllegalArgumentException(" k>=0");
        }
        this.k = k;
    }

    @Override
    public Iterator<List<T>> iterator() {
        if(k==0) {
            return emptyIterator();
        }
        List<Iterator<List<T>>> iterators = new UniqueCombination<>(seed, k)
                .stream()
                .map(e -> new UniquePermutation<>(e).iterator()).collect(Collectors.toList());
        return new IteratorSequence<>(iterators);
    }

}