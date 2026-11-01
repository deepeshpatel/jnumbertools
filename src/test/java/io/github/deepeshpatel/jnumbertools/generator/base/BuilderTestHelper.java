package io.github.deepeshpatel.jnumbertools.generator.base;

import io.github.deepeshpatel.jnumbertools.TestBase;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Helper class for testing builder validation methods consistently across all builder types.
 * <p>
 * This class provides common test patterns for:
 * <ul>
 *   <li>{@code lexOrderMth()} - fail-fast validation for m and start parameters</li>
 *   <li>{@code byRanks()} - null ranks validation</li>
 *   <li>{@code choice()} - sample size validation</li>
 *   <li>{@code sample()} - sample size validation with upper bound check</li>
 * </ul>
 * </p>
 *
 * <p>
 * Usage in builder test classes:
 * <pre>
 * BuilderTestHelper.testLexOrderMth(builder, builder.count());
 * BuilderTestHelper.testByRanks(builder);
 * BuilderTestHelper.testChoice(builder);
 * BuilderTestHelper.testSample(builder, builder.count());
 * </pre>
 * </p>
 */
public final class BuilderTestHelper {

    private BuilderTestHelper() {
        // Prevent instantiation
    }

    /**
     * Tests lexOrderMth method for fail-fast validation.
     * @param builder the builder instance being tested
     */
    public static void testLexOrderMth(Builder<?> builder) {

        // Valid parameters should not throw
        assertNotNull(builder.lexOrderMth(BigInteger.valueOf(2), BigInteger.ONE));

        // m <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ZERO, BigInteger.ONE));
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.valueOf(-1), BigInteger.ONE));

        // start < 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ONE, BigInteger.valueOf(-1)));

        // start >= count
        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ONE, builder.count()));

        assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ONE, builder.count().add(BigInteger.ONE)));
    }

    /**
     * Tests byRanks method for null ranks validation.
     * @param builder the builder instance being tested
     */
    public static void testByRanks(Builder<?> builder) {
        // Valid ranks
        var ranks = List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(2));
        assertNotNull(builder.byRanks(ranks));

        // Empty ranks allowed
        assertNotNull(builder.byRanks(Collections.emptyList()));

        // Null ranks
        assertThrows(IllegalArgumentException.class, () ->
                builder.byRanks(null));

        // Note: Invalid rank values (negative, out of bounds) are NOT tested here
        // They are deferred to iteration and tested in generator-specific test classes
    }

    /**
     * Tests choice method for sample size validation.
     * @param builder the builder instance being tested
     */
    public static void testChoice(Builder<?> builder) {
        // Valid sample size
        assertNotNull(builder.choice(3, TestBase.random));

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () ->
                builder.choice(-1, TestBase.random));

        // Note: sampleSize > total is allowed for choice (with replacement)
        // This is not tested here as it depends on the specific builder's total
    }

    /**
     * Tests sample method for sample size validation.
     *
     * @param builder the builder instance being tested
     */
    public static void testSample(Builder<?> builder) {
        // Valid sample size
        assertNotNull(builder.sample(3, TestBase.random));

        // sampleSize <= 0
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(-1, TestBase.random));

        // sampleSize > total
        int tooLarge = builder.count().intValue() + 1;
        assertThrows(IllegalArgumentException.class, () ->
                builder.sample(tooLarge, TestBase.random));
    }

    /**
     * Tests lexOrder method (simple existence and immutability check).
     *
     * @param builder the builder instance being tested
     */
    public static void testLexOrder(Builder<?> builder) {
        assertNotNull(builder.lexOrder());
        var gen1 = builder.lexOrder();
        var gen2 = builder.lexOrder();
        assertNotSame(gen1, gen2);
    }

    // ------------------------------------------------------------------------------------
    // Rules from ValidationRules.md that hold for EVERY builder (permutation, combination, ...).
    // Each method states the rule it checks. Call them from the builder's own test class.
    // ------------------------------------------------------------------------------------

    /**
     * Rule (isEmpty): {@code isEmpty()} is true if and only if {@code count() == 0}, i.e. the builder
     * yields no results at all. A builder whose only result is the empty list is NOT empty.
     */
    public static void testIsEmpty(Builder<?> builder) {
        boolean countIsZero = builder.count().signum() == 0;
        assertEquals(countIsZero, builder.isEmpty(), "isEmpty() must equal (count() == 0)");
        assertEquals(countIsZero, !builder.lexOrder().iterator().hasNext(),
                "isEmpty() must equal 'lexOrder() yields nothing'");
    }

    /**
     * Rule 9 (immutability): every list returned by lexOrder, lexOrderMth, byRanks, choice and sample
     * is immutable. Requires a non-empty builder.
     */
    public static void testResultsAreImmutable(Builder<?> builder) {
        assertTrue(builder.count().signum() > 0, "needs a non-empty builder");
        assertImmutable("lexOrder", builder.lexOrder().stream().limit(3).toList());
        assertImmutable("lexOrderMth", builder.lexOrderMth(1, 0).stream().limit(3).toList());
        assertImmutable("byRanks", builder.byRanks(List.of(BigInteger.ZERO)).stream().toList());
        assertImmutable("choice", builder.choice(2, new Random(1)).stream().toList());
        assertImmutable("sample", builder.sample(1, new Random(1)).stream().toList());
    }

    @SuppressWarnings("unchecked")
    private static void assertImmutable(String source, Iterable<? extends List<?>> results) {
        int checked = 0;
        for (List<?> result : results) {
            List<Object> list = (List<Object>) result;
            assertThrows(UnsupportedOperationException.class, () -> list.add("X"), source + ": add() on " + list);
            if (!list.isEmpty()) {
                assertThrows(UnsupportedOperationException.class, () -> list.set(0, "X"), source + ": set() on " + list);
            }
            checked++;
        }
        assertTrue(checked > 0, source + " produced nothing");
    }

    /**
     * Rule 9 (re-iterable): every call to iterator()/stream() on a generator starts a fresh, independent
     * traversal. Requires a non-empty builder.
     */
    public static void testReIterable(Builder<?> builder) {
        assertTrue(builder.count().signum() > 0, "needs a non-empty builder");
        var lex = builder.lexOrder();
        assertEquals(lex.stream().toList(), lex.stream().toList(), "lexOrder");
        var mth = builder.lexOrderMth(2, 0);
        assertEquals(mth.stream().toList(), mth.stream().toList(), "lexOrderMth");
        var byRanks = builder.byRanks(List.of(BigInteger.ZERO));
        assertEquals(byRanks.stream().toList(), byRanks.stream().toList(), "byRanks");
    }

    /**
     * Rule (byRanks): a user-supplied rank sequence is consumed lazily. It may be infinite; an invalid
     * rank (negative or >= count) is reported when that rank is reached, not when byRanks() is called,
     * and the valid ranks before it are still delivered. Requires a non-empty builder.
     */
    public static void testByRanksIsLazy(Builder<?> builder) {
        BigInteger count = builder.count();
        assertTrue(count.signum() > 0, "needs a non-empty builder");

        var tooBig = builder.byRanks(List.of(count));                         // no exception here
        var negative = builder.byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(-1)));
        assertThrows(IllegalArgumentException.class, () -> tooBig.stream().toList());

        Iterator<?> it = negative.iterator();
        assertTrue(it.hasNext());
        it.next();                                                            // first (valid) rank is delivered
        assertThrows(IllegalArgumentException.class, it::next);

        Iterable<BigInteger> infinite = () ->
                Stream.iterate(BigInteger.ZERO, r -> r.add(BigInteger.ONE).mod(count)).iterator();
        assertEquals(10, builder.byRanks(infinite).stream().limit(10).count());   // caller decides when to stop
    }

    /**
     * Rule (choice / sample): choice is with replacement, so {@code s > count} is accepted; sample is
     * without replacement, so exactly {@code count} items are all distinct. A null random source is an
     * error. Requires a small non-empty builder whose results are all different from each other.
     */
    public static void testChoiceMayExceedCountAndNullRandomIsRejected(Builder<?> builder) {
        int count = builder.count().intValueExact();
        assertTrue(count > 0, "needs a non-empty builder");
        assertEquals(count + 5, builder.choice(count + 5, new Random(1)).stream().count());
        assertEquals(count, builder.sample(count, new Random(1)).stream().distinct().count());
        assertThrows(NullPointerException.class, () -> builder.choice(1, null));
        assertThrows(NullPointerException.class, () -> builder.sample(1, null));
    }

    /**
     * Rules for a domain with {@code count() == 0}: isEmpty is true, nothing is generated,
     * lexOrderMth accepts any {@code start >= 0} (but still rejects m <= 0 and start < 0), an empty rank
     * source is fine while any rank is out of range, and sample/choice cannot draw anything.
     */
    public static void testEmptyDomain(Builder<?> builder) {
        assertEquals(BigInteger.ZERO, builder.count());
        assertTrue(builder.isEmpty());
        assertFalse(builder.lexOrder().iterator().hasNext());

        assertFalse(builder.lexOrderMth(1, 0).iterator().hasNext());
        assertFalse(builder.lexOrderMth(3, 5).iterator().hasNext());
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(0, 0));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(1, -1));

        assertFalse(builder.byRanks(List.of()).iterator().hasNext());
        assertThrows(IllegalArgumentException.class, () -> builder.byRanks(List.of(BigInteger.ZERO)).stream().toList());

        assertThrows(IllegalArgumentException.class, () -> builder.sample(1, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.choice(1, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(0, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.choice(0, new Random(1)));
    }

    /**
     * Rules for a domain whose only result is the empty list ({@code count() == 1}, e.g. the permutations
     * of the empty set or a selection of size 0): not empty, every rank must be 0 and one empty result is
     * produced per rank supplied, choice yields s empty results, sample(1) one, sample(2) is an error.
     */
    public static void testSingleEmptyResultDomain(Builder<?> builder) {
        assertEquals(BigInteger.ONE, builder.count());
        assertFalse(builder.isEmpty());
        assertEquals(List.of(List.of()), builder.lexOrder().stream().toList());
        assertEquals(List.of(List.of()), builder.lexOrderMth(1, 0).stream().toList());
        assertEquals(List.of(List.of()), builder.lexOrderMth(5, 0).stream().toList());
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(1, 1));

        BigInteger zero = BigInteger.ZERO;
        assertEquals(List.of(List.of(), List.of(), List.of()),
                builder.byRanks(List.of(zero, zero, zero)).stream().toList(), "one [] per rank supplied");
        assertThrows(IllegalArgumentException.class,
                () -> builder.byRanks(List.of(zero, BigInteger.ONE)).stream().toList());
        assertThrows(IllegalArgumentException.class,
                () -> builder.byRanks(List.of(BigInteger.valueOf(-1))).stream().toList());

        assertEquals(List.of(List.of(), List.of(), List.of()), builder.choice(3, new Random(1)).stream().toList());
        assertEquals(List.of(List.of()), builder.sample(1, new Random(1)).stream().toList());
        assertThrows(IllegalArgumentException.class, () -> builder.sample(2, new Random(1)));
    }
}