/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

/**
 * Central entry point for ranking, unranking, and number-system operations.
 *
 * <p>Dedicated static entry points provide generators for each combinatorial
 * family: {@link Permutation}, {@link Combination}, {@link Derangement},
 * {@link Subset}, and {@link Product}.</p>
 *
 * <h2>Ranking and Unranking</h2>
 * <pre>{@code
 * BigInteger rank = JNumberTools.rankOf()
 *     .permutation()
 *     .unique(2, 0, 1);
 *
 * int[] permutation = JNumberTools.unrankOf()
 *     .permutation()
 *     .unique(BigInteger.valueOf(5), 3);
 *
 * BigInteger combinationRank = JNumberTools.rankOf()
 *     .combination()
 *     .unique(4, 1, 3);
 * }</pre>
 *
 * @see Permutation
 * @see Combination
 * @see Derangement
 * @see Subset
 * @see Product
 * @author Deepesh Patel
 */
public final class JNumberTools {

    private JNumberTools() {
        // Prevent instantiation
    }

    /**
     * Creates a RankOf instance for ranking permutations and combinations.
     *
     * @return a RankOf instance for ranking operations
     * @see RankOf
     */
    public static RankOf rankOf() {
        return new RankOf();
    }

    /**
     * Creates an UnrankOf instance for unranking permutations and combinations.
     *
     * @return an UnrankOf instance for unranking operations
     * @see UnrankOf
     */
    public static UnrankOf unrankOf() {
        return new UnrankOf();
    }

    /**
     * Creates a NumberSystem instance for converting numbers to specialized number systems.
     *
     * @return a NumberSystem instance for number system conversions
     * @see NumberSystem
     * @see Permutadic
     * @see Combinadic
     * @see Factoradic
     */
    public static NumberSystem numberSystem() {
        return new NumberSystem(new Calculator());
    }
}
