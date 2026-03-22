package io.github.deepeshpatel.jnumbertools.generator.base;

import io.github.deepeshpatel.jnumbertools.TestBase;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.Builder;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.errMsgIncrement;
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
        var errZeroM = assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.ZERO, BigInteger.ONE));
        assertEquals(errMsgIncrement(0), errZeroM.getMessage());

        var negativeM = assertThrows(IllegalArgumentException.class, () ->
                builder.lexOrderMth(BigInteger.valueOf(-1), BigInteger.ONE));
        assertEquals(errMsgIncrement(-1), negativeM.getMessage());

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
}
