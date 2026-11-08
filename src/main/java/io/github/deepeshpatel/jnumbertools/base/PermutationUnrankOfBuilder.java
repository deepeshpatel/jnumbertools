/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.*;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Unranks permutations and derangements in lexicographical order.
 */
public final class PermutationUnrankOfBuilder {

    private final Calculator calculator;

    /**
     * Constructs a new PermutationUnrankOfBuilder instance with a default Calculator.
     */
    public PermutationUnrankOfBuilder() {
        this(new Calculator());
    }

    /**
     * Constructs a new PermutationUnrankOfBuilder instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public PermutationUnrankOfBuilder(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Determines the unique permutation (`n!`) at a given rank.
     * <p>
     * Un-ranks a full permutation of n distinct elements, returning an array of n
     * distinct indices from {0, 1, ..., n-1} in lexicographical order.
     * </p>
     *
     * @param rank the zero-based lexicographical rank
     * @param size the size of the permutation (n)
     * @return an array of n distinct indices
     * @throws NullPointerException if {@code rank} is null
     * @throws IllegalArgumentException if {@code size} is negative, {@code rank} is negative,
     *                                  or {@code rank >= size!}
     */
    public int[] unique(BigInteger rank, int size) {
        Objects.requireNonNull(rank, "rank must not be null");
        if (size < 0) {
            throw new IllegalArgumentException("size must be non-negative");
        }
        if (rank.signum() < 0 || rank.compareTo(calculator.factorial(size)) >= 0) {
            throw new IllegalArgumentException("rank must be in the range [0, " + size + "!)");
        }
        return FactoradicAlgorithms.unRank(rank, size);
    }

    /**
     * Determines the unique permutation (`n!`) at a given rank with minimal size.
     * <p>
     * Un-ranks a full permutation, determining the smallest n where rank &lt; n!, and
     * returns an array of n distinct indices from {0, 1, ..., n-1} in lexicographical order.
     * </p>
     *
     * @param rank the zero-based lexicographical rank
     * @return an array of n distinct indices
     * @throws NullPointerException if {@code rank} is null
     * @throws IllegalArgumentException if {@code rank} is negative
     */
    public int[] uniqueMinimumSize(BigInteger rank) {
        Objects.requireNonNull(rank, "rank must not be null");
        if (rank.signum() < 0) {
            throw new IllegalArgumentException("rank must be non-negative");
        }
        if (rank.signum() == 0) {
            return new int[0];
        }
        int size = calculator.factorialUpperBound(rank);
        return FactoradicAlgorithms.unRank(rank, size);
    }

    /**
     * Determines the k-permutation (`ⁿPₖ`) at a given rank.
     * <p>
     * Un-ranks a k-permutation of k distinct elements from {0, 1, ..., n-1}, returning
     * an array of k distinct indices in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank
     * @param n the number of distinct elements
     * @param k the size of the permutation (k ≥ 0)
     * @return an array of k distinct indices
     * @throws NullPointerException if the rank is null
     * @throws ArithmeticException  if the rank is negative or not below ⁿPₖ (this includes a negative n or k,
     *                              and k &gt; n, where no k-permutation exists)
     */
    public int[] k(BigInteger rank, int n, int k) {
        BigInteger max = calculator.nPr(n, k);
        if (rank.signum() < 0 || rank.compareTo(max) >= 0) {
            throw new ArithmeticException(
                    String.format("Out of range. Can't decode %d to mᵗʰ permutation as it is < 0 or ≥ Permutation(%d,%d)",
                            rank, n, k)
            );
        }
        return PermutadicAlgorithms.unRank(rank, n, k);
    }

    /**
     * Determines the repetitive permutation (`nʳ`) at a given lexicographical rank.
     * The unranking is delegated to the repetitive-permutation builder's rank-based generator.
     *
     * @param rank the zero-based lexicographical rank
     * @param n the number of distinct element types
     * @param width the length of the permutation
     * @return the permutation as indices in {@code [0, n)}
     * @throws NullPointerException if rank is null
     * @throws IllegalArgumentException if n or width is negative, or rank is out of range
     */
    public int[] repetitive(BigInteger rank, int n, int width) {
        Objects.requireNonNull(rank, "rank must not be null");
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (width < 0) throw new IllegalArgumentException("width must be non-negative");
        List<Integer> elements = IntStream.range(0, n).boxed().toList();
        List<Integer> result = new Permutations(calculator).repetitive(width, elements)
                .byRanks(List.of(rank)).iterator().next();
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Determines the multiset permutation at a given lexicographical rank.
     * Type indices in the result correspond to positions in {@code frequencies}.
     * Unranking is delegated to the multiset-permutation builder's rank-based generator.
     *
     * @param rank the zero-based lexicographical rank
     * @param frequencies multiplicity of each type
     * @return the sequence of type indices
     * @throws NullPointerException if rank or frequencies is null
     * @throws IllegalArgumentException if frequencies are invalid or rank is out of range
     */
    public int[] multiset(BigInteger rank, int... frequencies) {
        Objects.requireNonNull(rank, "rank must not be null");
        Objects.requireNonNull(frequencies, "frequencies must not be null");
        LinkedHashMap<Integer, Integer> options = new LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) options.put(i, frequencies[i]);
        List<Integer> result = new Permutations(calculator).multiset(options)
                .byRanks(List.of(rank)).iterator().next();
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Determines the derangement (`!n`) at a given rank.
     * <p>
     * Un-ranks a derangement of {@code n} distinct elements, returning an array
     * of {@code n} distinct indices in {@code [0, n)} such that no index equals
     * its position (i.e., a fixed-point-free permutation) in lexicographical
     * order.
     * </p>
     *
     * @param rank the lexicographical rank ({@code 0 ≤ rank < D_n})
     * @param n    the order of the derangement ({@code n ≥ 0})
     * @return an array of {@code n} distinct indices forming the derangement
     * @throws IllegalArgumentException if {@code rank} is out of range
     */
    public int[] derangement(BigInteger rank, int n) {
        Objects.requireNonNull(rank, "rank must not be null");
        // There is exactly one derangement of zero elements: the empty permutation.
        // DerangadicAlgorithms uses a minimum carrier length of 2 and cannot decode n=0.
        if (n == 0 && rank.signum() == 0) {
            return new int[0];
        }
        return new DerangadicAlgorithms(calculator).unrank(rank, n);
    }

    /**
     * Convenience overload of {@link #derangement(BigInteger, int)} for
     * {@code long} ranks.
     *
     * @param rank the lexicographical rank
     * @param n    the order of the derangement
     * @return an array of {@code n} distinct indices forming the derangement
     */
    public int[] derangement(long rank, int n) {
        return derangement(BigInteger.valueOf(rank), n);
    }
}
