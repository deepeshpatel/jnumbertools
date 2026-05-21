/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Public wrapper for the <strong>Derangadic</strong> number system — a combinatorial
 * number system for derangements (fixed-point-free permutations).
 *
 * <p>
 * For an order {@code n} with subfactorial {@code D_n = !n}, this class provides a
 * bijection between integers {@code [0, D_n − 1]} and the {@code D_n} derangements
 * of {@code n} elements taken in lexicographical order.
 * </p>
 * @author Deepesh Patel &amp; Aditya Patel
 * @see DerangadicAlgorithms  internal engine (not part of the stable API)
 * @see DerangadicIncrementStateMachine   internal incremental-successor engine (not part of the stable API)
 * @since 3.0.2
 */
public final class Derangadic  {

    private BigInteger rank;
    private final int order;

    private final Calculator calculator;
    private final DerangadicIncrementStateMachine stateMachine;

    public Derangadic(int order, BigInteger rank, Calculator calculator) {
        this(order, rank, calculator, new DerangadicIncrementStateMachine(order, rank, calculator));
    }

    private Derangadic(int order, BigInteger rank, Calculator calculator, DerangadicIncrementStateMachine stateWithNext) {
        this.order = order;
        this.rank = rank;
        this.calculator = calculator;
        this.stateMachine = stateWithNext;
    }

    public int[] derangement() {
        return stateMachine.derangement();
    }

    public int[] encoded() {
        return stateMachine.encoded();
    }

    public BigInteger rank() {
        return rank;
    }

    public int order() {
        return order;
    }

    public Derangadic add(BigInteger decimal) {
        return new Derangadic(order, rank.add(decimal), calculator);
    }

    /**
     * Advances this {@code Derangadic} in-place to the next lexicographical
     * rank and returns this same instance.
     *
     * <p><strong>Performance:</strong> this method delegates to the incremental
     * Derangadic successor engine. It does not recompute from rank 0 and is the
     * public entry point for fast consecutive derangement generation.</p>
     *
     * @throws NoSuchElementException if this is already the last rank
     */
    public void next() {
        boolean nextAvailable = stateMachine.increment();
        if(!nextAvailable) {
            throw new NoSuchElementException("Already at the last rank; no next derangement exists.");
        }
        this.rank = this.rank.add(BigInteger.ONE);
    }

    public void nextWithoutBoundCheck() {
        stateMachine.increment();
        this.rank = this.rank.add(BigInteger.ONE);
    }

    /**
     * Returns the total number of derangements of {@code n} elements,
     * i.e. the subfactorial {@code !n}.
     * <p>
     * Provided here so callers can validate their rank ranges without touching
     * the internal {@link DerangadicAlgorithms} class.
     * </p>
     * @return {@code D_n = !n}
     */
    public BigInteger count() {
        Objects.requireNonNull(calculator, "calculator");
        return calculator.subFactorial(order);
    }
}
