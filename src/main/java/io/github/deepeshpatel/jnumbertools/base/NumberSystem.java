/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

import java.math.BigInteger;

public class NumberSystem {

    private final Calculator calculator;

    public NumberSystem() {
        this(new Calculator());
    }

    public NumberSystem(Calculator calculator) {
        this.calculator = calculator;
    }

    public Permutadic permutadic(long decimalValue, int degree) {
        return Permutadic.of(decimalValue, degree);
    }

    public Permutadic permutadic(BigInteger decimalValue, int degree) {
        return Permutadic.of(decimalValue, degree);
    }

    public Combinadic combinadic(long positiveNumber, int degree) {
        return Combinadic.of(positiveNumber, degree, calculator);
    }

    public Combinadic combinadic(BigInteger positiveNumber, int degree) {
        return Combinadic.of(positiveNumber, degree, calculator);
    }

    public Factoradic factoradic(long positiveInt) {
        return Factoradic.of(positiveInt);
    }

    public Factoradic factoradic(BigInteger positiveInt) {
        return Factoradic.of(positiveInt);
    }
}
