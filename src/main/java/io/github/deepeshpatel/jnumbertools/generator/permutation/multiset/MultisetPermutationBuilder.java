/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.LinkedHashMap;

/**
 * Builder class for generating permutations of a multiset.
 * <p>
 * This class facilitates the generation of permutations for a multiset, where elements may repeat based on specified frequencies.
 * It supports generating all permutations in lexicographical order or retrieving a specific mᵗʰ permutation directly without
 * computing all preceding ones.
 * </p>
 *
 * @param <T> the type of elements in the multiset, must implement {@link Comparable}
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class MultisetPermutationBuilder<T> {

    private final LinkedHashMap<T, Integer> options;
    private final Calculator calculator;

    /**
     * Constructs a builder for generating multiset permutations.
     *
     * @param options    a {@link LinkedHashMap} containing elements as keys and their frequencies as values
     * @param calculator an instance of {@link Calculator} for performing combinatorial computations
     * @throws IllegalArgumentException if {@code options} is null, empty, or contains non-positive frequencies
     */
    public MultisetPermutationBuilder(LinkedHashMap<T, Integer> options, Calculator calculator) {
        if (options == null || options.isEmpty() || options.values().stream().anyMatch(f -> f <= 0)) {
            throw new IllegalArgumentException("Options must be non-null, non-empty, and contain positive frequencies");
        }
        this.options = options;
        this.calculator = calculator;
    }

    /**
     * Creates an instance of {@link MultisetPermutation} to generate all permutations based on
     * the lex order of options.keySet()
     * @return a new {@link MultisetPermutation} instance for generating permutations
     */
    public MultisetPermutation<T> lexOrder() {
        return new MultisetPermutation<>(options, calculator);
    }

    /**
     * Creates an instance of {@link MultisetPermutationMth} to generate the mᵗʰ permutation directly.
     * <p>
     * This method retrieves a specific permutation without generating all prior permutations.
     * The order of the permutation is lex order of options.keySet()

     * The {@code m} parameter specifies the 0-based index of the desired permutation, relative to the {@code start} index.
     * </p>
     *
     * @param m     the index of the desired permutation (0-based)
     * @param start the starting index for permutation generation
     * @return a new {@link MultisetPermutationMth} instance for generating the mᵗʰ permutation
     * @throws IllegalArgumentException if {@code m} or {@code start} is negative
     */
    public MultisetPermutationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        if (m.signum() < 0 || start.signum() < 0) {
            throw new IllegalArgumentException("Index 'm' and 'start' must be non-negative");
        }
        return new MultisetPermutationMth<>(options, m, start, calculator);
    }

    public MultisetPermutationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates an instance that samples multiset permutations randomly without replacement.
     *
     * @param sampleSize the number of permutations to generate; must be positive and ≤ total permutations
     * @return a new {@link MultisetPermutationOFRanks} instance for random sampling
     * @throws IllegalArgumentException if {@code sampleSize} is not positive or exceeds total permutations
     */
    public MultisetPermutationOFRanks<T> sample(int sampleSize) {
        BigInteger total = calculator.multinomial(options.values().stream().mapToInt(Integer::intValue).toArray());
        if (sampleSize <= 0 || BigInteger.valueOf(sampleSize).compareTo(total) > 0) {
            throw new IllegalArgumentException("Sample size must be positive and not exceed total permutations");
        }
        return new MultisetPermutationOFRanks<>(options, new BigIntegerSample(total, sampleSize), calculator);
    }

    /**
     * Creates an instance that samples multiset permutations randomly with replacement.
     *
     * @param sampleSize the number of permutations to generate; must be positive
     * @return a new {@link MultisetPermutationOFRanks} instance for random sampling with replacement
     * @throws IllegalArgumentException if {@code sampleSize} is not positive
     */
    public MultisetPermutationOFRanks<T> choice(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        BigInteger total = calculator.multinomial(options.values().stream().mapToInt(Integer::intValue).toArray());
        return new MultisetPermutationOFRanks<>(options, new BigIntegerChoice(total, sampleSize), calculator);
    }

    /**
     * Generates unique multiset permutations at specified lexicographical rank positions.
     * <p>
     * <b>Example for [A, A, B, C]:</b>
     * <pre>
     * Rank | Permutation    | Note
     * -----|---------------|------
     * 0    | [A, A, B, C]  |
     * 1    | [A, A, C, B]  |
     * 2    | [A, B, A, C]  |
     * ...  | ...           | (12 total unique permutations)
     * 11   | [C, B, A, A]  |
     *
     * atRanks([0, 2, 11]) → [A,A,B,C], [A,B,A,C], [C,B,A,A]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < n!/(m₁!m₂!...))
     *             where mᵢ are duplicate counts
     * @return Multiset permutation generator
     * @throws IllegalArgumentException if any rank ≥ total unique permutations
     * @throws IllegalStateException if no generation strategy was selected
     *
     */
    public MultisetPermutationOFRanks<T> arRanks(Iterable<BigInteger> ranks) {
        return new MultisetPermutationOFRanks<>(options, ranks, calculator);
    }
}