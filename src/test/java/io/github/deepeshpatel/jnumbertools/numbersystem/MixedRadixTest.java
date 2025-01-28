package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixedRadixTest {

    @Test
    void shouldConvertFromDecimalToClockAndBack() {
        int secondsInOneDay = 24 * 60 * 60;
        int[] bases = {Integer.MAX_VALUE, 24, 60, 60};
        int[] out = MixedRadix.toMixedRadix(secondsInOneDay, bases);
        int[] expected = {1, 0, 0, 0};
        assertArrayEquals(expected, out);

        int decimal  = MixedRadix.toDecimal(out, bases);
        assertEquals(1, decimal);

    }


}
