package io.github.deepeshpatel.jnumbertools.generator.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BigIntegerSampleTest {

    private static List<BigInteger> drain(Iterable<BigInteger> it) {
        List<BigInteger> out = new ArrayList<>();
        it.forEach(out::add);
        return out;
    }

    @Test
    void fullSampleIsAPermutationOfTheRange() {
        List<BigInteger> values = drain(new BigIntegerSample(BigInteger.valueOf(50), 50, new Random(1)));
        assertEquals(50, new HashSet<>(values).size());
        values.forEach(v -> assertTrue(v.signum() >= 0 && v.intValue() < 50));
    }

    @Test
    void valuesAreUniqueAndInRangeForSmallAndHugeBounds() {
        for (BigInteger max : List.of(BigInteger.valueOf(1_000_003), BigInteger.ONE.shiftLeft(100).add(BigInteger.ONE))) {
            List<BigInteger> values = drain(new BigIntegerSample(max, 500, new Random(7)));
            assertEquals(500, new HashSet<>(values).size(), "duplicates for max=" + max);
            values.forEach(v -> assertTrue(v.signum() >= 0 && v.compareTo(max) < 0));
        }
    }

    @Test
    void sampleSizeMayNotExceedMax() {
        assertThrows(IllegalArgumentException.class,
                () -> new BigIntegerSample(BigInteger.valueOf(3), 4, new Random()));
    }

    @Test
    void sameSeedGivesSameSequence() {
        BigInteger max = BigInteger.valueOf(1_000_003);
        assertEquals(drain(new BigIntegerSample(max, 20, new Random(11))),
                drain(new BigIntegerSample(max, 20, new Random(11))));
    }

    @Test
    void smallSampleFromLargeIntRangeNeedsNoLargeAllocation() {
        // Regression: sampling 3 values out of 12! used to allocate an int[12!] (~1.9 GB).
        BigInteger twelveFactorial = BigInteger.valueOf(479_001_600L);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            List<BigInteger> values = drain(new BigIntegerSample(twelveFactorial, 3, new Random(5)));
            assertEquals(3, values.size());
            assertEquals(3, new HashSet<>(values).size());
        });
    }

    @Test
    void randomBelowNeedsAboutTwoDrawsRegardlessOfBitLength() {
        // Regression: the mask was computed but never applied, so bit lengths just above a
        // byte boundary were rejected ~99.6% of the time.
        for (int bits : new int[]{72, 73, 120, 121}) {
            BigInteger bound = BigInteger.ONE.shiftLeft(bits).add(BigInteger.ONE);
            int[] calls = {0};
            Random random = new Random(1) {
                @Override public void nextBytes(byte[] bytes) { calls[0]++; super.nextBytes(bytes); }
            };
            int n = 1000;
            for (int i = 0; i < n; i++) {
                BigInteger v = BigIntegerSample.randomBelow(bound, random);
                assertTrue(v.signum() >= 0 && v.compareTo(bound) < 0);
            }
            assertTrue(calls[0] < n * 4, "bits=" + bits + " used " + calls[0] + " draws for " + n + " samples");
        }
    }
}