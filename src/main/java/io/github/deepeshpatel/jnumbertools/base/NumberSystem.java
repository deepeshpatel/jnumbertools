/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

import java.math.BigInteger;

/**
 * Converts decimal numbers to specialized number systems for combinatorial computations.
 * <p>
 * This class supports permutadic, combinadic, and factoradic number systems, used for
 * ranking and unranking permutations and combinations. Permutadic represents k-permutations
 * (`ⁿPₖ`), combinadic represents unique combinations (`ⁿCᵣ`), and factoradic represents
 * unique permutations (`ⁿ!`). Each method converts a decimal number to its respective
 * number system representation.
 * </p>
 * Example usage:
 * <pre>
 * NumberSystem numberSystem = new NumberSystem();
 * Permutadic permutadic = numberSystem.permutadic(42, 3);
 * Combinadic combinadic = numberSystem.combinadic(15, 2);
 * Factoradic factoradic = numberSystem.factoradic(120);
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public class NumberSystem {

    private final Calculator calculator;

    /**
     * Constructs a new NumberSystem instance with a default Calculator.
     */
    public NumberSystem() {
        this(new Calculator());
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
     * Converts a decimal number to its factoradic representation for unique permutations (`ⁿ!`).
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
     * Converts a decimal number to its factoradic representation for unique permutations (`ⁿ!`).
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