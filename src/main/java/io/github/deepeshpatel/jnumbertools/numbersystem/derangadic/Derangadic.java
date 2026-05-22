/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

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
 *
 * <h2>Array index convention for encoded digit arrays</h2>
 * <p>
 * All digit arrays produced or consumed by this class (and the underlying
 * {@link DerangadicAlgorithms} / {@link DerangadicIncrementStateMachine}) are stored
 * <strong>Least-Significant-Digit-first (LSD-first)</strong>:
 * </p>
 * <pre>
 *   a[0]          = D_0          — Least Significant Digit (LSD)
 *   a[1]          = D_1
 *   ...
 *   a[a.length-1] = D_{k-1}     — Most Significant Digit (MSD)
 * </pre>
 * <p>
 * This convention matches the paper's positional notation, which writes digits as
 * {@code (D_{k-1}, D_{k-2}, …, D_1, D_0)} — MSD on the left, LSD on the right.
 * The in-memory array simply reverses that written order: index&nbsp;0 holds the
 * rightmost (least-significant) digit of the paper notation.
 * </p>
 * <p>
 * To display an encoded array in the human-readable MSD-first form used by the paper,
 * iterate from {@code a[a.length-1]} down to {@code a[0]}.
 * </p>
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @see DerangadicAlgorithms internal encoding/decoding engine (not part of the stable API)
 * @see DerangadicIncrementStateMachine internal incremental-successor engine (not part of the stable API)
 * @since 3.0.2
 */
public final class Derangadic {

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

    /**
     * Returns the derangement corresponding to the current rank.
     *
     * <p><strong>Performance note:</strong> Returns a live reference to the internal array
     * for zero-copy performance. Callers must NOT modify the returned array.
     * Clone it explicitly if mutation is required.</p>
     *
     * @return derangement array of length {@code order}
     */
    public int[] derangement() {
        return stateMachine.derangement();
    }

    /**
     * Returns the Derangadic digit array for the current rank, stored
     * <strong>LSD-first</strong>: {@code encoded()[0] = D_0} (Least Significant Digit)
     * and {@code encoded()[encoded().length-1] = D_{k-1}} (Most Significant Digit).
     *
     * <p>
     * To display in the MSD-first notation used by the paper, iterate the returned
     * array from {@code encoded().length-1} down to {@code 0}.
     * </p>
     *
     * <p><strong>Performance note:</strong> Returns a live reference to the internal array
     * for zero-copy performance. Callers must NOT modify the returned array.
     * Clone it explicitly if mutation is required.</p>
     *
     * @return digit array in LSD-first order ({@code result[0] = D_0})
     */
    public int[] encoded() {
        return stateMachine.encoded();
    }

    /**
     * Returns the current rank.
     *
     * @return the 0-based lexicographical rank of the current derangement
     */
    public BigInteger rank() {
        return rank;
    }

    /**
     * Returns the order (number of elements) of this Derangadic instance.
     *
     * @return {@code n}, the number of elements
     */
    public int order() {
        return order;
    }

    /**
     * Returns a new {@code Derangadic} instance at rank {@code rank + decimal}.
     *
     * @param decimal the amount to add to the current rank
     * @return new {@code Derangadic} at the resulting rank
     */
    public Derangadic add(BigInteger decimal) {
        return new Derangadic(order, rank.add(decimal), calculator);
    }

    /**
     * Advances this {@code Derangadic} in-place to the next lexicographical
     * rank and returns this same instance.
     *
     * <p><strong>Performance:</strong> this method delegates to the incremental
     * Derangadic successor engine ({@link DerangadicIncrementStateMachine}). It does
     * not recompute from rank&nbsp;0 and is therefore the recommended entry point for
     * fast consecutive derangement generation.</p>
     *
     * @throws NoSuchElementException if this is already the last rank ({@code rank = D_n − 1})
     */
    public void next() {
        boolean nextAvailable = stateMachine.increment();
        if(!nextAvailable) {
            throw new NoSuchElementException("Already at the last rank; no next derangement exists.");
        }
        this.rank = this.rank.add(BigInteger.ONE);
    }

    /**
     * Advances this {@code Derangadic} in-place to the next lexicographical rank
     * without checking whether a next rank actually exists.
     *
     * <p><strong>Warning:</strong> calling this method when already at the last rank
     * results in undefined behaviour. Use {@link #next()} for safe traversal.</p>
     */
    public void nextWithoutBoundCheck() {
        stateMachine.increment();
        this.rank = this.rank.add(BigInteger.ONE);
    }

    /**
     * Returns the total number of derangements of {@code order} elements,
     * i.e. the subfactorial {@code !order = D_order}.
     *
     * <p>
     * Provided here so callers can validate their rank ranges without accessing
     * the internal {@link DerangadicAlgorithms} class directly.
     * </p>
     *
     * @return {@code D_n = !n} where {@code n = order}
     */
    public BigInteger count() {
        Objects.requireNonNull(calculator, "calculator");
        return calculator.subFactorial(order);
    }
}
