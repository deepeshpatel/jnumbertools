/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.api;

import io.github.deepeshpatel.jnumbertools.base.CalculatorImpl;
import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

import java.math.BigInteger;

/**
 * Converts decimal numbers to specialized number systems for combinatorial computations.
 * <p>
 * This class provides conversions to three fundamental combinatorial number systems:
 * <ul>
 *   <li><b>Permutadic</b> - For k-permutations (ⁿPₖ). Each permutadic number uniquely
 *       represents a k-permutation of n distinct elements.</li>
 *   <li><b>Combinadic</b> - For unique combinations (ⁿCᵣ). Each combinadic number uniquely
 *       represents a combination of r elements from n distinct elements.</li>
 *   <li><b>Factoradic</b> - For unique permutations (n!). Each factoradic number uniquely
 *       represents a full permutation of n distinct elements.</li>
 * </ul>
 * These number systems are essential for ranking (finding the position) and unranking
 * (generating the element at a given position) combinatorial structures without
 * enumerating all possibilities.
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Permutadic - For k-Permutations</h3>
 * <pre>
 * NumberSystem ns = new NumberSystem();
 *
 * // Convert decimal 42 to permutadic for k=3 (3-permutations)
 * Permutadic perm = ns.permutadic(42, 3);
 * System.out.println(perm);                 // e.g., [2,1,0](3)
 * System.out.println(perm.toMathExpression()); // Shows mathematical representation
 *
 * // Convert back to k-permutation of size 5
 * int[] permutation = perm.toMthPermutation(5);
 * System.out.println(Arrays.toString(permutation));
 * </pre>
 *
 * <h3>Combinadic - For Unique Combinations</h3>
 * <pre>
 * // Convert decimal 15 to combinadic for r=2 (2-combinations)
 * Combinadic comb = ns.combinadic(15, 2);
 * System.out.println(comb);                  // e.g., [6,2]
 *
 * // Get the next combinadic value
 * Combinadic next = comb.next();
 * System.out.println(next);
 * </pre>
 *
 * <h3>Factoradic - For Unique Permutations</h3>
 * <pre>
 * // Convert decimal 120 to factoradic
 * Factoradic fac = ns.factoradic(120);
 * System.out.println(fac);                    // e.g., [1,0,0,0,0]
 * System.out.println(fac.decimalValue);       // 120
 * </pre>
 *
 * <h3>BigInteger Support</h3>
 * <pre>
 * // All methods support BigInteger for arbitrarily large values
 * BigInteger huge = new BigInteger("10000000000000000000");
 * Permutadic bigPerm = ns.permutadic(huge, 10);
 * Combinadic bigComb = ns.combinadic(huge, 5);
 * Factoradic bigFac = ns.factoradic(huge);
 * </pre>
 *
 * <h3>Integration with Ranking</h3>
 * <pre>
 * // These number systems are used internally by RankOf and UnrankOf
 * RankOf rankOf = new RankOf();
 * UnrankOf unrankOf = new UnrankOf();
 *
 * // The rank of a permutation can be converted to factoradic
 * int[] permutation = {2, 0, 1};
 * BigInteger rank = rankOf.uniquePermutation(permutation);
 * Factoradic fac = ns.factoradic(rank);
 *
 * // And factoradic can generate the permutation at that rank
 * int[] samePerm = unrankOf.uniquePermutation(3).atRank(rank);
 * </pre>
 *
 * <p>
 * This class is immutable and thread-safe. All methods are stateless and can be safely shared across threads.
 * </p>
 *
 * @see AllExamples
 * @see Permutadic
 * @see Combinadic
 * @see Factoradic
 * @see RankOf
 * @see UnrankOf
 * @see <a href="https://en.wikipedia.org/wiki/Factorial_number_system">Factorial Number System</a>
 * @see <a href="https://en.wikipedia.org/wiki/Combinatorial_number_system">Combinatorial Number System</a>
 * @author Deepesh Patel
 */
public class NumberSystem {

    private final Calculator calculator;

    /**
     * Constructs a new NumberSystem instance with a default Calculator.
     */
    public NumberSystem() {
        this(new CalculatorImpl());
    }

    /**
     * Constructs a new NumberSystem instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public NumberSystem(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Converts a decimal number to its permutadic representation for k-permutations (`ⁿPₖ`).
     * <p>
     * Permutadic numbers are used to rank and unrank k-permutations of n distinct elements.
     * The degree specifies the permutation size (k).
     * </p>
     *
     * @param decimalValue the decimal number to convert (decimalValue ≥ 0)
     * @param degree the size of the permutation (k)
     * @return a Permutadic object representing the permutadic number
     */
    public Permutadic permutadic(long decimalValue, int degree) {
        return Permutadic.of(decimalValue, degree);
    }

    /**
     * Converts a decimal number to its permutadic representation for k-permutations (`ⁿPₖ`).
     * <p>
     * Permutadic numbers are used to rank and unrank k-permutations of n distinct elements.
     * The degree specifies the permutation size (k).
     * </p>
     *
     * @param decimalValue the decimal number to convert (decimalValue ≥ 0)
     * @param degree the size of the permutation (k)
     * @return a Permutadic object representing the permutadic number
     */
    public Permutadic permutadic(BigInteger decimalValue, int degree) {
        return Permutadic.of(decimalValue, degree);
    }

    /**
     * Converts a decimal number to its combinadic representation for unique combinations (`ⁿCᵣ`).
     * <p>
     * Combinadic numbers are used to rank and unrank unique combinations of r elements from
     * n distinct elements. The degree specifies the combination size (r).
     * </p>
     *
     * @param positiveNumber the decimal number to convert (positiveNumber ≥ 0)
     * @param degree the size of the combination (r)
     * @return a Combinadic object representing the combinadic number
     */
    public Combinadic combinadic(long positiveNumber, int degree) {
        return Combinadic.of(positiveNumber, degree, calculator);
    }

    /**
     * Converts a decimal number to its combinadic representation for unique combinations (`ⁿCᵣ`).
     * <p>
     * Combinadic numbers are used to rank and unrank unique combinations of r elements from
     * n distinct elements. The degree specifies the combination size (r).
     * </p>
     *
     * @param positiveNumber the decimal number to convert (positiveNumber ≥ 0)
     * @param degree the size of the combination (r)
     * @return a Combinadic object representing the combinadic number
     */
    public Combinadic combinadic(BigInteger positiveNumber, int degree) {
        return Combinadic.of(positiveNumber, degree, calculator);
    }

    /**
     * Converts a decimal number to its factoradic representation for unique permutations (`n!`).
     * <p>
     * Factoradic numbers are used to rank and unrank unique permutations of n distinct elements.
     * </p>
     *
     * @param positiveInt the decimal number to convert (positiveInt ≥ 0)
     * @return a Factoradic object representing the factoradic number
     */
    public Factoradic factoradic(long positiveInt) {
        return Factoradic.of(positiveInt);
    }

    /**
     * Converts a decimal number to its factoradic representation for unique permutations (`n!`).
     * <p>
     * Factoradic numbers are used to rank and unrank unique permutations of n distinct elements.
     * </p>
     *
     * @param positiveInt the decimal number to convert (positiveInt ≥ 0)
     * @return a Factoradic object representing the factoradic number
     */
    public Factoradic factoradic(BigInteger positiveInt) {
        return Factoradic.of(positiveInt);
    }
}
