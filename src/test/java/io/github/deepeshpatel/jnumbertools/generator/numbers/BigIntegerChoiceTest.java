package io.github.deepeshpatel.jnumbertools.generator.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BigIntegerChoiceTest {

    @Test
    void bigBoundsProduceValuesAboveLongRange() {
        // Regression: for bounds > Long.MAX_VALUE the old implementation could never return >= 2^63.
        BigInteger max = BigInteger.ONE.shiftLeft(100);
        BigInteger twoPow63 = BigInteger.ONE.shiftLeft(63);
        int n = 2000, above = 0;
        for (BigInteger v : new BigIntegerChoice(max, n, new Random(1))) {
            assertTrue(v.signum() >= 0 && v.compareTo(max) < 0, "out of range: " + v);
            if (v.compareTo(twoPow63) >= 0) above++;
        }
        assertTrue(above > n * 0.99, "only " + above + " of " + n + " draws were >= 2^63");
    }

    @Test
    void bigBoundsAreRoughlyUniformAcrossTopBit() {
        BigInteger max = BigInteger.ONE.shiftLeft(100);
        int n = 4000, topBitSet = 0;
        for (BigInteger v : new BigIntegerChoice(max, n, new Random(2))) {
            if (v.testBit(99)) topBitSet++;
        }
        assertTrue(topBitSet > n * 0.45 && topBitSet < n * 0.55, "top bit set in " + topBitSet + " of " + n);
    }

    @Test
    void smallBoundReachesEveryValueAndNothingElse() {
        Set<BigInteger> seen = new HashSet<>();
        for (BigInteger v : new BigIntegerChoice(BigInteger.valueOf(5), 1000, new Random(3))) seen.add(v);
        assertEquals(5, seen.size());
        seen.forEach(v -> assertTrue(v.signum() >= 0 && v.intValue() < 5));
    }

    @Test
    void producesExactlyRequestedCount() {
        int count = 0;
        for (BigInteger ignored : new BigIntegerChoice(BigInteger.TEN, 37, new Random(4))) count++;
        assertEquals(37, count);
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BigIntegerChoice(BigInteger.ZERO, 1, new Random()));
        assertThrows(IllegalArgumentException.class, () -> new BigIntegerChoice(BigInteger.TEN, 0, new Random()));
        assertThrows(NullPointerException.class, () -> new BigIntegerChoice(BigInteger.TEN, 1, null));
    }
}