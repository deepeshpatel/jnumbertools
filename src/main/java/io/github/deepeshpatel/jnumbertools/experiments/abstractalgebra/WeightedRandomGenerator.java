/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.experiments.abstractalgebra;

import java.math.BigInteger;
import java.util.*;

public class WeightedRandomGenerator implements Iterable<BigInteger> {

    private final Random random;
    private final BigInteger maxRank;
    private final double[] probabilities;
    private final int[] alias;

    /**
     * Constructs a weighted random generator with specified weights and maximum rank.
     * <p>
     * The weights define the relative probability of each index, normalized internally to sum to 1.0.
     * The {@code maxRank} determines the output range {@code [0, maxRank-1]}. Each index {@code i} maps to
     * a subrange {@code [i * maxRank/n, (i+1) * maxRank/n)}, with probability proportional to
     * {@code weights[i]}.
     * <p>
     * <b>Throws:</b>
     * <ul>
     *   <li>{@code IllegalArgumentException} if {@code weights} is null, empty, or contains negative values.</li>
     *   <li>{@code IllegalArgumentException} if {@code weights} sum to zero or less.</li>
     *   <li>{@code IllegalArgumentException} if {@code maxRank} is null or non-positive.</li>
     * </ul>
     *
     * @param weights List of non-negative weights defining the probability distribution.
     * @param maxRank Maximum rank (exclusive) for generated indices.
     * @param random Random instance for reproducibility (or null for default).
     */
    public WeightedRandomGenerator(List<Double> weights, BigInteger maxRank, Random random) {
        if (weights == null || weights.isEmpty() || maxRank == null || maxRank.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Weights must be non-empty and maxRank must be positive");
        }
        this.maxRank = maxRank;
        this.random = (random != null) ? random : new Random();

        int n = weights.size();
        double sum = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (sum <= 0 || weights.stream().anyMatch(w -> w < 0)) {
            throw new IllegalArgumentException("Weights must be non-negative and sum to a positive value");
        }

        // Initialize alias method arrays
        probabilities = new double[n];
        alias = new int[n];
        double[] normalized = weights.stream().mapToDouble(w -> w * n / sum).toArray();
        int[] small = new int[n];
        int[] large = new int[n];
        int smallCount = 0, largeCount = 0;

        // Partition weights into small and large
        for (int i = 0; i < n; i++) {
            if (normalized[i] < 1.0) {
                small[smallCount++] = i;
            } else {
                large[largeCount++] = i;
            }
        }

        // Build alias table
        while (smallCount > 0 && largeCount > 0) {
            int less = small[--smallCount];
            int more = large[--largeCount];
            probabilities[less] = normalized[less];
            alias[less] = more;
            normalized[more] = normalized[more] + normalized[less] - 1.0;
            if (normalized[more] < 1.0) {
                small[smallCount++] = more;
            } else {
                large[largeCount++] = more;
            }
        }

        while (smallCount > 0) {
            probabilities[small[--smallCount]] = 1.0;
        }
        while (largeCount > 0) {
            probabilities[large[--largeCount]] = 1.0;
        }
    }

    /**
     * Convenience constructor with default maxRank equal to weights size.
     * <p>
     * Produces ranks in {@code [0, weights.size()-1]}, where each rank {@code i} has probability
     * {@code weights[i] / sum(weights)}. Ideal for selecting indices directly.
     * <p>
     * <b>Throws:</b>
     * <ul>
     *   <li>{@code IllegalArgumentException} if {@code weights} is null, empty, or contains negative values.</li>
     *   <li>{@code IllegalArgumentException} if {@code weights} sum to zero or less.</li>
     * </ul>
     *
     * @param weights List of non-negative weights.
     * @param random Random instance (or null for default).
     */
    public WeightedRandomGenerator(List<Double> weights, Random random) {
        this(weights, BigInteger.valueOf(weights.size()), random);
    }

    /**
     * Returns an iterator over random ranks as {@link BigInteger}s.
     * <p>
     * The iterator is infinite, producing ranks on-demand. Use {@code Stream.limit(count)} or loops to
     * control the number of generated ranks. Each rank is sampled using the alias method and mapped to
     * a subrange within {@code [0, maxRank-1]} based on the weights.
     *
     * @return An infinite iterator of random ranks.
     */
    @Override
    public Iterator<BigInteger> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true; // Infinite iterator
            }

            @Override
            public BigInteger next() {
                // Alias method: O(1) sampling
                int i = random.nextInt(probabilities.length);
                double p = random.nextDouble();
                int index = (p < probabilities[i]) ? i : alias[i];
                // Map to subrange [i * maxRank/n, (i+1) * maxRank/n)
                BigInteger rangeSize = maxRank.divide(BigInteger.valueOf(probabilities.length));
                BigInteger start = BigInteger.valueOf(index).multiply(rangeSize);
                BigInteger end = start.add(rangeSize);
                BigInteger range = end.subtract(start);
                if (range.equals(BigInteger.ZERO)) {
                    return start;
                }
                return start.add(BigInteger.valueOf(random.nextLong(range.longValueExact())));
            }
        };
    }

    // Main method unchanged
    public static void main(String[] args) {
        // Weights: 0, 1, 2, 3, 4 with probabilities 10%, 10%, 10%, 50%, 10%
        List<Double> weights = List.of(0.1, 0.1, 0.1, 0.5, 0.1);
        BigInteger maxRank = BigInteger.valueOf(weights.size() * 2); // 10

        // Initialize generator
        var iterator = new WeightedRandomGenerator(weights, maxRank, new Random(System.currentTimeMillis())).iterator();

        // Accumulate frequencies
        Map<BigInteger, Integer> frequencyMap = new HashMap<>();
        int totalSamples = 2000;

        for (int i = 0; i < totalSamples; i++) {
            BigInteger rank = iterator.next();
            frequencyMap.merge(rank, 1, Integer::sum);
        }

        System.out.println(frequencyMap);

        // Print frequency percentages
        System.out.println("Frequency Distribution (%):");
        for (int i = 0; i < maxRank.intValue(); i++) {
            BigInteger key = BigInteger.valueOf(i);
            int count = frequencyMap.getOrDefault(key, 0);
            double percentage = (count * 100.0) / totalSamples;
            System.out.printf("Index %d: %.2f%%\n", i, percentage);
        }
    }
}