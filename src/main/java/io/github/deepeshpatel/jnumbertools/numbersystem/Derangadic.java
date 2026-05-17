/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
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
 * <h2>Public wrapper vs. internal algorithms</h2>
 * <p>
 * {@code Derangadic} is the <em>public</em> entry point and mirrors the role of
 * {@link Combinadic}, {@link Permutadic} and {@link Factoradic}.
 * The companion class {@link DerangadicAlgorithms} (and {@link DerangadicIncrement})
 * is considered <em>internal</em> — although currently public for testing and
 * advanced use, its API is not part of the stable contract and may change without
 * notice. Library users should prefer {@code Derangadic}.
 * </p>
 *
 * <h2>Construction requires a {@link Calculator}</h2>
 * <p>
 * Every factory method takes a {@link Calculator}. Reusing a single {@code Calculator}
 * across calls is strongly recommended because it memoizes factorials, subfactorials
 * and restricted-derangement counts that the Derangadic algorithms depend on.
 * Creating a fresh {@code Calculator} per call defeats that memoization.
 * </p>
 *
 * <h2>Digit array — variable length and the parity invariant</h2>
 * <p>
 * The Derangadic digit array has length {@code actualN ≤ n}, where {@code actualN}
 * is the <em>minimal carrier length</em>: the smallest integer with the same parity
 * as {@code n} for which {@code D_actualN > rank}. Positions beyond {@code actualN}
 * are implicit zeros.
 * </p>
 *
 * <p>
 * <strong>Why the carrier length jumps by 2 (parity invariant).</strong>
 * For a fixed rank {@code r}, the Derangadic encoding is identical for every
 * {@code n} of the same parity as long as {@code n} is large enough to host that
 * encoding. In other words, the encoding depends on {@code (rank, parity(n))}, not
 * on {@code n} itself. Consequently the natural carrier length grows in steps of
 * two, jumping from {@code k} to {@code k+2} the moment {@code rank ≥ D_k}.
 * </p>
 *
 * <p>Empirical evidence (see {@code Scrap2.java}). The encoding for a given rank
 * is shared across every {@code n} in the same parity family:</p>
 * <pre>
 * Even family (n = 12, 14, 16, …) — encodings are identical for all such n:
 *   rank   digits
 *      0   [0, 0]
 *      1   [0, 1, 1, 0]
 *      4   [0, 0, 1, 1]
 *      8   [0, 1, 1, 2]
 *      9   [0, 1, 0, 0, 1, 0]      ← carrier grew from 4 → 6 at rank D_4 = 9
 *     20   [0, 0, 0, 0, 2, 0]
 *
 * Odd family (n = 13, 15, 17, …) — encodings are identical for all such n:
 *   rank   digits
 *      0   [0, 1, 0]
 *      1   [0, 0, 1]
 *      2   [0, 0, 0, 1, 0]         ← carrier grew from 3 → 5 at rank D_3 = 2
 *     11   [0, 0, 0, 0, 1]         ← carrier grew from 5 → 7 at the next boundary
 * </pre>
 *
 * <p>
 * This is why {@link DerangadicAlgorithms} computes
 * {@code actualN = smallestN(n, rank)} by stepping down in twos, and why
 * {@link DerangadicIncrement} expands its state length by 2 when crossing a
 * boundary.
 * </p>
 *
 * <h2>Equality semantics</h2>
 * <p>
 * Two {@code Derangadic} instances are equal iff they have the same {@code order}
 * and the same {@code decimalValue}. Trailing zeros in the digit array (which can
 * differ between values produced via {@link #of} and via {@link #fromDerangement})
 * do not affect equality.
 * </p>
 *
 * <h2>Example: n = 4, D₄ = 9</h2>
 * <pre>
 * Calculator calc = new Calculator();
 *
 * Derangadic d0 = Derangadic.of(0, 4, calc);
 * d0.decimalValue();        // 0
 * d0.toDerangement();       // [1, 0, 3, 2]
 *
 * Derangadic d1 = Derangadic.of(1, 4, calc);
 * d1.derangadicValues();    // [0, 1, 1, 0]   (actualN = 4)
 * d1.toDerangement();       // [1, 2, 3, 0]
 *
 * Derangadic back = Derangadic.fromDerangement(new int[]{1, 0, 3, 2}, 4, calc);
 * back.decimalValue();      // 0
 * back.equals(d0);          // true
 * </pre>
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @see DerangadicAlgorithms  internal engine (not part of the stable API)
 * @see DerangadicIncrement   internal incremental-successor engine (not part of the stable API)
 * @since 3.0.2
 */
public final class Derangadic implements Serializable {

    private BigInteger decimalValue;
    private int[] derangadicValues;
    private final int order;

    private final transient DerangadicIncrement incrementor;
    private final transient DerangadicIncrement.DerangadicState state;

    private Derangadic(BigInteger decimalValue, int[] derangadicValues, int order,
                       DerangadicIncrement incrementor,
                       DerangadicIncrement.DerangadicState state) {
        this.decimalValue = decimalValue;
        this.derangadicValues = derangadicValues; // already owned; not aliased to caller input
        this.order = order;
        this.incrementor = incrementor;
        this.state = state;
    }

    // ==================== Factories ====================

    /**
     * Creates a {@code Derangadic} for the given rank and order.
     *
     * @param rank       the 0-based lexicographical rank, {@code 0 ≤ rank < D_n}
     * @param n          the order (number of elements), {@code n ≥ 2}
     * @param calculator memoizing calculator; reuse a single instance across calls
     *                   for best performance
     * @return a {@code Derangadic} representing {@code rank} in order {@code n}
     * @throws IllegalArgumentException if {@code rank} is out of range
     * @throws NullPointerException     if {@code calculator} is {@code null}
     */
    public static Derangadic of(BigInteger rank, int n, Calculator calculator) {
        Objects.requireNonNull(calculator, "calculator");
        DerangadicIncrement incrementor = new DerangadicIncrement(calculator);
        DerangadicIncrement.DerangadicState state = incrementor.initialState(n, rank);
        return new Derangadic(rank, state.getDigits(), n, incrementor, state);
    }

    /**
     * Convenience overload of {@link #of(BigInteger, int, Calculator)} for {@code long} ranks.
     *
     * @param rank       the 0-based lexicographical rank
     * @param n          the order (number of elements)
     * @param calculator memoizing calculator (reuse across calls)
     * @return a {@code Derangadic} representing {@code rank} in order {@code n}
     */
    public static Derangadic of(long rank, int n, Calculator calculator) {
        return of(BigInteger.valueOf(rank), n, calculator);
    }

    /**
     * Creates a {@code Derangadic} from an explicit derangement.
     *
     * @param derangement a valid derangement of {@code n} elements (no fixed points,
     *                    each value in {@code [0, n)}, all distinct)
     * @param n           the order (must equal {@code derangement.length})
     * @param calculator  memoizing calculator (reuse across calls)
     * @return a {@code Derangadic} whose {@link #toDerangement()} reproduces the input
     * @throws IllegalArgumentException if {@code derangement} is not a valid derangement
     * @throws NullPointerException     if any argument is {@code null}
     */
    public static Derangadic fromDerangement(int[] derangement, int n, Calculator calculator) {
        Objects.requireNonNull(derangement, "derangement");
        Objects.requireNonNull(calculator, "calculator");
        DerangadicAlgorithms algo = new DerangadicAlgorithms(calculator);
        BigInteger rank = algo.rank(derangement, n);
        DerangadicIncrement incrementor = new DerangadicIncrement(calculator);
        DerangadicIncrement.DerangadicState state = incrementor.initialState(n, rank);
        return new Derangadic(rank, state.getDigits(), n, incrementor, state);
    }

    // ==================== Static rank / unrank shortcuts ====================

    /**
     * Returns the derangement at the given lexicographical rank, without
     * constructing a {@code Derangadic} wrapper.
     * <p>
     * Equivalent to {@code Derangadic.of(rank, n, calculator).toDerangement()}
     * but avoids the wrapper allocation. Prefer this overload when you only need
     * the derangement and not the intermediate digit encoding.
     * </p>
     *
     * @param rank       the 0-based lexicographical rank, {@code 0 ≤ rank < D_n}
     * @param n          the order (number of elements), {@code n ≥ 2}
     * @param calculator memoizing calculator (reuse a single instance across calls)
     * @return the derangement as an array of length {@code n}
     * @throws IllegalArgumentException if {@code rank} is out of range
     * @throws NullPointerException     if {@code calculator} is {@code null}
     */
    public static int[] unrank(BigInteger rank, int n, Calculator calculator) {
        Objects.requireNonNull(calculator, "calculator");
        return new DerangadicAlgorithms(calculator).unrank(rank, n);
    }

    /**
     * Convenience overload of {@link #unrank(BigInteger, int, Calculator)} for {@code long} ranks.
     *
     * @param rank       the 0-based lexicographical rank
     * @param n          the order (number of elements)
     * @param calculator memoizing calculator (reuse across calls)
     * @return the derangement as an array of length {@code n}
     */
    public static int[] unrank(long rank, int n, Calculator calculator) {
        return unrank(BigInteger.valueOf(rank), n, calculator);
    }

    /**
     * Returns the lexicographical rank of the given derangement, without
     * constructing a {@code Derangadic} wrapper.
     * <p>
     * Equivalent to {@code Derangadic.fromDerangement(derangement, n, calculator).decimalValue()}
     * but avoids the wrapper allocation.
     * </p>
     *
     * @param derangement a valid derangement of {@code n} elements (no fixed points,
     *                    each value in {@code [0, n)}, all distinct)
     * @param n           the order (must equal {@code derangement.length})
     * @param calculator  memoizing calculator (reuse across calls)
     * @return the 0-based lexicographical rank
     * @throws IllegalArgumentException if {@code derangement} is not a valid derangement
     * @throws NullPointerException     if any argument is {@code null}
     */
    public static BigInteger rank(int[] derangement, int n, Calculator calculator) {
        Objects.requireNonNull(derangement, "derangement");
        Objects.requireNonNull(calculator, "calculator");
        return new DerangadicAlgorithms(calculator).rank(derangement, n);
    }

    /**
     * Returns the total number of derangements of {@code n} elements,
     * i.e. the subfactorial {@code !n}.
     * <p>
     * Provided here so callers can validate their rank ranges without touching
     * the internal {@link DerangadicAlgorithms} class.
     * </p>
     *
     * @param n          the order ({@code n ≥ 0})
     * @param calculator memoizing calculator
     * @return {@code D_n = !n}
     * @throws NullPointerException if {@code calculator} is {@code null}
     */
    public static BigInteger count(int n, Calculator calculator) {
        Objects.requireNonNull(calculator, "calculator");
        return calculator.subFactorial(n);
    }

    // ==================== Sequential iteration ====================

    /**
     * Advances this {@code Derangadic} in-place to the next lexicographical
     * rank and returns this same instance.
     *
     * <p><strong>Performance:</strong> this method delegates to the incremental
     * Derangadic successor engine. It does not recompute from rank 0 and is the
     * public entry point for fast consecutive derangement generation.</p>
     *
     * @return this instance, after mutation to rank {@code decimalValue() + 1}
     * @throws NoSuchElementException if this is already the last rank
     * @throws IllegalStateException if this instance was deserialized and has
     *         no attached engine
     */
    public Derangadic next() {
        if (incrementor == null || state == null) {
            throw new IllegalStateException(
                    "Derangadic was deserialized without an attached engine; " +
                            "rebuild it via Derangadic.of(decimalValue(), order(), calculator).");
        }
        if (!incrementor.increment(state)) {
            throw new NoSuchElementException("Already at the last derangement rank for order " + order);
        }
        decimalValue = decimalValue.add(BigInteger.ONE);
        derangadicValues = state.getDigits();
        return this;
    }

    // ==================== Accessors ====================

    /**
     * Returns the derangement (length {@code n}) corresponding to this rank.
     *
     * @return the derangement array
     * @throws IllegalStateException if this instance was deserialized and has no
     *         attached engine (the {@link Calculator} reference is transient)
     */
    public int[] toDerangement() {
        if (state == null) {
            throw new IllegalStateException(
                    "Derangadic was deserialized without an attached engine; " +
                            "rebuild it via Derangadic.of(decimalValue(), order(), calculator).");
        }
        return state.currentDerangement();
    }

    /** @return the decimal rank as a {@link BigInteger} */
    public BigInteger decimalValue() {
        return decimalValue;
    }

    /**
     * @return a defensive copy of the Derangadic digit array (length = {@link #getMinimalSize()})
     */
    public int[] derangadicValues() {
        return derangadicValues.clone();
    }

    /** @return the order {@code n} */
    public int order() {
        return order;
    }

    /**
     * Returns the carrier length (a.k.a. {@code actualN}): the smallest integer
     * with the same parity as {@link #order()} for which {@code D_actualN >
     * decimalValue()}.
     *
     * @return the length of the digit array
     */
    public int getMinimalSize() {
        return derangadicValues.length;
    }

    // ==================== Object overrides ====================

    @Override
    public String toString() {
        // Display the canonical (trimmed) digit form so the textual representation
        // is independent of any trailing-zero padding.
        int last = derangadicValues.length - 1;
        while (last >= 0 && derangadicValues[last] == 0) last--;
        int trimmedLen = Math.max(last + 1, 1); // keep at least one digit for rank 0

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < trimmedLen; i++) {
            if (i > 0) sb.append(", ");
            sb.append(i <= last ? derangadicValues[i] : 0);
        }
        sb.append("](").append(order).append(")");
        return sb.toString();
    }

    /**
     * Equality is by {@code (order, decimalValue)}. Trailing zeros in the digit
     * array do not influence equality: a {@code Derangadic} produced via
     * {@link #of} is equal to one produced via {@link #fromDerangement} as long as
     * both represent the same rank in the same order.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Derangadic that)) return false;
        return order == that.order && decimalValue.equals(that.decimalValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decimalValue, order);
    }

    /**
     * Bit-exact digit comparison (after trimming trailing zeros). Provided for
     * callers who specifically want to compare encodings rather than ranks; for
     * value equality use {@link #equals(Object)}.
     *
     * @param other another {@code Derangadic} (may be {@code null})
     * @return {@code true} iff both digit arrays are equal after trimming trailing zeros
     */
    public boolean digitsEqual(Derangadic other) {
        if (other == null) return false;
        return Arrays.equals(trimTrailingZeros(this.derangadicValues),
                trimTrailingZeros(other.derangadicValues));
    }

    private static int[] trimTrailingZeros(int[] a) {
        int last = a.length - 1;
        while (last >= 0 && a[last] == 0) last--;
        if (last < 0) return new int[]{0};
        return Arrays.copyOf(a, last + 1);
    }
}
